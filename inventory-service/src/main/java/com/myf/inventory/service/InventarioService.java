package com.myf.inventory.service;

import com.myf.inventory.config.RabbitConfig;
import com.myf.inventory.dto.*;
import com.myf.inventory.entity.*;
import com.myf.inventory.event.StockLowEvent;
import com.myf.inventory.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Service
public class InventarioService {
    private static final Logger log = LoggerFactory.getLogger(InventarioService.class);
    private final InventarioRepository repo;
    private final MovimientoRepository movs;
    private final RabbitTemplate rabbit;
    private final JsonMapper json;

    public InventarioService(InventarioRepository repo, MovimientoRepository movs, RabbitTemplate rabbit, JsonMapper json) {
        this.repo = repo; this.movs = movs; this.rabbit = rabbit; this.json = json;
    }

    public List<Inventario> listar() { return repo.findAll(); }
    public List<MovimientoInventario> movimientos() { return movs.findTop100ByOrderByFechaMovimientoDesc(); }
    public List<MovimientoInventario> movimientos(Long productoId) { return movs.findByProductoIdOrderByFechaMovimientoDesc(productoId); }

    public Inventario obtener(Long productoId) {
        return repo.findByProductoId(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no configurado para el producto"));
    }

    @Transactional
    public Inventario configurar(Long productoId, StockConfigRequest r) {
        Inventario i = repo.findByProductoId(productoId).orElseGet(() -> nuevo(productoId));
        if (r.stockActual() != null) i.setStockActual(r.stockActual());
        if (r.stockMinimo() != null) i.setStockMinimo(r.stockMinimo());
        if (r.volumenDecantMl() != null) i.setVolumenDecantMl(r.volumenDecantMl());
        if (r.volumenDecantMinimoMl() != null) i.setVolumenDecantMinimoMl(r.volumenDecantMinimoMl());
        i = repo.save(i);
        verificar(i);
        return i;
    }

    @Transactional
    public Inventario entrada(Long productoId, MovimientoRequest r) {
        Inventario i = repo.findByProductoId(productoId).orElseGet(() -> nuevo(productoId));
        i.setStockActual(i.getStockActual() + r.cantidad());
        i = repo.save(i);
        movimiento(productoId, "ENTRADA", r.cantidad(), "UNIDAD", r.motivo(), r.usuarioId(), r.referenciaVentaId());
        verificar(i);
        return i;
    }

    @Transactional
    public Inventario salida(Long productoId, MovimientoRequest r) {
        Inventario i = obtener(productoId);
        if (i.getStockActual() < r.cantidad()) throw new IllegalArgumentException("Stock insuficiente");
        i.setStockActual(i.getStockActual() - r.cantidad());
        i = repo.save(i);
        movimiento(productoId, "SALIDA", r.cantidad(), "UNIDAD", r.motivo(), r.usuarioId(), r.referenciaVentaId());
        verificar(i);
        return i;
    }

    @Transactional
    public Inventario entradaDecant(Long productoId, MovimientoDecantRequest r) {
        Inventario i = repo.findByProductoId(productoId).orElseGet(() -> nuevo(productoId));
        i.setVolumenDecantMl(i.getVolumenDecantMl() + r.mililitros());
        i = repo.save(i);
        movimiento(productoId, "DECANT_ENTRADA", r.mililitros(), "ML", r.motivo(), r.usuarioId(), r.referenciaVentaId());
        verificar(i);
        return i;
    }

    @Transactional
    public Inventario salidaDecant(Long productoId, MovimientoDecantRequest r) {
        Inventario i = obtener(productoId);
        if (i.getVolumenDecantMl() < r.mililitros()) throw new IllegalArgumentException("Volumen insuficiente para decant");
        i.setVolumenDecantMl(i.getVolumenDecantMl() - r.mililitros());
        i = repo.save(i);
        movimiento(productoId, "DECANT_SALIDA", r.mililitros(), "ML", r.motivo(), r.usuarioId(), r.referenciaVentaId());
        verificar(i);
        return i;
    }

    @Transactional
    public Inventario abrirBotellaParaDecants(Long productoId, AbrirBotellaRequest r) {
        Inventario i = obtener(productoId);
        if (i.getStockActual() < 1) throw new IllegalArgumentException("No hay botellas cerradas disponibles para abrir");
        i.setStockActual(i.getStockActual() - 1);
        i.setVolumenDecantMl(i.getVolumenDecantMl() + r.mililitros());
        i = repo.save(i);
        String motivo = (r.motivo() == null || r.motivo().isBlank()) ? "Botella abierta para decants" : r.motivo();
        movimiento(productoId, "BOTELLA_ABIERTA", 1, "UNIDAD", motivo, r.usuarioId(), null);
        movimiento(productoId, "DECANT_ENTRADA", r.mililitros(), "ML", motivo, r.usuarioId(), null);
        verificar(i);
        return i;
    }

    private Inventario nuevo(Long productoId) {
        Inventario x = new Inventario(); x.setProductoId(productoId); return x;
    }

    private void movimiento(Long productoId, String tipo, int cantidad, String unidad, String motivo, Long usuarioId, Long ventaId) {
        MovimientoInventario m = new MovimientoInventario();
        m.setProductoId(productoId); m.setTipoMovimiento(tipo); m.setCantidad(cantidad); m.setUnidad(unidad);
        m.setMotivo(motivo); m.setUsuarioId(usuarioId); m.setReferenciaVentaId(ventaId); movs.save(m);
    }

    private void verificar(Inventario i) {
        if (i.getStockMinimo() > 0 && i.getStockActual() <= i.getStockMinimo()) {
            String tipo = i.getStockActual() == 0 ? "SIN_STOCK" : "STOCK_BAJO";
            publicar("stock.low", new StockLowEvent(i.getProductoId(), i.getStockActual(), i.getStockMinimo(), tipo, "UNIDAD",
                    "Producto " + i.getProductoId() + " con " + i.getStockActual() + " botellas disponibles"));
        } else if (i.getStockMinimo() > 0) {
            publicar("stock.recovered", new StockLowEvent(i.getProductoId(), i.getStockActual(), i.getStockMinimo(), "RECUPERADO", "UNIDAD",
                    "Stock de botellas recuperado"));
        }

        if (i.getVolumenDecantMinimoMl() > 0 && i.getVolumenDecantMl() <= i.getVolumenDecantMinimoMl()) {
            String tipo = i.getVolumenDecantMl() == 0 ? "SIN_VOLUMEN_DECANT" : "VOLUMEN_BAJO";
            publicar("stock.low", new StockLowEvent(i.getProductoId(), i.getVolumenDecantMl(), i.getVolumenDecantMinimoMl(), tipo, "ML",
                    "Producto " + i.getProductoId() + " con " + i.getVolumenDecantMl() + " ml para decants"));
        } else if (i.getVolumenDecantMinimoMl() > 0) {
            publicar("stock.recovered", new StockLowEvent(i.getProductoId(), i.getVolumenDecantMl(), i.getVolumenDecantMinimoMl(), "RECUPERADO", "ML",
                    "Volumen de decants recuperado"));
        }
    }

    private void publicar(String routingKey, StockLowEvent event) {
        try {
            rabbit.convertAndSend(RabbitConfig.EXCHANGE, routingKey, json.writeValueAsString(event));
        } catch (Exception ex) {
            // La operación de inventario no se pierde si RabbitMQ está temporalmente fuera de línea.
            log.warn("No se pudo publicar evento {}: {}", routingKey, ex.getMessage());
        }
    }
}
