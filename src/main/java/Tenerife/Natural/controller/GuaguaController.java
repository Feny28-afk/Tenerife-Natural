package Tenerife.Natural.controller;

import Tenerife.Natural.repository.GuaguaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
// CAMBIO: Usamos /api/horarios para evitar el conflicto con StopController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "*")
public class GuaguaController {

    @Autowired
    private GuaguaRepository guaguaRepository;

    // La URL final será: http://localhost:8080/api/horarios/parada/{id}
    @GetMapping("/parada/{id}")
    public List<Map<String, Object>> getTiempos(@PathVariable String id) {
        List<Object[]> resultados = guaguaRepository.findProximosHorarios(id);
        List<Map<String, Object>> respuesta = new ArrayList<>();

        if (resultados != null) {
            for (Object[] fila : resultados) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("linea", fila[0]);
                // Formateamos el tiempo (HH:mm) validando que no venga nulo
                if (fila[1] != null) {
                    dto.put("tiempoLlegada", fila[1].toString().substring(0, 5));
                } else {
                    dto.put("tiempoLlegada", "--:--");
                }
                respuesta.add(dto);
            }
        }
        return respuesta;
    }
}