package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.view_button_with_preview.view.button
import kotlinx.android.synthetic.uganda.view_fingerprints_field.view.fingerprints_button_with_preview
import kotlinx.android.synthetic.uganda.view_fingerprints_field.view.fingerprints_disabled_message
import kotlinx.android.synthetic.uganda.view_fingerprints_field.view.fingerprints_error_message
import org.watsi.uhp.R
import java.util.UUID

class FingerprintsField @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_fingerprints_field, this, true)
    }

    fun setError(errorMessage: String?) {
        fingerprints_error_message.error = errorMessage
        button.toggleErrorState(errorMessage != null)
    }

    fun setFingerprints(fingerprintsId: UUID?) {
        if (fingerprintsId != null) {
            fingerprints_button_with_preview.setSuccessMessagePreview(R.string.fingerprints_success_message)
        }
    }

    fun toggleEnabled(fingerprintsEnabled: Boolean) {
        if (fingerprintsEnabled) {
            fingerprints_disabled_message.visibility = View.GONE
            fingerprints_button_with_preview.enableButton()
        } else {
            setError(null) // This is needed so both errors won't show up.
            fingerprints_disabled_message.visibility = View.VISIBLE
            fingerprints_button_with_preview.disableButton()
        }
    }

    override fun setOnClickListener(listener: OnClickListener) {
        fingerprints_button_with_preview.setOnClickListener(listener)
    }
}
