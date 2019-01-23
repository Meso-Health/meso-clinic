package org.watsi.uhp.activities

import android.app.Activity
import android.content.Intent
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class ScanNewCardActivity : QrCodeActivity() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var logger: Logger

    companion object {
        const val RESULT_LOOKUP_FAILED = 3
        const val CARD_ID_RESULT_KEY = "card_id"

        fun parseResult(resultCode: Int, data: Intent?, logger: Logger): Pair<String?, String?> {
            return when (resultCode) {
                Activity.RESULT_OK -> {
                    Pair(data?.getStringExtra(CARD_ID_RESULT_KEY), null)
                }
                else -> {
                    if (resultCode != Activity.RESULT_CANCELED) {
                        logger.error("QrCodeActivity.parseResult called with resultCode: $resultCode")
                    }
                    Pair(null, "failed")
                }
            }
        }
    }

    override fun onDetectedQrCode(qrCode: String) {
        if (!Member.isValidCardId(qrCode)) {
            logger.warning("Invalid card ID scanned", mapOf(Pair("cardId", qrCode)))
            setErrorMessage("Not a valid member card")
        } else {
            memberRepository.findByCardId(qrCode).subscribe({
                setErrorMessage("Card ID is already assigned to a different member")
            }, {
                logger.error(it)
                finishAsFailure(RESULT_LOOKUP_FAILED)
            }, {
                val resultIntent = Intent().apply {
                    putExtra(CARD_ID_RESULT_KEY, qrCode)
                }
                setResult(RESULT_OK, resultIntent)
                vibrate()
                finish()
            })
        }
    }
}
