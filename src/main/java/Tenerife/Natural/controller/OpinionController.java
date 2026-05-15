package Tenerife.Natural.controller;

import Tenerife.Natural.model.Opinion;
import Tenerife.Natural.model.Sendero;
import Tenerife.Natural.model.Usuario;
import Tenerife.Natural.repository.OpinionRepository;
import Tenerife.Natural.repository.SenderoRepository;
import Tenerife.Natural.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // IMPORTANTE: Para obtener el usuario en sesión
import java.util.List;

@RestController
@RequestMapping("/api/opiniones")
public class OpinionController {

    @Autowired
    private OpinionRepository opinionRepository;

    @Autowired
    private SenderoRepository senderoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. GUARDAR OPINIÓN
    @PostMapping("/enviar")
    public String enviar(@RequestParam Long senderoId,
                         @RequestParam String comentario,
                         @RequestParam int estrellas,
                         Principal principal) {

        try {
            if (principal == null) return "Error: No hay sesión activa";

            Sendero s = senderoRepository.findById(senderoId)
                    .orElseThrow(() -> new RuntimeException("Sendero no encontrado"));

            // Adaptado a tu UsuarioRepository que devuelve Optional
            Usuario u = usuarioRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

            Opinion op = new Opinion();
            op.setSendero(s);
            op.setUsuario(u);
            op.setComentario(comentario);
            op.setEstrellas(estrellas);

            opinionRepository.save(op);
            return "¡Opinión guardada con éxito!";

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // 2. LISTAR COMENTARIOS DE UN SENDERO
    @GetMapping("/sendero/{id}")
    public List<Opinion> listar(@PathVariable Long id) {
        return opinionRepository.findBySenderoId(id);
    }
}