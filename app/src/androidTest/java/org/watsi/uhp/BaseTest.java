package org.watsi.uhp;

import android.accounts.AccountManager;
import android.content.Context;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.PreferencesManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.AuthenticationToken;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.User;

import java.sql.SQLException;
import java.util.UUID;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class BaseTest {

    @BeforeClass
    public static void setUp() throws SQLException, AbstractModel.ValidationException {
        Context context = getInstrumentation().getTargetContext();

        login(context);
        DatabaseHelper.init(context);
        loadFixtures();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        Context context = getInstrumentation().getTargetContext();

        logout(context);
        context.deleteDatabase(DatabaseHelper.getHelper().getDatabaseName());
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

    private static void logout(Context context) {
        new PreferencesManager(context).clearUsername();
    }

    protected static void waitForUIToUpdate(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception ignored) {
        }
    }

    private static void loadFixtures() throws SQLException, AbstractModel.ValidationException {
        MemberDao.create(new MemberFactory(
                UUID.randomUUID(),
                "Lil Jon",
                "RWI000000",
                5,
                Member.GenderEnum.M
        ));

        MemberDao.create(new MemberFactory(
                UUID.randomUUID(),
                "Big Jon",
                "RWI000001",
                50,
                Member.GenderEnum.M
        ));

        BillableDao.create(new BillableFactory(
                "Consultation",
                Billable.TypeEnum.SERVICE,
                2000
        ));

        BillableDao.create(new BillableFactory(
                "Medical Form",
                Billable.TypeEnum.SERVICE,
                1000
        ));

        BillableDao.create(new BillableFactory(
                "Quinine",
                Billable.TypeEnum.DRUG,
                "100ml",
                "syrup",
                5000
        ));

        BillableDao.create(new BillableFactory(
                "Malaria (BS)",
                Billable.TypeEnum.LAB,
                2000
        ));
    }
}
