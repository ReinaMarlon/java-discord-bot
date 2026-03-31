package com.marlonreina.resisas.service;

import com.marlonreina.resisas.model.GuildConfig;
import com.marlonreina.resisas.repository.GuildRepository;

public class GuildService {

    private final GuildRepository repo = new GuildRepository();

    public GuildConfig getOrCreate(String guildId) {
        GuildConfig config = repo.findById(guildId);

        if (config == null) {
            config = new GuildConfig(guildId, "-");
            repo.save(config);
        }

        return config;
    }

    public void changePrefix(String guildId, String newPrefix) {
        repo.updatePrefix(guildId, newPrefix);
    }

}