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
            throw new IllegalArgumentException("option no puede ser null");
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
                throw new IllegalStateException("Ya existe una opción con id=" + option.getId());
            }
        }
        this.options.add(option);
    }

    public boolean removeOptionById(OptionId optionId) {
        if (optionId == null || !optionId.isAssigned()) {
            throw new IllegalArgumentException("optionId inválido");
        }
        if (this.options == null || this.options.isEmpty()) return false;
        boolean removed = this.options.removeIf(o -> o != null && o.getId() != null && optionId.equals(o.getId()));
        if (removed && optionId.equals(this.winningOptionId)) {
            // Si removemos la ganadora, limpiar la selección
            this.winningOptionId = null;
        }
        return removed;
    }

    public void selectWinner(OptionId optionId) {
        if (optionId == null || !optionId.isAssigned()) {
            throw new IllegalArgumentException("optionId inválido para seleccionar ganador");
        }
        if (this.options == null || this.options.isEmpty()) {
            throw new IllegalStateException("No hay opciones para seleccionar ganador");
        }
        boolean exists = this.options.stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(o -> o.getId() != null && optionId.equals(o.getId()));
        if (!exists) {
            throw new IllegalStateException("La opción seleccionada no pertenece a esta decisión");
        }
        this.winningOptionId = optionId;
    }
}
