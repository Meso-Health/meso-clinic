package org.watsi.uhp.basetests;

import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class ActivityTest {
    private static final long WAIT_FOR_UI_TO_UPDATE = 1000L;

    protected static void waitForUIToUpdate() {
        try {
            Thread.sleep(WAIT_FOR_UI_TO_UPDATE);
        } catch (Exception ignored) {
        }
    }

    protected static Member getMember(String cardId) throws SQLException {
        return MemberDao.findByCardId(cardId);
    }

    protected static IdentificationEvent getIdEvent(Member member) throws SQLException {
        return IdentificationEventDao.openCheckIn(member.getId());
    }
}
