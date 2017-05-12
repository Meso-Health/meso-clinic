package org.watsi.uhp.models;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.BuildConfig;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Response.class)
public class SyncableModelTest {

    @Mock
    Context mockContext;
    @Mock
    Call<Member> mockCall;
    @Mock
    Map<String, RequestBody> mockParams;
    @Mock
    Response<Member> mockResponse;

    // SyncableModel is an abstract class so testing with an implementation
    private Member member = new Member();
    private Member memberSpy = spy(member);
    private UUID id = UUID.randomUUID();
    private String token = "foo";

    @Mock
    Dao<Member, UUID> mockDao;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        doReturn(mockDao).when(memberSpy).getDao();
    }

    @Test
    public void isSynced_isNew_returnsFalse() throws Exception {
        when(memberSpy.isNew()).thenReturn(true);

        assertFalse(memberSpy.isSynced());
    }

    @Test
    public void isSynced_isNotNewHasDirtyFields_returnsFalse() throws Exception {
        Set<String> fields = new HashSet<>();
        fields.add("foo");

        when(memberSpy.getDirtyFields()).thenReturn(fields);
        when(memberSpy.isNew()).thenReturn(false);

        assertFalse(memberSpy.isSynced());
    }

    @Test
    public void isSynced_isNotNewHasNoDirtyFields_returnsTrue() throws Exception {
        when(memberSpy.getDirtyFields()).thenReturn(new HashSet<>());
        when(memberSpy.isNew()).thenReturn(false);

        assertTrue(memberSpy.isSynced());
    }

    @Test
    public void isNew_idIsNull_returnsTrue() throws Exception {
        member.setId(null);

        assertTrue(member.isNew());
    }

    @Test
    public void isNew_idPresentAndDirty_returnsTrue() throws Exception {
        when(memberSpy.dirty(Member.FIELD_NAME_ID)).thenReturn(true);

        assertTrue(member.isNew());
    }

    @Test
    public void isNew_idPresentAndNotDirtyDoesNotExistInDao_returnsTrue() throws Exception {
        memberSpy.setId(id);

        when(memberSpy.dirty(Member.FIELD_NAME_ID)).thenReturn(false);
        when(mockDao.idExists(id)).thenReturn(false);

        assertTrue(memberSpy.isNew());
    }

    @Test
    public void isNew_idPresentAndNotDirtyAndExistsInDao_returnsFalse() throws Exception {
        memberSpy.setId(id);

        when(memberSpy.dirty(Member.FIELD_NAME_ID)).thenReturn(false);
        when(mockDao.idExists(id)).thenReturn(true);

        assertFalse(memberSpy.isNew());
    }

    @Test
    public void changedFields_newModel_callsDiffFieldsWithNull() throws Exception {
        Set<String> changeSet = new HashSet<>();

        doReturn(true).when(memberSpy).isNew();
        doReturn(changeSet).when(memberSpy).diffFields(null);

        Set<String> result = memberSpy.changedFields();

        assertEquals(changeSet, result);
        verify(memberSpy, times(1)).diffFields(null);
    }

    @Test
    public void changedFields_notNewModel_callsDiffFieldsWithThePersistedModel() throws Exception {
        Set<String> changeSet = new HashSet<>();
        Member mockPersistedMember = mock(Member.class);
        memberSpy.setId(id);

        doReturn(false).when(memberSpy).isNew();
        when(mockDao.queryForId(id)).thenReturn(mockPersistedMember);
        doReturn(changeSet).when(memberSpy).diffFields(mockPersistedMember);

        Set<String> result = memberSpy.changedFields();

        assertEquals(changeSet, result);
        verify(memberSpy, times(1)).diffFields(mockPersistedMember);
    }

    @Test
    public void diffFields_refModelIsNull_returnsAllFieldsSetOnModel() throws Exception {
        member.setId(id);
        member.setAge(15);
        member.setFullName("Foo");

        Set<String> result = member.diffFields(null);

        assertEquals(result.toString(), result.size(), 3);
        assertTrue(result.contains("id"));
        assertTrue(result.contains("age"));
        assertTrue(result.contains("full_name"));
    }

    @Test
    public void diffFields_refModelIsNotNull_returnsAllDuffFields() throws Exception {
        member.setId(id);
        member.setAge(15);
        member.setGender(Member.GenderEnum.M);
        member.setFullName("Foo");
        Member refMember = new Member();
        refMember.setId(id);
        refMember.setGender(Member.GenderEnum.F);
        refMember.setCardId("RWI123456");

        Set<String> result = member.diffFields(refMember);

        assertEquals(result.toString(), result.size(), 4);
        assertTrue(result.contains("age"));
        assertTrue(result.contains("full_name"));
        assertTrue(result.contains("card_id"));
        assertTrue(result.contains("gender"));
    }

    @Test
    public void saveChanges_nullToken_throwsUnauthenticatedException() throws Exception {
        try {
            member.saveChanges(null);
            fail("Should throw unauthenticated exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Current user is not authenticated");
        }
    }

    @Test
    public void saveChanges_withToken_setsToken() throws Exception {
        doReturn(new HashSet<>()).when(memberSpy).changedFields();

        memberSpy.saveChanges(token);

        assertEquals(memberSpy.getToken(), token);
    }

    @Test
    public void saveChanges_withTokenNullId_setsId() throws Exception {
        memberSpy.setId(null);

        doReturn(new HashSet<>()).when(memberSpy).changedFields();

        memberSpy.saveChanges(token);

        assertNotNull(memberSpy.getId());
    }

    @Test
    public void saveChanges_withToken_setsDirtyFieldsToChangedFields() throws Exception {
        Set<String> changedFields = new HashSet<>();

        doReturn(changedFields).when(memberSpy).changedFields();

        memberSpy.saveChanges(token);

        assertEquals(memberSpy.getDirtyFields(), changedFields);
    }

    @Test
    public void saveChanges_withToken_persistsTheModel() throws Exception {
        Set<String> changedFields = new HashSet<>();

        doReturn(changedFields).when(memberSpy).changedFields();

        memberSpy.saveChanges(token);

        verify(mockDao, times(1)).createOrUpdate(memberSpy);
    }

    @Test
    public void saveChanges_withToken_persistsAssociations() throws Exception {
        Set<String> changedFields = new HashSet<>();

        doReturn(changedFields).when(memberSpy).changedFields();

        memberSpy.saveChanges(token);

        verify(memberSpy, times(1)).persistAssociations();
    }

    @Test
    public void update_callsUpdateFromApiResponse() throws Exception {
        memberSpy.updateFromSync();

        verify(memberSpy, times(1)).handleUpdate();
    }

    @Test
    public void updateFromSync_clearsDirtyFields() throws Exception {
        memberSpy.updateFromSync();

        verify(memberSpy, times(1)).clearDirtyFields();
        assertTrue(memberSpy.getDirtyFields().size() == 0);
    }

    @Test
    public void updateFromSync_persistsTheModel() throws Exception {
        memberSpy.updateFromSync();

        verify(mockDao, times(1)).createOrUpdate(memberSpy);
    }

    @Test
    public void sync_nullToken_throwsUnauthenticatedException() throws Exception {
        member.setToken(null);
        try {
            member.sync(mockContext);
            fail("Should throw unauthenticated exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Current user is not authenticated");
        }
    }

    @Test
    public void sync_isNotDirty_throwsSyncException() throws Exception {
        memberSpy.setToken(token);

        when(memberSpy.isDirty()).thenReturn(false);

        try {
            memberSpy.sync(mockContext);
            fail("Should throw unauthenticated exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Model does not have any fields that need to be synced");
        }
    }

    @Test
    public void sync_isNew_returnsTheResultOfPostRequest() throws Exception {
        memberSpy.setToken(token);

        when(memberSpy.isDirty()).thenReturn(true);
        doReturn(true).when(memberSpy).isNew();
        when(memberSpy.postApiCall(
                mockContext, memberSpy.getToken(), BuildConfig.PROVIDER_ID, memberSpy))
                .thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        Response<Member> response = memberSpy.sync(mockContext);

        assertEquals(response, mockResponse);
    }

    @Test
    public void sync_isNotNew_returnsTheResultOfPatchRequest() throws Exception {
        memberSpy.setId(id);
        memberSpy.setToken(token);

        when(memberSpy.isDirty()).thenReturn(true);
        doReturn(false).when(memberSpy).isNew();
        when(memberSpy.patchRequestBody(mockContext)).thenReturn(mockParams);
        when(memberSpy.patchApiCall(mockContext, memberSpy.getToken(), id, mockParams))
                .thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        Response<Member> response = memberSpy.sync(mockContext);

        assertEquals(response, mockResponse);
    }
}
