package com.myf.product.service;

import com.myf.product.dto.PresentacionRequest;
import com.myf.product.dto.ProductoRequest;
import com.myf.product.dto.ProductoResponse;
import com.myf.product.entity.Producto;
import com.myf.product.repository.CategoriaRepository;
import com.myf.product.repository.MarcaRepository;
import com.myf.product.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock
    private ProductoRepository repository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private MarcaRepository marcaRepository;

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService(repository, categoriaRepository, marcaRepository);
    }

    @Test
    void crearProductoGuardaPresentacionYDevuelveCatalogoActivo() {
        ProductoRequest request = new ProductoRequest(null, null, "MYF-UNIT-01", "Perfume de prueba",
                "Descripción", "PERFUME", new BigDecimal("100"), "UNISEX",
                new BigDecimal("250"), new BigDecimal("120"), null,
                List.of(new PresentacionRequest(null, "BOTELLA", new BigDecimal("100"),
                        new BigDecimal("250"), new BigDecimal("120"), true)));
        when(repository.existsByCodigoIgnoreCase(request.codigo())).thenReturn(false);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setId(17L);
            return producto;
        });

        ProductoResponse response = service.crear(request);

        assertThat(response.id()).isEqualTo(17L);
        assertThat(response.codigo()).isEqualTo("MYF-UNIT-01");
        assertThat(response.estado()).isEqualTo("ACTIVO");
        assertThat(response.presentaciones()).hasSize(1);
        assertThat(response.presentaciones().get(0).tipo()).isEqualTo("BOTELLA");
        assertThat(response.precio()).isEqualByComparingTo("250");

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPresentaciones()).hasSize(1);
        verifyNoInteractions(categoriaRepository, marcaRepository);
    }
}
