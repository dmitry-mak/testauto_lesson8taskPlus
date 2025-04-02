package ru.netology.apisql.data;

import lombok.Value;

public class DataHandler {

    public DataHandler() {
    }

    public static AuthInfo getRegisteredUserInfo() {
        return new AuthInfo("vasya", "qwerty123");
    }


    @Value
    public static class AuthInfo {
        String login;
        String password;
    }
}

