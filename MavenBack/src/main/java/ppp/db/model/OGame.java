package ppp.db.model;

import ppp.db.AbstractModel;
import ppp.db.controllers.CUser;
import ppp.meta.StatusEnum;
import ppp.meta.StatusEnum.Status;

import java.sql.Timestamp;

public class OGame extends AbstractModel {
    public int id = 0;
    public Timestamp date = null;
    public StatusEnum.Status status = Status.PENDING;
    public int sender = 0;
    public int receiver = 0;
    public int winner = 0;
    public int winnerScore = 0;
    public int loserScore = 0;
    
    private OUser senderUser = new OUser();
    private OUser receiverUser = new OUser();
    
    public String toJSON() {
    	senderUser = CUser.findById(sender, false);
    	receiverUser = CUser.findById(receiver, false);
    	return "{\"id\":\"" + id + 
    			"\",\"date\":\"" + date.toString() + 
    			"\",\"status\":\"" + status + 
    			"\",\"sender\":\"" + sender + 
    			"\",\"receiver\":\"" + receiver + 
    			"\",\"senderName\":\"" + senderUser.username + 
    			"\",\"receiverName\":\"" + receiverUser.username + 
    			"\",\"winner\":\"" + winner + 
    			"\",\"winnerScore\":\"" + winnerScore + 
    			"\",\"loserScore\":\"" + loserScore +
    			"\"}";
    }
}
