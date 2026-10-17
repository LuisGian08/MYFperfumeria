package com.myf.report.service;
import com.myf.report.config.RabbitConfig;
import com.myf.report.entity.*;
import com.myf.report.event.SaleCreatedEvent;
import com.myf.report.repository.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
@Service
public class ReportListener {
 private final ReporteVentaRepository ventas; private final ReporteProductoRepository productos; private final EventoVentaProcesadoRepository procesados; private final JsonMapper json;
 public ReportListener(ReporteVentaRepository v,ReporteProductoRepository p,EventoVentaProcesadoRepository procesados,JsonMapper j){ventas=v;productos=p;this.procesados=procesados;json=j;}
 @RabbitListener(queues=RabbitConfig.QUEUE) @Transactional
 public void receive(String payload)throws Exception{
  SaleCreatedEvent e=json.readValue(payload,SaleCreatedEvent.class);
  if(e.ventaId()!=null && procesados.existsByVentaId(e.ventaId())) return;
  ReporteVentaDiaria d=ventas.findByFecha(e.fecha()).orElseGet(()->{ReporteVentaDiaria x=new ReporteVentaDiaria();x.setFecha(e.fecha());return x;});
  d.setCantidadVentas(d.getCantidadVentas()+1);int units=e.items().stream().mapToInt(SaleCreatedEvent.Item::cantidad).sum();d.setUnidadesVendidas(d.getUnidadesVendidas()+units);
  int bottles=e.items().stream().filter(i->"BOTELLA".equals(i.tipoPresentacion())).mapToInt(SaleCreatedEvent.Item::cantidad).sum();
  int decants=e.items().stream().filter(i->"DECANT".equals(i.tipoPresentacion())).mapToInt(SaleCreatedEvent.Item::cantidad).sum();
  d.setBotellasVendidas(d.getBotellasVendidas()+bottles);d.setDecantsVendidos(d.getDecantsVendidos()+decants);d.setTotalVentas(d.getTotalVentas().add(e.total()));ventas.save(d);
  for(var item:e.items()){
   Long key=item.presentacionId()==null?-(item.productoId()):item.presentacionId();
   ReporteProductoVendido p=productos.findByFechaAndPresentacionId(e.fecha(),key).orElseGet(()->{ReporteProductoVendido x=new ReporteProductoVendido();x.setFecha(e.fecha());x.setProductoId(item.productoId());x.setPresentacionId(key);x.setNombreProducto(item.nombreProducto());x.setTipoPresentacion(item.tipoPresentacion());x.setVolumenMl(item.volumenMl());return x;});
   p.setCantidadVendida(p.getCantidadVendida()+item.cantidad());p.setTotalVendido(p.getTotalVendido().add(item.total()));productos.save(p);
  }
  if(e.ventaId()!=null){EventoVentaProcesado done=new EventoVentaProcesado();done.setVentaId(e.ventaId());procesados.save(done);}
 }
}
