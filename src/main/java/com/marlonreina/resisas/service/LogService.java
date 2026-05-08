package com.marlonreina.resisas.service;

import com.marlonreina.resisas.model.Log;
import com.marlonreina.resisas.repository.LogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    public void setLogChannel(String guildId, String channelId) {
        Log config = logRepository.findById(guildId)
                .orElse(new Log(guildId, null, true));
        config.setChannelId(channelId);
        config.setEnabled(true);
        logRepository.save(config);
    }

    public Optional<Log> getConfig(String guildId) {
        return logRepository.findById(guildId);
    }

    public void sendLog(net.dv8tion.jda.api.entities.Guild guild, String message) {
        getConfig(guild.getId()).ifPresent(config -> {
            if (config.getChannelId() != null && config.getEnabled()) {
                var channel = guild.getTextChannelById(config.getChannelId());
                if (channel != null) channel.sendMessage(message).queue();
            }
        });
    }

    public void setEnabled(String guildId, boolean enabled) {
        Log config = logRepository.findById(guildId)
                .orElse(new Log(guildId, null, false));
        config.setEnabled(enabled);
        logRepository.save(config);
    }
}