package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.database.DatabaseManager;
import com.marlonreina.resisas.model.GuildConfig;

import java.sql.*;

public class GuildRepository {

    public GuildConfig findById(String guildId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM guild_config WHERE guild_id = ?")) {

            stmt.setString(1, guildId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new GuildConfig(
                        rs.getString("guild_id"),
                        rs.getString("prefix")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void save(GuildConfig config) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO guild_config (guild_id, prefix) VALUES (?, ?)")) {

            stmt.setString(1, config.getGuildId());
            stmt.setString(2, config.getPrefix());
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePrefix(String guildId, String prefix) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE guild_config SET prefix = ? WHERE guild_id = ?")) {

            stmt.setString(1, prefix);
            stmt.setString(2, guildId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}