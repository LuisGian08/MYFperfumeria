package com.myf.product.config;

import com.myf.product.entity.*;
import com.myf.product.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class ProductDataInitializer {

    @Bean
    CommandLineRunner normalizePresentations(ProductoRepository productos,
                                             CategoriaRepository categorias,
                                             MarcaRepository marcas) {
        return args -> initialize(productos, categorias, marcas);
    }

    @Transactional
    public void initialize(ProductoRepository productos, CategoriaRepository categorias, MarcaRepository marcas) {
        Categoria perfume = categorias.findAll().stream().filter(x -> "Perfumes".equalsIgnoreCase(x.getNombre()))
                .findFirst().orElseGet(() -> { Categoria c = new Categoria(); c.setNombre("Perfumes"); c.setDescripcion("Perfumes completos y sus presentaciones"); return categorias.save(c); });
        Categoria decants = categorias.findAll().stream().filter(x -> "Decants".equalsIgnoreCase(x.getNombre()))
                .findFirst().orElseGet(() -> { Categoria c = new Categoria(); c.setNombre("Decants"); c.setDescripcion("Presentaciones fraccionadas"); return categorias.save(c); });

        Marca lattafa = marcas.findAll().stream().filter(x -> "Lattafa".equalsIgnoreCase(x.getNombre()))
                .findFirst().orElseGet(() -> { Marca m = new Marca(); m.setNombre("Lattafa"); m.setPaisOrigen("EAU"); return marcas.save(m); });
        Marca afnan = marcas.findAll().stream().filter(x -> "Afnan".equalsIgnoreCase(x.getNombre()))
                .findFirst().orElseGet(() -> { Marca m = new Marca(); m.setNombre("Afnan"); m.setPaisOrigen("EAU"); return marcas.save(m); });
        Marca armaf = marcas.findAll().stream().filter(x -> "Armaf".equalsIgnoreCase(x.getNombre()))
                .findFirst().orElseGet(() -> { Marca m = new Marca(); m.setNombre("Armaf"); m.setPaisOrigen("EAU"); return marcas.save(m); });

        if (productos.count() == 0) {
            productos.save(sample("PERF-KHAMRAH-DUKHAN", "Khamrah Dukhan", "Fragancia intensa, especiada y amaderada.", perfume, lattafa, "UNISEX", new BigDecimal("170"), new BigDecimal("110"), 100));
            productos.save(sample("PERF-9PM", "9 PM", "Fragancia dulce y nocturna de gran proyección.", perfume, afnan, "HOMBRE", new BigDecimal("160"), new BigDecimal("110"), 100));
            productos.save(sample("PERF-ICONIC", "Club de Nuit Iconic", "Fragancia fresca y elegante.", perfume, armaf, "HOMBRE", new BigDecimal("190"), new BigDecimal("170"), 105));
        }

        // Convierte productos heredados en un modelo de presentaciones sin perderlos.
        for (Producto p : productos.findAll()) {
            if (p.getPresentaciones().isEmpty()) {
                PresentacionProducto pr = new PresentacionProducto();
                pr.setCodigo(p.getCodigo() + ("DECANT".equalsIgnoreCase(p.getTipo()) ? "-D" : "-B"));
                pr.setTipo("DECANT".equalsIgnoreCase(p.getTipo()) ? "DECANT" : "BOTELLA");
                pr.setVolumenMl(p.getVolumenMl());
                pr.setPrecio(p.getPrecio());
                pr.setCosto(p.getCosto());
                p.addPresentacion(pr);
                productos.save(p);
            }
        }

        // Migra decants heredados que antes estaban guardados como productos separados.
        // Ej.: "Khamrah Dukhan Decant 5 ml" pasa a ser una presentación de "Khamrah Dukhan".
        List<Producto> all = productos.findAll();
        for (Producto legacy : all) {
            if (!"ACTIVO".equals(legacy.getEstado()) || !"DECANT".equalsIgnoreCase(legacy.getTipo())) continue;
            String baseName = legacy.getNombre().replaceFirst("(?i)\\s+decant.*$", "").trim();
            if (baseName.equalsIgnoreCase(legacy.getNombre())) continue;
            Producto base = all.stream()
                    .filter(x -> !x.getId().equals(legacy.getId()))
                    .filter(x -> "ACTIVO".equals(x.getEstado()))
                    .filter(x -> x.getNombre().equalsIgnoreCase(baseName))
                    .findFirst().orElse(null);
            if (base == null) continue;
            boolean exists = base.getPresentaciones().stream().anyMatch(x ->
                    "DECANT".equals(x.getTipo()) && x.getVolumenMl().compareTo(legacy.getVolumenMl()) == 0);
            if (!exists) {
                PresentacionProducto migrated = new PresentacionProducto();
                migrated.setCodigo(legacy.getCodigo() + "-MIGRATED");
                migrated.setTipo("DECANT");
                migrated.setVolumenMl(legacy.getVolumenMl());
                migrated.setPrecio(legacy.getPrecio());
                migrated.setCosto(legacy.getCosto());
                base.addPresentacion(migrated);
                productos.save(base);
            }
            legacy.setEstado("INACTIVO");
            legacy.getPresentaciones().forEach(x -> x.setEstado("INACTIVO"));
            productos.save(legacy);
        }
    }

    private Producto sample(String code, String name, String desc, Categoria cat, Marca brand,
                            String gender, BigDecimal price, BigDecimal cost, int ml) {
        Producto p = new Producto();
        p.setCodigo(code); p.setNombre(name); p.setDescripcion(desc); p.setCategoria(cat); p.setMarca(brand);
        p.setGenero(gender); p.setTipo("PERFUME"); p.setVolumenMl(BigDecimal.valueOf(ml)); p.setPrecio(price); p.setCosto(cost);
        add(p, "BOTELLA", ml, price, cost, code + "-B");
        add(p, "DECANT", 5, new BigDecimal("15"), BigDecimal.ZERO, code + "-D5");
        add(p, "DECANT", 10, new BigDecimal("28"), BigDecimal.ZERO, code + "-D10");
        add(p, "DECANT", 30, new BigDecimal("70"), BigDecimal.ZERO, code + "-D30");
        return p;
    }

    private void add(Producto p, String type, int ml, BigDecimal price, BigDecimal cost, String code) {
        PresentacionProducto x = new PresentacionProducto();
        x.setCodigo(code); x.setTipo(type); x.setVolumenMl(BigDecimal.valueOf(ml)); x.setPrecio(price); x.setCosto(cost);
        p.addPresentacion(x);
    }
}
