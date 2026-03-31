package com.marlonreina.resisas.commands.test;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.marlonreina.resisas.commands.Command;

public class PingCommand implements Command {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage("pong 🏓").queue();
    }
}