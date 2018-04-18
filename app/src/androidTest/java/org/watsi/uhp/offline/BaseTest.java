package org.watsi.uhp.offline;

import android.accounts.AccountManager;
import android.content.Context;
import android.support.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.watsi.domain.entities.AuthenticationToken;
import org.watsi.domain.entities.User;
import org.watsi.uhp.BillableFactory;
import org.watsi.uhp.DiagnosisFactory;
import org.watsi.uhp.MemberFactory;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.managers.PreferencesManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
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
        User u = new User(1, TEST_USER_NAME, "test_name", User.Role.PROVIDER, 1);

        AuthenticationToken authToken = new AuthenticationToken("test_token", "never", u);

        new SessionManager(new PreferencesManager(context), AccountManager.get(context))
                .setUserAsLoggedIn(authToken.getUser(), "test_password", authToken.getToken());
    }

    private static void logout(Context context) {
        new PreferencesManager(context).clearUsername();
    }

    private static void loadFixtures() throws SQLException, AbstractModel.ValidationException {
        MemberFactory.createMember("Lil Jon", "RWI000000", 5, Member.GenderEnum.M);
        MemberFactory.createMember("Big Jon", "RWI000001", 50, Member.GenderEnum.M);
        BillableFactory.createBillable("Consultation", Billable.TypeEnum.SERVICE, 2000, false);
        BillableFactory.createBillable("Medical Form", Billable.TypeEnum.SERVICE, 1000, false);
        BillableFactory.createBillable("Quinine", Billable.TypeEnum.DRUG, "100ml", "syrup", 5000, false);
        BillableFactory.createBillable("Malaria (BS)", Billable.TypeEnum.LAB, 2000, true);
        BillableFactory.createBillable("CD4", Billable.TypeEnum.LAB, 1500, false);
        BillableFactory.createBillable("Sutures", Billable.TypeEnum.SUPPLY, "1", "unit", 3000, false);
        DiagnosisFactory.createDiagnosis(1, "Severe Malaria", "mal s mal smal s. mal");
        DiagnosisFactory.createDiagnosis(2, "Urinary Tract Infection", "UTI");
        DiagnosisFactory.createDiagnosis(3, "Upper respiratory tract infection", "URTI");
        DiagnosisFactory.createDiagnosis(4, "Cushing's syndrome", null);
        DiagnosisFactory.createDiagnosis(5, "Severe Malaria in Pregnancy", "mal s mal smal s. mal");
        DiagnosisFactory.createDiagnosis(6, "Malaria in Pregnancy", "MAL");
        DiagnosisFactory.createDiagnosis(7, "Cough", null);
        DiagnosisFactory.createDiagnosis(9, "Runners itch", null);
        DiagnosisFactory.createDiagnosis(11, "SomediagnosiswithMALinit", null);
        DiagnosisFactory.createDiagnosis(12, "Utirenary", null);
    }

    void assertItemInList(Matcher<Object> matcher, int listId) {
        onView(withId(listId)).check(matches(withAdaptedData(matcher)));
    }

    void assertItemNotInList(Matcher<Object> matcher, int listId) {
        onView(withId(listId)).check(matches(not(withAdaptedData(matcher))));
    }

    void clickItemInList(Matcher<Object> matcher, int listId) {
        onData(matcher).inAdapterView(withId(listId)).perform(click());
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
