package com.eojeda89.decididorapi.adapter.in.web.dto;

import com.eojeda89.decididorapi.domain.model.OptionTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

// Sin userId/dueño: solo lo ve su propio dueño (ver TemplateController), no
// hace falta exponerlo -- mismo criterio que MakeDecisionResponse.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {
    private Long id;
    private String name;
    private List<String> options;
    private Instant createdAt;

    public static TemplateResponse fromDomain(OptionTemplate template) {
        TemplateResponse resp = new TemplateResponse();
        resp.id = template.getId() != null ? template.getId().value() : null;
        resp.name = template.getName();
        resp.options = template.getOptionValues();
        resp.createdAt = template.getCreatedAt();
        return resp;
    }
}
