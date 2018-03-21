package aabalde.bots.cmd;

import aabalde.bots.UfromBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Created by aabalde on 21/03/18.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize Api Context
        ApiContextInitializer.init();

        // Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // Register our bot
        try {
            botsApi.registerBot(new UfromBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
