package ppp.db.model;

import ppp.db.AbstractModel;

import java.sql.Timestamp;

public class OUser extends AbstractModel {
    public int id = 0;
    public Timestamp signUp = null;
    public Timestamp lastSignIn = null;
}
