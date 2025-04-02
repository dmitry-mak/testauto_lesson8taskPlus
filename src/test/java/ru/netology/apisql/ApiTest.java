package ru.netology.apisql;

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

    @Test
    public void shouldLoginAndVerifyTest() {

        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();
        ApiHandler.login(user.getLogin(), user.getPassword());

        String verificationCode = SqlHandler.getVerificationCode();
        System.out.println("Verification code: " + verificationCode);
        ApiHandler.verify(user.getLogin(), verificationCode);

        System.out.println("Token: " + ApiHandler.token);
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
    public void shouldCountTransactionsTest() {
        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();
        ApiHandler.login(user.getLogin(), user.getPassword());
        String verificationCode = SqlHandler.getVerificationCode();
        ApiHandler.verify(user.getLogin(), verificationCode);

        long transactionsCount = SqlHandler.getTransactionsCount();
        int lastTransactionAmount = SqlHandler.getLastTransactionAmount();
        System.out.println("Transactions count: " + transactionsCount);
        System.out.println("Last transaction amount: " + lastTransactionAmount);

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

        DataHandler.AuthInfo user = DataHandler.getRegisteredUserInfo();

        ApiHandler.login(user.getLogin(), user.getPassword());
        String verificationCode = SqlHandler.getVerificationCode();
        ApiHandler.verify(user.getLogin(), verificationCode);

        List<Map<String, Object>> cards = ApiHandler.getCards();

        String cardNumber1 = (String) cards.get(0).get("number");
        String cardNumber2 = (String) cards.get(1).get("number");
        int initialBalance1 = (int) cards.get(0).get("balance");
        int initialBalance2 = (int) cards.get(1).get("balance");
        long startTransactionsCount = SqlHandler.getTransactionsCount();

        int amountToTransfer = 5357;
        ApiHandler.makeTransfer(cardNumber2, cardNumber1, amountToTransfer);

        List<Map<String, Object>> cardsAfterTransfer = ApiHandler.getCards();
        int balance1AfterTransfer = (int) cardsAfterTransfer.get(0).get("balance");
        int balance2AfterTransfer = (int) cardsAfterTransfer.get(1).get("balance");
        long finalTransactionsCount = SqlHandler.getTransactionsCount();
        int lastTransactionAmount = SqlHandler.getLastTransactionAmount();

//        Так как балансы карт не меняются, добавлены дополнительные проверки,чтобы убедиться, в какой части программы возникает баг:
//        - количество транзакций должно увеличиваться на 1: это демонстрирует, что транзакция была зарегистрирована системой
//        - сумма перевода совпадает с суммой последней зарегистрированной транзакции

        Assertions.assertAll(
                () -> Assertions.assertEquals(initialBalance2 - amountToTransfer, balance2AfterTransfer),
                () -> Assertions.assertEquals(initialBalance1 + amountToTransfer, balance1AfterTransfer),
                () -> Assertions.assertEquals(startTransactionsCount + 1, finalTransactionsCount),
                () -> Assertions.assertEquals(amountToTransfer, lastTransactionAmount / 100)
        );
    }
}
