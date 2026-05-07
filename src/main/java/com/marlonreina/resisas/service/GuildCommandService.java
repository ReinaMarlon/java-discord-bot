package com.marlonreina.resisas.service;

import com.marlonreina.resisas.repository.GuildCommandRepository;
import org.springframework.stereotype.Service;

@Service
public class GuildCommandService {

    private final GuildCommandRepository guildCommandRepository;

    public GuildCommandService(GuildCommandRepository guildCommandRepository) {
        this.guildCommandRepository = guildCommandRepository;
    }

    public boolean isEnabled(String guildId, String commandName) {
        guildCommandRepository.ensureRow(guildId, commandName);
        Boolean enabled = guildCommandRepository.findEnabled(guildId, commandName);
        return enabled == null || Boolean.TRUE.equals(enabled);
    }
}

