package Tenerife.Natural.controller;

import Tenerife.Natural.model.Sendero;
import Tenerife.Natural.repository.SenderoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/senderos")
public class SenderoController {

    @Autowired
    private SenderoRepository senderoRepository;

    // 1. LISTAR SENDEROS (Read)
    @GetMapping
    public String listarSenderos(Model model) {
        model.addAttribute("senderos", senderoRepository.findAll());
        return "senderos/lista";
    }

    // 2. FORMULARIO PARA CREAR (Create)
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("sendero", new Sendero());
        return "senderos/formulario";
    }

    // 3. GUARDAR SENDERO (Create/Update)
    @PostMapping("/guardar")
    public String guardarSendero(@ModelAttribute("sendero") Sendero sendero) {
        senderoRepository.save(sendero);
        return "redirect:/senderos";
    }

    // 4. ELIMINAR SENDERO (Delete)
    @GetMapping("/eliminar/{id}")
    public String eliminarSendero(@PathVariable Long id) {
        senderoRepository.deleteById(id);
        return "redirect:/senderos";
    }


    @GetMapping("/alternar-acceso/{id}")
    public String alternarAcceso(@PathVariable Long id) {
        Sendero sendero = senderoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido:" + id));


        sendero.setAcceso(!sendero.isAcceso());

        senderoRepository.save(sendero);
        return "redirect:/senderos";
    }
}