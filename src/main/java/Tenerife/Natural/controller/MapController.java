package Tenerife.Natural.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController {

    @GetMapping("/")
    public String home() {
        return "index"; // Esto busca templates/index.html
    }

    @GetMapping("/index.html")
    public String index() {
        return "redirect:/"; // Redirige para limpiar la URL
    }
}