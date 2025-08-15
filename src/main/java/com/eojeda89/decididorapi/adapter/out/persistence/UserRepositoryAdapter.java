package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.eojeda89.decididorapi.adapter.out.persistence.repository.UserJpaRepository;
import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;
    private final UserPersistenceMapper mapper;

    @Override
    public boolean existsByUsername(String username) { return jpa.existsByUsername(username); }

    @Override
    public boolean existsByEmail(String email) { return jpa.existsByEmail(email); }

    @Override
    public Optional<User> findByUsername(String username) { return jpa.findByUsername(username).map(mapper::toDomain); }

    @Override
    public Optional<User> findByEmail(String email) { return jpa.findByEmail(email).map(mapper::toDomain); }

    @Override
    public User save(User user) { return mapper.toDomain(jpa.save(mapper.toEntity(user))); }
}
