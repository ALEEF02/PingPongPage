package ppp.meta;

import ppp.meta.LoginEnum.Status;

/**
 * Contains useful enums for the login process
 */
public final class LoginEnum {
	
	public static enum Status {
		
		AUTH_NOT_SENT(400),
		AUTH_TOO_LATE(401, "Wait a few minutes..."),
		TOO_MANY_ATTEMPTS(429, "Wait a few minutes until you can get a new code"),
		AUTH_ALREADY_SENT(429, "Check your email! If you're recieving this after 'TOO_MANY_ATTEMPTS' or 'AUTH_TOO_LATE', you need to wait 10 minutes before you can get a new code."),
		AUTH_INVALID(401, "Double-check your code"),
		
		EMAIL_INVALID(400, "Double-check the spelling of your Steven's email"),
	    TOKEN_INVALID(500, "Huh"),
	    TOKEN_EXPIRED(401, "It's been 7 days. Re-login."),
	    USER_INVALID(500, "Trying to login w/ email & token, but such a user doesn't exist to even have a token yet!"),
	    BANNED(401, "omegalol"),
	    
	    UNKNOWN_ERROR(500),
	    EMAILING_ERROR(503, "The emailer is having issues. Please contact PPP administrators."),
	    
	    EMAIL_SENT(200),
	    SUCCESS(200);
	
	    private final int type;
	    private final String extra;
		
	    Status(int type)
		{
	    	this(type, "");
		}
	    
	    Status(int type, String extra)
		{
			this.type = type;
			this.extra = extra;
		}
	    
	    /**
	     * Get the HTTP Status code that can be used with the status message
	     * @return A 3-byte integer
	     */
	    public int getCode()
	    {
	        return type;
	    }
	    
	    /**
	     * Get a user-readable message on what the status code means
	     * @return String describing the status
	     */
	    public String getMsg()
	    {
	    	if (extra.equalsIgnoreCase("")) return this.toString();
	        return this.toString() + " - " + extra;
	    }
	
	    /**
	     * Get the appropriate Status for a specified HTTP code. This probably has no use, but it's here anyways :D
	     * @param type The HTTP code
	     * @return The status, UNKNOWN_ERROR if it doesn't exist
	     */
	    public static Status fromNum(int type)
	    {
	        for (Status statusType : values())
	        {
	            if (statusType.type == type)
	            {
	                return statusType;
	            }
	        }
	        return UNKNOWN_ERROR;
	    }
	    
	    /**
	     * Get the appropriate Status for a specified Name. Useful for retrieving statuses to the DB (but still useless bc we don't do that)
	     * @param key The Status name
	     * @return The status, UNKNOWN_ERROR if it doesn't exist
	     */
	    public static Status fromString(String key)
	    {
	        for (Status status : values())
	        {
	            if (status.toString().equalsIgnoreCase(key))
	            {
	                return status;
	            }
	        }
	        return UNKNOWN_ERROR;
	    }
	}
}