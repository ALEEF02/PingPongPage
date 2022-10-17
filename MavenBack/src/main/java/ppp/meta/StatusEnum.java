package ppp.meta;

public final class StatusEnum {
	
	public static enum Status {
		REJECTED(0),
	    PENDING(1),
	    ACCEPTED(2),
	    ANY(-1);
	
	    private final int type;
		
	    Status(int type)
		{
			this.type = type;
		}
	    
	    public int getNum()
	    {
	        return type;
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
	        return ANY;
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
	        return ANY;
	    }
	}
}