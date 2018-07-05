package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.view_button_with_preview.view.button
import kotlinx.android.synthetic.main.view_member_card_field.view.member_card_button_with_preview
import kotlinx.android.synthetic.main.view_member_card_field.view.scan_card_error_message
import org.watsi.uhp.R

class MemberCardIdField @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_member_card_field, this, true)
    }

    fun setError(errorMessage: String?) {
        scan_card_error_message.error = errorMessage
        button.toggleErrorState(errorMessage != null)
    }

    fun setCardId(cardId: String) {
        member_card_button_with_preview.setIdPreview(cardId)
    }

    override fun setOnClickListener(listener: OnClickListener) {
        member_card_button_with_preview.setOnClickListener(listener)
    }
}
