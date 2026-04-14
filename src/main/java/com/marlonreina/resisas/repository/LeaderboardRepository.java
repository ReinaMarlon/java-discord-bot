package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.model.LeaderboardAccount;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardAccount, Long> {

    List<LeaderboardAccount> findByGuildId(String guildId);

    long deleteByGuildIdAndRiotNameAndRiotTag(String guildId, String riotName, String riotTag);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO leaderboard_accounts (guild_id, discord_id, riot_name, riot_tag)
            VALUES (:guildId, :discordId, :riotName, :riotTag)
            ON CONFLICT (guild_id, riot_name, riot_tag) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(@Param("guildId") String guildId,
                     @Param("discordId") String discordId,
                     @Param("riotName") String riotName,
                     @Param("riotTag") String riotTag);
}