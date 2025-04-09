package ru.netology.apisql.data;

import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
//import ru.netology.sql.mode.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class SqlHandler {

    private static final QueryRunner runner = new QueryRunner();

    public SqlHandler() {
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/app", "app", "pass");
    }

    @SneakyThrows
    public static void updateUsers(String login, String password) {
        var dataSql = "INSERT INTO users(login, password) VALUES (?, ?);";
        try (var connection = getConnection()) {
            runner.update(connection, dataSql, login, password);
        }
    }

    @SneakyThrows
    public static long countUsers() {
        var countSql = "SELECT COUNT(*) FROM users;";
        try (var connection = getConnection()) {
            return runner.query(connection, countSql, new ScalarHandler<>());
        }
    }

    @SneakyThrows
    public static DataHandler.User getFirstUser() {
        var userSql = "SELECT * FROM users;";
        try (var connection = getConnection()) {
            return runner.query(connection, userSql, new BeanHandler<>(DataHandler.User.class));
        }
    }

    @SneakyThrows
    public static List<DataHandler.User> getAllUsers() {
        var userSql = "SELECT * FROM users;";
        try (var connection = getConnection()) {
            return runner.query(connection, userSql, new BeanListHandler<>(DataHandler.User.class));
        }
    }

    @SneakyThrows
    public static String getVerificationCode() {
        var codeSql = "SELECT code FROM auth_codes ORDER BY created DESC LIMIT 1;";
        var connection = getConnection();

        return runner.query(connection, codeSql, new ScalarHandler<>());
    }

    @SneakyThrows
    public static void cleanAllTables() {
        var runner = new QueryRunner();
        var deleteCardsSql = "DELETE FROM cards;";
        var deleteUsersSql = "DELETE FROM users;";
        var deleteAuthCodesSql = "DELETE FROM auth_codes;";
        var deleteCardTransactionsSQL = "DELETE FROM card_transactions;";
        try (var connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/app", "app", "pass")) {
            runner.update(connection, deleteAuthCodesSql);
            runner.update(connection, deleteCardTransactionsSQL);
            runner.update(connection, deleteCardsSql);
            runner.update(connection, deleteUsersSql);
        }
    }

    @SneakyThrows
    public static void cleanAuthCodesTable() {
        var deleteAuthCodesSql = "DELETE FROM auth_codes;";
        try (var connection = getConnection();) {
            runner.update(connection, deleteAuthCodesSql);
        }
    }

    @SneakyThrows
    public static String getUserStatus(String login) {
        var statusSql = "SELECT status FROM users WHERE login = ?;";
        try (var connection = getConnection();) {
            return runner.query(connection, statusSql, new ScalarHandler<>(), login);
        }
    }

    @SneakyThrows
    public static List<String> getCardNumbersById(String userId) {
        var cardNumbersSql = "SELECT number FROM cards WHERE user_id = ?;";
        try (var connection = getConnection();) {
            return runner.query(connection, cardNumbersSql, new ColumnListHandler<>("number"), userId);
        }
    }

    @SneakyThrows
    public static String getUserIdByLogin(String login) {
        var userIdSql = "SELECT id FROM users WHERE login = ?;";
        try (var connection = getConnection();) {
            return runner.query(connection, userIdSql, new ScalarHandler<>(), login);
        }
    }

    @SneakyThrows
    public static long getTransactionsCount() {
        var countSql = "SELECT COUNT(*) FROM card_transactions;";
        try (var connection = getConnection();) {
            return runner.query(connection, countSql, new ScalarHandler<>());
        }
    }

    @SneakyThrows
    public static int getLastTransactionAmount() {
        var sqlTransactionAmount = "SELECT amount_in_kopecks FROM card_transactions ORDER BY created DESC LIMIT 1;";
        try (var connection = getConnection();) {
            return runner.query(connection, sqlTransactionAmount, new ScalarHandler<>());
        }
    }

    @SneakyThrows
    public static List<DataHandler.CardsInfo> getUserCards() {
        var sqlUserCards = "SELECT id, number, balance_in_kopecks / 100 AS balance FROM cards;";
        try (var connection = getConnection()) {
            return runner.query(connection, sqlUserCards, new BeanListHandler<>(DataHandler.CardsInfo.class));
        }
    }
}
