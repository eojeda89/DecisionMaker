package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.adapter.out.persistence.mapper.DecisionPersistenceMapper;
import com.eojeda89.decididorapi.adapter.out.persistence.repository.DecisionJpaRepository;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public List<Decision> findByUser(UserId userId) {
        UserEntity user = new UserEntity();
        user.setId(userId.value());
        return jpaRepository.findByUser(user)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
