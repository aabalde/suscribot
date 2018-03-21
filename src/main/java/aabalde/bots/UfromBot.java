package aabalde.bots;

import aabalde.bots.exception.UfromException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;

/**
 * Created by aabalde on 21/03/18.
 */
public class UfromBot extends TelegramLongPollingBot{

    String addCommand = "/imfrom ";
    String removeCommand = "/imnotfrom ";
    String printCommand = "/wearefrom";

    public enum Action{
        ADD,
        REMOVE,
        PRINT
    }

    private String botToken;

    private String backupDataPath;
    private String backupUsersPath;

    //Map with keys <LOCATION, USERS>
    private HashMap<String, List<BotUser>> data;
    //List with all the users contained in the system
    private List<BotUser> users;

    public UfromBot(String token, String backupData, String backupUsers){
        this.botToken = token;
        this.backupDataPath = backupData;
        this.backupUsersPath = backupUsers;

        this.data = new HashMap<>();
        if(backupData != null && !backupData.isEmpty()){
            try{
                loadData();
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        this.users = new ArrayList<>();
        if(backupUsers != null && !backupData.isEmpty()){
            try{
                loadUsers();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text. If so, this is a message from privat chat
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String text = message.getText();
            BotUser u = new BotUser(message.getFrom());

            try{
                //Only work if a bot command has been received
                if(commandReceived(text)){
                    switch (parseAction(text)){
                        case ADD:
                            add(text, u);
                            backup();
                            sendMessage(chatId,
                                    u.getFirstName() + " " + u.getLastName() + " added to location list");
                            break;
                        case REMOVE:
                            remove(text, u);
                            backup();
                            sendMessage(chatId,
                                    "User " + u.getFirstName() + " " + u.getLastName() + " added to location list");
                            break;
                        case PRINT:
                            sendMessage(chatId, print());
                            break;
                        default:
                            break;
                    }
                }
            } catch(UfromException e){
                sendMessage(chatId, e.getMessage());
            } catch(Exception e){
                sendMessage(chatId, "ERROR: " + e.getMessage());
            }
        }
    }

    protected void add(String text, BotUser u) throws Exception{
        if(users.contains(u)){
            throw new UfromException("User already exists");
        }

        String city = text.subSequence(text.indexOf(" ") + 1, text.length()).toString().toUpperCase();
        if(data.containsKey(city)){
            List<BotUser> userList = this.data.get(city);
            if(!userList.contains(u)){
                userList.add(u);
            }
        }
        else
        {
            ArrayList<BotUser> list = new ArrayList<>();
            list.add(u);
            this.data.put(city, list);
        }
        this.users.add(u);
    }

    protected void remove(String text, BotUser u) throws Exception{
        if(!users.contains(u)){
            throw new UfromException("Not existent user");
        }

        String city = text.subSequence(text.indexOf(" ") + 1, text.length()).toString().toUpperCase();
        if(data.containsKey(city)){
            List<BotUser> cityUsers = this.data.get(city);
            if(cityUsers.contains(u)){
                cityUsers.remove(u);
                this.users.remove(u);
                if(cityUsers.size() == 0) data.remove(city);
            }
        }
    }

    protected String print(){
        StringBuilder message = new StringBuilder();

        for(String key : this.data.keySet()){
            message.append(" -- " + key + " -- ");
            List<BotUser> users = this.data.get(key);
            for(BotUser u : users){
                message.append("\n" + u.getFirstName() + " " + u.getLastName());
            }
            message.append("\n\n");
        }

        return message.toString();
    }

    protected void backup() throws Exception{
        backupData();
        backupUsers();
    }

    private void backupData() throws Exception{
        File f = new File(backupDataPath);
        if(f.exists()){
            f.delete();
        }
        f.createNewFile();
        PrintWriter pw = new PrintWriter(new FileWriter(f, true));
        for(String key : this.data.keySet()){
            pw.println(key);
            List<BotUser> users = this.data.get(key);
            for(BotUser user : users){
                pw.println(user);
            }
        }
        pw.close();
    }

    private void backupUsers() throws Exception{
        File f = new File(backupUsersPath);
        if(f.exists()){
            f.delete();
        }
        f.createNewFile();
        PrintWriter pw = new PrintWriter(new FileWriter(f, true));
        for(BotUser user : this.users){
            pw.println(user);
        }
        pw.close();
    }

    protected void loadData() throws Exception{
        File dataFile = new File(this.backupDataPath);
        if(!dataFile.exists()){
            return;
        }

        BufferedReader br = new BufferedReader(new FileReader(dataFile));

        String line;
        String key = "";
        List<BotUser> users = new ArrayList<>();
        while((line = br.readLine())!= null){
            if(line.contains("&&&")){
                //Is an user
                BotUser user = new BotUser(line);
                users.add(user);
            } else {
                //Is a key
                if(!key.isEmpty()){
                    data.put(key, users);
                    users = new ArrayList<>();
                }
                key = line;
            }
        }
        //Add the last one
        data.put(key, users);
    }

    protected void loadUsers() throws Exception{
        File usersFile = new File(this.backupUsersPath);
        if(!usersFile.exists()){
            return;
        }

        BufferedReader br = new BufferedReader(new FileReader(usersFile));
        String line;
        while((line = br.readLine())!= null){
            if(!line.isEmpty()){
                BotUser user = new BotUser(line);
                this.users.add(user);
            }
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

    private Action parseAction(String text){
        if(text.startsWith(addCommand)){
            return Action.ADD;
        }
        if(text.startsWith(removeCommand)){
            return Action.REMOVE;
        }
        if(text.startsWith(printCommand)){
            return Action.PRINT;
        }
        return null;
    }

    private boolean commandReceived(String text){
        return (text.startsWith(addCommand) || text.startsWith(removeCommand) || text.startsWith(printCommand));
    }

    @Override
    public String getBotUsername() {
        return "UfromBot";
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }
}
