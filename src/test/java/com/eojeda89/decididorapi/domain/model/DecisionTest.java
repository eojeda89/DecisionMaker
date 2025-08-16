package com.eojeda89.decididorapi.domain.model;

import com.eojeda89.decididorapi.common.exception.Exceptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DecisionTest {

    @Test
    void addOption_HappyPath_AddsOption() {
        Decision decision = Decision.builder().options(null).build();
        Option option = mock(Option.class);
        when(option.getId()).thenReturn(null);

        decision.addOption(option);

        assertNotNull(decision.getOptions());
        assertEquals(1, decision.getOptions().size());
        assertSame(option, decision.getOptions().getFirst());
    }

    @Test
    void addOption_NullOption_ThrowsException() {
        Decision decision = new Decision();
        assertThrows(Exceptions.DomainValidationException.class, () -> decision.addOption(null));
    }

    @Test
    void addOption_DuplicateId_ThrowsConflict() {
        OptionId id = new OptionId(1L);
        Option option1 = mock(Option.class);
        Option option2 = mock(Option.class);
        when(option1.getId()).thenReturn(id);
        when(option2.getId()).thenReturn(id);

        Decision decision = Decision.builder().options(List.of(option1)).build();

        assertThrows(Exceptions.ConflictException.class, () -> decision.addOption(option2));
    }

    @Test
    void addOption_OptionWithUnassignedId_AllowsDuplicate() {
        OptionId id = mock(OptionId.class);
        Option option1 = mock(Option.class);
        Option option2 = mock(Option.class);
        when(option2.getId()).thenReturn(id);
        List<Option> options = new ArrayList<>();
        options.add(option1);

        Decision decision = Decision.builder().options(options).build();

        assertDoesNotThrow(() -> decision.addOption(option2));
        assertEquals(2, decision.getOptions().size());
    }

    @Test
    void removeOptionById_HappyPath_RemovesAndClearsWinner() {
        OptionId id = new OptionId(2L);
        Option option = mock(Option.class);
        when(option.getId()).thenReturn(id);

        List<Option> options = new ArrayList<>();
        options.add(option);

        Decision decision = Decision.builder()
                .options(options)
                .winningOptionId(id)
                .build();

        boolean removed = decision.removeOptionById(id);

        assertTrue(removed);
        assertTrue(decision.getOptions().isEmpty());
        assertNull(decision.getWinningOptionId());
    }

    @Test
    void removeOptionById_InvalidId_ThrowsException() {
        Decision decision = new Decision();
        OptionId id = mock(OptionId.class);

        assertThrows(Exceptions.DomainValidationException.class, () -> decision.removeOptionById(null));
        assertThrows(Exceptions.DomainValidationException.class, () -> decision.removeOptionById(id));
    }

    @Test
    void removeOptionById_OptionNotFound_ReturnsFalse() {
        OptionId id = new OptionId(1L);
        Decision decision = Decision.builder().options(List.of()).build();

        assertFalse(decision.removeOptionById(id));
    }

    @Test
    void selectWinner_HappyPath_SelectsWinner() {
        OptionId id = new OptionId(5L);
        Option option = mock(Option.class);
        when(option.getId()).thenReturn(id);

        Decision decision = Decision.builder().options(List.of(option)).build();

        decision.selectWinner(id);

        assertEquals(id, decision.getWinningOptionId());
    }

    @Test
    void selectWinner_InvalidId_ThrowsException() {
        Decision decision = new Decision();
        OptionId id = mock(OptionId.class);

        assertThrows(Exceptions.DomainValidationException.class, () -> decision.selectWinner(null));
        assertThrows(Exceptions.DomainValidationException.class, () -> decision.selectWinner(id));
    }

    @Test
    void selectWinner_NoOptions_ThrowsException() {
        OptionId id = new OptionId(1L);
        Decision decision = Decision.builder().options(List.of()).build();

        assertThrows(Exceptions.DomainValidationException.class, () -> decision.selectWinner(id));
    }

    @Test
    void selectWinner_OptionNotInList_ThrowsException() {
        OptionId id = new OptionId(1L);
        Option option = mock(Option.class);
        when(option.getId()).thenReturn(new OptionId(2L));
        Decision decision = Decision.builder().options(List.of(option)).build();

        assertThrows(Exceptions.DomainValidationException.class, () -> decision.selectWinner(id));
    }
}