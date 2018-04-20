package org.watsi.uhp.presenters

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager

class SearchMemberPresenter(
        private val mProgressDialog: ProgressDialog,
        private val mListView: ListView,
        private val mEmptyView: TextView,
        private val mSearchView: SearchView,
        private val mContext: Context,
        private val mNavigationManager: NavigationManager,
        private val memberRepository: MemberRepository,
        private val identificationEventRepository: IdentificationEventRepository) {

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

    internal fun performQuery(query: String): Pair<IdentificationEvent.SearchMethod, List<Member>> {
        val idMethod: IdentificationEvent.SearchMethod
        val matchingMembers: List<Member>

        if (query.matches(Regex(".*\\d+.*"))) {
            matchingMembers = memberRepository.fuzzySearchByCardId(query)
            idMethod = IdentificationEvent.SearchMethod.SEARCH_ID
        } else {
            matchingMembers = memberRepository.fuzzySearchByName(query)
            idMethod = IdentificationEvent.SearchMethod.SEARCH_NAME
        }
        return Pair(idMethod, matchingMembers)
    }

    fun displayMembersResult(searchMethod: IdentificationEvent.SearchMethod, members: List<Member>) {
        val adapter = MemberAdapter(mContext, members, false, identificationEventRepository)
        mListView.adapter = adapter
        mListView.emptyView = mEmptyView
        mListView.setOnItemClickListener { parent, _, position, _ ->
            val member = parent.getItemAtPosition(position) as Member
            mNavigationManager.setMemberDetailFragment(member, IdentificationEvent(member, searchMethod, null))
        }
        mProgressDialog.dismiss()
        mListView.requestFocus()
    }

    fun focus() {
        mSearchView.requestFocus()
    }

    inner class SearchMembersTask : AsyncTask<String, Void, Pair<IdentificationEvent.SearchMethod, List<Member>>>() {

        override fun onPreExecute() {
            this@SearchMemberPresenter.startSpinner()
        }

        override fun doInBackground(vararg params: String):
                Pair<IdentificationEvent.SearchMethod, List<Member>> {
            return this@SearchMemberPresenter.performQuery(params[0])
        }

        override fun onPostExecute(pair: Pair<IdentificationEvent.SearchMethod, List<Member>>) {
            this@SearchMemberPresenter.displayMembersResult(pair.first, pair.second)
        }
    }
}

