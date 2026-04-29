package com.marlonreina.resisas.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandContext {

    private final MessageReceivedEvent event;
    private final String[] args;
    private final String prefix;

    public CommandContext(MessageReceivedEvent event, String[] args, String prefix) {
        this.event = event;
        this.args = args.clone();
        this.prefix = prefix;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public String[] getArgs() {
        return args.clone();
    }

    public String getPrefix() {
        return prefix;
    }

    public String usage(String commandUsage) {
        return prefix + commandUsage;
    }
}
