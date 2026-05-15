package Tenerife.Natural.controller;

import Tenerife.Natural.model.Usuario;
import Tenerife.Natural.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired; // Faltaba esto
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Faltaba esto
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal; // Faltaba esto

@Controller
public class MapController {

    @Autowired
    private UsuarioRepository usuarioRepository; // Faltaba inyectar el repositorio

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            // Ahora IntelliJ ya encontrará findByUsername porque hemos importado UsuarioRepository
            Usuario u = usuarioRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            model.addAttribute("usuarioLogueado", u);
        }
        return "senderos";
    }

    @GetMapping("/senderos.html")
    public String senderos() {
        return "redirect:/";
    }
}