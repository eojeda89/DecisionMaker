package com.eojeda89.decididorapi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Decision {
    private DecisionId id;
    private User user;
    private AlgorithmType algorithmType;
    private AlgorithmDetails algorithmDetails;
    private List<Option> options;
    private OptionId winningOptionId;
    private Instant createdAt;
    private Instant updatedAt;

    public void addOption(Option option) {
        if (option == null) {
            throw new com.eojeda89.decididorapi.common.exception.Exceptions.DomainValidationException("option must not be null");
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
                throw new com.eojeda89.decididorapi.common.exception.Exceptions.ConflictException("An option with the same id already exists: " + option.getId());
            }
        }
        this.options.add(option);
    }

    public boolean removeOptionById(OptionId optionId) {
        if (optionId == null || !optionId.isAssigned()) {
            throw new com.eojeda89.decididorapi.common.exception.Exceptions.DomainValidationException("invalid optionId");
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
            throw new com.eojeda89.decididorapi.common.exception.Exceptions.DomainValidationException("invalid optionId to select winner");
        }
        if (this.options == null || this.options.isEmpty()) {
            throw new com.eojeda89.decididorapi.common.exception.Exceptions.DomainValidationException("no options to select a winner from");
        }
        boolean exists = this.options.stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(o -> o.getId() != null && optionId.equals(o.getId()));
        if (!exists) {
            throw new com.eojeda89.decididorapi.common.exception.Exceptions.DomainValidationException("selected option does not belong to this decision");
        }
        this.winningOptionId = optionId;
    }
}
