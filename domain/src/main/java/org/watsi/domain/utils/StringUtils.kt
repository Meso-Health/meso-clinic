package org.watsi.domain.utils

object StringUtils {
    fun formatCardId(cardId: String): String {
        return "${cardId.substring(0, 3)} ${cardId.substring(3, 6)} ${cardId.substring(6, 9)}"
    }
}
