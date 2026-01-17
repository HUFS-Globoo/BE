package com.Globoo.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // application.yml 의 globoo.upload.profile-dir 값 주입
    @Value("${globoo.upload.profile-dir:/home/ubuntu/app/uploads/profile/}")
    private String profileUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // dir 끝에 / 보정 (설정값이 슬래시 없이 들어오면 정적 매핑이 꼬일 수 있음)
        String dir = profileUploadDir.endsWith("/") ? profileUploadDir : profileUploadDir + "/";

        // 브라우저에서 /uploads/profile/** 로 들어오는 요청을
        // 서버 내부 dir 경로의 실제 파일로 매핑
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:" + dir);
    }
}
