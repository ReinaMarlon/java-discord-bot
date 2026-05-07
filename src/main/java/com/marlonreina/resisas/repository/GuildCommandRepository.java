package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.GuildCommand;
import com.marlonreina.resisas.model.GuildCommandId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildCommandRepository extends JpaRepository<GuildCommand, GuildCommandId> {

    @Query(value = """
            SELECT gc.enabled
            FROM guild_commands gc
            JOIN commands c ON c.command_id = gc.command_id
            WHERE gc.guild_id = :guildId
              AND c.command_name = :commandName
            """, nativeQuery = true)
    Boolean findEnabled(@Param("guildId") String guildId,
                        @Param("commandName") String commandName);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO guild_commands (guild_id, command_id, enabled)
            SELECT :guildId, c.command_id, true
            FROM commands c
            WHERE c.command_name = :commandName
            ON CONFLICT (guild_id, command_id) DO NOTHING
            """, nativeQuery = true)
    int ensureRow(@Param("guildId") String guildId,
                  @Param("commandName") String commandName);
}

