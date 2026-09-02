package com.eojeda89.decididorapi.adapter.out.persistence.repository;

import com.eojeda89.decididorapi.adapter.out.persistence.OptionTemplateEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OptionTemplateJpaRepository extends JpaRepository<OptionTemplateEntity, Long> {

    // JOIN FETCH: optionValues es un @ElementCollection, lazy por default
    // igual que una colección de entidades -- mismo riesgo de
    // LazyInitializationException con open-in-view=false si no se trae en
    // la misma query (ver DecisionJpaRepository). Sin paginar acá: las
    // plantillas por usuario son pocas, no hace falta el patrón de dos
    // queries que sí necesita el historial de decisiones.
    @Query("SELECT DISTINCT t FROM OptionTemplateEntity t LEFT JOIN FETCH t.optionValues WHERE t.user = :user ORDER BY t.id DESC")
    List<OptionTemplateEntity> findByUser(@Param("user") UserEntity user);

    @Query("SELECT t FROM OptionTemplateEntity t LEFT JOIN FETCH t.optionValues WHERE t.id = :id")
    Optional<OptionTemplateEntity> findByIdWithOptions(@Param("id") Long id);
}
