package com.myf.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Usuario")
public class Usuario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="Id") private Long id;
    @Column(name="Nombres",nullable=false) private String nombres;
    @Column(name="Apellidos",nullable=false) private String apellidos;
    @Column(name="Email",nullable=false,unique=true) private String email;
    @Column(name="PasswordHash",nullable=false) private String passwordHash;
    @Column(name="Estado",nullable=false) private String estado="ACTIVO";
    @Column(name="FechaCreacion",nullable=false) private LocalDateTime fechaCreacion=LocalDateTime.now();
    @Column(name="UltimoAcceso") private LocalDateTime ultimoAcceso;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "UsuarioRol",
            joinColumns = @JoinColumn(name = "UsuarioId"),
            inverseJoinColumns = @JoinColumn(name = "RolId"))
    private Set<Rol> roles = new HashSet<>();

    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNombres(){return nombres;} public void setNombres(String v){this.nombres=v;}
    public String getApellidos(){return apellidos;} public void setApellidos(String v){this.apellidos=v;}
    public String getEmail(){return email;} public void setEmail(String v){this.email=v;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){this.passwordHash=v;}
    public String getEstado(){return estado;} public void setEstado(String v){this.estado=v;}
    public LocalDateTime getFechaCreacion(){return fechaCreacion;} public void setFechaCreacion(LocalDateTime v){this.fechaCreacion=v;}
    public LocalDateTime getUltimoAcceso(){return ultimoAcceso;} public void setUltimoAcceso(LocalDateTime v){this.ultimoAcceso=v;}
    public Set<Rol> getRoles(){return roles;}
}
