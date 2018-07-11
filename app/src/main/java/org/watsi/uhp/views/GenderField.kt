package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import kotlinx.android.synthetic.main.view_gender_field.view.gender_button_female
import kotlinx.android.synthetic.main.view_gender_field.view.gender_button_male
import kotlinx.android.synthetic.main.view_gender_field.view.gender_error_message
import org.watsi.domain.entities.Member
import org.watsi.uhp.R

class GenderField @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var onGenderChange: (gender: Member.Gender) -> Unit

    init {
        LayoutInflater.from(context).inflate(R.layout.view_gender_field, this, true)
        gender_button_female.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                this.requestFocus()
                onGenderChange(Member.Gender.F)
                setGender(Member.Gender.F)
                true
            } else {
                false
            }
        }

        gender_button_male.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                this.requestFocus()
                onGenderChange(Member.Gender.M)
                setGender(Member.Gender.M)
                true
            } else {
                false
            }
        }
    }

    fun setGender(gender: Member.Gender?) {
        when (gender) {
            Member.Gender.F -> {
                gender_button_female.setSelected()
                gender_button_male.setUnselected(R.drawable.ic_member_placeholder_male)
            }
            Member.Gender.M -> {
                gender_button_male.setSelected()
                gender_button_female.setUnselected(R.drawable.ic_member_placeholder_female)
            }
        }
    }

    fun setOnGenderChange(onGenderChange: (gender: Member.Gender) -> Unit) {
        this.onGenderChange = onGenderChange
    }

    fun setError(errorMessage: String?) {
        gender_error_message.error = errorMessage
    }
}
