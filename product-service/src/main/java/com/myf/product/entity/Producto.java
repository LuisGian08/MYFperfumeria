package com.myf.product.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Producto", schema = "dbo")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CategoriaId")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "MarcaId")
    private Marca marca;

    @Column(name = "Codigo", nullable = false, unique = true)
    private String codigo;

    @Column(name = "Nombre", nullable = false)
    private String nombre;

    @Column(name = "Descripcion", length = 1200)
    private String descripcion;

    @Column(name = "Tipo", nullable = false)
    private String tipo = "PERFUME";

    @Column(name = "VolumenMl", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumenMl = BigDecimal.valueOf(100);

    @Column(name = "Genero", nullable = false)
    private String genero = "UNISEX";

    @Column(name = "Precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;

    @Column(name = "Costo", precision = 12, scale = 2)
    private BigDecimal costo = BigDecimal.ZERO;

    @Column(name = "ImagenUrl", length = 1000)
    private String imagenUrl;

    @Column(name = "Estado", nullable = false)
    private String estado = "ACTIVO";

    @Column(name = "FechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "FechaActualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @OneToMany(
            mappedBy = "producto",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<PresentacionProducto> presentaciones = new ArrayList<>();

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public void addPresentacion(PresentacionProducto presentacion) {
        presentacion.setProducto(this);
        presentaciones.add(presentacion);
    }

    public void clearPresentaciones() {
        presentaciones.clear();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getVolumenMl() {
        return volumenMl;
    }

    public void setVolumenMl(BigDecimal volumenMl) {
        this.volumenMl = volumenMl;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public List<PresentacionProducto> getPresentaciones() {
        return presentaciones;
    }
}