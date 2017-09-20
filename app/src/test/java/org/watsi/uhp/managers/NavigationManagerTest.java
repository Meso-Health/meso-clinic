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
    private FragmentManager mFragmentManager;

    @Before
    public void setUp() throws Exception {
        mFragmentActivity = Robolectric.buildActivity(ClinicActivity.class)
                .create().start().resume().get();
        mNavigationManager = mFragmentActivity.getNavigationManager();
        mFragmentManager = mFragmentActivity.getSupportFragmentManager();
    }


    @Test
    public void setUpSuccess() throws Exception {
        assertEquals(mFragmentManager.getBackStackEntryCount(), 0);
    }

    @Test
    public void setCurrentPatientsFragment_success() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));

        assertEquals(mFragmentManager.getBackStackEntryCount(), 1);
        assertEquals(mFragmentManager.getBackStackEntryAt(0).getName(), "addFragmentA");
    }

    @Test
    public void setFragment_twoDifferentFragments() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));

        assertEquals(mFragmentManager.getBackStackEntryCount(), 3);
        assertEquals(mFragmentManager.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(1).getName(), "removeFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(2).getName(), "addFragmentB");
    }

    @Test
    public void setFragment_twoDifferentFragmentsThenBackToSameFragment() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mNavigationManager.setFragment(new TestFragment("FragmentA"));

        assertEquals(mFragmentManager.getBackStackEntryCount(), 1);
        assertEquals(mFragmentManager.getBackStackEntryAt(0).getName(), "addFragmentA");
    }

    @Test
    public void setFragment_repeatedConsecutiveTransitions() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));

        assertEquals(mFragmentManager.getBackStackEntryCount(), 3);
        assertEquals(mFragmentManager.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(1).getName(), "removeFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(2).getName(), "addFragmentB");
    }

    @Test
    public void setFragment_overrideFragmentToPop_inclusive() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mNavigationManager.setFragment(new TestFragment("FragmentD"), "FragmentB");

        assertEquals(mFragmentManager.getBackStackEntryCount(), 3);
        assertEquals(mFragmentManager.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(1).getName(), "removeFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(2).getName(), "addFragmentD");
    }

    @Test
    public void setFragment_overrideFragmentToPop_exclusive() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mNavigationManager.setFragment(new TestFragment("FragmentD"), "FragmentB");

        assertEquals(mFragmentManager.getBackStackEntryCount(), 3);
        assertEquals(mFragmentManager.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(1).getName(), "removeFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(2).getName(), "addFragmentD");
    }

    @Test
    public void setFragment_backPressExitActivity() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mFragmentActivity.onBackPressed();

        assertEquals(mFragmentManager.getBackStackEntryCount(), 0);
    }

    @Test
    public void setFragment_backPressTwoFragments() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mFragmentActivity.onBackPressed();

        assertEquals(mFragmentManager.getBackStackEntryCount(), 1);
        assertEquals(mFragmentManager.getBackStackEntryAt(0).getName(), "addFragmentA");
    }

    @Test
    public void setFragment_backPressAfterPop() throws Exception {
        mNavigationManager.setFragment(new TestFragment("FragmentA"));
        mNavigationManager.setFragment(new TestFragment("FragmentB"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mNavigationManager.setFragment(new TestFragment("FragmentD"));
        mNavigationManager.setFragment(new TestFragment("FragmentC"));
        mFragmentActivity.onBackPressed();

        assertEquals(mFragmentManager.getBackStackEntryCount(), 3);
        assertEquals(mFragmentManager.getBackStackEntryAt(0).getName(), "addFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(1).getName(), "removeFragmentA");
        assertEquals(mFragmentManager.getBackStackEntryAt(2).getName(), "addFragmentB");
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
