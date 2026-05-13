package Tenerife.Natural.model;

import jakarta.persistence.*;
import java.util.List;

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

    private double latitud;
    private double longitud;

    // --- CAMPOS FIN DE RUTA ---
    private double latitudFin;
    private double longitudFin;

    // Relación inversa para gestionar el borrado de favoritos sin errores de SQL
    @ManyToMany(mappedBy = "favoritos", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Usuario> usuariosQueMeTienenComoFavorito;

    @OneToMany(mappedBy = "sendero")
    private List<Opinion> opiniones; // Sin cascade = REMOVE para que no se borren al borrar el sendero

    public Sendero() {}

    // Getters y Setters
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

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public double getLatitudFin() { return latitudFin; }
    public void setLatitudFin(double latitudFin) { this.latitudFin = latitudFin; }

    public double getLongitudFin() { return longitudFin; }
    public void setLongitudFin(double longitudFin) { this.longitudFin = longitudFin; }
}