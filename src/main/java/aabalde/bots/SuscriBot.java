package aabalde.bots;

import aabalde.bots.exception.SuscriBotException;
import aabalde.bots.model.BotList;
import aabalde.bots.model.BotCSVFormat;
import aabalde.bots.model.Emojis;
import org.apache.commons.csv.CSVRecord;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by aabalde on 21/03/18.
 */
public class SuscriBot extends TelegramLongPollingBot{

    private static String NEWLIST_CMD = "/newlist ";
    private static String DELETELIST_CMD = "/deletelist ";
    private static String LISTS_CMD = "/lists";
    private static String SHOWLIST_CMD = "/showlist ";
    private static String SUBSCRIBE_CMD = "/subscribe ";
    private static String UNSUBSCRIBE_CMD = "/unsubscribe ";
    private static String PROFILE_CMD = "/profile";
    private static String HELP_CMD = "/suscribot";

    public enum Action{
        NEW,
        DELETE,
        LISTS,
        SHOW,
        SUBSCRIBE,
        UNSUBSCRIBE,
        PROFILE,
        HELP
    }

    private String botToken;

    private String backupDataPath;

    private List<BotList> lists;

    public SuscriBot(String token, String backupData){
        this.botToken = token;
        this.backupDataPath = backupData;

        this.lists = new ArrayList<>();
        if(backupData != null && !backupData.isEmpty()){
            try{
                loadData();
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

            //Bot only for groups
            if(!message.getChat().isGroupChat() && !message.getChat().isSuperGroupChat()){
                String msg = "THIS BOT IS ONLY FOR WORKING WITH GROUPS\n\n";
                msg = msg + "Additional info about the bot: \n" + help();
                sendMessage(chatId,msg);
                return;
            }

            String text = message.getText().trim();
            BotUser u = new BotUser(message.getFrom());
            String lastName = u.getLastName();

            try{
                //Only work if a bot command has been received
                if(commandReceived(text)){
                    switch (parseAction(text)){
                        case NEW:
                            newList(text);
                            sendMessage(chatId, "New list added");
                            break;
                        case DELETE:
                            deleteList(text);
                            backup();
                            sendMessage(chatId, "List deleted");
                            break;
                        case LISTS:
                            sendMessage(chatId, toStringLists());
                            break;
                        case SUBSCRIBE:
                            subscribe(text, u);
                            backup();
                            StringBuilder sbSubscribe = new StringBuilder();
                            sbSubscribe.append(u.getFirstName());
                            if(lastName != null && !lastName.isEmpty()){
                                sbSubscribe.append(" " + lastName);
                            }
                            sbSubscribe.append(" added to list");
                            sendMessage(chatId, sbSubscribe.toString());
                            break;
                        case UNSUBSCRIBE:
                            unsubscribe(text, u);
                            backup();
                            StringBuilder sbUnsubscribe = new StringBuilder();
                            sbUnsubscribe.append(u.getFirstName());
                            if(lastName != null && !lastName.isEmpty()){
                                sbUnsubscribe.append(" " + lastName);
                            }
                            sendMessage(chatId,
                                    "User " + u.getFirstName() + " " + u.getLastName() + " removed list");
                            break;
                        case SHOW:
                            String[] cmdSplitted = text.split(" ");
                            String listName = cmdSplitted[1];
                            BotList list = getList(listName);
                            if(list == null) throw new SuscriBotException("No list found with name " + listName);
                            sendMessage(chatId, list.toString());
                            break;
                        case PROFILE:
                            String userProfile = profile(message);
                            sendMessage(chatId, userProfile);
                            break;
                        case HELP:
                            sendMessage(chatId, help());
                            break;
                        default:
                            break;
                    }
                }
            } catch(SuscriBotException e){
                sendMessage(chatId, e.getMessage());
            } catch(Exception e){
                sendMessage(chatId, "ERROR: " + e.getMessage());
            }
        }
    }

    protected void newList(String text) throws SuscriBotException {
        int firstSpaceOcurrence = text.indexOf(" ");
        int secondSpaceOcurrence = text.indexOf(" ",firstSpaceOcurrence + 1);

        String cmdPlusName;
        String name;
        String description = null;
        if(secondSpaceOcurrence > 0){
            cmdPlusName = text.subSequence(0, text.indexOf(" ", firstSpaceOcurrence + 1)).toString();
            name = cmdPlusName.split(" ")[1];
            description = text.subSequence(secondSpaceOcurrence, text.length()).toString().trim();
        } else {
            name = text.split(" ")[1];
        }

        if(getList(name) != null){
            throw new SuscriBotException("List already exists");
        }

        BotList newList = new BotList(name, description);
        this.lists.add(newList);
    }

    protected void deleteList(String text){
        String name = text.split(" ")[1];

        ListIterator<BotList> iterator = this.lists.listIterator();
        while(iterator.hasNext()){
            BotList list = iterator.next();
            if(list.getName().toUpperCase().equals(name.toUpperCase())){
                iterator.remove();
                return;
            }
        }
    }

    protected void subscribe(String text, BotUser user) throws SuscriBotException {
        String[] textSplitted = text.split(" ");
        if(textSplitted.length != 3) throw new SuscriBotException("Wrong command. /subscribe <list> <category> " +
                "(No whitespaces in the names)");
        String listName = textSplitted[1];
        String category = textSplitted[2].toUpperCase();

        BotList list = getList(listName);
        if(list == null){
            throw new SuscriBotException("Non existent list");
        }

        list.subscribe(category, user);
    }

    protected void unsubscribe(String text, BotUser user) throws SuscriBotException {
        String[] textSplitted = text.split(" ");
        if(textSplitted.length != 3) throw new SuscriBotException("Wrong command. /unsubscribe <list> <category> " +
                "(No whitespaces in the names)");
        String listName = textSplitted[1];

        BotList list = getList(listName);
        if(list == null){
            throw new SuscriBotException("Non existent list");
        }

        list.unsubscribe(user);
    }

    protected String profile(Message message) throws SuscriBotException{
        List<MessageEntity> entities = message.getEntities();
        StringBuilder profile = new StringBuilder();
        for(MessageEntity entity : entities){
            if(entity.getType().equals("text_mention")){
                BotUser user = new BotUser(entity.getUser());
                profile.append(Emojis.PERSON_SILOUETTE);
                profile.append(user.getFirstName());
                String lastName = user.getLastName();
                if(lastName != null){
                    profile.append(" " + lastName);
                }
                profile.append("\n\n");
                profile.append("-------------\n");

                //Collect user data
                for(BotList list : lists){
                    HashMap<String, List<BotUser>> listData = list.getData();
                    profile.append(Emojis.NOTEPAD + " " + list.getName() + "\n");
                    for(String category : listData.keySet()){
                        List<BotUser> users = listData.get(category);
                        if(users.contains(user)){
                            profile.append(Emojis.BLUE_DIAMOND);
                            profile.append(" " + category + " ");
                            profile.append(Emojis.BLUE_DIAMOND);
                            profile.append("\n");
                        }
                    }
                    profile.append("-------------");
                    profile.append("\n");
                }

            } else {
                if(entity.getType().equals("mention")){
                   throw new SuscriBotException("Wrong user mention");
                }
            }
        }
        return profile.toString();
    }

    protected String help() {
        StringBuilder sb = new StringBuilder();

        sb.append("This is a bot for subscribing list management. For using this bot you have the following options:\n\n");
        sb.append("/newlist <name>\n\nCreates a new list with the name provided. " + "No whitespaces allowed.\n\n\n");
        sb.append("/deletelist <name>\n\nDeletes a list with the name provided. No whitespaces in the name.\n\n\n");
        sb.append("/lists\n\nShows all the lists stored in the system.\n\n\n");
        sb.append("/subscribe <list> <category>\n\nSubscribe the user to the list-category provided.\n\n\n");
        sb.append("/unsubscribe <list> <category>\n\nUnsubscribe the user from the list-category provided.\n\n\n");
        sb.append("/profile <mention-user>\n\nShows an user profile. (List and categories where is subscribed)\n\n\n");
        sb.append("/showlist <list>\n\nShows all the categories and users subscribed to the list provided.\n\n\n");

        return sb.toString();
    }

    protected String toStringLists(){
        StringBuilder sb = new StringBuilder();

        if(this.lists.size() == 0){
            return "";
        }

//        sb.append(Emojis.NOTEPAD + " LISTS " + Emojis.NOTEPAD +"\n\n");

        for(BotList list : this.lists){
            sb.append(Emojis.NOTEPAD + " ");
            sb.append(list.getName());
//            String description = list.getDescription();
//            if(description != null && !description.isEmpty()){
//                sb.append(" - " + description);
//            }
            sb.append("\n\n");
        }

        return sb.toString();
    }

    protected BotList getList(String listName) throws SuscriBotException {
        String listToSearch = listName.trim().toUpperCase();
        for(BotList list : this.lists){
            if(list.getName().toUpperCase().equals(listToSearch)){
                return list;
            }
        }
        return null;
    }

    protected void backup() throws Exception{
        File f = new File(backupDataPath);
        if(f.exists()){
            f.delete();
        }
        f.createNewFile();
        PrintWriter pw = new PrintWriter(new FileWriter(f, true));

        //Navigate data and store it in the file
        for(BotList list : this.lists){
            String listName = list.getName();
            String description = list.getDescription();
            HashMap<String,List<BotUser>> listData = list.getData();
            for(String key : listData.keySet()){
                String category = key;
                List<BotUser> users = listData.get(category);
                for(BotUser user : users){
                    String userRep = user.toString();
                    BotCSVFormat.printCSVRecord(pw, listName, description, category, userRep);
                }
            }
        }

        pw.close();
    }

    protected void loadData() throws Exception{
        File dataFile = new File(this.backupDataPath);
        if(!dataFile.exists()){
            return;
        }
        FileReader fr = new FileReader(dataFile);

        List<CSVRecord> records = BotCSVFormat.readCSVRecord(fr);
        for(CSVRecord record : records){
            String listName = record.get(0);
            String listDesc = record.get(1);
            String category = record.get(2);
            String userRep = record.get(3);

            BotList list = getList(listName);
            if(list == null){
                list = new BotList(listName,listDesc);
                this.lists.add(list);
            }
            try{
                list.subscribe(category, new BotUser(userRep));
            } catch (SuscriBotException e){
                //Continue
            }
        }
    }

    private void sendMessage(long chatId, String text){
        String textToSend = text;
        if(textToSend == null || textToSend.isEmpty()){
            textToSend = "Empty";
        }

        SendMessage message = new SendMessage() // Create a message object object
                .setChatId(chatId)
                .setText(textToSend);
        try {
            execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Action parseAction(String text){
        if(text.toLowerCase().startsWith(NEWLIST_CMD)){
            return Action.NEW;
        }
        if(text.toLowerCase().startsWith(DELETELIST_CMD)){
            return Action.DELETE;
        }
        if(text.toLowerCase().startsWith(LISTS_CMD)){
            return Action.LISTS;
        }
        if(text.toLowerCase().startsWith(SHOWLIST_CMD)){
            return Action.SHOW;
        }
        if(text.toLowerCase().startsWith(SUBSCRIBE_CMD)){
            return Action.SUBSCRIBE;
        }
        if(text.toLowerCase().startsWith(UNSUBSCRIBE_CMD)){
            return Action.UNSUBSCRIBE;
        }
        if(text.toLowerCase().startsWith(PROFILE_CMD)){
            return Action.PROFILE;
        }
        if(text.toLowerCase().startsWith(HELP_CMD)){
            return Action.HELP;
        }

        return null;
    }

    private boolean commandReceived(String text){
        return (text.toLowerCase().startsWith(NEWLIST_CMD) ||
                text.toLowerCase().startsWith(DELETELIST_CMD) ||
                text.toLowerCase().startsWith(LISTS_CMD) ||
                text.toLowerCase().startsWith(SHOWLIST_CMD) ||
                text.toLowerCase().startsWith(SUBSCRIBE_CMD) ||
                text.toLowerCase().startsWith(UNSUBSCRIBE_CMD) ||
                text.toLowerCase().startsWith(PROFILE_CMD) ||
                text.toLowerCase().startsWith(HELP_CMD));
    }

    @Override
    public String getBotUsername() {
        return "SuscriBot";
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }
}
