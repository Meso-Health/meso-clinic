package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.User;

import java.sql.SQLException;

/**
 * POJO helper for querying Users
 */
public class UserDao {

    private static UserDao instance = new UserDao();

    private Dao<User, Integer> mUserDao;

    private static synchronized UserDao getInstance() {
        return instance;
    }

    private UserDao() {
    }

    private void setUserDao(Dao userDao) {
        this.mUserDao = userDao;
    }

    private Dao<User, Integer> getUserDao() throws SQLException {
        if (mUserDao == null) {
            setUserDao(DatabaseHelper.getHelper().getDao(User.class));
        }

        return mUserDao;
    }

    public static User findById(int id) throws SQLException {
        return getInstance().getUserDao().queryForId(id);
    }
}
