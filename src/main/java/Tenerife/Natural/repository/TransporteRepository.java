package Tenerife.Natural.repository;

import Tenerife.Natural.model.TransportePublico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransporteRepository extends JpaRepository<TransportePublico, Long> {
    List<TransportePublico> findBySenderoId(Long senderoId);
}