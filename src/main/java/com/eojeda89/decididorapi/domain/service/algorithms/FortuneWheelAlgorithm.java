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

        Map<String, Object> details = Map.of(
                "algorithm", "Fortune Wheel",
                "description", "Randomly selects a winner by spinning a fortune wheel",
                "optionsCount", String.valueOf(options.size()),
                "winningAngle", String.format("%.2f degrees", angle),
                "segmentSize", String.format("%.2f degrees", segment),
                "winningSegmentSize", String.format("%.2f° - %.2f°", startDegree, endDegree),
                "winnerIndex", index
        );
        return AlgorithmDetails.of(details);
    }
}
