package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Genera avisos clínicos a partir del estado actual del caso y de su evaluación más reciente.
 * Son avisos basados en las recomendaciones de la OPS; la decisión clínica corresponde al médico.
 */
public class AlertasClinicas {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Nombres legibles de los signos para los mensajes que lee el profesional. */
    private static final Map<String, String> NOMBRES = Map.ofEntries(
            Map.entry("POSTRACION", "postración"),
            Map.entry("LUMBALGIA", "lumbalgia"),
            Map.entry("VOMITOS_PERSISTENTES", "vómitos persistentes"),
            Map.entry("PETEQUIAS", "petequias"),
            Map.entry("EPISTAXIS", "epistaxis"),
            Map.entry("SANGRADO_GINGIVAL", "sangrado gingival"),
            Map.entry("VERTIGO", "vértigo"),
            Map.entry("LETARGIA", "letargia"),
            Map.entry("NISTAGMO", "nistagmo"),
            Map.entry("RIGIDEZ_NUCA", "rigidez de nuca"),
            Map.entry("DOLOR_ABDOMINAL_INTENSO", "dolor abdominal intenso"),
            Map.entry("ACUMULACION_LIQUIDOS", "acumulación de líquidos"),
            Map.entry("SANGRADO_MUCOSAS", "sangrado de mucosas"),
            Map.entry("LETARGO_IRRITABILIDAD", "letargo o irritabilidad"),
            Map.entry("HIPOTENSION_POSTURAL", "hipotensión postural"),
            Map.entry("HEPATOMEGALIA", "hepatomegalia"),
            Map.entry("AUMENTO_PROGRESIVO_HEMATOCRITO", "aumento progresivo del hematocrito"));


    public List<Alerta> evaluar(Caso caso, LocalDate hoy) {

        List<Alerta> alertas = new ArrayList<>();

        if (!caso.estaAbierto()) {
            return alertas;
        }

        EvaluacionClinica ultima = caso.getEvaluaciones().stream()
                .max(Comparator.comparing(EvaluacionClinica::getFecha))
                .orElse(null);

        Set<SignoClinico> neuro = ultima == null ? EnumSet.noneOf(SignoClinico.class)
                : interseccion(ultima.getSignosClinicos(), SignoClinico.NEUROLOGICOS);

        if (!neuro.isEmpty() || caso.getManifestaciones().contains(Manifestacion.NEUROLOGICA)) {
            String origen = neuro.isEmpty() ? "manifestación neurológica registrada en el caso" : nombres(neuro);
            alertas.add(new Alerta(TipoAlerta.NEUROINVASIVA,
                    "Posible forma neuroinvasiva (" + origen + "): la OPS indica valorar el ingreso en una"
                            + " unidad especializada con monitoreo constante y considerar meningitis o encefalitis"));
        }

        if (ultima != null && !ultima.getSignosAlarmaDengue().isEmpty()) {
            alertas.add(new Alerta(TipoAlerta.SIGNOS_ALARMA_DENGUE,
                    "Signos de alarma de dengue (" + nombres(ultima.getSignosAlarmaDengue()) + "): valorar"
                            + " dengue según el algoritmo de la OPS, que indica internar en hospital o unidad de dengue"));
        }

        if (ultima != null) {
            Set<SignoClinico> hemorragias = interseccion(ultima.getSignosClinicos(), SignoClinico.HEMORRAGICOS);
            if (!hemorragias.isEmpty()) {
                alertas.add(new Alerta(TipoAlerta.HEMORRAGIA,
                        "Manifestaciones hemorrágicas: " + nombres(hemorragias)));
            }
        }

        if (caso.isEmbarazada()) {
            alertas.add(new Alerta(TipoAlerta.GESTANTE,
                    "Caso en gestante: seguimiento estrecho y vigilancia de posible transmisión vertical (OPS)"));
        }

        LocalDate proximo = caso.proximoControl();
        if (proximo != null && proximo.isBefore(hoy)) {
            alertas.add(new Alerta(TipoAlerta.CONTROL_VENCIDO, "Control vencido desde el " + proximo.format(FECHA)));
        }

        return alertas;
    }

    private static <E extends Enum<E>> Set<E> interseccion(Set<E> a, Set<E> b) {
        Set<E> resultado = new HashSet<>(a);
        resultado.retainAll(b);
        return resultado;
    }

    private static String nombres(Set<? extends Enum<?>> valores) {
        return valores.stream()
                .map(v -> NOMBRES.getOrDefault(v.name(), v.name().toLowerCase().replace('_', ' ')))
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
