package com.Globoo.user.repository;

import com.Globoo.user.domain.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, String> {

    Optional<Language> findByCodeIgnoreCase(String code);

    Optional<Language> findByNameIgnoreCase(String name);
}
