package com.Globoo.auth.dto;

import com.Globoo.user.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignupStep1Req(
        // 기본 회원정보 (Step1)
        @Email @NotBlank
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@hufs\\.ac\\.kr$",
                message = "@hufs.ac.kr 이메일만 가능합니다."
        )
        String email,

        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z0-9_]{2,29}$",
                message = "아이디는 영문으로 시작하고, 영문/숫자/_만 사용하며 3~30자여야 합니다."
        )
        String username,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "비밀번호는 8~64자이며 영문과 숫자를 각각 1개 이상 포함해야 합니다."
        )
        String password,

        @NotBlank @Size(max = 50)
        String name,

        @Pattern(regexp = "^[0-9]{8,15}$", message = "전화번호는 숫자만 8~15자리로 입력해야 합니다.")
        String phoneNumber,   // optional

        @NotBlank
        @Size(min = 2, max = 30, message = "닉네임은 2~30자여야 합니다.")
        String nickname,

        LocalDate birthDate,   // optional
        Gender gender         // optional

) {}
