package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.GetDailyDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.model.OptionId;
import com.eojeda89.decididorapi.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/**
 * "Decisión del día" (Fase 3.5): resultado determinístico por (fecha UTC,
 * usuario, opciones) -- pedir la misma decisión varias veces el mismo día da
 * siempre el mismo resultado, sin persistir ni crear filas nuevas cada vez
 * (se recalcula desde la semilla en cada llamada). Deliberadamente no es un
 * DecisionAlgorithm: ninguno de los 7 registrados acepta una fuente de
 * aleatoriedad externa (usan ThreadLocalRandom/SecureRandom internos), así
 * que no hay forma de hacerlos reproducibles sin tocarlos -- este es un
 * mecanismo aparte con su propia selección seedeada.
 */
@Service
@RequiredArgsConstructor
public class DailyDecisionService implements GetDailyDecisionUseCase {

    private final Clock clock;

    @Override
    public DecisionResult getDaily(UserId userId, List<String> optionValues) {
        Objects.requireNonNull(userId, "userId");
        if (optionValues == null || optionValues.size() < 2) {
            throw new Exceptions.InvalidRequestException("At least 2 options are required");
        }

        LocalDate today = LocalDate.now(clock);
        // Se ordena antes de elegir (no solo para la semilla): así el mismo
        // conjunto de opciones da el mismo ganador sin importar en qué orden
        // las mande el cliente. El orden de respuesta sigue siendo el que
        // mandó el cliente; solo la selección interna usa el orden ordenado.
        List<String> sorted = new ArrayList<>(optionValues);
        Collections.sort(sorted);
        long seed = computeSeed(today, userId, sorted);
        String winnerValue = sorted.get(new Random(seed).nextInt(sorted.size()));

        // Ids sintéticos (1..n, OptionId exige positivos): esta decisión no
        // se persiste, así que no hay ids reales de DB -- alcanza con que
        // sean únicos dentro de esta respuesta para que el cliente pueda
        // identificar cuál ganó.
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < optionValues.size(); i++) {
            options.add(new Option(new OptionId((long) (i + 1)), optionValues.get(i)));
        }
        int winnerIndex = optionValues.indexOf(winnerValue);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("algorithm", "algorithm.daily.name");
        details.put("description", "algorithm.daily.description");
        details.put("custom_date", today.toString());
        details.put("custom_optionsCount", String.valueOf(optionValues.size()));
        details.put("steps", List.of(Map.of(
                "descriptionKey", "narrative.daily",
                "args", Arrays.asList(today.toString(), winnerValue)
        )));
        details.put("winnerIndex", winnerIndex);

        return DecisionResult.builder()
                .winningOptionId(options.get(winnerIndex).getId())
                .winningOptionValue(winnerValue)
                .options(options)
                .algorithmType(AlgorithmType.DAILY)
                .algorithmDetails(AlgorithmDetails.of(details))
                .build();
    }

    private long computeSeed(LocalDate date, UserId userId, List<String> sortedOptionValues) {
        String raw = date + "|" + userId.value() + "|" + String.join(",", sortedOptionValues);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            long seed = 0;
            for (int i = 0; i < Long.BYTES; i++) {
                seed = (seed << 8) | (hash[i] & 0xFF);
            }
            return seed;
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 es un algoritmo obligatorio en toda JVM estándar.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
