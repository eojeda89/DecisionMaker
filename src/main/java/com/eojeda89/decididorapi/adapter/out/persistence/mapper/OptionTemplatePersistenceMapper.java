package com.eojeda89.decididorapi.adapter.out.persistence.mapper;

import com.eojeda89.decididorapi.adapter.out.persistence.OptionTemplateEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OptionTemplatePersistenceMapper {

    public OptionTemplateEntity toEntity(OptionTemplate template) {
        if (template == null) return null;
        OptionTemplateEntity e = new OptionTemplateEntity();
        e.setId(toLong(template.getId()));
        UserEntity userEntity = new UserEntity();
        userEntity.setId(toLong(template.getUserId()));
        e.setUser(userEntity);
        e.setName(template.getName());
        e.setOptionValues(template.getOptionValues() == null ? null : new ArrayList<>(template.getOptionValues()));
        e.setCreatedAt(template.getCreatedAt());
        return e;
    }

    public OptionTemplate toDomain(OptionTemplateEntity entity) {
        if (entity == null) return null;
        return OptionTemplate.builder()
                .id(toTemplateId(entity.getId()))
                .userId(entity.getUser() != null ? toUserId(entity.getUser().getId()) : null)
                .name(entity.getName())
                .optionValues(entity.getOptionValues() == null ? null : new ArrayList<>(entity.getOptionValues()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private Long toLong(TemplateId id) { return id == null ? null : id.value(); }
    private Long toLong(UserId id) { return id == null ? null : id.value(); }
    private TemplateId toTemplateId(Long id) { return id == null ? null : new TemplateId(id); }
    private UserId toUserId(Long id) { return id == null ? null : new UserId(id); }
}
