package aabalde.bots;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Created by aabalde on 21/03/18.
 */
public class UfromBot extends TelegramLongPollingBot{

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text. If so, this is a message from privat chat
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String text = message.getText();
            User u = message.getFrom();

        }
    }

    private void sendMessage(long chatId, String text){
        SendMessage message = new SendMessage() // Create a message object object
                .setChatId(chatId)
                .setText(text);
        try {
            execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "UfromBot";
    }

    @Override
    public String getBotToken() {
        return "";
    }
}
