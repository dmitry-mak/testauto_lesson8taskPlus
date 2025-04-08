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

//    @AfterAll
//    public static void cleanUpAll() {
//        SqlHandler.cleanAllTables();
//    }

    @Test
    public void shouldLoginAndVerifyTest() {

        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();
        ApiHandler.login(user.getLogin(), user.getPassword());

        String verificationCode = SqlHandler.getVerificationCode();
        System.out.println("Verification code: " + verificationCode);
        ApiHandler.verify(user.getLogin(), verificationCode);

        System.out.println("Token: " + ApiHandler.getToken());
    }

    @Test
    public void shouldGetCardsListFromApiTest() {

        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();
        ApiHandler.login(user.getLogin(), user.getPassword());
        String verificationCode = SqlHandler.getVerificationCode();
        ApiHandler.verify(user.getLogin(), verificationCode);

        List<Map<String, Object>> cards = ApiHandler.getCards();

        for (Map<String, Object> card : cards) {
            System.out.println("Card id: " + card.get("id"));
            System.out.println("Card number: " + card.get("number"));
            System.out.println("Balance: " + card.get("balance"));
        }
    }

    @Test
    public void shouldGetCardNumbersByIdTest() {

        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();
        String login = user.getLogin();

        String userID = SqlHandler.getUserIdByLogin(login);

        List<String> cardNumbers = SqlHandler.getCardNumbersById(userID);

        Assertions.assertNotNull(cardNumbers);
        for (String cardNumber : cardNumbers) {
            System.out.println("Card number: " + cardNumber);
        }
    }

    @Test
    public void shouldTransferBetweenCardsTest() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();

        ApiHandler.login(user.getLogin(), user.getPassword());
        String verificationCode = SqlHandler.getVerificationCode();
        ApiHandler.verify(user.getLogin(), verificationCode);

        List<DataHandler.CardsInfo> cards = SqlHandler.getUserCards();
        System.out.println("Initial cards data:" + cards);

        String cardNumber1 = cards.get(0).getNumber();
        String cardNumber2 = cards.get(1).getNumber();
        int initialBalance1 = cards.get(0).getBalance();
        int initialBalance2 = cards.get(1).getBalance();

        long startTransactionsCount = SqlHandler.getTransactionsCount();

        int amountToTransfer = 5357;
        ApiHandler.makeTransfer(amountToTransfer, cardNumber2, cardNumber1);

        List<DataHandler.CardsInfo> cardsAfterTransfer = SqlHandler.getUserCards();
        System.out.println("Cards after transfer: " + cardsAfterTransfer);

        int balance1AfterTransfer = cardsAfterTransfer.get(0).getBalance();
        int balance2AfterTransfer = cardsAfterTransfer.get(1).getBalance();
        long finalTransactionsCount = SqlHandler.getTransactionsCount();
        int lastTransactionAmount = SqlHandler.getLastTransactionAmount();

        Assertions.assertAll(
                () -> Assertions.assertEquals(initialBalance2 - amountToTransfer, balance2AfterTransfer),
                () -> Assertions.assertEquals(initialBalance1 + amountToTransfer, balance1AfterTransfer),
                () -> Assertions.assertEquals(startTransactionsCount + 1, finalTransactionsCount),
                () -> Assertions.assertEquals(amountToTransfer, lastTransactionAmount / 100)
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
                () -> Assertions.assertEquals(initialBalance2 - amountToTransfer, balance2AfterTransfer),
                () -> Assertions.assertEquals(initialBalance1 + amountToTransfer, balance1AfterTransfer)
        );
    }
}
