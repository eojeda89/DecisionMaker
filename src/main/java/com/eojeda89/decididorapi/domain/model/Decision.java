package com.eojeda89.decididorapi.domain.model;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Decision {
    private DecisionId id;
    private User user;
    private AlgorithmType algorithmType;
    private AlgorithmDetails algorithmDetails;
    private List<Option> options;
    private OptionId winningOptionId;
    private Instant createdAt;
    private Instant updatedAt;
    // Fase 3.3 (salas compartidas): código corto para que cualquiera con el
    // link vea este resultado sin login, vía GET /api/decisions/shared/{code}.
    private String shareCode;

    public void addOption(Option option) {
        if (option == null) {
            throw new Exceptions.DomainValidationException("option must not be null");
        }
        if (this.options == null) {
            this.options = new java.util.ArrayList<>();
        }
        // Invariante: no duplicar por id
        if (option.getId() != null && option.getId().isAssigned()) {
            boolean exists = this.options.stream()
                    .filter(java.util.Objects::nonNull)
                    .anyMatch(o -> o.getId() != null && o.getId().isAssigned() && o.getId().equals(option.getId()));
            if (exists) {
                throw new Exceptions.ConflictException("An option with the same id already exists: " + option.getId());
            }
        }
        this.options.add(option);
    }

    public boolean removeOptionById(OptionId optionId) {
        if (optionId == null || !optionId.isAssigned()) {
            throw new Exceptions.DomainValidationException("invalid optionId");
        }
        if (this.options == null || this.options.isEmpty()) return false;
        boolean removed = this.options.removeIf(o -> o != null && o.getId() != null && optionId.equals(o.getId()));
        if (removed && optionId.equals(this.winningOptionId)) {
            // if we remove the winner, clear the selection
            this.winningOptionId = null;
        }
        return removed;
    }

    public void selectWinner(OptionId optionId) {
        if (optionId == null || !optionId.isAssigned()) {
            throw new Exceptions.DomainValidationException("invalid optionId to select winner");
        }
        if (this.options == null || this.options.isEmpty()) {
            throw new Exceptions.DomainValidationException("no options to select a winner from");
        }
        boolean exists = this.options.stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(o -> o.getId() != null && optionId.equals(o.getId()));
        if (!exists) {
            throw new Exceptions.DomainValidationException("selected option does not belong to this decision");
        }
        this.winningOptionId = optionId;
    }

    public String getWinningOptionValue() {
        if (this.winningOptionId == null || !this.winningOptionId.isAssigned()) {
            return null; // No winner selected
        }
        return this.options.stream()
                .filter(o -> o != null && o.getId() != null && o.getId().equals(this.winningOptionId))
                .map(Option::getValue)
                .findFirst()
                .orElse(null);
    }
}
