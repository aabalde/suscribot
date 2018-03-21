package aabalde.bots;

import aabalde.bots.exception.UfromException;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

/**
 * Created by aabalde on 21/03/18.
 */
public class UfromBotTest {

    @Test
    public void test_add_user_lowercase() throws Exception{
        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        UfromBot bot = new UfromBot("token", null, null);

        bot.add("vigo",user);

        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\n\n",result);
    }

    @Test
    public void test_add_user_uppercase() throws Exception{
        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        UfromBot bot = new UfromBot("token", null, null);

        bot.add("VIGO",user);

        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\n\n",result);
    }

    @Test
    public void test_add_user_mixcase() throws Exception{
        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        UfromBot bot = new UfromBot("token", null, null);

        bot.add("VigO",user);

        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\n\n",result);
    }

    @Test
    public void test_remove_user_lowercase() throws Exception{
        UfromBot bot = new UfromBot("token", null, null);

        String topic = "vigo";

        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        bot.add(topic,user);
        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\n\n",result);

        bot.remove(topic,user);
        String result2 = bot.print();

        assertEquals("The result is not correct","",result2);
    }

    @Test
    public void test_remove_user_uppercase() throws Exception{
        UfromBot bot = new UfromBot("token", null, null);

        String topic = "VIGO";

        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        bot.add(topic,user);
        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\n\n",result);

        bot.remove(topic,user);
        String result2 = bot.print();

        assertEquals("The result is not correct","",result2);
    }

    @Test
    public void test_remove_user_mixcase() throws Exception{
        UfromBot bot = new UfromBot("token", null, null);

        String topic = "vIGo";

        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        bot.add(topic,user);
        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\n\n",result);

        bot.remove(topic,user);
        String result2 = bot.print();

        assertEquals("The result is not correct","",result2);
    }

    @Test(expected = UfromException.class)
    public void test_add_user_existent() throws Exception{
        UfromBot bot = new UfromBot("token", null, null);

        String topic = "vIGo";

        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        bot.add(topic,user);
        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\n\n",result);

        bot.add(topic,user);
    }

    @Test(expected = UfromException.class)
    public void test_remove_user_non_existent() throws Exception{
        UfromBot bot = new UfromBot("token", null, null);

        String topic = "vIGo";

        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        bot.remove(topic,user);
    }

    @Test
    public void test_add_multiple_users_multiple_cities() throws Exception{
        UfromBot bot = new UfromBot("token", null, null);

        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        BotUser user2 = new BotUser();
        user2.setId(456);
        user2.setFirstName("Iron");
        user2.setLastName("Man");

        BotUser user3 = new BotUser();
        user3.setId(789);
        user3.setFirstName("Spider");
        user3.setLastName("Man");

        bot.add("vigo", user);
        bot.add("vigo", user2);
        bot.add("granada", user3);

        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\nIron Man\n\n" +
                " -- GRANADA -- \nSpider Man\n\n",result);
    }

    @Test
    public void test_backup() throws Exception{
        String dataFilePath = "src/test/resources/data.dat";
        String usersFilePath = "src/test/resources/users.dat";
        UfromBot bot = new UfromBot("token", dataFilePath, usersFilePath);

        BotUser user = new BotUser();
        user.setId(123);
        user.setFirstName("Captain");
        user.setLastName("America");

        BotUser user2 = new BotUser();
        user2.setId(456);
        user2.setFirstName("Iron");
        user2.setLastName("Man");

        BotUser user3 = new BotUser();
        user3.setId(789);
        user3.setFirstName("Spider");
        user3.setLastName("Man");

        bot.add("vigo", user);
        bot.add("vigo", user2);
        bot.add("granada", user3);

        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\nIron Man\n\n" +
                " -- GRANADA -- \nSpider Man\n\n",result);

        bot.backup();

        File resultFile = new File(dataFilePath);
        FileInputStream fis = new FileInputStream(resultFile);
        byte[] data = new byte[(int) resultFile.length()];
        fis.read(data);
        fis.close();
        String backupData = new String(data, "UTF-8");
        if(resultFile.exists()){
            resultFile.delete();
        }

        String expected = "VIGO\n123&&&Captain America\n456&&&Iron Man\nGRANADA\n789&&&Spider Man\n";

        assertEquals("The resulting file content must match with the expected one: '" + expected + "'",
                expected,backupData);

        File usersFile = new File(usersFilePath);
        FileInputStream fis2 = new FileInputStream(usersFile);
        byte[] data2 = new byte[(int) resultFile.length()];
        fis2.read(data2);
        fis2.close();
        String backupUsers = new String(data, "UTF-8");
        if(usersFile.exists()){
            usersFile.delete();
        }

        String expectedUsers = "123&&&Captain America\n456&&&Iron Man\n789&&&Spider Man\n";

        assertEquals("The resulting file content must match with the expected one: '" + expected + "'",
                expected,backupUsers);
    }

    @Test
    public void test_loadBackup() throws Exception{
        String dataFilePath = "src/test/resources/backup-data.dat";
        String usersFilePath = "src/test/resources/backup-users.dat";
        UfromBot bot = new UfromBot("token", dataFilePath, usersFilePath);

        String result = bot.print();

        assertEquals("The result is not correct"," -- VIGO -- \nCaptain America\nIron Man\n\n" +
                " -- GRANADA -- \nSpider Man\n\n",result);
    }
}
