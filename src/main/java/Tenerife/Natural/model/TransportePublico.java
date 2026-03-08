package Tenerife.Natural.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "transporte_rutas") // ¡Aquí está el cambio clave para evitar el error 1813!
public class TransportePublico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String matricula;

    private String linea; // Ejemplo: "348"
    private boolean disponibilidad;
    private int tiempoLlegada; // En minutos

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sendero_id")
    @JsonIgnore
    private Sendero sendero;

    public TransportePublico() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public boolean isDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(boolean disponibilidad) { this.disponibilidad = disponibilidad; }

    public int getTiempoLlegada() { return tiempoLlegada; }
    public void setTiempoLlegada(int tiempoLlegada) { this.tiempoLlegada = tiempoLlegada; }

    public Sendero getSendero() { return sendero; }
    public void setSendero(Sendero sendero) { this.sendero = sendero; }
}