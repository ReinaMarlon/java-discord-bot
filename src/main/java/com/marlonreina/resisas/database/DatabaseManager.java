package com.marlonreina.resisas.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseManager {

    private final DataSource dataSource;

    @Autowired
    public DatabaseManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        // Esto devuelve una conexión del pool (Hikari) configurado para Postgres
        return dataSource.getConnection();
    }
}
