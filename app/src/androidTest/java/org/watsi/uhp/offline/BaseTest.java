package org.watsi.uhp.offline;

import android.accounts.AccountManager;
import android.content.Context;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.watsi.uhp.BillableFactory;
import org.watsi.uhp.MemberFactory;
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

class BaseTest {

    @BeforeClass
    public static void setUp() throws SQLException, AbstractModel.ValidationException {
        Context context = getInstrumentation().getTargetContext();

        DatabaseHelper.init(context);
        loadFixtures();

        login(context);
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        Context context = getInstrumentation().getTargetContext();

        logout(context);
        DatabaseHelper.getHelper().clearDatabase();
    }

    private static void login(Context context) {
        User u = new User();
        u.setId(1);
        u.setUsername("test");
        u.setName("test_name");
        u.setRole("provider");

        AuthenticationToken authToken = new AuthenticationToken();
        authToken.setToken("test_token");
        authToken.setExpiresAt("never");
        authToken.setUser(u);

        new SessionManager(new PreferencesManager(context), AccountManager.get(context))
                .setUserAsLoggedIn(authToken.getUser(), "test_password", authToken.getToken());
    }

    private static void logout(Context context) {
        new PreferencesManager(context).clearUsername();
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
