package com.myf.sales.service;

import com.myf.sales.entity.EventoVentaProcesado;
import com.myf.sales.entity.ReporteProductoVendido;
import com.myf.sales.entity.ReporteVentaDiaria;
import com.myf.sales.event.SaleCreatedEvent;
import com.myf.sales.repository.EventoVentaProcesadoRepository;
import com.myf.sales.repository.ReporteProductoRepository;
import com.myf.sales.repository.ReporteVentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportListenerTest {
    @Mock
    private ReporteVentaRepository ventas;
    @Mock
    private ReporteProductoRepository productos;
    @Mock
    private EventoVentaProcesadoRepository procesados;

    @Test
    void receiveSaleEventAggregatesDailyAndPresentationTotals() throws Exception {
        JsonMapper json = JsonMapper.builder().build();
        ReportListener listener = new ReportListener(ventas, productos, procesados, json);
        LocalDate date = LocalDate.of(2026, 11, 15);
        SaleCreatedEvent event = new SaleCreatedEvent(101L, "V-TEST-101", date, new BigDecimal("70.00"),
                List.of(new SaleCreatedEvent.Item(12L, 33L, "Perfume de prueba", "DECANT",
                        new BigDecimal("10"), 2, new BigDecimal("70.00"))));
        when(procesados.existsByVentaId(101L)).thenReturn(false);
        when(ventas.findByFecha(date)).thenReturn(Optional.empty());
        when(productos.findByFechaAndPresentacionId(date, 33L)).thenReturn(Optional.empty());

        listener.receive(json.writeValueAsString(event));

        ArgumentCaptor<ReporteVentaDiaria> dailyCaptor = ArgumentCaptor.forClass(ReporteVentaDiaria.class);
        verify(ventas).save(dailyCaptor.capture());
        assertThat(dailyCaptor.getValue().getCantidadVentas()).isEqualTo(1);
        assertThat(dailyCaptor.getValue().getUnidadesVendidas()).isEqualTo(2);
        assertThat(dailyCaptor.getValue().getDecantsVendidos()).isEqualTo(2);
        assertThat(dailyCaptor.getValue().getTotalVentas()).isEqualByComparingTo("70.00");

        ArgumentCaptor<ReporteProductoVendido> productCaptor = ArgumentCaptor.forClass(ReporteProductoVendido.class);
        verify(productos).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getCantidadVendida()).isEqualTo(2);
        assertThat(productCaptor.getValue().getTotalVendido()).isEqualByComparingTo("70.00");

        ArgumentCaptor<EventoVentaProcesado> processedCaptor = ArgumentCaptor.forClass(EventoVentaProcesado.class);
        verify(procesados).save(processedCaptor.capture());
        assertThat(processedCaptor.getValue().getVentaId()).isEqualTo(101L);
    }

    @Test
    void duplicateSaleEventDoesNotChangeAggregates() throws Exception {
        JsonMapper json = JsonMapper.builder().build();
        ReportListener listener = new ReportListener(ventas, productos, procesados, json);
        SaleCreatedEvent event = new SaleCreatedEvent(101L, "V-TEST-101", LocalDate.of(2026, 11, 15),
                BigDecimal.TEN, List.of());
        when(procesados.existsByVentaId(101L)).thenReturn(true);

        listener.receive(json.writeValueAsString(event));

        verify(procesados).existsByVentaId(101L);
        verifyNoInteractions(ventas, productos);
        verify(procesados, never()).save(any(EventoVentaProcesado.class));
    }
}
