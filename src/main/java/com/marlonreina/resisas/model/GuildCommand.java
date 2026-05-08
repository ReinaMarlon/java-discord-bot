package com.marlonreina.resisas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "guild_commands")
@IdClass(GuildCommandId.class)
public class GuildCommand {

    @Id
    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Id
    @Column(name = "command_id", nullable = false)
    private Long commandId;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
}

