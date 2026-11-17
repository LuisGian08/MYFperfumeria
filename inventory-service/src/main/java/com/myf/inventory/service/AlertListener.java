package com.myf.inventory.service;

import com.myf.inventory.config.RabbitConfig;
import com.myf.inventory.entity.AlertaStock;
import com.myf.inventory.event.StockLowEvent;
import com.myf.inventory.repository.AlertaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

@Service
public class AlertListener {
    private final AlertaRepository repository;
    private final JsonMapper json;

    public AlertListener(AlertaRepository repository, JsonMapper json) {
        this.repository = repository;
        this.json = json;
    }

    @RabbitListener(queues = RabbitConfig.LOW_QUEUE)
    @Transactional
    public void low(String payload) throws Exception {
        StockLowEvent event = json.readValue(payload, StockLowEvent.class);
        AlertaStock alert = repository
                .findFirstByProductoIdAndUnidadAndEstadoOrderByFechaAlertaDesc(
                        event.productoId(), event.unidad(), "PENDIENTE")
                .orElseGet(AlertaStock::new);
        alert.setProductoId(event.productoId());
        alert.setTipo(event.tipo());
        alert.setStockActual(event.stockActual());
        alert.setStockMinimo(event.stockMinimo());
        alert.setUnidad(event.unidad());
        alert.setMensaje(event.mensaje());
        alert.setEstado("PENDIENTE");
        repository.save(alert);
    }

    @RabbitListener(queues = RabbitConfig.RECOVERY_QUEUE)
    @Transactional
    public void recovered(String payload) throws Exception {
        StockLowEvent event = json.readValue(payload, StockLowEvent.class);
        repository.findFirstByProductoIdAndUnidadAndEstadoOrderByFechaAlertaDesc(
                event.productoId(), event.unidad(), "PENDIENTE")
                .ifPresent(alert -> {
                    alert.setEstado("ATENDIDA");
                    alert.setFechaAtendida(LocalDateTime.now());
                    repository.save(alert);
                });
    }
}
