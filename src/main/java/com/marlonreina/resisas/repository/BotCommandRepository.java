package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.BotCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotCommandRepository extends JpaRepository<BotCommand, Long> {

    Optional<BotCommand> findByName(String name);
}
