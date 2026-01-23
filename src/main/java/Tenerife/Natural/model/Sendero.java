package Tenerife.Natural.model;
import jakarta.persistence.*;

@Table(name = "senderos")
public class Sendero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreSendero;
    private boolean acceso;
    private String dificultad;
    private String estadoMeteorologico;

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



    //Prueba Branch jorge
}
