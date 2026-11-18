package com.myf.product.service;

import com.myf.product.dto.PresentacionRequest;
import com.myf.product.dto.ProductoCatalogoResponse;
import com.myf.product.dto.ProductoRequest;
import com.myf.product.dto.ProductoResponse;
import com.myf.product.entity.Categoria;
import com.myf.product.entity.Marca;
import com.myf.product.entity.PresentacionProducto;
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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void catalogoPublicoOmiteCostosYPresentacionesInactivas() {
        Producto product = new Producto();
        product.setId(1002L);
        product.setEstado("ACTIVO");
        product.setNombre("Perfume de prueba");
        product.setDescripcion("Descripción pública");
        product.setPrecio(new BigDecimal("200"));
        Categoria category = new Categoria();
        category.setNombre("Perfumes");
        product.setCategoria(category);
        Marca brand = new Marca();
        brand.setNombre("MYF");
        product.setMarca(brand);

        PresentacionProducto active = new PresentacionProducto();
        active.setTipo("BOTELLA");
        active.setVolumenMl(new BigDecimal("100"));
        active.setPrecio(new BigDecimal("200"));
        active.setCosto(new BigDecimal("90"));
        active.setEstado("ACTIVO");
        product.addPresentacion(active);

        PresentacionProducto inactive = new PresentacionProducto();
        inactive.setTipo("DECANT");
        inactive.setVolumenMl(new BigDecimal("10"));
        inactive.setPrecio(new BigDecimal("30"));
        inactive.setCosto(new BigDecimal("5"));
        inactive.setEstado("INACTIVO");
        product.addPresentacion(inactive);
        when(repository.findDistinctByEstadoOrderByNombreAsc("ACTIVO")).thenReturn(List.of(product));

        List<ProductoCatalogoResponse> catalog = service.listarCatalogo();

        assertThat(catalog).hasSize(1);
        assertThat(catalog.get(0).id()).isEqualTo(1002L);
        assertThat(catalog.get(0).categoria()).isEqualTo("Perfumes");
        assertThat(catalog.get(0).marca()).isEqualTo("MYF");
        assertThat(catalog.get(0).presentaciones()).hasSize(1);
        assertThat(catalog.get(0).presentaciones().get(0).precio()).isEqualByComparingTo("200");
        verify(repository).findDistinctByEstadoOrderByNombreAsc("ACTIVO");
    }

    @Test
    void catalogoPublicoNoDevuelveProductosInactivosPorId() {
        Producto inactive = new Producto();
        inactive.setId(1002L);
        inactive.setNombre("Inactivo");
        inactive.setEstado("INACTIVO");
        when(repository.findById(1002L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.obtenerCatalogo(1002L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void administracionListaProductosActivosEInactivos() {
        Producto active = new Producto();
        active.setId(1001L);
        active.setNombre("Activo");
        active.setEstado("ACTIVO");
        Producto inactive = new Producto();
        inactive.setId(1002L);
        inactive.setNombre("Inactivo");
        inactive.setEstado("INACTIVO");
        when(repository.findAllByOrderByNombreAsc()).thenReturn(List.of(active, inactive));

        List<ProductoResponse> products = service.listarAdministracion();

        assertThat(products).extracting(ProductoResponse::estado).containsExactly("ACTIVO", "INACTIVO");
    }

    @Test
    void validacionRechazaPreciosNegativosEnProductoYPresentaciones() {
        ProductoRequest request = new ProductoRequest(null, null, "MYF-NEG-01", "Perfume de prueba",
                "Descripción", "PERFUME", new BigDecimal("100"), "UNISEX",
                new BigDecimal("-0.01"), BigDecimal.ZERO, null,
                List.of(new PresentacionRequest(null, "BOTELLA", new BigDecimal("100"),
                        new BigDecimal("-0.01"), BigDecimal.ZERO, true)));

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            assertThat(validator.validate(request))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("precio", "presentaciones[0].precio");
        }
    }

    @Test
    void cambiarEstadoPermiteDesactivarYReactivarProductoYPresentaciones() {
        Producto product = new Producto();
        product.setId(1002L);
        product.setNombre("Perfume de prueba");
        PresentacionProducto presentation = new PresentacionProducto();
        presentation.setTipo("BOTELLA");
        presentation.setVolumenMl(new BigDecimal("100"));
        presentation.setPrecio(new BigDecimal("200"));
        presentation.setEstado("ACTIVO");
        product.addPresentacion(presentation);
        when(repository.findById(1002L)).thenReturn(Optional.of(product));
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cambiarEstado(1002L, "INACTIVO");
        assertThat(product.getEstado()).isEqualTo("INACTIVO");
        assertThat(presentation.getEstado()).isEqualTo("INACTIVO");

        service.cambiarEstado(1002L, "ACTIVO");
        assertThat(product.getEstado()).isEqualTo("ACTIVO");
        assertThat(presentation.getEstado()).isEqualTo("ACTIVO");
    }
}
