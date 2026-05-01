package Tenerife.Natural.controller;

import Tenerife.Natural.model.Opinion;
import Tenerife.Natural.model.Sendero;
import Tenerife.Natural.model.Usuario;
import Tenerife.Natural.repository.OpinionRepository;
import Tenerife.Natural.repository.SenderoRepository;
import Tenerife.Natural.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    // Se usa @PostMapping para recibir los datos del formulario o fetch
    @PostMapping("/enviar")
    public String enviar(@RequestParam Long senderoId,
                         @RequestParam Long usuarioId,
                         @RequestParam String comentario,
                         @RequestParam int estrellas) {

        try {
            Sendero s = senderoRepository.findById(senderoId)
                    .orElseThrow(() -> new RuntimeException("Sendero no encontrado"));
            Usuario u = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Opinion op = new Opinion();
            op.setSendero(s);
            op.setUsuario(u);
            op.setComentario(comentario);
            op.setEstrellas(estrellas);

            opinionRepository.save(op);
            return "¡Opinión guardada con éxito!";

        } catch (Exception e) {
            return "Error al guardar la opinión: " + e.getMessage();
        }
    }

    // 2. LISTAR COMENTARIOS DE UN SENDERO
    // Devuelve un JSON con todas las opiniones de ese ID
    @GetMapping("/sendero/{id}")
    public List<Opinion> listar(@PathVariable Long id) {
        // Asegúrate de que este método esté declarado en OpinionRepository
        return opinionRepository.findBySenderoId(id);
    }
}