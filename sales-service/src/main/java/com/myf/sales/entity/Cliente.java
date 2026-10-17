package com.myf.sales.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="Cliente")
public class Cliente {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="Id") private Long id;
 @Column(name="Nombre",nullable=false) private String nombre;
 @Column(name="Telefono",length=50) private String telefono;
 @Column(name="Email",length=180) private String email;
 @Column(name="Direccion",length=500) private String direccion;
 @Column(name="FechaCreacion",nullable=false) private LocalDateTime fechaCreacion=LocalDateTime.now();
 @Column(name="FechaActualizacion",nullable=false) private LocalDateTime fechaActualizacion=LocalDateTime.now();
 @PreUpdate void u(){fechaActualizacion=LocalDateTime.now();}
 public Long getId(){return id;} public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
 public String getTelefono(){return telefono;} public void setTelefono(String v){telefono=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;}
 public String getDireccion(){return direccion;} public void setDireccion(String v){direccion=v;} public LocalDateTime getFechaCreacion(){return fechaCreacion;}
}
