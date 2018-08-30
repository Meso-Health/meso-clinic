package org.watsi.device.db.daos

import org.junit.Test
import org.junit.Assert.assertEquals
import org.watsi.device.factories.DiagnosisModelFactory

class DiagnosisDaoTest : DaoBaseTest() {

    @Test
    fun insert_multipleModels_replacesOnConflict() {
        val persistedDiagnosis = DiagnosisModelFactory.create(diagnosisDao, id = 1)
        val newDiagnosis = DiagnosisModelFactory.build(id = 2)
        val updatedDiagnosis = persistedDiagnosis.copy(description = "Fever")

        diagnosisDao.upsert(listOf(updatedDiagnosis, newDiagnosis))

        diagnosisDao.all().test().assertValue(listOf(updatedDiagnosis, newDiagnosis))
    }

    @Test
    fun delete() {
        DiagnosisModelFactory.create(diagnosisDao, id = 1)
        val model = DiagnosisModelFactory.create(diagnosisDao, id = 2)

        diagnosisDao.delete(listOf(1))

        diagnosisDao.all().test().assertValue(listOf(model))
    }

    @Test
    fun findAll() {
        val diagnosisModel1 = DiagnosisModelFactory.create(diagnosisDao, id = 1)
        val diagnosisModel2 = DiagnosisModelFactory.create(diagnosisDao, id = 2)
        val diagnosisModel3 = DiagnosisModelFactory.create(diagnosisDao, id = 3)

        val diagnosisIdList = listOf(1, 3)
        val expectedDiagnosisList = listOf(diagnosisModel1, diagnosisModel3)

        val resultDiagnosisList = diagnosisDao.findAll(diagnosisIdList).test().values().first()

        assertEquals(expectedDiagnosisList, resultDiagnosisList)
    }
}
