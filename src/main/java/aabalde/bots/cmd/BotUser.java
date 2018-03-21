package aabalde.bots.cmd;

import org.telegram.telegrambots.api.objects.User;

/**
 * Created by aabalde on 21/03/18.
 */
public class BotUser {

    private static String SPLITTER = "&&&";

    private Integer id; ///< Unique identifier for this user or bot
    private String firstName; ///< User‘s or bot’s first name
    private String lastName; ///< Optional. User‘s or bot’s last name

    public BotUser(){
        super();
    }

    public BotUser(User user){
        super();
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    //userRepresentation: ID&&&Name
    public BotUser(String userRepresentation){
        super();
        String[] params = userRepresentation.split(this.SPLITTER);
        this.id = Integer.parseInt(params[0]);

        String[] names = params[1].split(" ");
        this.firstName = names[0];
        this.lastName = names[1];
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        StringBuilder record = new StringBuilder();
        record.append(this.id);
        record.append(this.SPLITTER);
        record.append(this.firstName);
        if(this.lastName != null && !this.lastName.equals("")){
            record.append(" ");
            record.append(this.lastName);
        }
        return record.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotUser)) return false;

        BotUser botUser = (BotUser) o;
        return this.id.equals(botUser.getId());
    }
}
