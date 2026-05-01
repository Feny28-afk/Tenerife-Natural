package Tenerife.Natural.repository;

import Tenerife.Natural.model.TransportePublico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GuaguaRepository extends JpaRepository<TransportePublico, Long> {

    // Consulta usando tus tablas titsa_ para obtener horarios estáticos
    @Query(value = "SELECT r.route_short_name AS linea, st.arrival_time AS tiempoLlegada " +
            "FROM titsa_stop_times st " +
            "JOIN titsa_trips t ON st.trip_id = t.trip_id " +
            "JOIN titsa_routes r ON t.route_id = r.route_id " +
            "WHERE st.stop_id = :stopId " +
            "AND st.arrival_time > CURTIME() " +
            "ORDER BY st.arrival_time ASC LIMIT 3", nativeQuery = true)
    List<Object[]> findProximosHorarios(@Param("stopId") String stopId);
}