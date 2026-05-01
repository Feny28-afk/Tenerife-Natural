package Tenerife.Natural.model;

import jakarta.persistence.*;

@Entity
@Table(name = "titsa_stops")
public class Stop {

    @Id
    @Column(name = "stop_id")
    private String id;

    @Column(name = "stop_name")
    private String name;

    @Column(name = "stop_lat")
    private Double latitude;

    @Column(name = "stop_lon")
    private Double longitude;

    public Stop() {}

    // Getters necesarios para que Spring cree el JSON
    public String getId() { return id; }
    public String getName() { return name; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}