package com.eojeda89.decididorapi.adapter.out.persistence;

import com.eojeda89.decididorapi.adapter.out.persistence.mapper.OptionTemplatePersistenceMapper;
import com.eojeda89.decididorapi.adapter.out.persistence.repository.OptionTemplateJpaRepository;
import com.eojeda89.decididorapi.application.port.out.TemplateRepository;
import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TemplateRepositoryAdapter implements TemplateRepository {

    private final OptionTemplateJpaRepository jpaRepository;
    private final OptionTemplatePersistenceMapper mapper;

    @Override
    public OptionTemplate save(OptionTemplate template) {
        if (template == null) {
            throw new NullPointerException("Template cannot be null");
        }
        OptionTemplateEntity entity = mapper.toEntity(template);
        OptionTemplateEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<OptionTemplate> findByUser(UserId userId) {
        UserEntity user = new UserEntity();
        user.setId(userId.value());
        return jpaRepository.findByUser(user).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<OptionTemplate> findById(TemplateId id) {
        return jpaRepository.findByIdWithOptions(id.value()).map(mapper::toDomain);
    }

    @Override
    public void deleteById(TemplateId id) {
        jpaRepository.deleteById(id.value());
    }
}
