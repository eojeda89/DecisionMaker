package com.eojeda89.decididorapi.application.port.out;

import com.eojeda89.decididorapi.domain.model.Decision;
import com.eojeda89.decididorapi.domain.model.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DecisionRepository {
    Decision save(Decision decision);
    Page<Decision> findByUser(UserId userId, Pageable pageable);
    Optional<Decision> findByShareCode(String shareCode);
    // Fase 4.2 (estadísticas): a diferencia de findByUser, no pagina -- se
    // necesita el historial completo para calcular agregados (algoritmo más
    // usado, opción que más ganó). Para uso personal el volumen es chico
    // (ver Fase 1, "Historial sin paginar" en docs/plan-mejoras.md); si esto
    // deja de ser cierto, este método es el primer candidato a revisar.
    List<Decision> findAllByUser(UserId userId);
}
