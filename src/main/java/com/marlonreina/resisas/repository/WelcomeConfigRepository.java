package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.WelcomeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WelcomeConfigRepository extends JpaRepository<WelcomeConfig, String> {
}
