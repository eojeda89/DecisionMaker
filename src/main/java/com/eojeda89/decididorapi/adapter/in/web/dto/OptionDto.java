package com.eojeda89.decididorapi.adapter.in.web.dto;

import com.eojeda89.decididorapi.domain.model.Option;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDto {
    private Long id;
    private String value;

    public static OptionDto fromDomain(Option o) {
        OptionDto dto = new OptionDto();
        dto.id = o.getId() != null ? o.getId().value() : null;
        dto.value = o.getValue();
        return dto;
    }
}
