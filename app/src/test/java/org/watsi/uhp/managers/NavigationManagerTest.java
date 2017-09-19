package org.watsi.uhp.managers;

import android.support.v4.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.BaseFragment;

import static junit.framework.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class NavigationManagerTest {

    private ClinicActivity mFragmentActivity;
    private NavigationManager mNavigationManager;

    @Before
    public void setUp() throws Exception {
        mFragmentActivity = Robolectric.buildActivity(ClinicActivity.class)
                .create().start().resume().get();
        mNavigationManager = mFragmentActivity.getNavigationManager();
    }


    @Test
    public void setUpSuccess() throws Exception {
        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        assertEquals(fm.getBackStackEntryCount(), 0);
    }

    @Test
    public void setCurrentPatientsFragment_success() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));

        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        assertEquals(fm.getBackStackEntryCount(), 1);
        assertEquals(fm.getBackStackEntryAt(0).getName(), "addFragmentA");
    }

    @Test
    public void setFragment_manyFragments() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mNavigationManager.setFragment(new TestFragment("FragmentD"));
        mNavigationManager.setFragment(new TestFragment("FragmentE"));


        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        assertEquals(fm.getBackStackEntryCount(), 5);
        assertEquals(fm.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(fm.getBackStackEntryAt(1).getName(), "addFragmentB");
        assertEquals(fm.getBackStackEntryAt(2).getName(), "addFragmentC");
        assertEquals(fm.getBackStackEntryAt(3).getName(), "addFragmentD");
        assertEquals(fm.getBackStackEntryAt(4).getName(), "addFragmentE");
    }

    @Test
    public void setFragment_twoDifferentFragmentsThenBackToSameFragment() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mNavigationManager.setFragment(new TestFragment("FragmentA"));

        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        assertEquals(fm.getBackStackEntryCount(), 1);
        assertEquals(fm.getBackStackEntryAt(0).getName(), "addFragmentA");
    }

    @Test
    public void setFragment_repeatedConsecutiveTransitions() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));

        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        assertEquals(fm.getBackStackEntryCount(), 3);
        assertEquals(fm.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(fm.getBackStackEntryAt(1).getName(), "addFragmentB");
        assertEquals(fm.getBackStackEntryAt(2).getName(), "addFragmentC");
    }

    @Test
    public void setFragment_overideFragmentToPop() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mNavigationManager.setFragment(new TestFragment("FragmentD"), "FragmentB");

        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        assertEquals(fm.getBackStackEntryCount(), 2);
        assertEquals(fm.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(fm.getBackStackEntryAt(1).getName(), "addFragmentD");
    }

    @Test
    public void setFragment_backPressActivity() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mFragmentActivity.onBackPressed();

        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        assertEquals(fm.getBackStackEntryCount(), 0);
    }

    @Test
    public void setFragment_withBackPress() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mFragmentActivity.onBackPressed();

        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        assertEquals(fm.getBackStackEntryCount(), 1);
        assertEquals(fm.getBackStackEntryAt(0).getName(), "addFragmentA");
    }

    @Test
    public void setFragment_backPressWithPopping() throws Exception {
        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();

        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mNavigationManager.setFragment(new TestFragment("FragmentD"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mFragmentActivity.onBackPressed();

        assertEquals(fm.getBackStackEntryCount(), 2);
        assertEquals(fm.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(fm.getBackStackEntryAt(1).getName(), "addFragmentB");
    }

    @Test
    public void formatUniqueFragmentTransition() throws Exception {
        assertEquals(mNavigationManager.formatUniqueFragmentTransition(null, "nextFragmentName"), "->nextFragmentName");
        assertEquals(mNavigationManager.formatUniqueFragmentTransition(new TestFragment("firstFragmentName"), "nextFragmentName"), "firstFragmentName->nextFragmentName");
    }

    public static class TestFragment extends BaseFragment {
        String mName;

        public TestFragment(String fragmentName) {
            mName = fragmentName;
        }

        public String getName() {
            return mName;
        }
    }
}
