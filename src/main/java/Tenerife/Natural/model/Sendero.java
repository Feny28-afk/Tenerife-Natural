package Tenerife.Natural.model;
import jakarta.persistence.*;

@Entity
@Table(name = "senderos")
public class Sendero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreSendero;
    private boolean acceso;
    private String dificultad;
    private String estadoMeteorologico;

    // --- NUEVOS CAMPOS PARA EL SPRINT 4 ---
    private double latitud;
    private double longitud;

    public Sendero() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreSendero() { return nombreSendero; }
    public void setNombreSendero(String nombreSendero) { this.nombreSendero = nombreSendero; }

    public boolean isAcceso() { return acceso; }
    public void setAcceso(boolean acceso) { this.acceso = acceso; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public String getEstadoMeteorologico() { return estadoMeteorologico; }
    public void setEstadoMeteorologico(String estadoMeteorologico) { this.estadoMeteorologico = estadoMeteorologico; }

    // --- GETTERS Y SETTERS PARA COORDENADAS ---
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
}