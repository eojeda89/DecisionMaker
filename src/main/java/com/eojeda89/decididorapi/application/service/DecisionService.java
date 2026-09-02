package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.GetDecisionHistoryUseCase;
import com.eojeda89.decididorapi.application.port.in.GetSharedDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.MakeDecisionUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.*;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DecisionService implements MakeDecisionUseCase, GetDecisionHistoryUseCase, GetSharedDecisionUseCase {

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

        // Crear agregado Decision sin ganador todavía y sin detalles de algoritmo
        Decision decision = Decision.builder()
                .user(user)
                .algorithmType(command.getAlgorithmType())
                .options(options)
                .createdAt(Instant.now())
                .shareCode(ShareCodeGenerator.generate())
                .build();

        // Elegir ganador por índice (sin depender de ids aún)
        AlgorithmDetails algorithmDetails = algorithm.chooseWinnerIndex(options);
        decision.setAlgorithmDetails(algorithmDetails);
        if (algorithmDetails == null) {
            throw new Exceptions.InvalidRequestException("Algorithm did not return valid details");
        }
        int winnerIndex = algorithmDetails.get("winnerIndex", Integer.class);
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
                .shareCode(finalized.getShareCode())
                .build();
    }

    @Override
    public Page<Decision> listByUser(UserId userId, Pageable pageable) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(pageable, "pageable");
        return decisionRepository.findByUser(userId, pageable);
    }

    @Override
    public Decision getByShareCode(String shareCode) {
        Objects.requireNonNull(shareCode, "shareCode");
        return decisionRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Shared decision not found"));
    }
}
