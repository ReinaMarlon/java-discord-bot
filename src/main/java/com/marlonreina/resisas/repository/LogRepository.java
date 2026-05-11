package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, String> {
}
