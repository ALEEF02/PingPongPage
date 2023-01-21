package ppp;

import com.kaaz.configuration.ConfigurationOption;

public class ServerConfig {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36";
    
    /**
     * Should we print extra debug information?
     */
    @ConfigurationOption
    public static boolean BOT_DEBUG = false;

    /**
     * mariaDB/mysql hostname
     */
    @ConfigurationOption
    public static String DB_HOST = "localhost";

    /**
     * mariaDB/mysql port
     */
    @ConfigurationOption
    public static int DB_PORT = 3306;

    /**
     * mariaDB/mysql user
     */
    @ConfigurationOption
    public static String DB_USER = "";

    /**
     * mariaDB/mysql password
     */
    @ConfigurationOption
    public static String DB_PASS = "";

    /**
     * mariaDB/mysql database name
     */
    @ConfigurationOption
    public static String DB_NAME = "ppp";

    /**
     * Num of digits for the authentication code
     */
    @ConfigurationOption
    public static int AUTH_NUM_DIGITS = 8;

    /**
     * Time, in minutes, the authentication code remains valid for
     */
    @ConfigurationOption
    public static int AUTH_CODE_VALIDITY_PERIOD = 5;

    /**
     * Time, in minutes, between requests for authentication codes
     */
    @ConfigurationOption
    public static int AUTH_CODE_DELAY_PERIOD = 10;
    
    /**
     * Time, in days, until a token expires
     */
    @ConfigurationOption
    public static int TOKEN_VALIDITY_PERIOD = 7;
    

    //Emailer host
    @ConfigurationOption
    public static String EMAIL_HOST = "";
    
    /**
     * Emailer user's email address
     */
    @ConfigurationOption
    public static String EMAIL_USER = "";
    
    /**
     * Emailer's password
     */
    @ConfigurationOption
    public static String EMAIL_PASSWORD = "";
}
