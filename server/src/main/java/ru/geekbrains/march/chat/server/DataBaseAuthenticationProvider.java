package ru.geekbrains.march.chat.server;

import java.sql.*;


public class DataBaseAuthenticationProvider implements AuthenticationProvider {
    private static Connection connection;
    private static Statement statement;




    public static void dbConnect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            statement = connection.createStatement();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error DB connection");
        }
    }

    public static void dbDisconnect () {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }



    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try (ResultSet rs = statement.executeQuery("select nickname from logins where login = '" + login + "' and password = '" + password + "';")) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void changeNickname(String oldNickname, String newNickname) {
        throw new UnsupportedOperationException();

    }

}
