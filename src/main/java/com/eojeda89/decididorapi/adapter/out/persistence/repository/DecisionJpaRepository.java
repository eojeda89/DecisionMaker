package com.eojeda89.decididorapi.adapter.out.persistence.repository;

import com.eojeda89.decididorapi.adapter.out.persistence.DecisionEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DecisionJpaRepository extends JpaRepository<DecisionEntity, Long> {

    // Solo una decisión, sin paginar: el JOIN FETCH acá es seguro (no hay
    // límite de filas que Hibernate deba aplicar en memoria).
    @Query("SELECT DISTINCT d FROM DecisionEntity d LEFT JOIN FETCH d.options WHERE d.shareCode = :shareCode")
    Optional<DecisionEntity> findByShareCode(@Param("shareCode") String shareCode);

    // Paginar con un JOIN FETCH de una colección to-many no es seguro: Hibernate
    // no puede paginar en la base de datos y termina trayendo TODAS las filas
    // igual, paginando en memoria (warning HHH000104). Por eso son dos queries:
    // 1) pagina solo los ids (liviano, paginable de verdad),
    // 2) trae las entidades completas -con "options"- para esos ids puntuales.
    @Query(value = "SELECT d.id FROM DecisionEntity d WHERE d.user = :user ORDER BY d.id DESC",
            countQuery = "SELECT COUNT(d) FROM DecisionEntity d WHERE d.user = :user")
    Page<Long> findIdsByUser(@Param("user") UserEntity user, Pageable pageable);

    // JOIN FETCH + DISTINCT: trae "options" (lazy por default) en la misma
    // query. Sin esto, con spring.jpa.open-in-view=false la sesión de
    // Hibernate ya está cerrada para cuando el mapper intenta leer la
    // colección lazy -> LazyInitializationException.
    @Query("SELECT DISTINCT d FROM DecisionEntity d LEFT JOIN FETCH d.options WHERE d.id IN :ids")
    List<DecisionEntity> findByIdInWithOptions(@Param("ids") List<Long> ids);
}
