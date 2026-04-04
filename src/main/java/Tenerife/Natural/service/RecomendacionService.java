package Tenerife.Natural.service;

import org.springframework.stereotype.Service;

@Service
public class RecomendacionService {

    public String evaluarAptitud(int nivelUsuario, String dificultadSendero) {
        // Normalizamos la dificultad a números para comparar
        int nivelSendero = 0;
        if (dificultadSendero.equalsIgnoreCase("Baja")) nivelSendero = 1;
        else if (dificultadSendero.equalsIgnoreCase("Media")) nivelSendero = 2;
        else if (dificultadSendero.equalsIgnoreCase("Alta")) nivelSendero = 3;

        if (nivelUsuario >= nivelSendero) {
            return "✅ Recomendado para tu nivel";
        } else if (nivelUsuario + 1 == nivelSendero) {
            return "⚠️ ¡Cuidado! Es un reto para ti";
        } else {
            return "❌ No recomendado (Demasiado exigente)";
        }
    }
}