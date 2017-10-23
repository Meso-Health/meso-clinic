package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;

import org.watsi.uhp.models.Photo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * POJO helper for querying Photos
 */
public class PhotoDao {

    private static Dao<Photo, UUID> getDao() throws SQLException {
        return DatabaseHelper.fetchDao(Photo.class);
    }

    public static List<Photo> canBeDeleted() throws SQLException {
        String rawQuery =
                "SELECT photos.id\n" +
                        "FROM photos\n" +
                        "LEFT OUTER JOIN members ON (\n" +
                            "members.local_member_photo_id = photos.id OR\n" +
                            "members.local_national_id_photo_id = photos.id)\n" +
                        "LEFT OUTER JOIN encounter_forms ON photos.id = encounter_forms.photo_id\n" +
                        "WHERE photos.deleted = 0 AND\n" +
                            // delete if the associated member or encounter form does not have a dirty photo field
                            "(((members.id IS NOT NULL AND members.dirty_fields NOT LIKE '%photo%') OR\n" +
                            "(encounter_forms.id IS NOT NULL AND encounter_forms.dirty_fields NOT LIKE '%photo_id%')) OR\n" +
                            // or if not associated with any member encounter form model
                            //  this occurs when a photo is taken but is never associated with another model
                            //  (e.g. user exits the complete enrollment flow after photo but before saving)
                            //  AND photo is at least 30 minutes old so we do not delete it before it
                            //  has a chance to be associated with its corresponding member/encounter form
                            "(members.id IS NULL AND encounter_forms.id IS NULL AND\n" +
                            "photos.created_at <= datetime('now', 'localtime', '-30 Minute')))";

        GenericRawResults<String[]> results = getDao().queryRaw(rawQuery);

        List<Photo> photosToDelete = new ArrayList<>();
        for (String[] result : results) {
            Photo photo = getDao().queryForId(UUID.fromString(result[0]));
            photosToDelete.add(photo);
        }
        return photosToDelete;
    }
}
