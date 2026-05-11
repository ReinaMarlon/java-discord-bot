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
@Table(name = "log_config")
public class Log {

    @Id
    @Column(name = "guild_id")
    private String guildId;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "enabled")
    private Boolean enabled;
}