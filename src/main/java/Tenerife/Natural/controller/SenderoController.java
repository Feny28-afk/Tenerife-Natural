package Tenerife.Natural.controller;

import Tenerife.Natural.model.Sendero;
import Tenerife.Natural.model.Usuario;
import Tenerife.Natural.repository.SenderoRepository;
import Tenerife.Natural.repository.UsuarioRepository;
import Tenerife.Natural.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/senderos")
public class SenderoController {

    @Autowired
    private SenderoRepository senderoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Necesario para el Sprint 6

    @Autowired
    private WeatherService weatherService;

    // 1. LISTAR SENDEROS (Gestión administrativa)
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

    // --- NOVEDADES SPRINT 6: PERFILES Y FAVORITOS ---

    // 6. GUARDAR FAVORITO (Llamada desde el botón del Mapa)
    @PostMapping("/favorito/{senderoId}")
    @ResponseBody // Importante: devuelve texto plano, no una página HTML
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

    // 7. VER PERFIL DEL USUARIO (Rutas favoritas y nivel físico)
    @GetMapping("/perfil/{usuarioId}")
    public String verPerfil(@PathVariable Long usuarioId, Model model) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("usuario", usuario);
        model.addAttribute("favoritos", usuario.getFavoritos());
        return "usuarios/perfil"; // Necesitarás crear esta vista perfil.html
    }
}