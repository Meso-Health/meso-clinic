package org.watsi.uhp.managers;

import com.rollbar.android.Rollbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Rollbar.class, Request.class})
public class ExceptionManagerTest {

    private Request request;
    private Response response;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Rollbar.class);
        request = new Request.Builder()
                .url("http://uhp-test.watsi.org")
                .post(mock(RequestBody.class))
                .build();
        response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_2)
                .code(400)
                .build();
    }

    @Test
    public void requestFailure_withResponse() throws Exception {
        ExceptionManager.requestFailure("foo", request, response);

        verifyStatic(times(1));
        Rollbar.reportMessage(anyString(), anyString(), anyMap());
    }

    @Test
    public void requestFailure_noResponse() throws Exception {
        ExceptionManager.requestFailure("foo", request, null);

        verifyStatic(times(1));
        Rollbar.reportMessage(anyString(), anyString(), anyMap());
    }
}
