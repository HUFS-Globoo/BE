// src/main/java/com/Globoo/user/web/CountryController.java
package com.Globoo.user.web;

import com.Globoo.user.domain.CountryList;
import com.Globoo.user.dto.CountryRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@Tag(name="Countries", description="국가 목록 조회")
public class CountryController {
    @GetMapping
    public List<CountryRes> listAll() {
        return CountryList.toList();
    }
}
