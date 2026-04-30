package com.marlonreina.resisas.config;

import com.marlonreina.resisas.dto.CommandMetadata;
import com.marlonreina.resisas.service.BotCommandService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandCatalogInitializer implements ApplicationRunner {

    private final BotCommandService botCommandService;

    public CommandCatalogInitializer(BotCommandService botCommandService) {
        this.botCommandService = botCommandService;
    }

    @Override
    public void run(ApplicationArguments args) {
        botCommandService.upsertAll(List.of(
                new CommandMetadata("ping", "Comprueba si Hexa esta activo.", "Ninguno", false),
                new CommandMetadata("prefix", "Cambia el prefijo del servidor.", "Administrador", false),
                new CommandMetadata("help", "Muestra el centro de ayuda interactivo.", "Ninguno", false),
                new CommandMetadata("welcome", "Configura mensajes de bienvenida.", "Administrador", false),
                new CommandMetadata("clear", "Elimina mensajes recientes del canal.", "Gestionar mensajes", false),
                new CommandMetadata("kick", "Expulsa a un miembro del servidor.", "Expulsar miembros", false),
                new CommandMetadata("ban", "Banea a un miembro del servidor.", "Banear miembros", false),
                new CommandMetadata("economy", "Muestra el menu de economia.", "Ninguno", false),
                new CommandMetadata("balance", "Muestra tu balance o el de otro miembro.", "Ninguno", false),
                new CommandMetadata("daily", "Reclama tu recompensa diaria.", "Ninguno", false),
                new CommandMetadata("pay", "Transfiere monedas a otro miembro.", "Ninguno", false),
                new CommandMetadata("consultar", "Muestra informacion general de Valorant.", "Ninguno", false),
                new CommandMetadata("vplayer", "Muestra un perfil avanzado de Valorant.", "Ninguno", false),
                new CommandMetadata("vrank", "Muestra el rango competitivo actual.", "Ninguno", false),
                new CommandMetadata("vmatch", "Resume la ultima partida competitiva.", "Ninguno", false),
                new CommandMetadata("vregisteraccount", "Registra una cuenta en el leaderboard.", "Ninguno", false),
                new CommandMetadata("vleaderboard", "Muestra el ranking Valorant del servidor.", "Ninguno", false)
        ));
    }
}
