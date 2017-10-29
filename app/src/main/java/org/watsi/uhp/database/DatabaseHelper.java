package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Diagnosis;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.LabResult;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.Photo;
import org.watsi.uhp.models.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Singleton for managing access to local Sqlite DB
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "org.watsi.db";
    private static final int DATABASE_VERSION = 13;

    private static DatabaseHelper instance;

    private final Map<Class, Dao> daoMap = new HashMap<>();

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getHelper() {
        if (instance == null) {
            throw new RuntimeException("Must initialize DatabaseHelper before acquiring instance");
        }
        return instance;
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
    }

    public static synchronized void reset() {
        instance.close();
        instance = null;
    }

    public static synchronized Dao fetchDao(Class clazz) throws SQLException {
        if (!instance.daoMap.containsKey(clazz)) {
            instance.daoMap.put(clazz, instance.getDao(clazz));
        }
        return instance.daoMap.get(clazz);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Member.class);
            TableUtils.createTable(connectionSource, Billable.class);
            TableUtils.createTable(connectionSource, IdentificationEvent.class);
            TableUtils.createTable(connectionSource, Encounter.class);
            TableUtils.createTable(connectionSource, EncounterItem.class);
            TableUtils.createTable(connectionSource, EncounterForm.class);
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Photo.class);
            TableUtils.createTable(connectionSource, Diagnosis.class);
            TableUtils.createTable(connectionSource, LabResult.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            switch (oldVersion) {
                default:
                case 2:
                    TableUtils.dropTable(connectionSource, IdentificationEvent.class, false);
                    TableUtils.dropTable(connectionSource, Encounter.class,false);
                    TableUtils.dropTable(connectionSource, EncounterItem.class, false);

                    TableUtils.createTable(connectionSource, IdentificationEvent.class);
                    TableUtils.createTable(connectionSource, Encounter.class);
                    TableUtils.createTable(connectionSource, EncounterItem.class);
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN dirty_fields STRING NOT NULL DEFAULT '[]';");
                    onUpgrade(database, connectionSource, 4, newVersion);
                    break;
                case 3:
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN dirty_fields STRING NOT NULL DEFAULT '[]';");
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN dirty_fields STRING NOT NULL DEFAULT '[]';");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN dirty_fields STRING NOT NULL DEFAULT '[]';");
                case 4:
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN birthdate DATE;");
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN birthdate_accuracy STRING;");
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN is_new BOOLEAN NOT NULL DEFAULT 0;");
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN enrolled_at DATE;");
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN is_new BOOLEAN NOT NULL DEFAULT 0;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN is_new BOOLEAN NOT NULL DEFAULT 0;");
                case 5:
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN backdated_occurred_at BOOLEAN NOT NULL DEFAULT 0;");
                case 6:
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN dismissed BOOLEAN NOT NULL DEFAULT 0;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN dismissal_reason STRING;");
                case 7:
                    TableUtils.createTable(connectionSource, EncounterForm.class);
                case 8:
                    // After talking with @pete and @byronium we need this no-op here because in the past, we had to update
                    // some phones's data directly. Those methods no longer will compile here, and all phones should be > version 8 now.
                case 9:
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN fingerprints_verification_result_code INT;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN fingerprints_verification_tier STRING;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN fingerprints_verification_confidence FLOAT;");
                case 10:
                    TableUtils.createTable(connectionSource, Photo.class);
                    // copy unsynced Member photos to Photo model
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN local_member_photo_id INT REFERENCES photos(id);");
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN local_national_id_photo_id INT REFERENCES photos(id);");
                    for (Member member : Member.unsynced(Member.class)) {
                        // remote member photo url stored the local URI prior to this migration
                        if (member.getRemoteMemberPhotoUrl() != null) {
                            Uri uri = Uri.parse(member.getRemoteMemberPhotoUrl());
                            if (uri.getScheme().equals("content")) {
                                Photo photo = new Photo();
                                photo.setUrl(member.getRemoteMemberPhotoUrl());

                                photo.create();
                                member.setLocalMemberPhoto(photo);
                                member.setRemoteMemberPhotoUrl(null);
                                getDao(Member.class).update(member);
                            }
                        }

                        if (member.getRemoteNationalIdPhotoUrl() != null) {
                            Uri uri = Uri.parse(member.getRemoteNationalIdPhotoUrl());
                            if (uri.getScheme().equals("content")) {
                                Photo photo = new Photo();
                                photo.setUrl(member.getRemoteNationalIdPhotoUrl());
                                photo.create();
                                member.setLocalNationalIdPhoto(photo);
                                member.setRemoteNationalIdPhotoUrl(null);
                                getDao(Member.class).update(member);
                            }
                        }
                    }

                    // remove non-null restriction from url column in EncounterForm table renaming
                    //  the encounter forms table, creating a new encounter forms table with the
                    //  updated column schema, copying the data from original table to the new table
                    //  and dropping the old table
                    getDao(EncounterForm.class).executeRaw("ALTER TABLE `encounter_forms` RENAME TO `encounter_forms_deprecated`");
                    TableUtils.createTable(connectionSource, EncounterForm.class);

                    // because photo_id is a required field on the new EncounterForm table with
                    // a foreign key constraint, we need to pass a valid photo ID when we copy
                    // over records - so this creates a placeholder Photo to use as the valid ID
                    // and after the table is created loops through and replaces the photo_id
                    // with a new Photo model created using the proper URL
                    Photo placeholderPhoto = new Photo();
                    placeholderPhoto.setId(UUID.randomUUID());
                    placeholderPhoto.setUrl("placeholder");
                    placeholderPhoto.create();
                    String placeholderPhotoId = placeholderPhoto.getId().toString();
                    getDao(EncounterForm.class).executeRaw("INSERT INTO `encounter_forms` \n" +
                                    "(id, created_at, token, dirty_fields, encounter_id, url, photo_id) \n" +
                                    "SELECT id, created_at, token, dirty_fields, encounter_id, url, ? FROM `encounter_forms_deprecated`",
                            placeholderPhotoId);
                    getDao(EncounterForm.class).executeRaw("DROP TABLE `encounter_forms_deprecated`");

                    // create Photo models for all EncounterForms
                    for (EncounterForm form : getDao(EncounterForm.class).queryForAll()) {
                        String url = form.getUrl();
                        Photo photo = new Photo();
                        photo.setUrl(url);
                        photo.create();
                        form.setPhoto(photo);
                        getDao(EncounterForm.class).update(form);
                    }
                case 11:
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN copayment_paid BOOLEAN NOT NULL DEFAULT 1;");
                case 12:
                    TableUtils.createTable(connectionSource, Diagnosis.class);
                    TableUtils.createTable(connectionSource, LabResult.class);
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN diagnosis_ids STRING NOT NULL DEFAULT '[]';");
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN has_fever BOOLEAN;");
                    getDao(Billable.class).executeRaw("ALTER TABLE `billables` ADD COLUMN requires_lab_result BOOLEAN NOT NULL DEFAULT 0;");
            }
            ExceptionManager.reportMessage("Migration run from version " + oldVersion + " to " +
                    newVersion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearDatabase() {
        try {
            ConnectionSource connectionSource = getConnectionSource();
            TableUtils.clearTable(connectionSource, Member.class);
            TableUtils.clearTable(connectionSource, Billable.class);
            TableUtils.clearTable(connectionSource, IdentificationEvent.class);
            TableUtils.clearTable(connectionSource, Encounter.class);
            TableUtils.clearTable(connectionSource, EncounterItem.class);
            TableUtils.clearTable(connectionSource, EncounterForm.class);
            TableUtils.clearTable(connectionSource, User.class);
            TableUtils.clearTable(connectionSource, Photo.class);
            TableUtils.clearTable(connectionSource, Diagnosis.class);
            TableUtils.clearTable(connectionSource, LabResult.class);
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
