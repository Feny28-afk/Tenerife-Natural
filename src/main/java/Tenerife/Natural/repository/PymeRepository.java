package Tenerife.Natural.repository;

import Tenerife.Natural.model.Pyme;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PymeRepository extends JpaRepository<Pyme, Long> {
    List<Pyme> findBySenderoId(Long senderoId);
}