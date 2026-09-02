package com.eojeda89.decididorapi.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "option_templates")
public class OptionTemplateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String name;

    // Lista simple de valores, no entidades propias (a diferencia de
    // OptionEntity/Decision: acá no hace falta un id por opción, nadie
    // referencia "la opción 3 de la plantilla X").
    @ElementCollection
    @CollectionTable(name = "option_template_values", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "value")
    @OrderColumn(name = "position")
    private List<String> optionValues;

    private Instant createdAt;
}
