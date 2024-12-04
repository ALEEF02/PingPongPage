package ppp.db;

import ppp.db.model.OUser;

public interface UserRepository {
	OUser findByEmail(String email, boolean withToken);
	void update(OUser user);
}
