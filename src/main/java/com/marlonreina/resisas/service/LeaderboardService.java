package com.marlonreina.resisas.service;

import com.marlonreina.resisas.model.LeaderboardAccount;
import com.marlonreina.resisas.repository.LeaderboardRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings("EI_EXPOSE_REP2")
@Service
public class LeaderboardService {

    private final LeaderboardRepository repo;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repository safely; no external mutation risk"
    )
    public LeaderboardService(LeaderboardRepository repo) {
        this.repo = repo;
    }

    public boolean register(String guildId, String discordId, String riotName, String riotTag) {
        int rows = repo.insertIgnore(guildId, discordId, riotName, riotTag);
        return rows > 0; // false si ya existía
    }

    public List<LeaderboardAccount> getAccounts(String guildId) {
        return repo.findByGuildId(guildId);
    }

    public boolean unregister(String guildId, String riotName, String riotTag) {
        return repo.deleteByGuildIdAndRiotNameAndRiotTag(guildId, riotName, riotTag) > 0;
    }
}