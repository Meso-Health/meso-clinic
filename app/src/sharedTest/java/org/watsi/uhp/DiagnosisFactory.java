package org.watsi.uhp;

import org.watsi.uhp.models.Diagnosis;

import java.sql.SQLException;

public class DiagnosisFactory {
    public static Diagnosis createDiagnosis(Integer id, String description, String searchAliases) throws SQLException {
        Diagnosis diagnosis = new Diagnosis(id, description, searchAliases);
        diagnosis.createOrUpdate();
        return diagnosis;
    }
}
