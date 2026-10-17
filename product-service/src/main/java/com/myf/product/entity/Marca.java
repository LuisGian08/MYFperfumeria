package com.myf.product.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Marca", schema = "dbo")
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "PaisOrigen")
    private String paisOrigen;

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

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}