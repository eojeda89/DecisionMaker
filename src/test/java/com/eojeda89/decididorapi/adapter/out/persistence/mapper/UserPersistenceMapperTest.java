package com.eojeda89.decididorapi.adapter.out.persistence.mapper;

import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.eojeda89.decididorapi.domain.model.User;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserPersistenceMapperTest {

    private final UserPersistenceMapper mapper = new UserPersistenceMapper();

    @Test
    void toEntity_HappyPath() {
        User user = new User();
        user.setId(new UserId(1L));
        user.setUsername("testuser");
        user.setEmail("test@mail.com");
        user.setPassword("secret");
        user.setCreatedAt(Instant.parse("2024-01-01T10:00:00Z"));
        user.setUpdatedAt(Instant.parse("2024-01-02T12:00:00Z"));

        UserEntity entity = mapper.toEntity(user);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@mail.com", entity.getEmail());
        assertEquals("secret", entity.getPassword());
        assertEquals(Date.from(Instant.parse("2024-01-01T10:00:00Z")), entity.getCreatedAt());
        assertEquals(Date.from(Instant.parse("2024-01-02T12:00:00Z")), entity.getUpdatedAt());
    }

    @Test
    void toEntity_NullUser_ReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_NullFields() {
        User user = new User();
        UserEntity entity = mapper.toEntity(user);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getUsername());
        assertNull(entity.getEmail());
        assertNull(entity.getPassword());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void toDomain_HappyPath() {
        UserEntity entity = new UserEntity();
        entity.setId(2L);
        entity.setUsername("otheruser");
        entity.setEmail("other@mail.com");
        entity.setPassword("pass");
        entity.setCreatedAt(Date.from(Instant.parse("2024-02-01T08:00:00Z")));
        entity.setUpdatedAt(Date.from(Instant.parse("2024-02-02T09:00:00Z")));

        User user = mapper.toDomain(entity);

        assertNotNull(user);
        assertEquals(2L, user.getId().value());
        assertEquals("otheruser", user.getUsername());
        assertEquals("other@mail.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals(Instant.parse("2024-02-01T08:00:00Z"), user.getCreatedAt());
        assertEquals(Instant.parse("2024-02-02T09:00:00Z"), user.getUpdatedAt());
    }

    @Test
    void toDomain_NullEntity_ReturnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_NullFields() {
        UserEntity entity = new UserEntity();
        User user = mapper.toDomain(entity);

        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }
}