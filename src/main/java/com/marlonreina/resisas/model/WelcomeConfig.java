package com.marlonreina.resisas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "welcome_config")
public class WelcomeConfig {

    public static final String SIMPLE_MESSAGE = "simply";
    public static final String COMPLEX_MESSAGE = "complex";

    @Id
    @Column(name = "guild_id")
    private String guildId;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "message")
    private String message;

    @Column(name = "embed_json")
    private String embedJson;

    @Column(name = "enabled")
    private Boolean enabled;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public String getMessageMode() {
        if (message == null || message.isBlank()) {
            return SIMPLE_MESSAGE;
        }
        return message;
    }
}
