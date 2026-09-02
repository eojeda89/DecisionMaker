package com.eojeda89.decididorapi.adapter.out.persistence.repository;

import com.eojeda89.decididorapi.adapter.out.persistence.DecisionEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DecisionJpaRepository extends JpaRepository<DecisionEntity, Long> {

    // JOIN FETCH + DISTINCT: trae "options" (lazy por default) en la misma
    // query. Sin esto, con spring.jpa.open-in-view=false la sesión de
    // Hibernate ya está cerrada para cuando el mapper intenta leer la
    // colección lazy -> LazyInitializationException.
    @Query("SELECT DISTINCT d FROM DecisionEntity d LEFT JOIN FETCH d.options WHERE d.user = :user")
    List<DecisionEntity> findByUser(@Param("user") UserEntity user);
}
