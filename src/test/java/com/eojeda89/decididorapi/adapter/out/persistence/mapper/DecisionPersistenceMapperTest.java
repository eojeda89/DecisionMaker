package com.eojeda89.decididorapi.adapter.out.persistence.mapper;

import com.eojeda89.decididorapi.adapter.out.persistence.DecisionEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.OptionEntity;
import com.eojeda89.decididorapi.adapter.out.persistence.UserEntity;
import com.eojeda89.decididorapi.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DecisionPersistenceMapperTest {

    private final DecisionPersistenceMapper mapper = new DecisionPersistenceMapper();

    @Test
    void toEntity_HappyPath() {
        User user = new User();
        user.setId(new UserId(1L));
        user.setUsername("test");
        user.setEmail("mail@mail.com");
        user.setPassword("pass");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        Option opt1 = new Option(new OptionId(10L), "A");
        Option opt2 = new Option(new OptionId(11L), "B");
        List<Option> options = List.of(opt1, opt2);

        Decision decision = new Decision();
        decision.setId(new DecisionId(100L));
        decision.setUser(user);
        decision.setAlgorithmType(AlgorithmType.THREAD_RACE);
        decision.setAlgorithmDetails(AlgorithmDetails.of(Map.of("seed", 42, "winnerIndex", 1)));
        decision.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        decision.setWinningOptionId(new OptionId(10L));
        decision.setOptions(options);
        decision.setShareCode("ABCDEFGH");

        DecisionEntity entity = mapper.toEntity(decision);

        assertNotNull(entity);
        assertEquals(100L, entity.getId());
        assertNotNull(entity.getUser());
        assertEquals("test", entity.getUser().getUsername());
        assertEquals(AlgorithmType.THREAD_RACE, entity.getAlgorithmType());
        assertEquals(2, entity.getOptions().size());
        assertEquals(10L, entity.getWinningOptionId());
        assertEquals(Date.valueOf("2024-01-01"), entity.getFechaDecision());
        assertEquals(42, entity.getDetallesAlgoritmo().get("seed"));
        assertEquals("ABCDEFGH", entity.getShareCode());
    }

    @Test
    void toEntity_NullDecision_ReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_NullFields() {
        Decision decision = new Decision();
        DecisionEntity entity = mapper.toEntity(decision);
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getUser());
        assertNull(entity.getAlgorithmType());
        assertNull(entity.getDetallesAlgoritmo());
        assertNull(entity.getFechaDecision());
        assertNotNull(entity.getOptions());
        assertTrue(entity.getOptions().isEmpty());
        assertNull(entity.getWinningOptionId());
    }

    @Test
    void toDomain_HappyPath() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("test");
        userEntity.setEmail("mail@mail.com");
        userEntity.setPassword("pass");
        userEntity.setCreatedAt(new java.util.Date(1704067200000L)); // 2024-01-01T00:00:00Z
        userEntity.setUpdatedAt(new java.util.Date(1704153600000L)); // 2024-01-02T00:00:00Z

        OptionEntity opt1 = new OptionEntity();
        opt1.setId(10L);
        opt1.setValue("A");
        OptionEntity opt2 = new OptionEntity();
        opt2.setId(11L);
        opt2.setValue("B");
        List<OptionEntity> optionEntities = List.of(opt1, opt2);

        DecisionEntity entity = new DecisionEntity();
        entity.setId(100L);
        entity.setUser(userEntity);
        entity.setAlgorithmType(AlgorithmType.THREAD_RACE);
        entity.setDetallesAlgoritmo(Map.of("seed", 42, "winnerIndex", 1));
        entity.setFechaDecision(Date.valueOf("2024-01-01"));
        entity.setOptions(optionEntities);
        entity.setWinningOptionId(10L);
        entity.setShareCode("ABCDEFGH");

        Decision decision = mapper.toDomain(entity);

        assertNotNull(decision);
        assertEquals(100L, decision.getId().value());
        assertNotNull(decision.getUser());
        assertEquals("test", decision.getUser().getUsername());
        assertEquals(AlgorithmType.THREAD_RACE, decision.getAlgorithmType());
        assertEquals(2, decision.getOptions().size());
        assertEquals(10L, decision.getWinningOptionId().value());
        assertEquals(42, decision.getAlgorithmDetails().getProperties().get("seed"));
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), decision.getCreatedAt());
        assertEquals("ABCDEFGH", decision.getShareCode());
    }

    @Test
    void toDomain_NullEntity_ReturnsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toDomain_NullFields() {
        DecisionEntity entity = new DecisionEntity();
        entity.setDetallesAlgoritmo(Map.of("winnerIndex", 1));
        Decision decision = mapper.toDomain(entity);
        assertNotNull(decision);
        assertNull(decision.getId());
        assertNull(decision.getUser());
        assertNull(decision.getAlgorithmType());
        assertNotNull(decision.getAlgorithmDetails());
        assertNull(decision.getCreatedAt());
        assertNotNull(decision.getOptions());
        assertTrue(decision.getOptions().isEmpty());
        assertNull(decision.getWinningOptionId());
    }
}