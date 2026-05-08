package com.marlonreina.resisas.model;

import java.io.Serializable;
import java.util.Objects;

public class GuildCommandId implements Serializable {

    private String guildId;
    private Long commandId;

    public GuildCommandId() {
    }

    public GuildCommandId(String guildId, Long commandId) {
        this.guildId = guildId;
        this.commandId = commandId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GuildCommandId that = (GuildCommandId) o;
        return Objects.equals(guildId, that.guildId) && Objects.equals(commandId, that.commandId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, commandId);
    }
}

