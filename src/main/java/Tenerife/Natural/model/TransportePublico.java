package Tenerife.Natural.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "transporte_rutas")
public class TransportePublico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String matricula;

    private String linea;
    private boolean disponibilidad;
    private int tiempoLlegada;

    // --- NUEVOS CAMPOS PARA EL REPOSITORIO ---

    @Column(name = "stop_id")
    private String stopId; // Para buscar por parada de TITSA

    @Column(name = "sendero_id_simple")
    private Long senderoId; // Para buscar por ID de sendero fácilmente

    // -----------------------------------------

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

    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }

    public Long getSenderoId() { return senderoId; }
    public void setSenderoId(Long senderoId) { this.senderoId = senderoId; }

    public Sendero getSendero() { return sendero; }
    public void setSendero(Sendero sendero) { this.sendero = sendero; }
}