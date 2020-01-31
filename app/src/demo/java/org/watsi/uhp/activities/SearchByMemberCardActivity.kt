package org.watsi.uhp.activities

import android.content.Intent
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.usecases.FindHouseholdIdByCardIdUseCase
import org.watsi.uhp.R
import javax.inject.Inject

class SearchByMemberCardActivity : QrCodeActivity() {

    @Inject lateinit var findHouseholdIdByCardIdUseCase: FindHouseholdIdByCardIdUseCase
    @Inject lateinit var logger: Logger

    companion object {
        const val RESULT_LOOKUP_FAILED = 3
        const val RESULT_NOT_FOUND = 4
        const val MEMBER_RESULT_KEY = "member"
        const val CARD_ID_KEY = "card_id"
    }

    override fun onDetectedQrCode(qrCode: String) {
        if (!Member.isValidCardId(qrCode)) {
            logger.warning("Invalid card ID scanned", mapOf(Pair("cardId", qrCode)))
            setErrorMessage(getString(R.string.scan_member_card_invalid_id))
        } else {
            findHouseholdIdByCardIdUseCase.execute(qrCode).subscribe({ householdId ->
                val resultIntent = Intent().apply {
                    putExtra(MEMBER_RESULT_KEY, householdId)
                }
                setResult(RESULT_OK, resultIntent)
                vibrate()
                finish()
            }, {
                logger.error(it)
                finishAsFailure(RESULT_LOOKUP_FAILED)
            }, {
                val resultIntent = Intent().apply {
                    putExtra(CARD_ID_KEY, qrCode)
                }
                setResult(RESULT_NOT_FOUND, resultIntent)
                finish()
            })
        }
    }
}
