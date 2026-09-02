package com.eojeda89.decididorapi.adapter.in.web;

import com.eojeda89.decididorapi.application.port.in.RegisterUserUseCase;
import com.eojeda89.decididorapi.application.port.in.command.DecideCommand;
import com.eojeda89.decididorapi.application.port.in.command.RegisterUserCommand;
import com.eojeda89.decididorapi.application.port.out.UserRepository;
import com.eojeda89.decididorapi.application.service.DecisionService;
import com.eojeda89.decididorapi.common.exception.Exceptions;
import com.eojeda89.decididorapi.domain.model.AlgorithmType;
import com.eojeda89.decididorapi.domain.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AppController {

    private final DecisionService decisionService;
    private final UserRepository userRepository;
    private final RegisterUserUseCase registerUserUseCase;
    private final AlgorithmDetailsLocalizer algorithmDetailsLocalizer;

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
        model.addAttribute("algorithms", AlgorithmType.values());

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
        model.addAttribute("winningOptionValue", result.getWinningOptionValue());
        model.addAttribute("algorithm", resolvedDetails.get("algorithm"));
        model.addAttribute("description", resolvedDetails.get("description"));
        String prefijo = "custom_";
        Map<String, Object> customDetails = resolvedDetails.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefijo))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().substring(prefijo.length()),
                        Map.Entry::getValue
                ));
        model.addAttribute("customDetails", customDetails);
        return "decision-result";
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
