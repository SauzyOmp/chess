package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;

public class UserService {
    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        try {
            dao.getUser(req.username());
            throw new DataAccessException("username already taken");
        } catch (DataAccessException e) {
            if (!e.getMessage().contains("not found")) throw e;
        }

        UserData newUser = new UserData(req.username(), req.password(), req.email());
        dao.createUser(newUser);
        AuthData auth = dao.createAuth(req.username());
        return new RegisterResult(auth.username(), auth.authToken());
    }

    public LoginResult login(LoginRequest req) throws DataAccessException {
        if (req.username() == null || req.password() == null) {
            throw new DataAccessException("bad request");
        }

        UserData stored = dao.getUser(req.username());

        if (!stored.password().equals(req.password())) {
            throw new DataAccessException("username/password incorrect");
        }

        AuthData auth = dao.createAuth(req.username());
        return new LoginResult(auth.username(), auth.authToken());
    }

    public void logout(String authToken) throws DataAccessException {
        dao.getAuth(authToken);
        dao.deleteAuth(authToken);
    }
}
