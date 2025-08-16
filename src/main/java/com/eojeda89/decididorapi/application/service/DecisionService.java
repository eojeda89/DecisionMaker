package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.*;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DecisionService implements MakeDecisionUseCase, GetDecisionHistoryUseCase {

    private final DecisionRepository decisionRepository;
    private final Map<AlgorithmType, DecisionAlgorithm> algorithms;

    @Override
    public DecisionResult decide(DecideCommand command) {
        Objects.requireNonNull(command, "command");
        if (command.getOptionValues() == null || command.getOptionValues().size() < 2) {
            throw new Exceptions.InvalidRequestException("At least 2 options are required");
        }
        if (command.getAlgorithmType() == null) {
            throw new Exceptions.InvalidRequestException("algorithmType is required");
        }
        DecisionAlgorithm algorithm = algorithms.get(command.getAlgorithmType());
        if (algorithm == null) {
            throw new Exceptions.UnsupportedAlgorithmException("Unsupported algorithm: " + command.getAlgorithmType());
        }

        // Construir opciones de dominio (ids aún no asignadas)
        List<Option> options = command.getOptionValues().stream()
                .map(v -> new Option(null, v))
                .toList();

        // Construir usuario (por ahora solo id)
        User user = new User();
        user.setId(command.getUserId());

        // Crear agregado Decision sin ganador todavía
        Decision decision = new Decision(null, user, command.getAlgorithmType(),
                command.getAlgorithmDetails(), options, null, Instant.now(), null);

        // Elegir ganador por índice (sin depender de ids aún)
        int winnerIndex = algorithm.chooseWinnerIndex(options, command.getAlgorithmDetails());
        if (winnerIndex < 0 || winnerIndex >= options.size()) {
            throw new Exceptions.DomainValidationException("Algorithm produced an out-of-range index");
        }

        // Persistir para obtener ids
        Decision persisted = decisionRepository.save(decision);
        OptionId winnerId = persisted.getOptions().get(winnerIndex).getId();
        persisted.selectWinner(winnerId);

        // Guardar el ganador
        Decision finalized = decisionRepository.save(persisted);

        return DecisionResult.builder()
                .decisionId(finalized.getId())
                .winningOptionId(finalized.getWinningOptionId())
                .winningOptionValue(finalized.getWinningOptionValue())
                .options(finalized.getOptions())
                .algorithmType(finalized.getAlgorithmType())
                .algorithmDetails(finalized.getAlgorithmDetails())
                .createdAt(finalized.getCreatedAt())
                .build();
    }

    @Override
    public List<Decision> listByUser(UserId userId) {
        Objects.requireNonNull(userId, "userId");
        return decisionRepository.findByUser(userId);
    }
}
