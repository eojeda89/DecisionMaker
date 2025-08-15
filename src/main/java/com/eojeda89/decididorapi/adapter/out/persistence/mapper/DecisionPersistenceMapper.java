package com.eojeda89.decididorapi.adapter.out.persistence.mapper;

import com.eojeda89.decididorapi.adapter.out.persistence.DecisionEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.OptionEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import com.eojeda89.decididorapi.domain.model.*;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DecisionPersistenceMapper {

    public DecisionEntity toEntity(Decision decision) {
        if (decision == null) return null;
        DecisionEntity e = new DecisionEntity();
        e.setId(toLong(decision.getId()));
        e.setUser(toEntity(decision.getUser()));
        e.setAlgorithmType(decision.getAlgorithmType());
        e.setDetallesAlgoritmo(toMap(decision.getAlgorithmDetails()));
        if (decision.getCreatedAt() != null) {
            e.setFechaDecision(Date.valueOf(decision.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate()));
        }
        List<OptionEntity> optionEntities = toEntityOptions(decision.getOptions(), e);
        e.setOptions(optionEntities);
        e.setWinningOptionId(toLong(decision.getWinningOptionId()));
        return e;
    }

    public Decision toDomain(DecisionEntity entity) {
        if (entity == null) return null;
        Decision d = new Decision();
        d.setId(toDecisionId(entity.getId()));
        d.setUser(toDomain(entity.getUser()));
        d.setAlgorithmType(entity.getAlgorithmType());
        d.setAlgorithmDetails(AlgorithmDetails.of(entity.getDetallesAlgoritmo()));
        if (entity.getFechaDecision() != null) {
            d.setCreatedAt(entity.getFechaDecision().toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        }
        d.setWinningOptionId(toOptionId(entity.getWinningOptionId()));
        d.setOptions(toDomainOptions(entity.getOptions()));
        return d;
    }

    private UserEntity toEntity(User user) {
        if (user == null) return null;
        UserEntity u = new UserEntity();
        u.setId(toLong(user.getId()));
        u.setUsername(user.getUsername());
        u.setEmail(user.getEmail());
        u.setPassword(user.getPassword());
        if (user.getCreatedAt() != null) {
            u.setCreatedAt(java.util.Date.from(user.getCreatedAt()));
        }
        if (user.getUpdatedAt() != null) {
            u.setUpdatedAt(java.util.Date.from(user.getUpdatedAt()));
        }
        return u;
    }

    private User toDomain(UserEntity entity) {
        if (entity == null) return null;
        User u = new User();
        u.setId(toUserId(entity.getId()));
        u.setUsername(entity.getUsername());
        u.setEmail(entity.getEmail());
        u.setPassword(entity.getPassword());
        if (entity.getCreatedAt() != null) {
            u.setCreatedAt(entity.getCreatedAt().toInstant());
        }
        if (entity.getUpdatedAt() != null) {
            u.setUpdatedAt(entity.getUpdatedAt().toInstant());
        }
        return u;
    }

    private List<OptionEntity> toEntityOptions(List<Option> options, DecisionEntity parent) {
        if (options == null) return null;
        List<OptionEntity> list = new ArrayList<>(options.size());
        for (Option o : options) {
            if (o == null) continue;
            OptionEntity e = new OptionEntity();
            e.setId(toLong(o.getId()));
            e.setValue(o.getValue());
            e.setDecision(parent);
            list.add(e);
        }
        return list;
    }

    private List<Option> toDomainOptions(List<OptionEntity> entities) {
        if (entities == null) return null;
        List<Option> list = new ArrayList<>(entities.size());
        for (OptionEntity e : entities) {
            if (e == null) continue;
            Option o = new Option();
            o.setId(toOptionId(e.getId()));
            o.setValue(e.getValue());
            list.add(o);
        }
        return list;
    }

    private Map<String, Object> toMap(AlgorithmDetails details) {
        return details == null ? null : details.getProperties();
    }

    private Long toLong(UserId id) { return id == null ? null : id.value(); }
    private Long toLong(DecisionId id) { return id == null ? null : id.value(); }
    private Long toLong(OptionId id) { return id == null ? null : id.value(); }

    private UserId toUserId(Long id) { return id == null ? null : new UserId(id); }
    private DecisionId toDecisionId(Long id) { return id == null ? null : new DecisionId(id); }
    private OptionId toOptionId(Long id) { return id == null ? null : new OptionId(id); }
}
