package ru.netology.apisql.data;

import io.restassured.response.Response;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiHandler {

    private static final String BASE_URL = "http://localhost:9999/api";
    private static final String LOGIN = "/auth";
    private static final String VERIFICATION = "/auth/verification";
    private static final String CARDS = "/cards";
    private static final String TRANSFER = "/transfer";
    @Getter
    private static String token;

    public static void login(String login, String password) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("login", login);
        requestBody.put("password", password);

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + LOGIN)
                .then()
                .statusCode(200);
    }


    public static void verify(String login, String verificationCode) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("login", login);
        requestBody.put("code", verificationCode);

        Response verificationResponse = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + VERIFICATION)
                .then()
                .statusCode(200)
                .extract()
                .response();

        token = verificationResponse.path("token");
    }


    public static List<Map<String, Object>> getCards() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        Response response = given()
                .contentType("application/json")
                .headers(headers)
                .when()
                .get(BASE_URL + CARDS)
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.jsonPath().getList("$");
    }


    public static void makeTransfer(int amount, String fromCard, String toCard) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from", fromCard);
        requestBody.put("to", toCard);
        requestBody.put("amount", amount);

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .log().all()
                .when()
                .post(BASE_URL + TRANSFER)
                .then()
                .log().all()
                .statusCode(200);
    }
}
