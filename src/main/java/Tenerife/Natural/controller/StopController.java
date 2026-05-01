package Tenerife.Natural.controller;

import Tenerife.Natural.model.Stop;
import Tenerife.Natural.model.TransportePublico;
import Tenerife.Natural.repository.StopRepository;
import Tenerife.Natural.service.TransporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guaguas")
@CrossOrigin(origins = "*")
public class StopController {

    @Autowired
    private StopRepository stopRepository;

    // 1. AÑADE ESTO: Inyectamos el servicio correctamente
    @Autowired
    private TransporteService transporteService;

    @GetMapping("/paradas")
    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }

    @GetMapping("/parada/{id}/tiempos")
    public List<TransportePublico> getTiemposParada(@PathVariable String id) {
        // 2. CORRECCIÓN: Usamos 'transporteService' (la instancia), no 'TransporteService' (la clase)
        return transporteService.obtenerTiemposParaParada(id);
    }
}