package com.eojeda89.decididorapi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UserId id;
    private String username;
    private String email;
    private String password;
    private Instant createdAt;
    private Instant updatedAt;
}
