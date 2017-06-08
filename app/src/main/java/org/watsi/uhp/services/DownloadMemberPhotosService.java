package org.watsi.uhp.services;

import android.util.Log;

import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Service class to handle downloading member photos
 */
public class DownloadMemberPhotosService extends AbstractSyncJobService {

    private static int MAX_FETCH_FAILURE_ATTEMPTS = 5;

    @Override
    public boolean performSync() {
        Log.i("UHP", "DownloadMemberPhotosService.performSync is called");
        try {
            fetchMemberPhotos();
            return true;
        } catch (SQLException | IllegalStateException e) {
            ExceptionManager.reportException(e);
            return false;
        }
    }

    protected void fetchMemberPhotos() throws SQLException {
        List<Member> membersWithPhotosToFetch = MemberDao.membersWithPhotosToFetch();
        Iterator<Member> iterator = membersWithPhotosToFetch.iterator();
        int fetchFailures = 0;
        while (iterator.hasNext()) {
            Log.i("UHP", "DownloadMemberPhotosService iterator");
            Member member = iterator.next();
            try {
                if (!FileManager.isLocal(member.getPhotoUrl())) {
                    member.fetchAndSetPhotoFromUrl();
                    member.updateFromFetch();
                }
            } catch (IOException e) {
                ExceptionManager.reportException(e);
                fetchFailures++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    ExceptionManager.reportException(e1);
                }
            }

            iterator.remove();
            if (fetchFailures == MAX_FETCH_FAILURE_ATTEMPTS) {
                ExceptionManager.reportMessage(
                        "Aborting DownloadMemberPhoto sync due to reaching max fetch failures");
                return;
            }
        }
    }
}
