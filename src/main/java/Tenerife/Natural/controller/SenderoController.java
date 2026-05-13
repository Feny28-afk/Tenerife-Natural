package Tenerife.Natural.controller;

import Tenerife.Natural.model.*;
import Tenerife.Natural.repository.*;
import Tenerife.Natural.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/senderos")
public class SenderoController {

    @Autowired private SenderoRepository senderoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private OpinionRepository opinionRepository;
    @Autowired private PymeRepository pymeRepository;
    @Autowired private WeatherService weatherService;

    @GetMapping("/sugerencias")
    public String mostrarSugerencias() {
        return "senderos/sugerencias";
    }

    @GetMapping
    public String listarSenderos(Model model) {
        List<Sendero> senderos = senderoRepository.findAll();
        for (Sendero sendero : senderos) {
            if (sendero.getLatitud() != 0) {
                String climaActual = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
                sendero.setEstadoMeteorologico(climaActual);
            }
        }
        model.addAttribute("senderos", senderos);
        model.addAttribute("sendero", new Sendero());
        return "senderos/lista";
    }

    @PostMapping("/guardar")
    public String guardarSendero(@ModelAttribute("sendero") Sendero sendero) {
        String climaActual = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
        sendero.setEstadoMeteorologico(climaActual);
        if (sendero.getDificultad() == null) sendero.setDificultad("Baja");
        sendero.setAcceso(true);
        senderoRepository.save(sendero);
        return "redirect:/senderos";
    }

    @PostMapping("/api/nuevo")
    @ResponseBody
    public String guardarDesdeMapa(@RequestBody Sendero sendero) {
        try {
            String clima = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
            sendero.setEstadoMeteorologico(clima);

            double distanciaKm = calcularDistancia(
                    sendero.getLatitud(), sendero.getLongitud(),
                    sendero.getLatitudFin(), sendero.getLongitudFin()
            );

            int temp = 22;
            try {
                String tempNum = clima.replaceAll("[^0-9]", "");
                if (tempNum.length() >= 2) {
                    temp = Integer.parseInt(tempNum.substring(0, 2));
                } else if (!tempNum.isEmpty()) {
                    temp = Integer.parseInt(tempNum);
                }
            } catch(Exception e) { temp = 22; }

            // --- LÓGICA DE 1 KM ---
            String dificultadFinal = "Baja";

            if (distanciaKm > 1.0) {
                // Solo si supera 1km evaluamos el clima y distancias mayores
                if (distanciaKm > 7.0 || temp >= 40) {
                    dificultadFinal = "Alta";
                } else if (distanciaKm > 3.0 || temp >= 32) {
                    dificultadFinal = "Media";
                }
            } else {
                // Paseo corto: Siempre Baja
                dificultadFinal = "Baja";
            }

            sendero.setDificultad(dificultadFinal);
            sendero.setAcceso(true);
            senderoRepository.save(sendero);

            return "¡Sendero registrado! Distancia: " + String.format("%.2f", distanciaKm) +
                    " km. Dificultad final: " + sendero.getDificultad() + " UwU";
        } catch (Exception e) {
            return "Error al guardar: " + e.getMessage();
        }
    }

    @GetMapping("/eliminar/{id}")
    @Transactional
    public String eliminarSendero(@PathVariable Long id) {
        try {
            Sendero sendero = senderoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID inválido:" + id));

            List<Usuario> usuarios = usuarioRepository.findAll();
            for (Usuario u : usuarios) {
                if (u.getFavoritos().contains(sendero)) {
                    u.getFavoritos().remove(sendero);
                    usuarioRepository.save(u);
                }
            }

            List<Opinion> opiniones = opinionRepository.findAll();
            for (Opinion op : opiniones) {
                if (op.getSendero() != null && op.getSendero().getId().equals(id)) {
                    op.setSendero(null);
                    opinionRepository.save(op);
                }
            }

            senderoRepository.delete(sendero);
            return "redirect:/senderos";
        } catch (Exception e) {
            return "redirect:/senderos?error=true";
        }
    }

    @GetMapping("/alternar-acceso/{id}")
    public String alternarAcceso(@PathVariable Long id) {
        Sendero sendero = senderoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido:" + id));
        sendero.setAcceso(!sendero.isAcceso());
        senderoRepository.save(sendero);
        return "redirect:/senderos";
    }

    @PostMapping("/favorito/{senderoId}")
    @ResponseBody
    public String añadirFavorito(@PathVariable Long senderoId, @RequestParam Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
            Sendero sendero = senderoRepository.findById(senderoId).orElseThrow();

            if (!usuario.getFavoritos().contains(sendero)) {
                usuario.getFavoritos().add(sendero);
                usuarioRepository.save(usuario);
                return "¡Añadido a favoritos! ⭐";
            }
            return "Ya está en tu lista.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Sendero sendero = senderoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido:" + id));

        Usuario usuario = usuarioRepository.findById(1L).orElse(new Usuario());
        String clima = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
        sendero.setEstadoMeteorologico(clima);

        List<Opinion> opiniones = opinionRepository.findAll().stream()
                .filter(o -> o.getSendero() != null && o.getSendero().getId().equals(id))
                .collect(Collectors.toList());

        model.addAttribute("sendero", sendero);
        model.addAttribute("usuario", usuario);
        model.addAttribute("opiniones", opiniones);
        model.addAttribute("pymes", pymeRepository.findBySenderoId(id));
        model.addAttribute("usuarioId", 1L);

        return "senderos/detalle";
    }

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

    @GetMapping("/favoritos/lista")
    public String listarFavoritosDetallados(Model model) {
        Usuario usuario = usuarioRepository.findById(1L).orElseThrow();
        List<Sendero> favoritos = usuario.getFavoritos();
        for (Sendero s : favoritos) {
            String clima = weatherService.obtenerEstadoTiempo(s.getLatitud(), s.getLongitud());
            s.setEstadoMeteorologico(clima);
        }
        model.addAttribute("favoritos", favoritos);
        return "senderos/favoritos";
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        double radioTierra = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return radioTierra * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}