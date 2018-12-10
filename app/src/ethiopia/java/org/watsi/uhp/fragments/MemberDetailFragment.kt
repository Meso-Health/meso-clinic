package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import kotlinx.android.synthetic.ethiopia.fragment_member_detail.check_in_button
import kotlinx.android.synthetic.ethiopia.fragment_member_detail.member_detail
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.MemberDetailViewModel
import java.util.UUID
import javax.inject.Inject

class MemberDetailFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var createIdentificationEventUseCase: CreateIdentificationEventUseCase

    lateinit var viewModel: MemberDetailViewModel
    lateinit var member: Member
    private lateinit var searchMethod: IdentificationEvent.SearchMethod
    private var isCheckedIn: Boolean = false

    companion object {
        const val PARAM_MEMBER = "member"
        const val PARAM_SEARCH_METHOD = "search_method"
        const val PARAM_IS_CHECKED_IN = "is_checked_in"

        fun forParams(
            member: Member,
            searchMethod: IdentificationEvent.SearchMethod,
            isCheckedIn: Boolean
        ): MemberDetailFragment {
            val memberDetailFragment = MemberDetailFragment()
            memberDetailFragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
                putSerializable(PARAM_SEARCH_METHOD, searchMethod)
                putBoolean(PARAM_IS_CHECKED_IN, isCheckedIn)
            }
            return memberDetailFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        member = arguments.getSerializable(PARAM_MEMBER) as Member
        searchMethod = arguments.getSerializable(PARAM_SEARCH_METHOD) as IdentificationEvent.SearchMethod
        isCheckedIn = arguments.getBoolean(PARAM_IS_CHECKED_IN)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MemberDetailViewModel::class.java)
        viewModel.getObservable(member).observe(this, Observer<MemberWithThumbnail> { memberWithThumbnail ->
            memberWithThumbnail?.let {
                member_detail.setMember(it.member, it.photo, clock)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(member.name, R.drawable.ic_arrow_back_white_24dp)
        return inflater?.inflate(R.layout.fragment_member_detail, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        check_in_button.setOnClickListener {
            createIdentificationEvent().subscribe({
                navigationManager.popTo(HomeFragment.withSnackbarMessage(
                    getString(R.string.checked_in_snackbar_message, member.name)
                ))
            }, {
                logger.error(it)
            })
        }

        if (isCheckedIn) {
            check_in_button.isEnabled = false
            check_in_button.text = getString(R.string.checked_in)
        } else {
            check_in_button.isEnabled = true
            check_in_button.text = getString(R.string.check_in)
        }
    }

    private fun createIdentificationEvent(): Completable {
        val idEvent = IdentificationEvent(
            id = UUID.randomUUID(),
            memberId = member.id,
            occurredAt = clock.instant(),
            searchMethod = searchMethod,
            throughMemberId = null,
            clinicNumber = null,
            clinicNumberType = null,
            fingerprintsVerificationTier = null,
            fingerprintsVerificationConfidence = null,
            fingerprintsVerificationResultCode = null
        )
        return createIdentificationEventUseCase.execute(idEvent)
    }
}
