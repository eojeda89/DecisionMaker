package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "decisions")
public class DecisionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private AlgorithmType algorithmType;
    private Date fechaDecision;

    // Con esta anotación, JPA sabe que debe guardar el objeto como JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> detallesAlgoritmo;

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionEntity> options;

    @Column(name = "winning_option_id")
    private Long winningOptionId;

    // Fase 3.3: código corto para ver el resultado sin login (GET
    // /api/decisions/shared/{code}). Único; nullable porque las filas ya
    // existentes antes de esta columna no tienen uno asignado.
    @Column(name = "share_code", unique = true)
    private String shareCode;
}