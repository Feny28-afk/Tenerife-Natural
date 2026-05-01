package Tenerife.Natural.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pymes")
public class Pyme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipo; // Ejemplo: Restaurante, Tienda de artesanía, Albergue
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "sendero_id")
    private Sendero sendero; // La PYME está asociada a un punto de salida de un sendero

    public Pyme() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Sendero getSendero() { return sendero; }
    public void setSendero(Sendero sendero) { this.sendero = sendero; }
}