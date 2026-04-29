package com.marlonreina.resisas.commands.test;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.utils.EmbedUtil;

public class PingCommand implements Command {

    @Override
    public void execute(CommandContext context) {
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Pong").setDescription("Hexa esta activo.").build()
        ).queue();
    }
}
