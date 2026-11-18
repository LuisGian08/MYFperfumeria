package com.myf.product.service;

import com.myf.product.dto.*;
import com.myf.product.entity.*;
import com.myf.product.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductoService {
    private final ProductoRepository repo;
    private final CategoriaRepository cats;
    private final MarcaRepository marcas;

    public ProductoService(ProductoRepository repo, CategoriaRepository cats, MarcaRepository marcas) {
        this.repo = repo;
        this.cats = cats;
        this.marcas = marcas;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarAdministracion() {
        return repo.findAllByOrderByNombreAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoCatalogoResponse> listarCatalogo() {
        return repo.findDistinctByEstadoOrderByNombreAsc("ACTIVO").stream()
                .map(this::toCatalogoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return toResponse(entidad(id));
    }

    @Transactional(readOnly = true)
    public ProductoCatalogoResponse obtenerCatalogo(Long id) {
        Producto producto = entidad(id);
        if (!"ACTIVO".equals(producto.getEstado())) {
            throw new IllegalArgumentException("Producto no encontrado");
        }
        return toCatalogoResponse(producto);
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest r) {
        if (repo.existsByCodigoIgnoreCase(r.codigo())) {
            throw new IllegalArgumentException("El código ya existe");
        }
        Producto p = new Producto();
        aplicarBase(p, r);
        aplicarPresentaciones(p, r, false);
        return toResponse(repo.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest r) {
        Producto p = entidad(id);
        if (!p.getCodigo().equalsIgnoreCase(r.codigo()) && repo.existsByCodigoIgnoreCase(r.codigo())) {
            throw new IllegalArgumentException("El código ya existe");
        }
        aplicarBase(p, r);
        p.clearPresentaciones();
        repo.saveAndFlush(p);
        aplicarPresentaciones(p, r, false);
        return toResponse(repo.save(p));
    }

    @Transactional
    public ProductoResponse cambiarEstado(Long id, String estado) {
        String nuevoEstado = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVO".equals(nuevoEstado) && !"INACTIVO".equals(nuevoEstado)) {
            throw new IllegalArgumentException("El estado debe ser ACTIVO o INACTIVO");
        }
        Producto p = entidad(id);
        p.setEstado(nuevoEstado);
        p.getPresentaciones().forEach(x -> x.setEstado(nuevoEstado));
        return toResponse(repo.save(p));
    }

    private Producto entidad(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    private void aplicarBase(Producto p, ProductoRequest r) {
        p.setCategoria(r.categoriaId() == null ? null : cats.findById(r.categoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada")));
        p.setMarca(r.marcaId() == null ? null : marcas.findById(r.marcaId())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada")));
        p.setCodigo(r.codigo().trim());
        p.setNombre(r.nombre().trim());
        p.setDescripcion(r.descripcion());
        p.setGenero(r.genero());
        p.setImagenUrl(r.imagenUrl());

        // Compatibilidad con versión anterior. La presentación principal alimenta estos campos.
        p.setTipo(r.tipo() == null ? "PERFUME" : r.tipo());
        p.setVolumenMl(r.volumenMl() == null ? BigDecimal.valueOf(100) : r.volumenMl());
        p.setPrecio(r.precio() == null ? BigDecimal.ZERO : r.precio());
        p.setCosto(r.costo() == null ? BigDecimal.ZERO : r.costo());
    }

    private void aplicarPresentaciones(Producto p, ProductoRequest r, boolean clearFirst) {
        List<PresentacionRequest> prs = r.presentaciones();
        if (prs == null || prs.isEmpty()) {
            prs = List.of(new PresentacionRequest(
                    null,
                    "DECANT".equalsIgnoreCase(r.tipo()) ? "DECANT" : "BOTELLA",
                    r.volumenMl() == null ? BigDecimal.valueOf(100) : r.volumenMl(),
                    r.precio() == null ? BigDecimal.ZERO : r.precio(),
                    r.costo(),
                    true
            ));
        }

        if (clearFirst) p.clearPresentaciones();
        int seq = 1;
        for (PresentacionRequest pr : prs) {
            if (Boolean.FALSE.equals(pr.activo())) continue;
            PresentacionProducto x = new PresentacionProducto();
            String suffix = pr.tipo().equals("DECANT") ? "D" : "B";
            String vol = pr.volumenMl().stripTrailingZeros().toPlainString().replace('.', '-');
            x.setCodigo((pr.codigo() == null || pr.codigo().isBlank())
                    ? p.getCodigo() + "-" + suffix + "-" + vol + "-" + seq
                    : pr.codigo().trim());
            x.setTipo(pr.tipo());
            x.setVolumenMl(pr.volumenMl());
            x.setPrecio(pr.precio());
            x.setCosto(pr.costo() == null ? BigDecimal.ZERO : pr.costo());
            x.setEstado("ACTIVO");
            p.addPresentacion(x);
            seq++;
        }

        if (p.getPresentaciones().isEmpty()) {
            throw new IllegalArgumentException("El producto debe tener al menos una presentación activa");
        }

        PresentacionProducto main = p.getPresentaciones().stream()
                .filter(x -> "BOTELLA".equals(x.getTipo()))
                .findFirst().orElse(p.getPresentaciones().get(0));
        p.setTipo("BOTELLA".equals(main.getTipo()) ? "PERFUME" : "DECANT");
        p.setVolumenMl(main.getVolumenMl());
        p.setPrecio(main.getPrecio());
        p.setCosto(main.getCosto());
    }

    private ProductoResponse toResponse(Producto p) {
        var prs = p.getPresentaciones().stream()
                .sorted(Comparator.comparing(PresentacionProducto::getVolumenMl))
                .map(x -> new ProductoResponse.PresentacionResponse(
                        x.getId(), x.getCodigo(), x.getTipo(), x.getVolumenMl(), x.getPrecio(),
                        x.getCosto(), x.getEstado()))
                .toList();

        return new ProductoResponse(
                p.getId(),
                p.getCategoria() == null ? null : p.getCategoria().getId(),
                p.getCategoria() == null ? null : p.getCategoria().getNombre(),
                p.getMarca() == null ? null : p.getMarca().getId(),
                p.getMarca() == null ? null : p.getMarca().getNombre(),
                p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getGenero(), p.getImagenUrl(), p.getEstado(),
                p.getTipo(), p.getVolumenMl(), p.getPrecio(), p.getCosto(), prs
        );
    }

    private ProductoCatalogoResponse toCatalogoResponse(Producto producto) {
        ProductoResponse response = toResponse(producto);
        List<ProductoCatalogoResponse.Presentacion> presentations = response.presentaciones().stream()
                .filter(p -> "ACTIVO".equals(p.estado()))
                .map(p -> new ProductoCatalogoResponse.Presentacion(
                        p.id(), p.tipo(), p.volumenMl(), p.precio()))
                .toList();
        return new ProductoCatalogoResponse(
                response.id(),
                response.categoria(),
                response.marca(),
                response.nombre(),
                response.descripcion(),
                response.precio(),
                response.imagenUrl(),
                presentations
        );
    }
}
