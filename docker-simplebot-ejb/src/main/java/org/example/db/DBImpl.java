package org.example.db;


import org.example.IDBOperations;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Default;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Default
@Singleton
@Startup
public class DBImpl implements IDBOperations {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private Connection connection;

    @PostConstruct
    public void initialize() {
        try {
            logger.log(Level.SEVERE, "Initial datasource and connection...");
            DataSource dataSource = (DataSource) new InitialContext().lookup("java:jboss/datasources/SqliteDS");
            connection = dataSource.getConnection();
            logger.log(Level.SEVERE, "Initial done.");
        } catch (SQLException | NamingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<String> selectDataFromDB() {
        logger.log(Level.INFO, "Select data ...");
        List<String> usersInfo = null;
        String selectQuery = "select * from users;";
        String user;
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            usersInfo = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                user = String.valueOf(resultSet.getString("userId")).concat(" - ").concat(resultSet.getString("name"));
                usersInfo.add(user);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return usersInfo;
    }

    @Override
    public void createTableToDB() {
        logger.log(Level.INFO, "Create table ...");
        String createTableQuery = "create table users(\n" +
                "userId integer primary key, \n" +
                "name varchar (50));";
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.execute();
            logger.log(Level.SEVERE, "Create table done...");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            logger.log(Level.SEVERE, "Table already exist, create table failed...");
        }
    }

    @Override
    public void insertDataToDB(int userId, String name) {

        if (this.selectDataFromDB(userId) != null) return;

        logger.log(Level.INFO, "Insert data ...");
        String insertQuery = "insert into users (userId, name) values(?, ?);";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setInt(1, userId);
            statement.setString(2, name);
            statement.execute();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String selectDataFromDB(int userId) {
        logger.log(Level.INFO, "Select user - {0}", userId);
        String selectQuery = "select name from users where userId = ?;";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                logger.log(Level.INFO, "User - {0} already exist, go on...", userId);
                return resultSet.getString("name");
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @PreDestroy
    public void cleanup() {
        try {
            logger.log(Level.SEVERE, "Close connection...");
            connection.close();
            connection = null;
            logger.log(Level.SEVERE, "Connection was closed...");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}