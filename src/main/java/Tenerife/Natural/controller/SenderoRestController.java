package Tenerife.Natural.controller;

import Tenerife.Natural.model.Sendero;
import Tenerife.Natural.repository.SenderoRepository;
import Tenerife.Natural.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/senderos")
@CrossOrigin(origins = "*") // Permite al mapa acceder a los datos
public class SenderoRestController {

    @Autowired
    private SenderoRepository senderoRepository;

    @Autowired
    private WeatherService weatherService;

    @GetMapping
    public List<Sendero> obtenerSenderosParaMapa() {
        List<Sendero> senderos = senderoRepository.findAll();

        // Le inyectamos el clima en tiempo real usando coordenadas
        for (Sendero s : senderos) {
            // CAMBIO AQUÍ: Usamos getLatitud() y getLongitud() en lugar del nombre
            String clima = weatherService.obtenerEstadoTiempo(s.getLatitud(), s.getLongitud());
            s.setEstadoMeteorologico(clima);
        }

        return senderos;
    }
}