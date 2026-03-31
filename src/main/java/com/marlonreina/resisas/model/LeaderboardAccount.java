package com.marlonreina.resisas.model;

public class LeaderboardAccount {
    private String guildId;
    private String discordId;
    private String riotName;
    private String riotTag;

    public LeaderboardAccount(String guildId, String discordId, String riotName, String riotTag) {
        this.guildId   = guildId;
        this.discordId = discordId;
        this.riotName  = riotName;
        this.riotTag   = riotTag;
    }

    public String getGuildId()   { return guildId;   }
    public String getDiscordId() { return discordId; }
    public String getRiotName()  { return riotName;  }
    public String getRiotTag()   { return riotTag;   }
}