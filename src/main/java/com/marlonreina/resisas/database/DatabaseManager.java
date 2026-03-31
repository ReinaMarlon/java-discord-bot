package com.marlonreina.resisas.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:bot.db";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL);
    }
}