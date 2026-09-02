package com.eojeda89.decididorapi.domain.service.algorithms;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class FortuneWheelAlgorithm implements DecisionAlgorithm {
    @Override
    public AlgorithmDetails chooseWinnerIndex(List<Option> options) {
        Objects.requireNonNull(options, "options");
        if (options.size() < 2) throw new Exceptions.InvalidRequestException("At least 2 options are required");
        int n = options.size();
        double angle = ThreadLocalRandom.current().nextDouble(360.0);
        double segment = 360.0 / n;
        int index = (int) Math.floor(angle / segment);
        if (index >= n) index = n - 1;
        double startDegree = index * segment;
        double endDegree = startDegree + segment;

        Map<String, Object> details = new java.util.LinkedHashMap<>();
        details.put("algorithm", "algorithm.fortune-wheel.name");
        details.put("description", "algorithm.fortune-wheel.description");
        details.put("custom_optionsCount", String.valueOf(options.size()));
        details.put("custom_winningAngle", String.format("%.2f degrees", angle));
        details.put("custom_segmentSize", String.format("%.2f degrees", segment));
        details.put("custom_winningSegmentSize", String.format("%.2f° - %.2f°", startDegree, endDegree));
        details.put("steps", NarrativeSteps.singleStep("narrative.fortune-wheel", String.format("%.0f", angle), options.get(index).getValue()));
        // Sin prefijo "custom_": no se muestra en la lista genérica de detalles
        // (Fase 4.1), la consume directamente AppController para animar la
        // ruleta hasta el ángulo real devuelto acá.
        details.put("winningAngleDegrees", angle);
        details.put("winnerIndex", index);
        return AlgorithmDetails.of(details);
    }
}
