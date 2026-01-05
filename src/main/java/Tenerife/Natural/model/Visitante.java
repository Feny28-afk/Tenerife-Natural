package Tenerife.Natural.model;
import jakarta.persistence.*;

@Entity
@Table(name = "visitantes")
public class Visitante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String fisicoUsuario;
    private String nivelAccesibilidad;

    public Visitante() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getFisicoUsuario() { return fisicoUsuario; }
    public void setFisicoUsuario(String fisicoUsuario) { this.fisicoUsuario = fisicoUsuario; }

    public String getNivelAccesibilidad() { return nivelAccesibilidad; }
    public void setNivelAccesibilidad(String nivelAccesibilidad) { this.nivelAccesibilidad = nivelAccesibilidad; }
}