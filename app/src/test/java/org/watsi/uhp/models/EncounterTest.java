package org.watsi.uhp.models;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.api.UhpApi;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.EncounterItemDao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiService.class, BillableDao.class, EncounterItemDao.class })
public class EncounterTest {

    @Mock
    Context mockContext;
    @Mock
    UhpApi mockApi;
    @Mock
    Call<Encounter> mockCall;

    private Encounter encounter;

    @Before
    public void setup() {
        mockStatic(ApiService.class);
        mockStatic(BillableDao.class);
        mockStatic(EncounterItemDao.class);
        encounter = new Encounter();
    }

    @Test
    public void postApiCall() throws Exception {
        encounter.setToken("foo");
        Member member = new Member();
        UUID memberId = UUID.randomUUID();
        member.setId(memberId);
        encounter.setMember(member);
        IdentificationEvent event = new IdentificationEvent();
        UUID eventId = UUID.randomUUID();
        event.setId(eventId);
        encounter.setIdentificationEvent(event);
        List<EncounterItem> encounterItemList = new ArrayList<>();
        encounter.setEncounterItems(encounterItemList);

        when(EncounterItemDao.fromEncounter(encounter)).thenReturn(encounterItemList);
        when(ApiService.requestBuilder(mockContext)).thenReturn(mockApi);
        when(mockApi.syncEncounter(anyString(), anyInt(), any(Encounter.class)))
                .thenReturn(mockCall);

        Call result = encounter.postApiCall(mockContext);

        assertEquals(encounter.getMemberId(), memberId);
        assertEquals(encounter.getIdentificationEventId(), eventId);
        assertEquals(encounter.getEncounterItems(), encounterItemList);
        assertEquals(result, mockCall);
        verify(mockApi, times(1)).syncEncounter("Token foo", BuildConfig.PROVIDER_ID, encounter);
    }

    @Test
    public void persistAssociations() throws Exception {
        encounter.setToken("foo");
        EncounterItem encounterItemWithExistingBillable = new EncounterItem();
        Billable existingBillable = mock(Billable.class);
        encounterItemWithExistingBillable.setBillable(existingBillable);
        EncounterItem encounterItemWithNewBillable = new EncounterItem();
        Billable newBillable = mock(Billable.class);
        encounterItemWithNewBillable.setBillable(newBillable);
        List<EncounterItem> encounterItems = new ArrayList<>();
        encounterItems.add(encounterItemWithExistingBillable);
        encounterItems.add(encounterItemWithNewBillable);
        encounter.setEncounterItems(encounterItems);
        EncounterForm form = mock(EncounterForm.class);
        encounter.addEncounterForm(form);

        when(existingBillable.getId()).thenReturn(UUID.randomUUID());

        encounter.persistAssociations();

        assertEquals(encounterItemWithExistingBillable.getEncounter(), encounter);
        verifyStatic();
        EncounterItemDao.create(encounterItemWithExistingBillable);
        assertEquals(encounterItemWithNewBillable.getEncounter(), encounter);
        verify(newBillable, times(1)).generateId();
        verifyStatic();
        BillableDao.create(newBillable);
        verifyStatic();
        EncounterItemDao.create(encounterItemWithNewBillable);
        verify(form, times(1)).saveChanges("foo");
    }

    @Test
    public void addEncounterForm_setsEncounterOnForm() throws Exception {
        EncounterForm form = new EncounterForm();

        encounter.addEncounterForm(form);

        assertEquals(form.getEncounter(), encounter);
    }

    @Test
    public void price_noItems() throws Exception {
        assertEquals(encounter.price(), 0);
    }

    @Test
    public void price_withItems() throws Exception {
        List<EncounterItem> encounterItems = new ArrayList<>();
        EncounterItem ei1 = new EncounterItem();
        Billable b1 = new Billable();
        b1.setPrice(500);
        ei1.setQuantity(2);
        ei1.setBillable(b1);
        encounterItems.add(ei1);

        EncounterItem ei2 = new EncounterItem();
        Billable b2 = new Billable();
        b2.setPrice(1400);
        ei2.setQuantity(4);
        ei2.setBillable(b2);
        encounterItems.add(ei2);

        encounter.setEncounterItems(encounterItems);
        assertEquals(encounter.price(), 6600);
    }
}
