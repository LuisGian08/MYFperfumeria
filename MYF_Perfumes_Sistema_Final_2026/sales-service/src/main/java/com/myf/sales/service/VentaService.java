package com.myf.sales.service;

import com.myf.sales.config.RabbitConfig;
import com.myf.sales.dto.*;
import com.myf.sales.entity.*;
import com.myf.sales.event.SaleCreatedEvent;
import com.myf.sales.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VentaService {
 private static final Logger log=LoggerFactory.getLogger(VentaService.class);
 private final VentaRepository repo; private final ClienteRepository clientes; private final RestClient client;
 private final RabbitTemplate rabbit; private final JsonMapper json; private final String productUrl,inventoryUrl;

 public VentaService(VentaRepository repo,ClienteRepository clientes,RestClient.Builder builder,RabbitTemplate rabbit,JsonMapper json,
                     @Value("${services.product-url}") String productUrl,@Value("${services.inventory-url}") String inventoryUrl){
  this.repo=repo;this.clientes=clientes;this.client=builder.build();this.rabbit=rabbit;this.json=json;this.productUrl=productUrl;this.inventoryUrl=inventoryUrl;
 }

 public List<Venta> listar(){return repo.findAllByOrderByFechaVentaDesc();}
 public Venta obtener(Long id){return repo.findById(id).orElseThrow(()->new IllegalArgumentException("Venta no encontrada"));}
 public List<Cliente> listarClientes(){return clientes.findAllByOrderByFechaCreacionDesc();}

 @Transactional
 public Venta crear(VentaRequest r, boolean checkout){
  if(r.items()==null||r.items().isEmpty()) throw new IllegalArgumentException("La venta debe tener productos");
  if(checkout && r.cliente()==null) throw new IllegalArgumentException("Los datos del cliente son obligatorios para completar el pedido");
  List<Prepared> prepared=new ArrayList<>(); BigDecimal subtotal=BigDecimal.ZERO;

  for(VentaRequest.Item item:r.items()){
   ProductoDto p=client.get().uri(productUrl+"/api/productos/"+item.productoId()).retrieve().body(ProductoDto.class);
   if(p==null||!"ACTIVO".equals(p.estado())) throw new IllegalArgumentException("Producto inválido: "+item.productoId());
   ProductoDto.PresentacionDto pr=elegirPresentacion(p,item.presentacionId());
   InventarioDto inv=client.get().uri(inventoryUrl+"/api/inventario/"+item.productoId()).retrieve().body(InventarioDto.class);
   validarStock(p,pr,item.cantidad(),inv);
   BigDecimal line=pr.precio().multiply(BigDecimal.valueOf(item.cantidad()));
   subtotal=subtotal.add(line); prepared.add(new Prepared(p,pr,item.cantidad(),line));
  }

  // El checkout público no puede autoconcederse descuentos. Los descuentos administrativos sí se validan.
  BigDecimal descuento=checkout?BigDecimal.ZERO:(r.descuento()==null?BigDecimal.ZERO:r.descuento());
  if(descuento.compareTo(BigDecimal.ZERO)<0||descuento.compareTo(subtotal)>0) throw new IllegalArgumentException("Descuento inválido");

  Cliente cliente=resolverCliente(r.cliente());
  Venta v=new Venta();
  v.setUsuarioId(checkout?null:r.usuarioId()); v.setClienteId(cliente==null?null:cliente.getId());
  v.setMetodoPago(r.metodoPago().toUpperCase()); v.setObservacion(r.observacion());
  v.setCanal(checkout?"WEB":(r.canal()==null?"ADMIN":r.canal().toUpperCase()));
  v.setEstado(checkout?"PENDIENTE":"REGISTRADA");
  v.setNumeroVenta("V-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+"-"+UUID.randomUUID().toString().substring(0,4).toUpperCase());
  v.setSubtotal(subtotal); v.setDescuento(descuento); v.setTotal(subtotal.subtract(descuento));
  for(Prepared x:prepared){
   DetalleVenta d=new DetalleVenta(); d.setProductoId(x.producto.id());d.setPresentacionId(x.presentacion.id());
   d.setNombreProducto(x.producto.nombre());d.setTipoProducto(x.presentacion.tipo());d.setVolumenMl(x.presentacion.volumenMl());
   d.setCantidad(x.qty);d.setPrecioUnitario(x.presentacion.precio());d.setSubtotal(x.line);v.addDetalle(d);
  }
  v=repo.save(v);

  List<Prepared> descontados=new ArrayList<>();
  try{
   for(Prepared x:prepared){descontar(v,x);descontados.add(x);}
  }catch(Exception ex){
   for(Prepared x:descontados){try{reintegrar(v,x);}catch(Exception ignored){}}
   throw new IllegalArgumentException("No se pudo completar la venta: "+mensaje(ex));
  }

  publicarVenta(v,prepared);
  if(checkout) publicarSimple("order.created", Map.of("ventaId",v.getId(),"numeroVenta",v.getNumeroVenta(),"total",v.getTotal()));
  return v;
 }

 @Transactional
 public Venta cambiarEstado(Long id,String estado){
  Venta v=obtener(id); String nuevo=estado.toUpperCase();
  Set<String> permitidos=Set.of("PENDIENTE","CONFIRMADO","PREPARANDO","LISTO","ENTREGADO","CANCELADO","REGISTRADA");
  if(!permitidos.contains(nuevo)) throw new IllegalArgumentException("Estado de pedido inválido");
  if("CANCELADO".equals(v.getEstado()) && !"CANCELADO".equals(nuevo)) throw new IllegalArgumentException("Un pedido cancelado no puede reactivarse automáticamente");
  if("ENTREGADO".equals(v.getEstado()) && "CANCELADO".equals(nuevo)) throw new IllegalArgumentException("Un pedido entregado no puede cancelarse desde este flujo");
  if("CANCELADO".equals(nuevo)&&!"CANCELADO".equals(v.getEstado())&&!Boolean.TRUE.equals(v.getStockReintegrado())){
   for(DetalleVenta d:v.getDetalles()) reintegrar(v,new Prepared(null,new ProductoDto.PresentacionDto(d.getPresentacionId(),"",d.getTipoProducto(),d.getVolumenMl(),d.getPrecioUnitario(),BigDecimal.ZERO,"ACTIVO"),d.getCantidad(),d.getSubtotal(),d.getProductoId(),d.getNombreProducto()));
   v.setStockReintegrado(true); publicarSimple("order.cancelled",Map.of("ventaId",v.getId(),"numeroVenta",v.getNumeroVenta()));
  }
  v.setEstado(nuevo); return repo.save(v);
 }

 private Cliente resolverCliente(VentaRequest.ClienteRequest c){
  if(c==null) return null;
  Optional<Cliente> found=Optional.empty();
  if(c.telefono()!=null&&!c.telefono().isBlank()) found=clientes.findFirstByTelefono(c.telefono());
  if(found.isEmpty()&&c.email()!=null&&!c.email().isBlank()) found=clientes.findFirstByEmailIgnoreCase(c.email());
  Cliente x=found.orElseGet(Cliente::new);x.setNombre(c.nombre());x.setTelefono(c.telefono());x.setEmail(c.email());x.setDireccion(c.direccion());return clientes.save(x);
 }

 private ProductoDto.PresentacionDto elegirPresentacion(ProductoDto p,Long id){
  if(p.presentaciones()==null||p.presentaciones().isEmpty()) return new ProductoDto.PresentacionDto(null,p.codigo(),"DECANT".equals(p.tipo())?"DECANT":"BOTELLA",p.volumenMl(),p.precio(),p.costo(),"ACTIVO");
  if(id==null) return p.presentaciones().stream().filter(x->"BOTELLA".equals(x.tipo())).findFirst().orElse(p.presentaciones().get(0));
  return p.presentaciones().stream().filter(x->Objects.equals(x.id(),id)&&"ACTIVO".equals(x.estado())).findFirst().orElseThrow(()->new IllegalArgumentException("Presentación no disponible"));
 }

 private void validarStock(ProductoDto p,ProductoDto.PresentacionDto pr,int qty,InventarioDto inv){
  if(inv==null) throw new IllegalArgumentException("Inventario no configurado para "+p.nombre());
  if("DECANT".equals(pr.tipo())){
   int ml=pr.volumenMl().multiply(BigDecimal.valueOf(qty)).setScale(0,RoundingMode.CEILING).intValueExact();
   if(inv.volumenDecantMl()==null||inv.volumenDecantMl()<ml) throw new IllegalArgumentException("Volumen insuficiente para "+p.nombre()+" ("+pr.volumenMl()+" ml)");
  }else if(inv.stockActual()==null||inv.stockActual()<qty) throw new IllegalArgumentException("Stock insuficiente para "+p.nombre());
 }

 private void descontar(Venta v,Prepared x){
  Map<String,Object> body=new LinkedHashMap<>();body.put("motivo","Venta "+v.getNumeroVenta());body.put("usuarioId",v.getUsuarioId());body.put("referenciaVentaId",v.getId());
  if("DECANT".equals(x.presentacion.tipo())){
   int ml=x.presentacion.volumenMl().multiply(BigDecimal.valueOf(x.qty)).setScale(0,RoundingMode.CEILING).intValueExact();body.put("mililitros",ml);
   client.post().uri(inventoryUrl+"/api/inventario/"+x.productoId()+"/decant/salida").body(body).retrieve().toBodilessEntity();
  }else{body.put("cantidad",x.qty);client.post().uri(inventoryUrl+"/api/inventario/"+x.productoId()+"/salida").body(body).retrieve().toBodilessEntity();}
 }

 private void reintegrar(Venta v,Prepared x){
  Map<String,Object> body=new LinkedHashMap<>();body.put("motivo","Reintegro "+v.getNumeroVenta());body.put("usuarioId",v.getUsuarioId());body.put("referenciaVentaId",v.getId());
  if("DECANT".equals(x.presentacion.tipo())){int ml=x.presentacion.volumenMl().multiply(BigDecimal.valueOf(x.qty)).setScale(0,RoundingMode.CEILING).intValueExact();body.put("mililitros",ml);client.post().uri(inventoryUrl+"/api/inventario/"+x.productoId()+"/decant/entrada").body(body).retrieve().toBodilessEntity();}
  else{body.put("cantidad",x.qty);client.post().uri(inventoryUrl+"/api/inventario/"+x.productoId()+"/entrada").body(body).retrieve().toBodilessEntity();}
 }

 private void publicarVenta(Venta v,List<Prepared> prepared){
  List<SaleCreatedEvent.Item> items=prepared.stream().map(x->new SaleCreatedEvent.Item(x.productoId(),x.presentacion.id(),x.nombreProducto(),x.presentacion.tipo(),x.presentacion.volumenMl(),x.qty,x.line)).toList();
  SaleCreatedEvent event=new SaleCreatedEvent(v.getId(),v.getNumeroVenta(),LocalDate.now(),v.getTotal(),items);
  try{rabbit.convertAndSend(RabbitConfig.EXCHANGE,"sale.created",json.writeValueAsString(event));}catch(Exception e){log.warn("Venta creada, pero no se pudo publicar sale.created: {}",e.getMessage());}
 }
 private void publicarSimple(String key,Object event){try{rabbit.convertAndSend(RabbitConfig.EXCHANGE,key,json.writeValueAsString(event));}catch(Exception e){log.warn("No se pudo publicar {}: {}",key,e.getMessage());}}
 private String mensaje(Exception e){return e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();}

 private record Prepared(ProductoDto producto,ProductoDto.PresentacionDto presentacion,int qty,BigDecimal line,Long forcedProductoId,String forcedNombre){
  Prepared(ProductoDto p,ProductoDto.PresentacionDto pr,int qty,BigDecimal line){this(p,pr,qty,line,null,null);}
  Long productoId(){return forcedProductoId!=null?forcedProductoId:producto.id();}
  String nombreProducto(){return forcedNombre!=null?forcedNombre:producto.nombre();}
 }
}
