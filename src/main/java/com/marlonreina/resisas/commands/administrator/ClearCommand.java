package com.marlonreina.resisas.commands.administrator;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;

import java.util.Objects;

public class ClearCommand implements Command {

    private static final int DEFAULT_AMOUNT = 10;
    private static final int MAX_AMOUNT = 100;

    @Override
    public void execute(CommandContext context) {
        var event = context.getEvent();

        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tienes permisos para gestionar mensajes.").build()
            ).queue();
            return;
        }

        int amount = DEFAULT_AMOUNT;
        if (context.getArgs().length >= 1) {
            try {
                amount = Integer.parseInt(context.getArgs()[0]);
            } catch (NumberFormatException e) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtil.usage(context.usage("clear <cantidad>")).build()
                ).queue();
                return;
            }
        }

        if (amount < 1 || amount > MAX_AMOUNT) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtil.error("La cantidad debe estar entre 1 y 100.").build()
            ).queue();
            return;
        }

        event.getChannel().getHistory().retrievePast(amount)
                .queue(messages -> {
                    event.getChannel().purgeMessages(messages);
                    event.getChannel().sendMessageEmbeds(
                            EmbedUtil.success("Mensajes eliminados")
                                    .setDescription("Se eliminaron " + messages.size() + " mensaje(s).")
                                    .build()
                    ).queue();
                });
    }
}
