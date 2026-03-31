package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.GuildConfig;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuildRepository extends JpaRepository<GuildConfig, String> {

    Optional<GuildConfig> findByGuildId(String guildId);

    @Modifying
    @Transactional
    @Query("UPDATE GuildConfig g SET g.prefix = :prefix WHERE g.guildId = :guildId")
    void updatePrefix(@Param("guildId") String guildId,
                      @Param("prefix") String prefix);

}