package Tenerife.Natural.controller;

import Tenerife.Natural.model.Sendero;
import Tenerife.Natural.repository.SenderoRepository;
import Tenerife.Natural.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/senderos")
public class SenderoController {

    @Autowired
    private SenderoRepository senderoRepository;

    @Autowired
    private WeatherService weatherService;

    // 1. LISTAR SENDEROS
    @GetMapping
    public String listarSenderos(Model model) {
        List<Sendero> senderos = senderoRepository.findAll();

        // Para cada sendero, consultamos el clima real usando latitud y longitud
        for (Sendero sendero : senderos) {
            // Pasamos latitud y longitud al servicio corregido
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
        return "senderos/formulario"; // Quitamos el .html para que Thymeleaf resuelva bien
    }

    // 3. GUARDAR SENDERO
    @PostMapping("/guardar")
    public String guardarSendero(@ModelAttribute("sendero") Sendero sendero) {
        // Obtenemos el clima antes de guardar para que ya tenga datos en la base de datos
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
}