package com.myf.product.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Categoria", schema = "dbo")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "Descripcion")
    private String descripcion;

    @Column(name = "Estado", nullable = false)
    private String estado = "ACTIVO";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}