package ppp.db.controllers;

import ppp.db.UserRepository;
import ppp.db.model.OUser;

public class CUserRepository implements UserRepository {

    @Override
    public OUser findByEmail(String email, boolean withToken) {
        return CUser.findByEmail(email, withToken);
    }

    @Override
    public void update(OUser user) {
        CUser.update(user);
    }
}
