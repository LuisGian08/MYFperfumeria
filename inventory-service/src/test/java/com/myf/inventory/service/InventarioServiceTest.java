package com.myf.inventory.service;

import com.myf.inventory.config.RabbitConfig;
import com.myf.inventory.dto.AbrirBotellaRequest;
import com.myf.inventory.dto.MovimientoDecantRequest;
import com.myf.inventory.entity.Inventario;
import com.myf.inventory.entity.MovimientoInventario;
import com.myf.inventory.repository.InventarioRepository;
import com.myf.inventory.repository.MovimientoRepository;
import com.myf.inventory.event.StockLowEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {
    @Mock
    private InventarioRepository inventarioRepository;
    @Mock
    private MovimientoRepository movimientoRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private JsonMapper jsonMapper;

    private InventarioService service;

    @BeforeEach
    void setUp() {
        service = new InventarioService(inventarioRepository, movimientoRepository, rabbitTemplate, jsonMapper);
    }

    @Test
    void abrirBotellaMueveUnaUnidadAlVolumenDeDecantsYRegistraAmbosMovimientos() throws Exception {
        when(jsonMapper.writeValueAsString(any(StockLowEvent.class))).thenReturn("{}");
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Inventario inventario = inventario(10, 0);
        when(inventarioRepository.findByProductoId(7L)).thenReturn(Optional.of(inventario));

        Inventario resultado = service.abrirBotellaParaDecants(
                7L, new AbrirBotellaRequest(100, "Preparar decants", 3L));

        assertThat(resultado.getStockActual()).isEqualTo(9);
        assertThat(resultado.getVolumenDecantMl()).isEqualTo(100);

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(MovimientoInventario::getTipoMovimiento)
                .containsExactly("BOTELLA_ABIERTA", "DECANT_ENTRADA");
        assertThat(captor.getAllValues())
                .extracting(MovimientoInventario::getCantidad)
                .containsExactly(1, 100);
        assertThat(captor.getAllValues())
                .extracting(MovimientoInventario::getUnidad)
                .containsExactly("UNIDAD", "ML");
        verify(inventarioRepository).save(inventario);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq("stock.recovered"), eq("{}"));
    }

    @Test
    void salidaDeBotellaRechazaCantidadMayorAlStockSinModificarDatos() {
        Inventario inventario = inventario(1, 0);
        when(inventarioRepository.findByProductoId(7L)).thenReturn(Optional.of(inventario));

        assertThatThrownBy(() -> service.salida(7L,
                new com.myf.inventory.dto.MovimientoRequest(2, "Venta", 3L, 10L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stock insuficiente");

        assertThat(inventario.getStockActual()).isEqualTo(1);
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(movimientoRepository, never()).save(any(MovimientoInventario.class));
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void salidaDeDecantRestaMililitrosYRegistraUnidadCorrecta() {
        Inventario inventario = inventario(5, 50);
        when(inventarioRepository.findByProductoId(7L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventario resultado = service.salidaDecant(7L,
                new MovimientoDecantRequest(10, "Venta decant", 3L, 10L));

        assertThat(resultado.getVolumenDecantMl()).isEqualTo(40);
        assertThat(resultado.getStockActual()).isEqualTo(5);
        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipoMovimiento()).isEqualTo("DECANT_SALIDA");
        assertThat(captor.getValue().getUnidad()).isEqualTo("ML");
        assertThat(captor.getValue().getCantidad()).isEqualTo(10);
    }

    @Test
    void entradaConStockSobreElMinimoNoPublicaUnaRecuperacionSiNuncaEstuvoBajoMinimo() {
        Inventario inventario = inventario(10, 50);
        when(inventarioRepository.findByProductoId(7L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoRepository.save(any(MovimientoInventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.entrada(7L, new com.myf.inventory.dto.MovimientoRequest(1, "Reposición", 3L, null));

        verifyNoInteractions(rabbitTemplate);
    }

    private Inventario inventario(int botellas, int mililitros) {
        Inventario inventario = new Inventario();
        inventario.setProductoId(7L);
        inventario.setStockActual(botellas);
        inventario.setStockMinimo(1);
        inventario.setVolumenDecantMl(mililitros);
        inventario.setVolumenDecantMinimoMl(10);
        return inventario;
    }
}
