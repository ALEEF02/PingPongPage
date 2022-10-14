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
	        for (Status ticketType : values())
	        {
	            if (ticketType.type == type)
	            {
	                return ticketType;
	            }
	        }
	        return ANY;
	    }
	}
}