// com/Globoo/auth/dto/VerifyCodeReq.java
package com.Globoo.auth.dto;

import jakarta.validation.constraints.*;

public record VerifyCodeReq(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6, max = 6) String code
) {}
