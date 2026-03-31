package com.marlonreina.resisas.service;

import com.marlonreina.resisas.model.LeaderboardAccount;
import com.marlonreina.resisas.repository.LeaderboardRepository;

import java.util.List;

public class LeaderboardService {

    private final LeaderboardRepository repo = new LeaderboardRepository();

    public boolean register(String guildId, String discordId, String riotName, String riotTag) {
        return repo.save(new LeaderboardAccount(guildId, discordId, riotName, riotTag));
    }

    public List<LeaderboardAccount> getAccounts(String guildId) {
        return repo.findByGuild(guildId);
    }

    public boolean unregister(String guildId, String riotName, String riotTag) {
        return repo.delete(guildId, riotName, riotTag);
    }
}