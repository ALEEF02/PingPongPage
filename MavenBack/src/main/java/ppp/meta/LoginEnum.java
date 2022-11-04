package ppp.meta;

import ppp.meta.LoginEnum.Status;

public final class LoginEnum {
	
	public static enum Status {
		
		AUTH_NOT_SENT(-21),
		AUTH_TOO_LATE(-22, "Wait a few minutes..."),
		TOO_MANY_ATTEMPTS(-23, "Wait a few minutes..."),
		AUTH_ALREADY_SENT(-24, "Check your email!"),
		AUTH_INVALID(-25, "Double-check your code"),
		
		EMAIL_INVALID(-1),
	    TOKEN_INVALID(-2),
	    TOKEN_EXPIRED(-3),
	    USER_INVALID(-2),
	    BANNED(-10),
	    
	    UNKNOWN_ERROR(-99),
	    
	    SUCCESS(1);
	
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
	    
	    public int getNum()
	    {
	        return type;
	    }
	    
	    public String getMsg()
	    {
	    	if (extra.equalsIgnoreCase("")) return this.toString();
	        return extra;
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