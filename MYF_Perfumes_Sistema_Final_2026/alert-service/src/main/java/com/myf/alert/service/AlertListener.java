package com.myf.alert.service;
import com.myf.alert.config.RabbitConfig;
import com.myf.alert.entity.AlertaStock;
import com.myf.alert.event.StockLowEvent;
import com.myf.alert.repository.AlertaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import java.time.LocalDateTime;

@Service
public class AlertListener {
 private final AlertaRepository repo; private final JsonMapper json;
 public AlertListener(AlertaRepository repo,JsonMapper json){this.repo=repo;this.json=json;}

 @RabbitListener(queues=RabbitConfig.LOW_QUEUE)
 @Transactional
 public void low(String payload) throws Exception {
  StockLowEvent e=json.readValue(payload,StockLowEvent.class);
  AlertaStock a=repo.findFirstByProductoIdAndUnidadAndEstadoOrderByFechaAlertaDesc(e.productoId(),e.unidad(),"PENDIENTE")
          .orElseGet(AlertaStock::new);
  a.setProductoId(e.productoId());a.setTipo(e.tipo());a.setStockActual(e.stockActual());a.setStockMinimo(e.stockMinimo());
  a.setUnidad(e.unidad());a.setMensaje(e.mensaje());a.setEstado("PENDIENTE");repo.save(a);
 }

 @RabbitListener(queues=RabbitConfig.RECOVERY_QUEUE)
 @Transactional
 public void recovered(String payload) throws Exception {
  StockLowEvent e=json.readValue(payload,StockLowEvent.class);
  repo.findFirstByProductoIdAndUnidadAndEstadoOrderByFechaAlertaDesc(e.productoId(),e.unidad(),"PENDIENTE").ifPresent(a->{
   a.setEstado("ATENDIDA");a.setFechaAtendida(LocalDateTime.now());repo.save(a);
  });
 }
}
