package Tenerife.Natural.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Locale; // Importante para forzar el punto

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String obtenerEstadoTiempo(double lat, double lon) {
        // Forzamos Locale.US para que los decimales usen PUNTO (.) y no COMA (,)
        // Esto evita que la API reciba coordenadas mal formadas
        String url = String.format(Locale.US, "%s?key=%s&q=%.6f,%.6f&lang=es", apiUrl, apiKey, lat, lon);

        try {
            System.out.println("URL enviada: " + url); // Para que puedas clicar en la consola y verificar

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("current")) {
                Map<String, Object> current = (Map<String, Object>) response.get("current");
                Map<String, Object> condition = (Map<String, Object>) current.get("condition");

                String estado = (String) condition.get("text");
                // Usamos Number para evitar errores si el JSON devuelve un entero o un decimal
                Number temp = (Number) current.get("temp_c");

                System.out.println("Consultando clima para: " + lat + "," + lon);
                System.out.println("Respuesta real: " + estado + " con " + temp + "°C");

                return estado + ", " + temp + "°C";
            }
            return "Datos no encontrados";

        } catch (Exception e) {
            System.err.println("Error en WeatherService: " + e.getMessage());
            return "Clima no disponible";
        }
    }
}