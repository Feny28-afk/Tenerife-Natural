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

    // 1. LISTAR SENDEROS (Vista Unificada)
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
        model.addAttribute("sendero", new Sendero()); // Objeto para el formulario en la misma página
        return "senderos/lista";
    }

    // 2. GUARDAR SENDERO (Desde formulario HTML unificado)
    @PostMapping("/guardar")
    public String guardarSendero(@ModelAttribute("sendero") Sendero sendero) {
        String climaActual = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
        sendero.setEstadoMeteorologico(climaActual);

        // Asignación por defecto si viene del formulario simple
        if (sendero.getDificultad() == null) sendero.setDificultad("Baja");
        sendero.setAcceso(true);

        senderoRepository.save(sendero);
        return "redirect:/senderos";
    }

    // 3. API: GUARDAR SENDERO DESDE EL MAPA (Lógica de dificultad inteligente)
    // 3. API: GUARDAR SENDERO DESDE EL MAPA (Lógica reajustada)
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

            // Extraer temperatura de forma más segura
            int temp = 22;
            try {
                // Solo extraemos los primeros 2 dígitos para evitar errores con coordenadas
                String tempNum = clima.replaceAll("[^0-9]", "");
                if (tempNum.length() >= 2) {
                    temp = Integer.parseInt(tempNum.substring(0, 2));
                } else if (!tempNum.isEmpty()) {
                    temp = Integer.parseInt(tempNum);
                }
            } catch(Exception e) { temp = 22; }

            // --- LÓGICA BLINDADA ---
            String dificultadFinal = "Baja"; // Por defecto

            if (distanciaKm < 3.0) {
                // SI MIDE MENOS DE 3KM ES BAJA, SIN IMPORTAR EL CLIMA
                dificultadFinal = "Baja";
            } else if (distanciaKm > 7.0 || temp >= 40) {
                dificultadFinal = "Alta";
            } else if (distanciaKm > 3.0 || temp >= 32) {
                dificultadFinal = "Media";
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

    // 4. ELIMINAR SENDERO (Corregido para evitar el White Label Error)
    @GetMapping("/eliminar/{id}")
    @Transactional // Esto asegura que si algo falla, no se borre nada a medias
    public String eliminarSendero(@PathVariable Long id) {
        try {
            Sendero sendero = senderoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID inválido:" + id));

            // PASO A: Rompemos el vínculo con los FAVORITOS de los usuarios
            // Esto evita el error de la pantalla blanca (DataIntegrityViolation)
            List<Usuario> usuarios = usuarioRepository.findAll();
            for (Usuario u : usuarios) {
                if (u.getFavoritos().contains(sendero)) {
                    u.getFavoritos().remove(sendero);
                    usuarioRepository.save(u);
                }
            }

            // PASO B: Las opiniones NO se borran.
            // Las ponemos en NULL para que la opinión exista pero el sendero ya no.
            List<Opinion> opiniones = opinionRepository.findAll();
            for (Opinion op : opiniones) {
                if (op.getSendero() != null && op.getSendero().getId().equals(id)) {
                    op.setSendero(null);
                    opinionRepository.save(op);
                }
            }

            // PASO C: Ahora que el sendero está "suelto", lo borramos de la base de datos
            senderoRepository.delete(sendero);

            return "redirect:/senderos";
        } catch (Exception e) {
            // Log del error para depuración
            System.out.println("Error al borrar: " + e.getMessage());
            return "redirect:/senderos?error=true";
        }
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

    // 7. VER DETALLE (Actualizado para incluir al usuario y sus favoritos)
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Sendero sendero = senderoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido:" + id));

        // IMPORTANTE: Buscamos al usuario para que la pestaña de favoritos no salga vacía
        Usuario usuario = usuarioRepository.findById(1L).orElse(new Usuario());

        String clima = weatherService.obtenerEstadoTiempo(sendero.getLatitud(), sendero.getLongitud());
        sendero.setEstadoMeteorologico(clima);

        // Corregido para filtrar correctamente
        List<Opinion> opiniones = opinionRepository.findAll().stream()
                .filter(o -> o.getSendero() != null && o.getSendero().getId().equals(id))
                .collect(Collectors.toList());

        model.addAttribute("sendero", sendero);
        model.addAttribute("usuario", usuario); // Clave para que funcione 'usuario.favoritos' en el HTML
        model.addAttribute("opiniones", opiniones);
        model.addAttribute("pymes", pymeRepository.findBySenderoId(id));
        model.addAttribute("usuarioId", 1L);

        return "senderos/detalle";
    }

    // 8. PROCESAR NUEVA OPINIÓN
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
    // Nuevo método para ver la lista detallada de favoritos
    @GetMapping("/favoritos/lista")
    public String listarFavoritosDetallados(Model model) {
        Usuario usuario = usuarioRepository.findById(1L).orElseThrow();
        List<Sendero> favoritos = usuario.getFavoritos();

        for (Sendero s : favoritos) {
            String clima = weatherService.obtenerEstadoTiempo(s.getLatitud(), s.getLongitud());
            s.setEstadoMeteorologico(clima);
        }

        model.addAttribute("favoritos", favoritos);
        // ANTES: return "senderos/favoritos_detalle";
        return "senderos/favoritos"; // AHORA: Coincide con tu nuevo nombre de archivo
    }

    // --- UTILIDAD: FÓRMULA HAVERSINE ---
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