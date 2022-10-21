package ppp.db.model;

import ppp.db.AbstractModel;

import java.sql.Timestamp;

public class OUser extends AbstractModel {
    public int id = 0;
    public String username = "";
    public String email = "";
    public String token = "";
    public Timestamp tokenExpiryDate = null;
    public int elo = 1400;
    public Timestamp signUpDate = null;
    public Timestamp lastSignIn = null;
    public boolean banned = false;
    public int rank = -1;
    
    public void generateToken() {
    	
    }
}
