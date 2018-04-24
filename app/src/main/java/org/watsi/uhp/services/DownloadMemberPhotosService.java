package org.watsi.uhp.services;

import android.util.Log;

import org.watsi.domain.entities.Member;
import org.watsi.domain.repositories.MemberRepository;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.managers.ExceptionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

/**
 * Service class to handle downloading member photos
 */
public class DownloadMemberPhotosService extends AbstractSyncJobService {

    private static int MAX_FETCH_FAILURE_ATTEMPTS = 5;

    @Inject MemberRepository memberRepository;

    @Override
    public boolean performSync() {
        Log.i("UHP", "DownloadMemberPhotosService.performSync is called");
        try {
            fetchMemberPhotos();
            return true;
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
            return false;
        }
    }

    protected void fetchMemberPhotos() throws SQLException {
        List<Member> membersWithPhotosToFetch = memberRepository.membersWithPhotosToFetch();
        Iterator<Member> iterator = membersWithPhotosToFetch.iterator();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(ApiService.HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .readTimeout(ApiService.HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .build();
        int fetchFailures = 0;
        while (iterator.hasNext()) {
            // TODO: fetch photo
//            Member member = iterator.next();
//            try {
//                member.fetchAndSetPhotoFromUrl(okHttpClient);
//                memberRepository.updateFromFetch(member);
//            } catch (IOException e) {
//                // count fetch failures so we can abort fetching early if it is consistently failing
//                fetchFailures++;
//                ExceptionManager.reportException(e);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e1) {
//                    ExceptionManager.reportExceptionWarning(e1);
//                }
//            }

            iterator.remove();
            if (fetchFailures == MAX_FETCH_FAILURE_ATTEMPTS) {
                ExceptionManager.reportMessage(
                        "Aborting DownloadMemberPhoto sync due to reaching max fetch failures");
                return;
            }
        }
    }
}
