package ru.netology.apisql.data;

import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiHandler {

    public static final String BASE_URL = "http://localhost:9999/api";
    public static String token;

    public static void login(String login, String password) {
        given()
                .contentType("application/json")
                .body("{\"login\": \"" + login + "\", \"password\": \"" + password + "\"}")
                .when()
                .post(BASE_URL + "/auth")
                .then()
                .statusCode(200);
    }

    public static void verify(String login, String verificationCode) {
        Response verificationResponse = given()
                .contentType("application/json")
                .body("{\"login\": \"" + login + "\", \"code\": \"" + verificationCode + "\"}")
                .when()
                .post(BASE_URL + "/auth/verification")
                .then()
                .statusCode(200)
                .extract()
                .response();

        token = verificationResponse.path("token");
    }

    public static List<Map<String, Object>> getCards() {
            return given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get(BASE_URL + "/cards")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getList("$");
    }

    public static void makeTransfer(String fromCard, String toCard, int amount){
        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body("{\"from\": \"" + fromCard + "\", \"to\": \"" + toCard + "\", \"amount\": " + amount + "}")
                .when()
                .post(BASE_URL + "/transfer")
                .then()
                .statusCode(200);
    }
}
