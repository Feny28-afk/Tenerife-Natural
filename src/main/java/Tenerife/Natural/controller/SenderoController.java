package Tenerife.Natural.controller;

import Tenerife.Natural.model.*;
import Tenerife.Natural.repository.*;
import Tenerife.Natural.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/senderos")
public class SenderoController {

    @Autowired
    private SenderoRepository senderoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OpinionRepository opinionRepository;

    @Autowired
    private PymeRepository pymeRepository;

    @Autowired
    private WeatherService weatherService;

    // 1. LISTAR SENDEROS
    @GetMapping
    public String listarSenderos(Model model) {
        List<Sendero> senderos = senderoRepository.findAll();
        for (Sendero sendero : senderos) {
            String climaActual = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
            sendero.setEstadoMeteorologico(climaActual);
        }
        model.addAttribute("senderos", senderos);
        return "senderos/lista";
    }

    // 2. FORMULARIO PARA CREAR
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("sendero", new Sendero());
        return "senderos/formulario";
    }

    // 3. GUARDAR SENDERO
    @PostMapping("/guardar")
    public String guardarSendero(@ModelAttribute("sendero") Sendero sendero) {
        String climaActual = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
        sendero.setEstadoMeteorologico(climaActual);
        senderoRepository.save(sendero);
        return "redirect:/senderos";
    }

    // 4. ELIMINAR SENDERO
    @GetMapping("/eliminar/{id}")
    public String eliminarSendero(@PathVariable Long id) {
        senderoRepository.deleteById(id);
        return "redirect:/senderos";
    }

    // 5. ALTERNAR ACCESO
    @GetMapping("/alternar-acceso/{id}")
    public String alternarAcceso(@PathVariable Long id) {
        Sendero sendero = senderoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido:" + id));
        sendero.setAcceso(!sendero.isAcceso());
        senderoRepository.save(sendero);
        return "redirect:/senderos";
    }

    // 6. GUARDAR FAVORITO
    @PostMapping("/favorito/{senderoId}")
    @ResponseBody
    public String añadirFavorito(@PathVariable Long senderoId, @RequestParam Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Sendero sendero = senderoRepository.findById(senderoId)
                    .orElseThrow(() -> new RuntimeException("Sendero no encontrado"));

            if (!usuario.getFavoritos().contains(sendero)) {
                usuario.getFavoritos().add(sendero);
                usuarioRepository.save(usuario);
                return "¡Añadido a tus favoritos! ⭐";
            } else {
                return "Este sendero ya está en tu lista.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // 7. VER PERFIL DEL USUARIO
    @GetMapping("/perfil/{usuarioId}")
    public String verPerfil(@PathVariable Long usuarioId, Model model) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("usuario", usuario);
        model.addAttribute("favoritos", usuario.getFavoritos());
        return "usuarios/perfil";
    }

    // 8. VER DETALLE, RESEÑAS Y PYMES
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Sendero sendero = senderoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido:" + id));

        String clima = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
        sendero.setEstadoMeteorologico(clima);

        List<Opinion> opiniones = opinionRepository.findAll().stream()
                .filter(o -> o.getSendero().getId().equals(id))
                .toList();

        List<Pyme> pymes = pymeRepository.findBySenderoId(id);

        model.addAttribute("sendero", sendero);
        model.addAttribute("opiniones", opiniones);
        model.addAttribute("pymes", pymes);
        model.addAttribute("usuarioId", 1L);

        Usuario usuario = usuarioRepository.findById(1L).orElse(null);
        if(usuario != null) {
            model.addAttribute("favoritos", usuario.getFavoritos());
        }

        return "senderos/detalle";
    }

    // 9. PROCESAR NUEVA OPINIÓN
    @PostMapping("/opiniones/guardar")
    public String guardarOpinion(@RequestParam Long senderoId,
                                 @RequestParam int estrellas,
                                 @RequestParam String comentario) {

        Opinion nuevaOpinion = new Opinion();
        nuevaOpinion.setEstrellas(estrellas);
        nuevaOpinion.setComentario(comentario);
        nuevaOpinion.setSendero(senderoRepository.findById(senderoId).orElse(null));
        nuevaOpinion.setUsuario(usuarioRepository.findById(1L).orElse(null));

        opinionRepository.save(nuevaOpinion);
        return "redirect:/senderos/detalle/" + senderoId;
    }

    // 10. ALGORITMO DE REDISTRIBUCIÓN CON PYMES (Actualizado)
    @GetMapping("/sugerencias")
    public String obtenerSugerencias(Model model) {
        List<Sendero> todos = senderoRepository.findAll();

        // Filtramos rutas con menos de 3 reseñas
        List<Sendero> sugerencias = todos.stream()
                .filter(s -> {
                    long conteo = opinionRepository.findAll().stream()
                            .filter(o -> o.getSendero().getId().equals(s.getId()))
                            .count();
                    return conteo < 3;
                })
                .collect(Collectors.toList());

        Collections.shuffle(sugerencias);
        List<Sendero> seleccionados = sugerencias.stream().limit(3).toList();

        // Buscamos las pymes para cada sendero seleccionado
        Map<Long, List<Pyme>> pymesPorSendero = seleccionados.stream()
                .collect(Collectors.toMap(
                        Sendero::getId,
                        s -> pymeRepository.findBySenderoId(s.getId())
                ));

        model.addAttribute("sugerencias", seleccionados);
        model.addAttribute("pymesPorSendero", pymesPorSendero);

        return "senderos/sugerencias";
    }
}