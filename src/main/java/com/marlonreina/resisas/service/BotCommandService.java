package com.marlonreina.resisas.service;

import com.marlonreina.resisas.dto.CommandMetadata;
import com.marlonreina.resisas.model.BotCommand;
import com.marlonreina.resisas.repository.BotCommandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BotCommandService {

    private final BotCommandRepository repo;

    public BotCommandService(BotCommandRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void upsertAll(List<CommandMetadata> commands) {
        for (CommandMetadata metadata : commands) {
            upsert(metadata);
        }
    }

    private void upsert(CommandMetadata metadata) {
        BotCommand command = repo.findByName(metadata.name())
                .orElseGet(() -> new BotCommand(null, metadata.name(), metadata.description(),
                        metadata.permissions(), metadata.premium()));

        command.setDescription(metadata.description());
        command.setPermissions(metadata.permissions());
        command.setPremium(metadata.premium());
        repo.save(command);
    }
}
