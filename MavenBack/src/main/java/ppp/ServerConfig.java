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
    public static String DB_USER = "root";

    //mysql password
    @ConfigurationOption
    public static String DB_PASS = "";

    //mysql database name
    @ConfigurationOption
    public static String DB_NAME = "ppp";

    //mysql database name
    @ConfigurationOption
    public static String OKTA_ISSUER = "";

    //mysql database name
    @ConfigurationOption
    public static String OKTA_CLIENT_ID = "";

    //mysql database name
    @ConfigurationOption
    public static String OKTA_CLIENT_SECRET = "";

    //mysql database name
    @ConfigurationOption
    public static String OKTA_REDIRECT_URI = "";
}
