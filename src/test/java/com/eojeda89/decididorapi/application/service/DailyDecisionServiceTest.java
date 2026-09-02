package com.eojeda89.decididorapi.application.service;

import com.eojeda89.decididorapi.application.port.in.result.DecisionResult;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DailyDecisionServiceTest {

    private static Clock fixedClock(String isoInstant) {
        return Clock.fixed(Instant.parse(isoInstant), ZoneOffset.UTC);
    }

    @Test
    void getDaily_SameDayUserAndOptions_AlwaysReturnsSameWinner() {
        DailyDecisionService service = new DailyDecisionService(fixedClock("2026-09-02T10:00:00Z"));
        List<String> options = List.of("Pizza", "Sushi", "Tacos");

        DecisionResult first = service.getDaily(UserId.of(1L), options);
        DecisionResult second = service.getDaily(UserId.of(1L), options);
        DecisionResult third = service.getDaily(UserId.of(1L), options);

        assertEquals(first.getWinningOptionValue(), second.getWinningOptionValue());
        assertEquals(first.getWinningOptionValue(), third.getWinningOptionValue());
    }

    @Test
    void getDaily_OptionOrderDoesNotMatter() {
        DailyDecisionService service = new DailyDecisionService(fixedClock("2026-09-02T10:00:00Z"));

        DecisionResult inOneOrder = service.getDaily(UserId.of(1L), List.of("Pizza", "Sushi", "Tacos"));
        DecisionResult inAnotherOrder = service.getDaily(UserId.of(1L), List.of("Tacos", "Pizza", "Sushi"));

        assertEquals(inOneOrder.getWinningOptionValue(), inAnotherOrder.getWinningOptionValue());
    }

    @Test
    void getDaily_DifferentUser_CanGiveADifferentWinner_OverManySeeds() {
        // No hay garantía de que UN par puntual de usuarios difiera (podrían
        // coincidir por azar), así que se prueba con muchos usuarios
        // distintos y se verifica que no todos caen en el mismo resultado.
        DailyDecisionService service = new DailyDecisionService(fixedClock("2026-09-02T10:00:00Z"));
        List<String> options = List.of("Pizza", "Sushi", "Tacos");

        Set<String> winners = new HashSet<>();
        for (long userId = 1; userId <= 30; userId++) {
            winners.add(service.getDaily(UserId.of(userId), options).getWinningOptionValue());
        }

        assertTrue(winners.size() > 1, "30 usuarios distintos no deberían converger todos al mismo ganador");
    }

    @Test
    void getDaily_DifferentDay_CanGiveADifferentWinner_OverManyDays() {
        List<String> options = List.of("Pizza", "Sushi", "Tacos");
        Set<String> winners = new HashSet<>();
        for (int day = 1; day <= 30; day++) {
            DailyDecisionService service = new DailyDecisionService(
                    fixedClock(String.format("2026-01-%02dT10:00:00Z", day)));
            winners.add(service.getDaily(UserId.of(1L), options).getWinningOptionValue());
        }

        assertTrue(winners.size() > 1, "30 días distintos no deberían converger todos al mismo ganador");
    }

    @Test
    void getDaily_ResponseIsNotPersisted_HasNoDecisionId() {
        DailyDecisionService service = new DailyDecisionService(fixedClock("2026-09-02T10:00:00Z"));

        DecisionResult result = service.getDaily(UserId.of(1L), List.of("A", "B"));

        assertNull(result.getDecisionId(), "no se persiste -> no hay id de decisión real");
    }

    @Test
    void getDaily_WinningOptionId_MatchesWinningValueInOptionsList() {
        DailyDecisionService service = new DailyDecisionService(fixedClock("2026-09-02T10:00:00Z"));

        DecisionResult result = service.getDaily(UserId.of(1L), List.of("A", "B", "C"));

        var matching = result.getOptions().stream()
                .filter(o -> o.getId().equals(result.getWinningOptionId()))
                .findFirst()
                .orElseThrow();
        assertEquals(result.getWinningOptionValue(), matching.getValue());
    }

    @Test
    void getDaily_FewerThanTwoOptions_ThrowsException() {
        DailyDecisionService service = new DailyDecisionService(fixedClock("2026-09-02T10:00:00Z"));

        assertThrows(Exceptions.InvalidRequestException.class,
                () -> service.getDaily(UserId.of(1L), List.of("SoloUno")));
    }

    @Test
    void getDaily_NullUserId_ThrowsException() {
        DailyDecisionService service = new DailyDecisionService(fixedClock("2026-09-02T10:00:00Z"));

        assertThrows(NullPointerException.class, () -> service.getDaily(null, List.of("A", "B")));
    }
}
