package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.adapter.out.persistence.mapper.DecisionPersistenceMapper;
import com.eojeda89.decididorapi.adapter.out.persistence.repository.DecisionJpaRepository;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DecisionRepositoryAdapter implements DecisionRepository {

    private final DecisionJpaRepository jpaRepository;
    private final DecisionPersistenceMapper mapper;

    @Override
    public Decision save(Decision decision) {
        if (decision == null) {
            throw new NullPointerException("Decision cannot be null");
        }
        DecisionEntity entity = mapper.toEntity(decision);
        DecisionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Page<Decision> findByUser(UserId userId, Pageable pageable) {
        UserEntity user = new UserEntity();
        user.setId(userId.value());

        Page<Long> idsPage = jpaRepository.findIdsByUser(user, pageable);
        List<Long> ids = idsPage.getContent();
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, idsPage.getTotalElements());
        }

        // El JOIN FETCH no garantiza el orden de la página -> se reordena
        // según el orden ya resuelto por findIdsByUser.
        Map<Long, DecisionEntity> byId = new LinkedHashMap<>();
        for (DecisionEntity entity : jpaRepository.findByIdInWithOptions(ids)) {
            byId.put(entity.getId(), entity);
        }
        List<Decision> content = ids.stream()
                .map(byId::get)
                .map(mapper::toDomain)
                .toList();

        return new PageImpl<>(content, pageable, idsPage.getTotalElements());
    }

    @Override
    public Optional<Decision> findByShareCode(String shareCode) {
        return jpaRepository.findByShareCode(shareCode).map(mapper::toDomain);
    }

    @Override
    public List<Decision> findAllByUser(UserId userId) {
        UserEntity user = new UserEntity();
        user.setId(userId.value());
        return jpaRepository.findAllByUserWithOptions(user).stream().map(mapper::toDomain).toList();
    }
}
