package org.watsi.device.db.daos

import org.junit.Test
import org.watsi.device.factories.DiagnosisModelFactory

class DiagnosisDaoTest : DaoBaseTest() {

    @Test
    fun insert_multipleModels_replacesOnConflict() {
        val persistedDiagnosis = DiagnosisModelFactory.create(diagnosisDao, id = 1)
        val newDiagnosis = DiagnosisModelFactory.build(id = 2)
        val updatedDiagnosis = persistedDiagnosis.copy(description = "Fever")

        diagnosisDao.insert(listOf(updatedDiagnosis, newDiagnosis))

        diagnosisDao.all().test().assertValue(listOf(updatedDiagnosis, newDiagnosis))
    }

    @Test
    fun deleteNotInList() {
        DiagnosisModelFactory.create(diagnosisDao, id = 1)
        val model = DiagnosisModelFactory.create(diagnosisDao, id = 2)

        diagnosisDao.deleteNotInList(listOf(model.id))

        diagnosisDao.all().test().assertValue(listOf(model))
    }
}
