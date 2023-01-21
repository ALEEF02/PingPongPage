package ppp.meta;

/**
 * Contains useful enums for the status of games
 */
public final class StatusEnum {
	
	public static enum Status {
		REJECTED(0),
	    PENDING(1),
	    ACCEPTED(2),
	    CALCULATED(3),
	    ANY(-1),
	    /**
	     * Accepted or Calculated AKA cannot be changed by the user 
	     */
	    FILED(-2);
	
	    private final int type;
		
	    Status(int type)
		{
			this.type = type;
		}
	    
	    /**
	     * Get a number representing the status which can be easily stored in the DB
	     * @return The number for the status
	     */
	    public int getNum()
	    {
	        return type;
	    }
	
	    /**
	     * Get the appropriate status from its corresponding number. Useful for turning DB numbers back into statuses
	     * @param type The number representation of the status
	     * @return The appropriate status, ANY by default
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
	        return ANY;
	    }
	    
	    /**
	     * Get the appropriate status from its corresponding string.
	     * @param type The string representation of the status
	     * @return The appropriate status, ANY by default
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
	        return ANY;
	    }
	}
}