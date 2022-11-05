package ppp.meta;

import ppp.meta.LoginEnum.Status;

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
	    
	    public int getCode()
	    {
	        return type;
	    }
	    
	    public String getMsg()
	    {
	    	if (extra.equalsIgnoreCase("")) return this.toString();
	        return this.toString() + " - " + extra;
	    }
	
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