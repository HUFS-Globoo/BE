// com/Globoo/auth/dto/VerifyCodeReq.java
package com.Globoo.auth.dto;

import com.Globoo.user.domain.Campus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record VerifyCodeReq(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6, max = 6) String code,
        @NotNull(message = "캠퍼스 선택은 필수입니다.")
        Campus campus
) {}
