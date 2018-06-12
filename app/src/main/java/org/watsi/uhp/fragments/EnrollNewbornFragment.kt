package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import dagger.android.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_enroll_newborn.*
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.activities.SavePhotoActivity
import org.watsi.uhp.activities.ScanNewCardActivity
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.EnrollNewbornViewModel
import java.util.*
import javax.inject.Inject

class EnrollNewbornFragment : DaggerFragment(), NavigationManager.HandleOnBack {
    // TODO TextView.OnEditorActionListener?

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var logger: Logger
    lateinit var viewModel: EnrollNewbornViewModel
    private val memberId = UUID.randomUUID()
    private val parent = arguments.getSerializable(PARAM_MEMBER) as Member

    companion object {
        const val AUTO_SCROLL_PADDING_IN_DP = 60
        const val CAPTURE_PHOTO_INTENT = 1
        const val SCAN_QRCODE_INTENT = 2
        const val PARAM_MEMBER = "member"

        fun forParent(member: Member): EnrollNewbornFragment {
            val fragment = EnrollNewbornFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    private fun setErrors(errorMap: Map<String, String>) {
        gender_field.setError(errorMap[EnrollNewbornViewModel.MEMBER_GENDER_ERROR])
        name_layout.setError(errorMap[EnrollNewbornViewModel.MEMBER_NAME_ERROR])
        birthdate_dialog_field.setErrorOnField(errorMap[EnrollNewbornViewModel.MEMBER_BIRTHDATE_ERROR])
        // TODO: set other error too
        photo_field.setError(errorMap[EnrollNewbornViewModel.MEMBER_PHOTO_ERROR])
        card_id_field.setError(errorMap[EnrollNewbornViewModel.MEMBER_CARD_ERROR])
    }

    private fun scrollToFirstError(errorMap: Map<String, String>) {
        val validationKeysToField = linkedMapOf(
                EnrollNewbornViewModel.MEMBER_GENDER_ERROR to gender_field,
                EnrollNewbornViewModel.MEMBER_NAME_ERROR to name_layout,
                EnrollNewbornViewModel.MEMBER_BIRTHDATE_ERROR to birthdate_dialog_field,
                EnrollNewbornViewModel.MEMBER_PHOTO_ERROR to photo_field,
                EnrollNewbornViewModel.MEMBER_CARD_ERROR to card_id_field
        )

        validationKeysToField.forEach {
            val validationKey = it.key
            val layout = it.value
            if (errorMap[validationKey] != null) {
                (view as ScrollView).smoothScrollTo(0, layout.top - AUTO_SCROLL_PADDING_IN_DP)
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EnrollNewbornViewModel::class.java)
        viewModel.getViewStateObservable().observe(this, Observer {
            if (it != null) {
                setErrors(it.errors)

                if (it.name.isEmpty()) {
                    activity.title = "Newborn"
                    // TODO: clean up string (add to R -> context.getString(R.string.____))
                } else {
                    activity.title = it.name
                    // TODO check design that we do want it to update at the top when the user types in the name
                }

                if(it.photoId != null && it.thumbnailPhoto != null) {
                    val thumbnailBitmap = BitmapFactory.decodeByteArray(
                            it.thumbnailPhoto.bytes, 0, it.thumbnailPhoto.bytes.size)
                    photo_field.setPhotoPreview(thumbnailBitmap)
                }

                it.cardId?.let {
                    card_id_field.setCardId(org.watsi.domain.utils.StringUtils.formatCardId(it))
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstance: Bundle?): View? {
        activity.title = "Newborn"
        // TODO: clean up string (add to R -> context.getString(R.string.____))
        return inflater?.inflate(R.layout.fragment_enroll_newborn, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        gender_field.setOnGenderChange { gender -> viewModel.onGenderChange(gender) }

        name.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onNameChange(text)
        })

        name.onFocusChangeListener = View.OnFocusChangeListener{ view, hasFocus ->
            if (!hasFocus) { keyboardManager.hideKeyboard(view) }
        }

        photo_field.setOnClickListener{
            startActivityForResult(Intent(activity, SavePhotoActivity::class.java), CAPTURE_PHOTO_INTENT)
        }

        card_id_field.setOnClickListener {
            startActivityForResult(Intent(activity, ScanNewCardActivity::class.java), SCAN_QRCODE_INTENT)
        }

        done.setOnClickListener {
            viewModel.saveMember(memberId, parent.householdId).subscribe({
                // TODO: navigationManager.popTo(parentFragment)
            }, { throwable ->
                showSnackbar(throwable)
            })
        }

//        name.setOnEditorActionListener(this)
//        TODO: check if we need this^
    }

    fun showSnackbar(throwable: Throwable) {
        var errorMessage = "An unknown error occurred. Try again later."
        // TODO: clean up string (add to R -> context.getString(R.string.____))
        if (throwable is EnrollNewbornViewModel.ValidationException) {
            errorMessage = throwable.localizedMessage
            scrollToFirstError(throwable.errors)
        } else {
            logger.error(throwable)
        }
        view?.let {
            val snackbar = Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG)
            val textView = snackbar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
            textView.setTextColor(context.getColor(R.color.white))
            snackbar.view.setBackgroundColor(context.getColor(R.color.red6))
            snackbar.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAPTURE_PHOTO_INTENT -> {
                val (photoIds, error) = SavePhotoActivity.parseResult(resultCode, data, logger)
                if (photoIds != null) {
                    viewModel.onPhotoTaken(photoIds.first, photoIds.second)
                }
            }
            SCAN_QRCODE_INTENT -> {
                val (cardId, error) = ScanNewCardActivity.parseResult(resultCode, data, logger)
                if (cardId != null) {
                    viewModel.onCardScan(cardId)
                }
            }
            else -> {
                logger.error("unknown request code")
                // TODO: clean up string (add to R -> context.getString(R.string.____))
            }
        }
    }

    override fun onBack(): Single<Boolean> {
        // if no information has entered, do not show confirmation dialog onBack
        return if (viewModel.getViewStateObservable().value == EnrollNewbornViewModel.ViewState()) {
            Single.just(true)
        } else {
            Single.create<Boolean> { single ->
                AlertDialog.Builder(activity)
                        // TODO: clean up strings (add to R -> context.getString(R.string.____))
                        .setTitle("Are you sure?")
                        .setMessage("You will lose any information about this member.")
                        .setPositiveButton("Discard") { _, _ -> single.onSuccess(true) }
                        .setNegativeButton("Cancel") { _, _ -> single.onSuccess(false) }
                        .setOnDismissListener { single.onSuccess(false) }
                        .show()
            }
        }
    }
}
