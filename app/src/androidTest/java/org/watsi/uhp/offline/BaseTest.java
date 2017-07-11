package org.watsi.uhp.offline;

import android.accounts.AccountManager;
import android.content.Context;
import android.support.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
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

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;
import static org.watsi.uhp.CustomMatchers.withAdaptedData;

class BaseTest {

    final static String TEST_USER_NAME = "test";

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
        u.setUsername(TEST_USER_NAME);
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
                "Lil Jon",
                "RWI000000",
                5,
                Member.GenderEnum.M
        ));

        MemberDao.create(new MemberFactory(
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

        BillableDao.create(new BillableFactory(
                "Sutures",
                Billable.TypeEnum.SUPPLY,
                "1",
                "unit",
                3000
        ));
    }

    void assertItemInList(Matcher<Object> matcher, int listId) {
        onData(matcher).inAdapterView(withId(listId)).check(matches(isDisplayed()));
    }

    void assertItemNotInList(Matcher<Object> matcher, int listId) {
        onView(withId(listId)).check(matches(not(withAdaptedData(matcher))));
    }

    void assertDisplaysToast(ActivityTestRule rule, int messageId) {
        onView(withText(messageId))
                .inRoot(withDecorView(not(rule.getActivity().getWindow()
                        .getDecorView())))
                .check(matches(isDisplayed()));
    }

    void assertDisplaysToast(ActivityTestRule rule, String message) {
        onView(withText(message))
                .inRoot(withDecorView(not(rule.getActivity().getWindow()
                        .getDecorView())))
                .check(matches(isDisplayed()));
    }
}
