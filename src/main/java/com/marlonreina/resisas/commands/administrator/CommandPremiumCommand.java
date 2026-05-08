package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.repository.BotCommandRepository;
import com.marlonreina.resisas.utils.EmbedUtil;

public class CommandPremiumCommand implements Command {

    private static final long OWNER_ID = 595766968095866911L;

    private final BotCommandRepository botCommandRepository;

    public CommandPremiumCommand(BotCommandRepository botCommandRepository) {
        this.botCommandRepository = botCommandRepository;
    }

    @Override
    public void execute(CommandContext context) {
        var event = context.getEvent();

        if (event.getAuthor().getIdLong() != OWNER_ID) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tienes permisos para usar este comando.").build()
            ).queue();
            return;
        }

        String[] args = context.getArgs();
        if (args.length < 2) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("cmdpremium <comando> <on|off>")).build()
            ).queue();
            return;
        }

        String name = args[0].toLowerCase();
        String raw = args[1].toLowerCase();
        java.util.Optional<Boolean> premiumOpt = parseBoolean(raw);
        if (premiumOpt.isEmpty()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("Valor invalido. Usa `on` o `off`.").build()
            ).queue();
            return;
        }
        boolean premium = premiumOpt.get();

        int updated = botCommandRepository.updatePremiumByName(name, premium);
        if (updated == 0) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No encontre ese comando en la tabla `commands`.").build()
            ).queue();
            return;
        }

        event.getChannel().sendMessageEmbeds(
                EmbedUtil.success("Premium actualizado")
                        .setDescription("`" + name + "` premium: `" + premium + "`")
                        .build()
        ).queue();
    }

    private java.util.Optional<Boolean> parseBoolean(String raw) {
        return switch (raw) {
            case "on", "true", "1", "yes", "si" -> java.util.Optional.of(true);
            case "off", "false", "0", "no" -> java.util.Optional.of(false);
            default -> java.util.Optional.empty();
        };
    }
}
