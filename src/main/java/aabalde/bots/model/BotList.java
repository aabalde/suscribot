package aabalde.bots.model;

import aabalde.bots.BotUser;
import aabalde.bots.exception.SuscriBotException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aabalde on 22/03/18.
 */
public class BotList {

    private String name;

    private String description;

    private List<String> categories;

    private List<BotUser> users;

    private HashMap<String, List<BotUser>> data;

    public BotList(){
        super();
        this.categories = new ArrayList<>();
        this.users = new ArrayList<>();
        this.data = new HashMap<>();
    }

    public BotList(String name, String description){
        this();
        this.name = name;
        this.description = description;
    }

    public BotList(String name, String description, HashMap<String, List<BotUser>> data){
        this(name, description);
        loadData(data);
    }

    protected void loadData(HashMap<String, List<BotUser>> data){
        for(String key : data.keySet()){
            this.categories.add(key);
            List<BotUser> users = data.get(key);
            this.users.addAll(users);
        }
        this.data.putAll(data);
    }

    public void subscribe(String category, BotUser user) throws SuscriBotException {
        this.users.add(user);
        if(isCategoryCreated(category)){
            this.data.get(category).add(user);
        } else {
            this.categories.add(category);
            List<BotUser> list = new ArrayList<>();
            list.add(user);
            this.data.put(category, list);
        }
    }

    public void unsubscribe(BotUser user) throws SuscriBotException {
        if(!isUserSubscribed(user)){
            throw new SuscriBotException("Non existent user in " + this.name);
        }

        String category = "";
        for(String key : this.data.keySet()){
            if(this.data.get(key).contains(user)){
                category = key;
            }
        }

        if(isCategoryCreated(category)){
            List<BotUser> categoryUsers = this.data.get(category);
            this.users.remove(user);
            categoryUsers.remove(user);
            if(categoryUsers.size() == 0){
                this.data.remove(category);
                this.categories.remove(category);
            }
        } else {
            throw new SuscriBotException("Non existent category " + category);
        }
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder();

        for(String key : this.data.keySet()){
            message.append(Emojis.BLUE_DIAMOND + " " + key + " " + Emojis.BLUE_DIAMOND);
            List<BotUser> users = this.data.get(key);
            for(BotUser u : users){
                message.append("\n" + Emojis.PERSON_SILOUETTE + " " + u.getFirstName());
                if(u.getLastName() != null){
                    message.append(" " + u.getLastName());
                }
            }
            message.append("\n\n");
        }

        return message.toString();
    }

    public boolean isUserSubscribed(BotUser user){
        return users.contains(user);
    }

    public boolean isCategoryCreated(String category){
        return this.categories.contains(category);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<BotUser> getUsers() {
        return users;
    }

    public void setUsers(List<BotUser> users) {
        this.users = users;
    }

    public HashMap<String, List<BotUser>> getData() {
        return data;
    }

    public void setData(HashMap<String, List<BotUser>> data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
