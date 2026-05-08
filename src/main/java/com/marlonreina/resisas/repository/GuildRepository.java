package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.GuildConfig;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Modifying
    @Transactional
    @Query("UPDATE GuildConfig g SET g.premium = :premium WHERE g.guildId = :guildId")
    void updatePremium(@Param("guildId") String guildId,
                       @Param("premium") boolean premium);

}
