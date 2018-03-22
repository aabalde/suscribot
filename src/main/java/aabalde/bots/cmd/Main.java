package aabalde.bots.cmd;

import aabalde.bots.UfromBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by aabalde on 21/03/18.
 */
public class Main {

    private static String configFilePath = "src/main/resources/bot-token.txt";
    private static String dataFilePath = "src/main/resources/data.dat";

    public static void main(String[] args) {
        // Initialize Api Context
        ApiContextInitializer.init();

        // Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();


        // Register our bot
        try {
            botsApi.registerBot(new UfromBot(getBotToken(), dataFilePath));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static String getBotToken() {
        try{
            File tokenFile = new File(configFilePath);
            BufferedReader br = new BufferedReader(new FileReader(tokenFile));
            String token = br.readLine();
            br.close();
            return token;
        } catch(IOException ioe){
            return null;
        }
    }
}
