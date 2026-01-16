// com/Globoo/auth/dto/VerifyCodeReq.java
package com.Globoo.auth.dto;

import com.Globoo.user.domain.Campus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyCodeReq(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6, max = 6) String code,
        Campus campus              // optional (SEOUL/GLOBAL)
) {}
