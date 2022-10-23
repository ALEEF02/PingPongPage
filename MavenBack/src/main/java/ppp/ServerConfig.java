package ppp;

import com.kaaz.configuration.ConfigurationOption;

public class ServerConfig {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36";
    
    @ConfigurationOption
    public static boolean BOT_DEBUG = false;

    //mysql hostname
    @ConfigurationOption
    public static String DB_HOST = "localhost";

    //mysql port
    @ConfigurationOption
    public static int DB_PORT = 3306;

    //mysql user
    @ConfigurationOption
    public static String DB_USER = "";

    //mysql password
    @ConfigurationOption
    public static String DB_PASS = "";

    //mysql database name
    @ConfigurationOption
    public static String DB_NAME = "ppp";

    //Num of digits for the authentication code
    @ConfigurationOption
    public static int AUTH_NUM_DIGITS = 8;

    //Emailer host
    @ConfigurationOption
    public static String EMAIL_HOST = "";
    
    //Emailer host
    @ConfigurationOption
    public static String EMAIL_USER = "";
    
    //Emailer host
    @ConfigurationOption
    public static String EMAIL_PASSWORD = "";
}
