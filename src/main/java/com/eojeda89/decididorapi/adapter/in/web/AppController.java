package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.application.port.in.GetUserStatsUseCase;
import com.eojeda89.decididorapi.application.port.in.RegisterUserUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.command.RegisterUserCommand;
import com.eojeda89.decididorapi.application.port.in.result.UserStatsResult;
import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.application.service.DecisionService;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AppController {

    // Fase 4.1: paleta cíclica para dibujar los gajos de la ruleta (no tiene
    // relación con las opciones en sí, solo variedad visual). Alineada con
    // la paleta de marca del rebranding de Fase 4.
    private static final List<String> WHEEL_COLORS = List.of(
            "#8b5cf6", "#fb7185", "#fbbf24", "#34d399", "#60a5fa", "#f472b6");

    private final DecisionService decisionService;
    private final UserRepository userRepository;
    private final RegisterUserUseCase registerUserUseCase;
    private final AlgorithmDetailsLocalizer algorithmDetailsLocalizer;
    private final GetUserStatsUseCase getUserStatsUseCase;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String welcome(Model model) {
        // Obtiene el objeto de autenticación del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Si el usuario está autenticado, su nombre de usuario está disponible aquí
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        // Devuelve la vista de la página principal
        return "welcome";
    }

    @GetMapping("/form")
    public String decisionForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        // BEST_OF_N y DAILY no son un DecisionAlgorithm de un solo tiro: no
        // encajan en este dropdown (ver /api/decisions/best-of-n y
        // /api/decisions/daily para esas mecánicas).
        model.addAttribute("algorithms", java.util.Arrays.stream(AlgorithmType.values())
                .filter(type -> type != AlgorithmType.BEST_OF_N && type != AlgorithmType.DAILY)
                .toList());

        return "decision-form";
    }

    @PostMapping("/decide")
    public String makeDecision(
            @RequestParam("options") List<@NotBlank String> opciones,
            @RequestParam("algorithm") String algoritmo,
            Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));
        DecideCommand decideCommand = DecideCommand.builder()
                .optionValues(opciones)
                .algorithmType(AlgorithmType.fromCode(algoritmo))
                .userId(user.getId())
                .build();
        var result = decisionService.decide(decideCommand);
        Map<String, Object> resolvedDetails = algorithmDetailsLocalizer.localize(result.getAlgorithmDetails());
        populateResultModel(model, result.getWinningOptionValue(), resolvedDetails);

        // Fase 4.1: la vista solo necesita saber qué algoritmo corrió para
        // decidir si anima ruleta/dado antes de revelar el resultado.
        model.addAttribute("algorithmCode", algoritmo);
        if (AlgorithmType.FORTUNE_WHEEL.getCode().equals(algoritmo)) {
            addWheelAnimationAttributes(model, opciones, resolvedDetails.get("winningAngleDegrees"));
        }

        // Fase 4.3: la decisión ya tiene shareCode (asignado en decide()),
        // la vista arma el link público a partir de este código.
        model.addAttribute("shareCode", result.getShareCode());

        return "decision-result";
    }

    // Atributos comunes a decision-result.html (recién decidido) y
    // shared-decision.html (Fase 4.3, consultado luego vía link público).
    private void populateResultModel(Model model, String winningOptionValue, Map<String, Object> resolvedDetails) {
        model.addAttribute("winningOptionValue", winningOptionValue);
        model.addAttribute("algorithm", resolvedDetails.get("algorithm"));
        model.addAttribute("description", resolvedDetails.get("description"));
        model.addAttribute("steps", resolvedDetails.get("steps"));
        String prefijo = "custom_";
        Map<String, Object> customDetails = resolvedDetails.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefijo))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(prefijo.length()),
                        Map.Entry::getValue
                ));
        model.addAttribute("customDetails", customDetails);
    }

    // Arma el conic-gradient (un gajo por opción, mismo orden que el índice
    // que usa FortuneWheelAlgorithm) y la posición de cada etiqueta, para que
    // el JS de la vista solo tenga que rotar la rueda ya dibujada hasta el
    // ángulo real devuelto por el algoritmo.
    private void addWheelAnimationAttributes(Model model, List<String> opciones, Object winningAngle) {
        int n = opciones.size();
        double segment = 360.0 / n;
        StringBuilder gradient = new StringBuilder("conic-gradient(");
        List<Map<String, Object>> labels = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String color = WHEEL_COLORS.get(i % WHEEL_COLORS.size());
            double start = i * segment;
            double end = start + segment;
            gradient.append(color).append(" ").append(start).append("deg ").append(end).append("deg");
            if (i < n - 1) gradient.append(", ");

            double midAngle = start + segment / 2;
            Map<String, Object> label = new LinkedHashMap<>();
            label.put("text", opciones.get(i));
            label.put("style", String.format(java.util.Locale.ROOT,
                    "transform: rotate(%.4fdeg) translateY(-100px) rotate(%.4fdeg);", midAngle, -midAngle));
            labels.add(label);
        }
        gradient.append(")");

        model.addAttribute("wheelGradientCss", gradient.toString());
        model.addAttribute("wheelLabels", labels);
        model.addAttribute("winningAngleDegrees", winningAngle);
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));
        model.addAttribute("username", username);

        UserStatsResult stats = getUserStatsUseCase.getStats(user.getId());
        model.addAttribute("totalDecisions", stats.getTotalDecisions());
        model.addAttribute("mostUsedAlgorithmName",
                stats.getMostUsedAlgorithm() != null ? stats.getMostUsedAlgorithm().getUiName() : null);
        model.addAttribute("mostWonOptionValue", stats.getMostWonOptionValue());
        model.addAttribute("mostWonOptionCount", stats.getMostWonOptionCount());

        long maxAlgorithmCount = stats.getDecisionsByAlgorithm().values().stream()
                .mapToLong(Long::longValue).max().orElse(0);
        List<Map<String, Object>> algorithmBreakdown = stats.getDecisionsByAlgorithm().entrySet().stream()
                .sorted(Map.Entry.<AlgorithmType, Long>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", entry.getKey().getUiName());
                    row.put("count", entry.getValue());
                    row.put("percent", maxAlgorithmCount == 0 ? 0 : (entry.getValue() * 100 / maxAlgorithmCount));
                    return row;
                })
                .toList();
        model.addAttribute("algorithmBreakdown", algorithmBreakdown);

        long maxWinCount = stats.getTopWinningOptions().values().stream()
                .mapToLong(Long::longValue).max().orElse(0);
        List<Map<String, Object>> topWinningOptions = stats.getTopWinningOptions().entrySet().stream()
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("value", entry.getKey());
                    row.put("count", entry.getValue());
                    row.put("percent", maxWinCount == 0 ? 0 : (entry.getValue() * 100 / maxWinCount));
                    return row;
                })
                .toList();
        model.addAttribute("topWinningOptions", topWinningOptions);

        return "stats";
    }

    // Fase 4.3: página pública (sin login, ver SecurityConfig) para el link
    // que "Compartir resultado" arma en decision-result.html. La API
    // equivalente (GET /api/decisions/shared/{code}) ya existía desde la
    // Fase 3.3; esto solo le agrega una vista Thymeleaf.
    @GetMapping("/shared/{shareCode}")
    public String sharedDecision(@PathVariable String shareCode, Model model) {
        try {
            Decision decision = decisionService.getByShareCode(shareCode);
            Map<String, Object> resolvedDetails = algorithmDetailsLocalizer.localize(decision.getAlgorithmDetails());
            populateResultModel(model, decision.getWinningOptionValue(), resolvedDetails);
        } catch (Exceptions.ResourceNotFoundException e) {
            model.addAttribute("notFound", true);
        }
        return "shared-decision";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email) {
        RegisterUserCommand command = RegisterUserCommand.builder()
                .username(username)
                .password(password)
                .email(email)
                .build();
        try {
            registerUserUseCase.register(command);
        } catch (Exceptions.InvalidRequestException | Exceptions.ConflictException e) {
            return "redirect:/register?error=" + e.getMessage();
        }
        return "redirect:/login";
    }
}
