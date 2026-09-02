package com.eojeda89.decididorapi.adapter.out.persistence.mapper;

import com.eojeda89.decididorapi.adapter.out.persistence.OptionTemplateEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import com.eojeda89.decididorapi.domain.model.TemplateId;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptionTemplatePersistenceMapperTest {

    private final OptionTemplatePersistenceMapper mapper = new OptionTemplatePersistenceMapper();

    @Test
    void toEntity_HappyPath() {
        OptionTemplate template = OptionTemplate.builder()
                .id(TemplateId.of(10L))
                .userId(UserId.of(1L))
                .name("Restaurantes")
                .optionValues(List.of("A", "B"))
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        OptionTemplateEntity entity = mapper.toEntity(template);

        assertEquals(10L, entity.getId());
        assertEquals(1L, entity.getUser().getId());
        assertEquals("Restaurantes", entity.getName());
        assertEquals(List.of("A", "B"), entity.getOptionValues());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), entity.getCreatedAt());
    }

    @Test
    void toEntity_NullTemplate_ReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toDomain_HappyPath() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        OptionTemplateEntity entity = new OptionTemplateEntity();
        entity.setId(10L);
        entity.setUser(userEntity);
        entity.setName("Restaurantes");
        entity.setOptionValues(List.of("A", "B"));
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        OptionTemplate template = mapper.toDomain(entity);

        assertEquals(TemplateId.of(10L), template.getId());
        assertEquals(UserId.of(1L), template.getUserId());
        assertEquals("Restaurantes", template.getName());
        assertEquals(List.of("A", "B"), template.getOptionValues());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), template.getCreatedAt());
    }

    @Test
    void toDomain_NullEntity_ReturnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_NullUser_ReturnsNullUserId() {
        OptionTemplateEntity entity = new OptionTemplateEntity();
        entity.setOptionValues(List.of("A", "B"));

        OptionTemplate template = mapper.toDomain(entity);

        assertNull(template.getUserId());
    }
}
