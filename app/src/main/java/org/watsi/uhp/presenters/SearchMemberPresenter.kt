package org.watsi.uhp.presenters

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.database.MemberDao
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.models.IdentificationEvent
import org.watsi.uhp.models.Member

class SearchMemberPresenter(
        private val mProgressDialog: ProgressDialog,
        private val mListView: ListView,
        private val mEmptyView: TextView,
        private val mSearchView: SearchView,
        private val mContext: Context,
        private val mNavigationManager: NavigationManager) {

    fun String.containsDigit(): Boolean {
        return this.matches(Regex(".*\\d+.*"))
    }

    fun setupSearchListeners() {
        // necessary to automatically show the search keyboard when requestFocus() is called
        mSearchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // for SearchViews, in order to properly show the search keyboard, we need to
                // use findFocus() to grab and pass a view *inside* of the SearchView
                KeyboardManager.showKeyboard(view.findFocus(), mContext)
            }
        }

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String) = true

            override fun onQueryTextSubmit(query: String): Boolean {
                SearchMembersTask().execute(query)
                KeyboardManager.hideKeyboard(mSearchView, mContext)
                return true
            }
        })
    }

    fun startSpinner() {
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage("Searching...")
        mProgressDialog.show()
    }

    internal fun performQuery(query: String): Pair<IdentificationEvent.SearchMethodEnum, List<Member>> {
        val idMethod: IdentificationEvent.SearchMethodEnum
        val matchingMembers: List<Member>

        if (query.containsDigit()) {
            matchingMembers = MemberDao.withCardIdLike(query)
            idMethod = IdentificationEvent.SearchMethodEnum.SEARCH_ID
        } else {
            matchingMembers = MemberDao.fuzzySearchMembers(query, 20, 60)
            idMethod = IdentificationEvent.SearchMethodEnum.SEARCH_NAME
        }
        return Pair(idMethod, matchingMembers)
    }

    fun displayMembersResult(searchMethod: IdentificationEvent.SearchMethodEnum, members: List<Member>) {
        val adapter = MemberAdapter(mContext, members, false)
        mListView.adapter = adapter
        mListView.emptyView = mEmptyView
        mListView.setOnItemClickListener { parent, _, position, _ ->
            val member = parent.getItemAtPosition(position) as Member
            mNavigationManager.setMemberDetailFragment(member, searchMethod, null)
        }
        mProgressDialog.dismiss()
        mListView.requestFocus()
    }

    fun focus() {
        mSearchView.requestFocus()
    }

    inner class SearchMembersTask : AsyncTask<String, Void, Pair<IdentificationEvent.SearchMethodEnum, List<Member>>>() {

        override fun onPreExecute() {
            this@SearchMemberPresenter.startSpinner()
        }

        override fun doInBackground(vararg params: String):
                Pair<IdentificationEvent.SearchMethodEnum, List<Member>> {
            return this@SearchMemberPresenter.performQuery(params[0])
        }

        override fun onPostExecute(pair: Pair<IdentificationEvent.SearchMethodEnum, List<Member>>) {
            this@SearchMemberPresenter.displayMembersResult(pair.first, pair.second)
        }
    }
}

