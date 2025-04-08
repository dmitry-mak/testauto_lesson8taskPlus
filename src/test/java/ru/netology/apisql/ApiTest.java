package ru.netology.apisql;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.netology.apisql.data.ApiHandler;
import ru.netology.apisql.data.DataHandler;
import ru.netology.apisql.data.SqlHandler;

import java.util.List;
import java.util.Map;

public class ApiTest {

    @AfterEach
    public void cleanUp() {
        SqlHandler.cleanAuthCodesTable();
    }

    @AfterAll
    public static void cleanUpAll() {
        SqlHandler.cleanAllTables();
    }


    @Test
    public void shouldTransferBetweenCardsTest() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();

        ApiHandler.login(user.getLogin(), user.getPassword());
        String verificationCode = SqlHandler.getVerificationCode();
        ApiHandler.verify(user.getLogin(), verificationCode);

        List<DataHandler.CardsInfo> cards = SqlHandler.getUserCards();

        String cardNumber1 = cards.get(0).getNumber();
        String cardNumber2 = cards.get(1).getNumber();
        int initialBalance1 = cards.get(0).getBalance();
        int initialBalance2 = cards.get(1).getBalance();

        int amountToTransfer = initialBalance2 / 10;
        ApiHandler.makeTransfer(amountToTransfer, cardNumber2, cardNumber1);

        List<DataHandler.CardsInfo> cardsAfterTransfer = SqlHandler.getUserCards();

        int balance1AfterTransfer = cardsAfterTransfer.get(0).getBalance();
        int balance2AfterTransfer = cardsAfterTransfer.get(1).getBalance();

        Assertions.assertAll(
                () -> Assertions.assertEquals(initialBalance2 - amountToTransfer, balance2AfterTransfer),
                () -> Assertions.assertEquals(initialBalance1 + amountToTransfer, balance1AfterTransfer)
        );
    }

    @Test
    public void shouldNotTransferOverBalanceTest() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();

        ApiHandler.login(user.getLogin(), user.getPassword());
        String verificationCode = SqlHandler.getVerificationCode();
        ApiHandler.verify(user.getLogin(), verificationCode);

        List<DataHandler.CardsInfo> cards = SqlHandler.getUserCards();

        String cardNumber1 = cards.get(0).getNumber();
        String cardNumber2 = cards.get(1).getNumber();
        int initialBalance1 = cards.get(0).getBalance();
        int initialBalance2 = cards.get(1).getBalance();

        int amountToTransfer = initialBalance1 + 10000;
        ApiHandler.makeTransfer(amountToTransfer, cardNumber2, cardNumber1);

        List<DataHandler.CardsInfo> cardsAfterTransfer = SqlHandler.getUserCards();

        int balance1AfterTransfer = cardsAfterTransfer.get(0).getBalance();
        int balance2AfterTransfer = cardsAfterTransfer.get(1).getBalance();

        Assertions.assertAll(
                () -> Assertions.assertEquals(initialBalance2, balance2AfterTransfer),
                () -> Assertions.assertEquals(initialBalance1, balance1AfterTransfer)
        );
    }
}
