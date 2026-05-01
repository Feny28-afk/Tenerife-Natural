package Tenerife.Natural.model;

import jakarta.persistence.*;

@Entity
@Table(name = "opiniones")
public class Opinion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int estrellas;
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "sendero_id")
    private Sendero sendero;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Opinion() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getEstrellas() { return estrellas; }
    public void setEstrellas(int estrellas) { this.estrellas = estrellas; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public Sendero getSendero() { return sendero; }
    public void setSendero(Sendero sendero) { this.sendero = sendero; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}