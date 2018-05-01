package org.watsi.uhp.activities

import android.app.Activity
import android.content.Intent
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class SearchByMemberCardActivity : QrCodeActivity() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var logger: Logger

    companion object {
        const val RESULT_LOOKUP_FAILED = 3
        const val MEMBER_RESULT_KEY = "member"

        fun parseResult(resultCode: Int, data: Intent?, logger: Logger): Pair<Member?, String?> {
            return when (resultCode) {
                Activity.RESULT_OK -> {
                    Pair(data?.getSerializableExtra(MEMBER_RESULT_KEY) as Member?, null)
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
        if (!Member.validCardId(qrCode)) {
            logger.warning("Invalid card ID scanned", mapOf(Pair("cardId", qrCode)))
            setErrorMessage("Not a valid member card")
        } else {
            memberRepository.findByCardId(qrCode).subscribe({
                val resultIntent = Intent().apply {
                    putExtra(MEMBER_RESULT_KEY, it)
                }
                setResult(RESULT_OK, resultIntent)
                vibrate()
                finish()

            }, {
                logger.error(it)
                finishAsFailure(RESULT_LOOKUP_FAILED)
            }, {
                setErrorMessage("This card is not associated with a member")
            })
        }
    }
}
