package com.myf.sales.service;

import com.myf.sales.config.RabbitConfig;
import com.myf.sales.entity.EventoVentaProcesado;
import com.myf.sales.entity.ReporteProductoVendido;
import com.myf.sales.entity.ReporteVentaDiaria;
import com.myf.sales.event.SaleCreatedEvent;
import com.myf.sales.repository.EventoVentaProcesadoRepository;
import com.myf.sales.repository.ReporteProductoRepository;
import com.myf.sales.repository.ReporteVentaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ReportListener {
    private final ReporteVentaRepository ventas;
    private final ReporteProductoRepository productos;
    private final EventoVentaProcesadoRepository procesados;
    private final JsonMapper json;

    public ReportListener(
            ReporteVentaRepository ventas,
            ReporteProductoRepository productos,
            EventoVentaProcesadoRepository procesados,
            JsonMapper json) {
        this.ventas = ventas;
        this.productos = productos;
        this.procesados = procesados;
        this.json = json;
    }

    @RabbitListener(queues = RabbitConfig.SALE_QUEUE)
    @Transactional
    public void receive(String payload) throws Exception {
        SaleCreatedEvent event = json.readValue(payload, SaleCreatedEvent.class);
        if (event.ventaId() != null && procesados.existsByVentaId(event.ventaId())) {
            return;
        }

        ReporteVentaDiaria daily = ventas.findByFecha(event.fecha())
                .orElseGet(() -> {
                    ReporteVentaDiaria reporte = new ReporteVentaDiaria();
                    reporte.setFecha(event.fecha());
                    return reporte;
                });
        daily.setCantidadVentas(daily.getCantidadVentas() + 1);
        int units = event.items().stream().mapToInt(SaleCreatedEvent.Item::cantidad).sum();
        int bottles = event.items().stream()
                .filter(item -> "BOTELLA".equals(item.tipoPresentacion()))
                .mapToInt(SaleCreatedEvent.Item::cantidad)
                .sum();
        int decants = event.items().stream()
                .filter(item -> "DECANT".equals(item.tipoPresentacion()))
                .mapToInt(SaleCreatedEvent.Item::cantidad)
                .sum();
        daily.setUnidadesVendidas(daily.getUnidadesVendidas() + units);
        daily.setBotellasVendidas(daily.getBotellasVendidas() + bottles);
        daily.setDecantsVendidos(daily.getDecantsVendidos() + decants);
        daily.setTotalVentas(daily.getTotalVentas().add(event.total()));
        ventas.save(daily);

        for (SaleCreatedEvent.Item item : event.items()) {
            Long presentationId = item.presentacionId() == null
                    ? -item.productoId()
                    : item.presentacionId();
            ReporteProductoVendido product = productos
                    .findByFechaAndPresentacionId(event.fecha(), presentationId)
                    .orElseGet(() -> {
                        ReporteProductoVendido reporte = new ReporteProductoVendido();
                        reporte.setFecha(event.fecha());
                        reporte.setProductoId(item.productoId());
                        reporte.setPresentacionId(presentationId);
                        reporte.setNombreProducto(item.nombreProducto());
                        reporte.setTipoPresentacion(item.tipoPresentacion());
                        reporte.setVolumenMl(item.volumenMl());
                        return reporte;
                    });
            product.setCantidadVendida(product.getCantidadVendida() + item.cantidad());
            product.setTotalVendido(product.getTotalVendido().add(item.total()));
            productos.save(product);
        }

        if (event.ventaId() != null) {
            EventoVentaProcesado processed = new EventoVentaProcesado();
            processed.setVentaId(event.ventaId());
            procesados.save(processed);
        }
    }
}
