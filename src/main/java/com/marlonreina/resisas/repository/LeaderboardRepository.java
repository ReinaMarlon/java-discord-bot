package com.marlonreina.resisas.repository;

import com.marlonreina.resisas.database.DatabaseManager;
import com.marlonreina.resisas.model.LeaderboardAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardRepository {

    public boolean save(LeaderboardAccount account) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR IGNORE INTO leaderboard_accounts (guild_id, discord_id, riot_name, riot_tag) " +
                             "VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, account.getGuildId());
            stmt.setString(2, account.getDiscordId());
            stmt.setString(3, account.getRiotName());
            stmt.setString(4, account.getRiotTag());
            int rows = stmt.executeUpdate();
            return rows > 0; // false si ya existía

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<LeaderboardAccount> findByGuild(String guildId) {
        List<LeaderboardAccount> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM leaderboard_accounts WHERE guild_id = ?")) {

            stmt.setString(1, guildId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new LeaderboardAccount(
                        rs.getString("guild_id"),
                        rs.getString("discord_id"),
                        rs.getString("riot_name"),
                        rs.getString("riot_tag")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean delete(String guildId, String riotName, String riotTag) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM leaderboard_accounts WHERE guild_id = ? AND riot_name = ? AND riot_tag = ?")) {

            stmt.setString(1, guildId);
            stmt.setString(2, riotName);
            stmt.setString(3, riotTag);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}