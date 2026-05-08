package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.BotCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
public interface BotCommandRepository extends JpaRepository<BotCommand, Long> {

    Optional<BotCommand> findByName(String name);

    @Modifying
    @Transactional
    @Query("UPDATE BotCommand c SET c.premium = :premium WHERE c.name = :name")
    int updatePremiumByName(@Param("name") String name,
                            @Param("premium") boolean premium);
}
