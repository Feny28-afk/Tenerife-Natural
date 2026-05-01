package Tenerife.Natural.repository;

import Tenerife.Natural.model.TransportePublico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransporteRepository extends JpaRepository<TransportePublico, Long> {

    // Este es el que añadimos antes para las paradas individuales
    List<TransportePublico> findByStopId(String stopId);

    // ESTA ES LA LÍNEA QUE TE FALTA AHORA:
    List<TransportePublico> findBySenderoId(Long senderoId);
}