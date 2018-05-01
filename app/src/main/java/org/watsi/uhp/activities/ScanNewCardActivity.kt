package org.watsi.uhp.activities

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
    }

    override fun onDetectedQrCode(qrCode: String) {
        if (!Member.validCardId(qrCode)) {
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
                    putExtra(QrCodeActivity.QR_CODE_RESULT_KEY, qrCode)
                }
                setResult(RESULT_OK, resultIntent)
                vibrate()
                finish()
            })
        }
    }
}
