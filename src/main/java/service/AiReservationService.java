package service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import exception.AiReservationException;
import model.CategoriaRecurso;

import java.time.LocalDate;

public class AiReservationService implements api.AiReservationService {

    private final Gson gson = new Gson();

    public AiReservationResponse interpretarSolicitud(String solicitud) throws AiReservationException {

        if (solicitud == null || solicitud.trim().isEmpty()) {
            throw new AiReservationException("La solicitud no puede estar vacía.");
        }

        try {
            String respuestaIA = consultarIA(solicitud);

            if (respuestaIA.trim().isEmpty()) {
                throw new AiReservationException("La IA no devolvió una respuesta.");
            }

            return convertirRespuesta(respuestaIA);

        } catch (AiReservationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiReservationException("Ocurrió un error al procesar la solicitud.", e);
        }
    }

    private String consultarIA(String solicitud) throws AiReservationException {
        try {
            Client client = Client.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();

            GenerateContentResponse respuesta = client.models.generateContent(
                    "gemini-3.6-flash",
                    construirPrompt(solicitud),
                    null
            );

            String texto = respuesta.text();

            if (texto == null || texto.isBlank()) {
                throw new AiReservationException("La IA no devolvió ningún resultado.");
            }

            // Limpiar bloques de markdown si la IA los incluye
            texto = texto.strip();
            if (texto.startsWith("```")) {
                texto = texto.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").strip();
            }

            return texto;
        } catch (AiReservationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiReservationException("No se pudo conectar con la IA: " + e.getMessage(), e);
        }
    }

    private String construirPrompt(String solicitud) {

        LocalDate hoy = LocalDate.now();

        // Cargar las categorías reales del sistema
        java.util.List<CategoriaRecurso> cats =
                repository.Repositories.categorias().listarTodos();
        StringBuilder listaCats = new StringBuilder();
        for (CategoriaRecurso c : cats) {
            listaCats.append("- ").append(c.getDescripcion()).append("\n");
        }

        return String.format("""
            Eres un asistente para un sistema de reservas.

            La fecha de hoy es: %s

            Analiza la solicitud del usuario y extrae:

            - actividad
            - fecha
            - horaInicio
            - horaFin
            - categorias

            Las categorías disponibles en el sistema son (usa exactamente estos nombres):
            %s

            Devuelve únicamente un JSON con este formato:

            {
              "actividad": "string",
              "fecha": "YYYY-MM-DD",
              "horaInicio": "HH:mm",
              "horaFin": "HH:mm",
              "categorias": ["Nombre exacto de la categoría"]
            }

            Para expresiones como "mañana", "pasado mañana",
            "el próximo martes", etc., utiliza la fecha de hoy
            como referencia.

            No agregues explicaciones ni texto fuera del JSON.

            Solicitud del usuario: %s
            """, hoy, listaCats.toString(), solicitud);
    }


    private AiReservationResponse convertirRespuesta(String respuestaIA) throws AiReservationException {
        try {
            RawIA raw = gson.fromJson(respuestaIA, RawIA.class);

            if (raw == null) {
                throw new AiReservationException("La respuesta de la IA no tiene el formato esperado.");
            }

            AiReservationResponse respuesta = new AiReservationResponse(
                    raw.actividad,
                    raw.fecha != null ? java.time.LocalDate.parse(raw.fecha) : null,
                    raw.horaInicio != null ? java.time.LocalTime.parse(raw.horaInicio) : null,
                    raw.horaFin != null ? java.time.LocalTime.parse(raw.horaFin) : null,
                    raw.categorias != null
                            ? raw.categorias.stream()
                                .map(CategoriaRecurso::valueOf)
                                .collect(java.util.stream.Collectors.toList())
                            : null
            );

            validarRespuesta(respuesta);
            return respuesta;
        } catch (JsonSyntaxException e) {
            throw new AiReservationException("La respuesta de la IA tiene un formato inválido.", e);
        } catch (AiReservationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiReservationException("Ocurrió un error inesperado al convertir la respuesta de la IA.", e);
        }
    }

    private static class RawIA {
        String actividad;
        String fecha;
        String horaInicio;
        String horaFin;
        java.util.List<String> categorias;
    }

    private void validarRespuesta(AiReservationResponse respuesta) throws AiReservationException {
        if (respuesta.getActividad() == null || respuesta.getActividad().trim().isEmpty()) {
            throw new AiReservationException("La actividad no puede estar vacía.");
        }

        if (respuesta.getFecha() == null) {
            throw new AiReservationException("La fecha no pudo ser interpretada.");
        }
        if (respuesta.getHoraInicio() == null || respuesta.getHoraFin() == null) {
            throw new AiReservationException("La hora no pudo ser interpretada.");
        }

        if(!respuesta.getHoraInicio().isBefore(respuesta.getHoraFin())) {
            throw new AiReservationException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        if(respuesta.getCategorias() == null || respuesta.getCategorias().isEmpty()) {
            throw new AiReservationException("Debe especificar al menos una categoría de recurso.");
        }
    }
}