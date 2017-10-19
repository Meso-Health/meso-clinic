package org.watsi.uhp.presenters

import android.app.ProgressDialog
import android.content.Context
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.database.MemberDao
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.models.IdentificationEvent
import org.watsi.uhp.models.Member

@RunWith(PowerMockRunner::class)
@PrepareForTest(MemberDao::class)
class SearchMemberPresenterTest {

    @Mock lateinit var mockProgressDialog: ProgressDialog
    @Mock lateinit var mockListView: ListView
    @Mock lateinit var mockEmptyView: TextView
    @Mock lateinit var mockSearchView: SearchView
    @Mock lateinit var mockContext: Context
    @Mock lateinit var mockNavigationManager: NavigationManager
    @Mock lateinit var mockMemberList: List<Member>

    lateinit var presenter: SearchMemberPresenter

    @Before
    fun setup() {
        mockStatic(MemberDao::class.java)
        presenter = SearchMemberPresenter(
                mockProgressDialog, mockListView, mockEmptyView,
                mockSearchView, mockContext, mockNavigationManager)
    }

    @Test
    fun startSpinner() {
        presenter.startSpinner()

        verify(mockProgressDialog, times(1)).setCancelable(false)
        verify(mockProgressDialog, times(1)).setMessage("Searching...")
        verify(mockProgressDialog, times(1)).show()
    }

    @Test
    fun performQuery_queryContainsDigit() {
        val query = "123"

        `when`(MemberDao.withCardIdLike(query)).thenReturn(mockMemberList)

        val result = presenter.performQuery(query)

        assertEquals(result.first, IdentificationEvent.SearchMethodEnum.SEARCH_ID)
        assertEquals(result.second, mockMemberList)
    }

    @Test
    fun performQuery_queryContainsOnlyLetters() {
        val query = "Foo"

        `when`(MemberDao.fuzzySearchMembers(query)).thenReturn(mockMemberList)

        val result = presenter.performQuery(query)

        assertEquals(result.first, IdentificationEvent.SearchMethodEnum.SEARCH_NAME)
        assertEquals(result.second, mockMemberList)
    }

    @Test
    fun displayMembersResult() {
        presenter.displayMembersResult(IdentificationEvent.SearchMethodEnum.SEARCH_ID, mockMemberList)

        verify(mockListView, times(1)).setEmptyView(mockEmptyView)
        verify(mockListView, times(1)).setAdapter(any(MemberAdapter::class.java))
        verify(mockListView, times(1)).setOnItemClickListener(any(AdapterView.OnItemClickListener::class.java))
        verify(mockProgressDialog, times(1)).dismiss()
        verify(mockListView, times(1)).requestFocus()
    }

    @Test
    fun focus() {
        presenter.focus()

        verify(mockSearchView, times(1)).requestFocus()
    }
}
