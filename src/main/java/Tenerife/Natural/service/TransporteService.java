package Tenerife.Natural.service;

import Tenerife.Natural.model.TransportePublico;
import Tenerife.Natural.repository.TransporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class TransporteService {

    @Autowired
    private TransporteRepository transporteRepository;

    public List<TransportePublico> obtenerTransporteEnTiempoReal(Long senderoId) {
        List<TransportePublico> transportes = transporteRepository.findBySenderoId(senderoId);
        Random random = new Random();

        for (TransportePublico t : transportes) {
            // Simulamos que la API de TITSA nos dice cuánto falta
            // Si hay disponibilidad, ponemos un tiempo entre 2 y 25 mins
            if (t.isDisponibilidad()) {
                t.setTiempoLlegada(random.nextInt(23) + 2);
            } else {
                t.setTiempoLlegada(-1); // No disponible
            }
        }
        return transportes;
    }
}