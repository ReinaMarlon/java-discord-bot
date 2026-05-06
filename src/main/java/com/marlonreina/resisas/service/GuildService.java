package com.marlonreina.resisas.service;

import com.marlonreina.resisas.model.GuildConfig;
import com.marlonreina.resisas.repository.GuildRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuildService {

    private final GuildRepository repo;

    public GuildService(GuildRepository repo) {
        this.repo = repo;
    }

    public GuildConfig getOrCreate(String guildId) {
        return repo.findById(guildId)
                .orElseGet(() -> {
                    GuildConfig config = new GuildConfig(guildId, "-", false);
                    return repo.save(config);
                });
    }

    @Transactional
    public void changePrefix(String guildId, String newPrefix) {
        repo.updatePrefix(guildId, newPrefix);
    }

    public boolean isPremium(String guildId) {
        return repo.findById(guildId)
                .map(GuildConfig::getPremium)
                .orElse(false);
    }
}
