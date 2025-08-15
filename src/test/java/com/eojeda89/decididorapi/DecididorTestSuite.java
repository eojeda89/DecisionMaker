package com.eojeda89.decididorapi;

import com.eojeda89.decididorapi.adapter.in.web.DecisionController;
import com.eojeda89.decididorapi.application.port.out.DecisionRepository;
import com.eojeda89.decididorapi.application.service.DecisionService;
import com.eojeda89.decididorapi.configuration.DecisionAlgorithmConfig;
import com.eojeda89.decididorapi.domain.model.*;
import com.eojeda89.decididorapi.domain.service.DecisionAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.DiceRollAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.FortuneWheelAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.RandomWeightedAlgorithm;
import com.eojeda89.decididorapi.domain.service.algorithms.ThreadRaceAlgorithm;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Single test suite file that includes unit, integration, and E2E tests.
 * Using package-private classes allows multiple test classes in one file.
 */
class DecididorTestSuite {

    // ------------------------ UNIT TESTS ------------------------

    @Nested
    class DecisionDomainTests {
        @Test
        void addOption_duplicateId_shouldThrowConflict() {
            Decision d = new Decision();
            Option o1 = new Option(OptionId.of(1L), "A");
            Option o2 = new Option(OptionId.of(1L), "B");
            d.addOption(o1);
            assertThatThrownBy(() -> d.addOption(o2))
                    .isInstanceOf(com.eojeda89.decididorapi.common.exception.Exceptions.ConflictException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        void selectWinner_invalidId_shouldThrowDomainValidation() {
            Decision d = new Decision();
            Option o1 = new Option(OptionId.of(10L), "X");
            d.addOption(o1);
            assertThatThrownBy(() -> d.selectWinner(OptionId.of(99L)))
                    .isInstanceOf(com.eojeda89.decididorapi.common.exception.Exceptions.DomainValidationException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        void selectWinner_success_shouldSetWinnerId() {
            Decision d = new Decision();
            Option o1 = new Option(OptionId.of(10L), "X");
            Option o2 = new Option(OptionId.of(11L), "Y");
            d.addOption(o1);
            d.addOption(o2);
            d.selectWinner(OptionId.of(11L));
            assertThat(d.getWinningOptionId()).isEqualTo(OptionId.of(11L));
        }
    }

    @Nested
    class AlgorithmsTests {
        private List<Option> options(int n) {
            List<Option> list = new ArrayList<>();
            for (int i = 0; i < n; i++) list.add(new Option(null, "opt-" + i));
            return list;
        }

        @Test
        void threadRaceAlgorithm_shouldReturnValidIndex() {
            DecisionAlgorithm algo = new ThreadRaceAlgorithm();
            int idx = algo.chooseWinnerIndex(options(3), AlgorithmDetails.of(Map.of()));
            assertThat(idx).isBetween(0, 2);
        }

        @Test
        void diceRollAlgorithm_shouldReturnValidIndex() {
            DecisionAlgorithm algo = new DiceRollAlgorithm();
            int idx = algo.chooseWinnerIndex(options(5), AlgorithmDetails.of(Map.of()));
            assertThat(idx).isBetween(0, 4);
        }

        @Test
        void fortuneWheelAlgorithm_shouldReturnValidIndex() {
            DecisionAlgorithm algo = new FortuneWheelAlgorithm();
            int idx = algo.chooseWinnerIndex(options(4), AlgorithmDetails.of(Map.of()));
            assertThat(idx).isBetween(0, 3);
        }

        @Test
        void randomWeightedAlgorithm_shouldReturnValidIndex() {
            DecisionAlgorithm algo = new RandomWeightedAlgorithm();
            int idx = algo.chooseWinnerIndex(options(6), AlgorithmDetails.of(Map.of()));
            assertThat(idx).isBetween(0, 5);
        }

        @Test
        void algorithms_withLessThanTwoOptions_shouldThrowInvalidRequest() {
            DecisionAlgorithm algo = new DiceRollAlgorithm();
            assertThatThrownBy(() -> algo.chooseWinnerIndex(options(1), AlgorithmDetails.of(Map.of())))
                    .isInstanceOf(com.eojeda89.decididorapi.common.exception.Exceptions.InvalidRequestException.class);
        }
    }

    @Nested
    class DecisionServiceTests {
        @Test
        void decide_shouldPersistDecision_andReturnWinner() {
            // Arrange repository mock
            DecisionRepository repo = Mockito.mock(DecisionRepository.class);
            // Deterministic algorithm: always choose index 1
            DecisionAlgorithm fixedAlgo = (opts, details) -> 1;
            Map<AlgorithmType, DecisionAlgorithm> map = Map.of(AlgorithmType.DICE_ROLL, fixedAlgo);
            DecisionService service = new DecisionService(repo, map);

            // Prepare save behavior: assign ids to decision and options on first save
            Mockito.when(repo.save(any(Decision.class))).thenAnswer(invocation -> {
                Decision d = invocation.getArgument(0);
                if (d.getId() == null) {
                    d.setId(DecisionId.of(100L));
                    AtomicLong oid = new AtomicLong(1);
                    if (d.getOptions() != null) {
                        d.getOptions().forEach(o -> o.setId(OptionId.of(oid.getAndIncrement())));
                    }
                }
                return d;
            });

            // Build command
            com.eojeda89.decididorapi.application.port.in.command.DecideCommand cmd =
                    new com.eojeda89.decididorapi.application.port.in.command.DecideCommand(
                            UserId.of(77L), AlgorithmType.DICE_ROLL, AlgorithmDetails.of(Map.of()),
                            List.of("A", "B", "C")
                    );

            // Act
            var result = service.decide(cmd);

            // Assert index 1 -> option id should be 2
            assertThat(result.getWinningOptionId()).isEqualTo(OptionId.of(2L));
            assertThat(result.getDecisionId()).isEqualTo(DecisionId.of(100L));
        }
    }

    // ------------------- INTEGRATION TESTS (MockMvc) -------------------

    @SpringBootTest(
            classes = {DecididorApiApplication.class, DecisionAlgorithmConfig.class, DecisionService.class, DecisionController.class},
            properties = {
                    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
            }
    )
    @AutoConfigureMockMvc
    @Import(DecisionAlgorithmConfig.class)
    static class DecisionControllerIntegrationTests {
        @Autowired
        MockMvc mockMvc;

        @Autowired
        DecisionService service;

        @org.springframework.boot.test.mock.mockito.MockBean
        DecisionRepository repo; // mock persistence to avoid DB

        @Test
        void post_decisions_shouldValidateAndReturn200() throws Exception {
            // mock persistence as in service test
            Mockito.when(repo.save(any(Decision.class))).thenAnswer(invocation -> {
                Decision d = invocation.getArgument(0);
                if (d.getId() == null) {
                    d.setId(DecisionId.of(200L));
                    AtomicLong oid = new AtomicLong(10);
                    if (d.getOptions() != null) {
                        d.getOptions().forEach(o -> o.setId(OptionId.of(oid.getAndIncrement())));
                    }
                }
                return d;
            });

            String json = "{" +
                    "\"userId\":77," +
                    "\"algorithmType\":\"dice-roll\"," +
                    "\"algorithmDetails\":{}," +
                    "\"options\":[\"A\",\"B\",\"C\"]" +
                    "}";

            mockMvc.perform(post("/api/decisions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.decisionId").value(200))
                    .andExpect(jsonPath("$.winningOptionId").exists())
                    .andExpect(jsonPath("$.options").isArray());
        }

        @Test
        void post_decisions_withLessThanTwoOptions_shouldReturn400() throws Exception {
            String json = "{" +
                    "\"userId\":77," +
                    "\"algorithmType\":\"dice-roll\"," +
                    "\"options\":[\"A\"]" +
                    "}";

            mockMvc.perform(post("/api/decisions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        void get_decisions_shouldReturnList200() throws Exception {
            // mock repo for history
            Mockito.when(repo.findByUser(UserId.of(77L))).thenReturn(List.of(
                    new Decision(DecisionId.of(301L), new User(UserId.of(77L), "u","e","p", Instant.now(), Instant.now()),
                            AlgorithmType.DICE_ROLL, AlgorithmDetails.of(Map.of()),
                            List.of(new Option(OptionId.of(1L),"A"), new Option(OptionId.of(2L),"B")),
                            OptionId.of(2L), Instant.now(), Instant.now())
            ));

            mockMvc.perform(get("/api/decisions").param("userId", "77"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].decisionId").value(301))
                    .andExpect(jsonPath("$[0].winningOptionId").value(2));
        }
    }

    // ------------------- E2E TESTS (RANDOM_PORT) -------------------

    @SpringBootTest(
            classes = {DecididorApiApplication.class, DecisionAlgorithmConfig.class, DecisionService.class, DecisionController.class},
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {
                    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
            }
    )
    static class DecisionE2ETests {
        @LocalServerPort
        int port;

        @Autowired
        TestRestTemplate restTemplate;

        @org.springframework.boot.test.mock.mockito.MockBean
        DecisionRepository repo;

        @Test
        void e2e_makeDecision_shouldReturn200() {
            Mockito.when(repo.save(any(Decision.class))).thenAnswer(invocation -> {
                Decision d = invocation.getArgument(0);
                if (d.getId() == null) {
                    d.setId(DecisionId.of(500L));
                    AtomicLong oid = new AtomicLong(100);
                    if (d.getOptions() != null) {
                        d.getOptions().forEach(o -> o.setId(OptionId.of(oid.getAndIncrement())));
                    }
                }
                return d;
            });

            String url = "http://localhost:" + port + "/api/decisions";
            String json = "{" +
                    "\"userId\":12," +
                    "\"algorithmType\":\"FORTUNE_WHEEL\"," +
                    "\"options\":[\"apples\",\"oranges\",\"bananas\"]" +
                    "}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).contains("decisionId");
        }
    }
}
