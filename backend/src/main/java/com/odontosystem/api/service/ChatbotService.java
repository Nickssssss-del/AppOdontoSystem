package com.odontosystem.api.service;

import com.odontosystem.api.entity.Dentist;
import com.odontosystem.api.repository.DentistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Lógica de negocio del ChatbotService.
 * Corresponde al endpoint POST /api/v1/chatbot/message.
 *
 * Implementa un asistente conversacional basado en reglas (coincidencia
 * de palabras clave: distrito, especialidad, saludo, agradecimiento),
 * suficiente para el alcance de la Entrega 1. Queda documentado como
 * línea de trabajo futuro migrar este motor a un servicio de PLN/IA
 * (ver sección 7 del documento técnico del proyecto).
 */
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final DentistRepository dentistRepository;

    public String processMessage(String rawMessage) {
        String message = normalize(rawMessage);

        if (containsAny(message, "hola", "buenas", "buenos dias", "buenas tardes")) {
            return "¡Hola! Soy el asistente virtual de OdontoSystem. Puedo ayudarte a buscar un odontólogo "
                    + "por distrito o especialidad, y a agendar tu cita. ¿Qué necesitas hoy?";
        }

        if (containsAny(message, "gracias")) {
            return "¡Con gusto! Si necesitas algo más, aquí estaré para ayudarte con tu cita odontológica.";
        }

        // Búsqueda por distrito: intenta encontrar coincidencias con distritos registrados
        List<Dentist> allDentists = dentistRepository.findAll();
        for (Dentist d : allDentists) {
            if (message.contains(normalize(d.getDistrict()))) {
                List<String> enDistrito = allDentists.stream()
                        .filter(x -> x.getDistrict().equalsIgnoreCase(d.getDistrict()))
                        .map(Dentist::getName)
                        .toList();
                return "En " + d.getDistrict() + " encontré " + enDistrito.size()
                        + " odontólogo(s) disponible(s): " + String.join(", ", enDistrito)
                        + ". Puedes revisar sus horarios y agendar tu cita desde la pantalla de búsqueda.";
            }
        }

        if (containsAny(message, "cita", "agendar", "reservar")) {
            return "Para agendar tu cita, selecciona un odontólogo desde la pantalla de búsqueda, elige el día "
                    + "y horario disponible, e indícame el motivo de tu consulta. ¿Buscas alguna especialidad "
                    + "o distrito en particular?";
        }

        if (containsAny(message, "precio", "costo", "cuanto cuesta")) {
            return "Los precios de consulta varían según el odontólogo y la especialidad, generalmente entre "
                    + "S/ 100 y S/ 220. Puedes ver el precio exacto de cada especialista en su perfil.";
        }

        return "He procesado tu solicitud. Puedes indicarme un distrito (por ejemplo, Miraflores o San Isidro) "
                + "o una especialidad, y te mostraré los odontólogos disponibles para agendar tu cita.";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /** Normaliza texto: minúsculas y sin tildes, para comparaciones robustas. */
    private String normalize(String text) {
        String lower = text == null ? "" : text.toLowerCase().trim();
        String withoutAccents = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(withoutAccents).replaceAll("");
    }
}
