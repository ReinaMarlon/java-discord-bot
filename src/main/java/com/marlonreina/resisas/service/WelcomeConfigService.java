package com.marlonreina.resisas.service;

import com.marlonreina.resisas.model.WelcomeConfig;
import com.marlonreina.resisas.repository.WelcomeConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WelcomeConfigService {

    private final WelcomeConfigRepository repo;

    public WelcomeConfigService(WelcomeConfigRepository repo) {
        this.repo = repo;
    }

    public WelcomeConfig getOrCreate(String guildId) {
        return repo.findById(guildId)
                .orElseGet(() -> {
                    WelcomeConfig config = new WelcomeConfig(guildId, null,
                            WelcomeConfig.SIMPLE_MESSAGE, null, false);
                    return repo.save(config);
                });
    }

    @Transactional
    public WelcomeConfig changeChannel(String guildId, String channelId) {
        WelcomeConfig config = getOrCreate(guildId);
        config.setChannelId(channelId);
        return repo.save(config);
    }

    @Transactional
    public WelcomeConfig changeEnabled(String guildId, boolean enabled) {
        WelcomeConfig config = getOrCreate(guildId);
        config.setEnabled(enabled);
        return repo.save(config);
    }

    @Transactional
    public WelcomeConfig changeMessageMode(String guildId, String messageMode) {
        WelcomeConfig config = getOrCreate(guildId);
        config.setMessage(messageMode);
        if (WelcomeConfig.SIMPLE_MESSAGE.equals(messageMode)) {
            config.setEmbedJson(null);
        }
        return repo.save(config);
    }
}
