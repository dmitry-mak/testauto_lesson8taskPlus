package ru.netology.apisql.data;

import lombok.*;

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


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CardsInfo {
        String id;
        String number;
        Integer balance;
    }

    @Value
    public static class User {

    }
}

