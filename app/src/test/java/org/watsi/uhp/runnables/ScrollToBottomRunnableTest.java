package org.watsi.uhp.runnables;

import android.widget.ListView;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScrollToBottomRunnable.class)
public class ScrollToBottomRunnableTest {

    private Runnable scrollToBottomRunnable;

    @Mock
    ListView listView;

    @Before
    public void setup() {
        scrollToBottomRunnable = new ScrollToBottomRunnable(listView);
    }

//    public void run() throws Exception {
//        when(listView.getAdapter().getCount()).thenReturn(4);
//
//        assertEquals(scrollToBottomRunnable.run(), listView.setSelection(3));
//    }


}
