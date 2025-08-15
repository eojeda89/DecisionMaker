package com.eojeda89.decididorapi.adapter.out.persistence.repository;

import com.eojeda89.decididorapi.adapter.out.persistence.DecisionEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DecisionJpaRepository extends JpaRepository<DecisionEntity, Long> {
    List<DecisionEntity> findByUser(UserEntity user);
}
