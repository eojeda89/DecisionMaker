package com.eojeda89.decididorapi.adapter.out.persistence.mapper;

import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import com.eojeda89.decididorapi.domain.model.User;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {
    public UserEntity toEntity(User user) {
        if (user == null) return null;
        UserEntity e = new UserEntity();
        e.setId(user.getId() != null ? user.getId().value() : null);
        e.setUsername(user.getUsername());
        e.setEmail(user.getEmail());
        e.setPassword(user.getPassword());
        if (user.getCreatedAt() != null) e.setCreatedAt(java.util.Date.from(user.getCreatedAt()));
        if (user.getUpdatedAt() != null) e.setUpdatedAt(java.util.Date.from(user.getUpdatedAt()));
        return e;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        User u = new User();
        u.setId(entity.getId() != null ? new UserId(entity.getId()) : null);
        u.setUsername(entity.getUsername());
        u.setEmail(entity.getEmail());
        u.setPassword(entity.getPassword());
        if (entity.getCreatedAt() != null) u.setCreatedAt(entity.getCreatedAt().toInstant());
        if (entity.getUpdatedAt() != null) u.setUpdatedAt(entity.getUpdatedAt().toInstant());
        return u;
    }
}
