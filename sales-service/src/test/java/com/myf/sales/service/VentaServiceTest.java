package com.myf.sales.service;

import com.myf.sales.config.RabbitConfig;
import com.myf.sales.dto.VentaRequest;
import com.myf.sales.entity.Cliente;
import com.myf.sales.entity.DetalleVenta;
import com.myf.sales.entity.Venta;
import com.myf.sales.repository.ClienteRepository;
import com.myf.sales.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {
    private static final String PRODUCT_URL = "http://product.test";
    private static final String INVENTORY_URL = "http://inventory.test";

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private MockRestServiceServer restServer;
    private VentaService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        restServer = MockRestServiceServer.bindTo(builder).build();
        service = new VentaService(ventaRepository, clienteRepository, builder, rabbitTemplate,
                JsonMapper.builder().build(), PRODUCT_URL, INVENTORY_URL);
    }

    @Test
    void checkoutRecalculaImporteIgnoraDescuentoYDescuentaInventario() {
        stubRepositoriesForSuccessfulCreate();
        expectAvailableBottleAndInventory(5);
        restServer.expect(requestTo(INVENTORY_URL + "/api/inventario/1/salida"))
                .andExpect(method(POST))
                .andExpect(content().json("{\"cantidad\":2}", false))
                .andRespond(withSuccess());

        Venta venta = service.crear(checkoutRequest(new BigDecimal("999.00"), 2), true);

        assertThat(venta.getSubtotal()).isEqualByComparingTo("160.00");
        assertThat(venta.getDescuento()).isEqualByComparingTo("0");
        assertThat(venta.getTotal()).isEqualByComparingTo("160.00");
        assertThat(venta.getEstado()).isEqualTo("PENDIENTE");
        assertThat(venta.getCanal()).isEqualTo("WEB");
        assertThat(venta.getDetalles()).hasSize(1);
        assertThat(venta.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo("80.00");

        ArgumentCaptor<Venta> captor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(venta);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq("sale.created"), any(String.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq("order.created"), any(String.class));
        restServer.verify();
    }

    @Test
    void checkoutConStockInsuficienteNoGuardaVentaNiDescuentaInventario() {
        expectAvailableBottleAndInventory(1);

        assertThatThrownBy(() -> service.crear(checkoutRequest(BigDecimal.ZERO, 2), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(ventaRepository, never()).save(any(Venta.class));
        verify(clienteRepository, never()).save(any(Cliente.class));
        verifyNoInteractions(rabbitTemplate);
        restServer.verify();
    }

    @Test
    void ventaAdministrativaRechazaDescuentoMayorAlSubtotalSinGuardarVenta() {
        expectAvailableBottleAndInventory(5);
        VentaRequest request = new VentaRequest(3L, new BigDecimal("200.00"), "YAPE",
                "Prueba de descuento", "ADMIN", null,
                java.util.List.of(new VentaRequest.Item(1L, 11L, 1)));

        assertThatThrownBy(() -> service.crear(request, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Descuento inválido");

        verify(ventaRepository, never()).save(any(Venta.class));
        verify(clienteRepository, never()).save(any(Cliente.class));
        verifyNoInteractions(rabbitTemplate);
        restServer.verify();
    }

    @Test
    void actualizarPagoYComprobantePersisteLaVenta() {
        Venta venta = new Venta();
        ReflectionTestUtils.setField(venta, "id", 101L);
        venta.setEstado("PENDIENTE");
        when(ventaRepository.findById(101L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cambiarEstadoPago(101L, "pagado");
        service.guardarComprobante(101L, "/comprobantes/pago.jpg");

        assertThat(venta.getEstadoPago()).isEqualTo("PAGADO");
        assertThat(venta.getComprobanteUrl()).isEqualTo("/comprobantes/pago.jpg");
        verify(ventaRepository, times(2)).save(venta);
    }

    @Test
    void pagoInvalidoYComprobanteEnPedidoCanceladoSonRechazados() {
        Venta venta = new Venta();
        ReflectionTestUtils.setField(venta, "id", 101L);
        venta.setEstado("CANCELADO");
        when(ventaRepository.findById(101L)).thenReturn(Optional.of(venta));

        assertThatThrownBy(() -> service.cambiarEstadoPago(101L, "DESCONOCIDO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estado de pago inválido");
        assertThatThrownBy(() -> service.cambiarEstadoPago(101L, "PAGADO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pedido cancelado");
        assertThatThrownBy(() -> service.guardarComprobante(101L, "/comprobantes/pago.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pedido cancelado");

        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void reintentarCancelacionTrasReintegroParcialNoDuplicaElPrimerReintegro() {
        Venta venta = new Venta();
        ReflectionTestUtils.setField(venta, "id", 101L);
        venta.setNumeroVenta("V-TEST-101");
        venta.setEstado("PENDIENTE");
        venta.addDetalle(detalle(1L, "BOTELLA", 100, 1));
        venta.addDetalle(detalle(2L, "DECANT", 10, 1));
        when(ventaRepository.findById(101L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AtomicInteger reintegrosBotella = new AtomicInteger();
        AtomicInteger stockBotellaReintegrado = new AtomicInteger();
        AtomicInteger volumenDecantReintegrado = new AtomicInteger();
        restServer.expect(requestTo(INVENTORY_URL + "/api/inventario/1/entrada"))
                .andExpect(method(POST))
                .andRespond(request -> {
                    reintegrosBotella.incrementAndGet();
                    stockBotellaReintegrado.incrementAndGet();
                    return withSuccess().createResponse(request);
                });
        restServer.expect(requestTo(INVENTORY_URL + "/api/inventario/2/decant/entrada"))
                .andExpect(method(POST))
                .andRespond(withServerError());
        restServer.expect(requestTo(INVENTORY_URL + "/api/inventario/1/salida"))
                .andExpect(method(POST))
                .andRespond(request -> {
                    stockBotellaReintegrado.decrementAndGet();
                    return withSuccess().createResponse(request);
                });
        restServer.expect(requestTo(INVENTORY_URL + "/api/inventario/1/entrada"))
                .andExpect(method(POST))
                .andRespond(request -> {
                    reintegrosBotella.incrementAndGet();
                    stockBotellaReintegrado.incrementAndGet();
                    return withSuccess().createResponse(request);
                });
        restServer.expect(requestTo(INVENTORY_URL + "/api/inventario/2/decant/entrada"))
                .andExpect(method(POST))
                .andRespond(request -> {
                    volumenDecantReintegrado.addAndGet(10);
                    return withSuccess().createResponse(request);
                });

        assertThatThrownBy(() -> service.cambiarEstado(101L, "CANCELADO"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("se revirtieron los movimientos completados");
        service.cambiarEstado(101L, "CANCELADO");

        assertThat(reintegrosBotella).hasValue(2);
        assertThat(stockBotellaReintegrado).hasValue(1);
        assertThat(volumenDecantReintegrado).hasValue(10);
        assertThat(venta.getStockReintegrado()).isTrue();
        assertThat(venta.getEstado()).isEqualTo("CANCELADO");
        restServer.verify();
    }

    private DetalleVenta detalle(Long productoId, String tipo, int volumenMl, int cantidad) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId(productoId);
        detalle.setPresentacionId(productoId + 10);
        detalle.setNombreProducto("Producto " + productoId);
        detalle.setTipoProducto(tipo);
        detalle.setVolumenMl(BigDecimal.valueOf(volumenMl));
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(BigDecimal.TEN);
        detalle.setSubtotal(BigDecimal.TEN);
        return detalle;
    }

    private void expectAvailableBottleAndInventory(int stock) {
        restServer.expect(requestTo(PRODUCT_URL + "/internal/api/productos/1"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "codigo": "MYF-001",
                          "nombre": "Perfume de prueba",
                          "estado": "ACTIVO",
                          "tipo": "PERFUME",
                          "volumenMl": 100,
                          "precio": 999,
                          "costo": 100,
                          "presentaciones": [
                            {
                              "id": 11,
                              "codigo": "MYF-001-B-100",
                              "tipo": "BOTELLA",
                              "volumenMl": 100,
                              "precio": 80,
                              "costo": 30,
                              "estado": "ACTIVO"
                            }
                          ]
                        }
                        """, org.springframework.http.MediaType.APPLICATION_JSON));
        restServer.expect(requestTo(INVENTORY_URL + "/api/inventario/1"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "productoId": 1,
                          "stockActual": %d,
                          "stockMinimo": 1,
                          "volumenDecantMl": 0,
                          "volumenDecantMinimoMl": 10
                        }
                        """.formatted(stock), org.springframework.http.MediaType.APPLICATION_JSON));
    }

    private void stubRepositoriesForSuccessfulCreate() {
        when(clienteRepository.findFirstByTelefono(any())).thenReturn(Optional.empty());
        when(clienteRepository.findFirstByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0);
            ReflectionTestUtils.setField(venta, "id", 101L);
            return venta;
        });
    }

    private VentaRequest checkoutRequest(BigDecimal descuento, int cantidad) {
        VentaRequest.ClienteRequest cliente = new VentaRequest.ClienteRequest(
                "Cliente de prueba", "999123456", "test@example.com", "Lima");
        return new VentaRequest(null, descuento, "YAPE", "Pedido de prueba", "WEB", cliente,
                java.util.List.of(new VentaRequest.Item(1L, 11L, cantidad)));
    }
}
