package com.myf.alert.service;

import com.myf.alert.entity.AlertaStock;
import com.myf.alert.event.StockLowEvent;
import com.myf.alert.repository.AlertaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertListenerTest {
    @Mock
    private AlertaRepository repository;

    @Test
    void lowEventCreatesPendingAlertFromMessage() throws Exception {
        JsonMapper json = JsonMapper.builder().build();
        AlertListener listener = new AlertListener(repository, json);
        StockLowEvent event = new StockLowEvent(12L, 1, 3, "STOCK_BAJO", "UNIDAD",
                "Producto 12 con 1 botella disponible");
        when(repository.findFirstByProductoIdAndUnidadAndEstadoOrderByFechaAlertaDesc(
                12L, "UNIDAD", "PENDIENTE")).thenReturn(Optional.empty());
        when(repository.save(any(AlertaStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        listener.low(json.writeValueAsString(event));

        ArgumentCaptor<AlertaStock> captor = ArgumentCaptor.forClass(AlertaStock.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getProductoId()).isEqualTo(12L);
        assertThat(captor.getValue().getTipo()).isEqualTo("STOCK_BAJO");
        assertThat(captor.getValue().getEstado()).isEqualTo("PENDIENTE");
        assertThat(captor.getValue().getStockActual()).isEqualTo(1);
        assertThat(captor.getValue().getStockMinimo()).isEqualTo(3);
    }
}
