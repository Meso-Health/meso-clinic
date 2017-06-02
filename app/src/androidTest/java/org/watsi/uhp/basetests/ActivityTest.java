package org.watsi.uhp.basetests;

import android.accounts.AccountManager;
import android.content.Context;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.managers.PreferencesManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.AuthenticationToken;
import org.watsi.uhp.models.User;

import java.sql.SQLException;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class ActivityTest {
    @BeforeClass
    public static void setUp() {
        Context context = getInstrumentation().getTargetContext();

        login(context);
        DatabaseHelper.init(context);
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        Context context = getInstrumentation().getTargetContext();

        new PreferencesManager(context).clearUsername();
    }

    private static void login(Context context) {
        User u = new User();
        u.setId(1);
        u.setUsername("klinik");
        u.setName("test_name");
        u.setRole("provider");

        AuthenticationToken authToken = new AuthenticationToken();
        authToken.setToken("test_token");
        authToken.setExpiresAt("never");
        authToken.setUser(u);

        new SessionManager(new PreferencesManager(context), AccountManager.get(context))
                .setUserAsLoggedIn(authToken.getUser(), authToken.getToken());
    }

    protected static void waitForUIToUpdate(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception ignored) {
        }
    }
}
