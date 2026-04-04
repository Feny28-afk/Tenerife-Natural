package Tenerife.Natural.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    // Sprint 6: Nivel físico del usuario (1: Bajo, 2: Medio, 3: Alto)
    private int nivelFisico;

    // Sprint 6: Relación de muchos a muchos para guardar rutas favoritas
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_favoritos",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "sendero_id")
    )
    private List<Sendero> favoritos = new ArrayList<>();

    public Usuario() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getNivelFisico() { return nivelFisico; }
    public void setNivelFisico(int nivelFisico) { this.nivelFisico = nivelFisico; }

    public List<Sendero> getFavoritos() { return favoritos; }
    public void setFavoritos(List<Sendero> favoritos) { this.favoritos = favoritos; }
}