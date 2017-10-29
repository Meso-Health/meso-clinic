package org.watsi.uhp.database;

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.watsi.uhp.DiagnosisFactory.createDiagnosis
import java.sql.SQLException

class DiagnosisDaoTest: DaoTest() {
    @Test
    fun searchByFuzzyDescriptionAndSearchAlias() {
        val d1 = createDiagnosis(1, "Severe Malaria", "mal s mal smal s. mal")
        val d2 = createDiagnosis(2, "Urinary Tract Infection", "UTI")
        val d3 = createDiagnosis(3, "Upper respiratory tract infection", "URTI")
        val d4 = createDiagnosis(4, "Cushing's syndrome", null)
        val d5 = createDiagnosis(5, "Severe Malaria in Pregnancy", "mal s mal smal s. mal")
        val d6 = createDiagnosis(6, "Malaria in Pregnancy", "MAL")
        val d7 = createDiagnosis(7, "Cough", null)
        val d8 = createDiagnosis(9, "Runners itch", null)
        val d9 = createDiagnosis(11, "SomediagnosiswithMALinit", null)
        val d10 = createDiagnosis(12, "Utirenary", null)

        assertEquals(DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias("pregnancy"), listOf(d6, d5))
        assertEquals(DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias("malaria"), listOf(d6, d1, d5))
        assertEquals(DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias("mal"), listOf(d6, d1, d5, d9))
        assertEquals(DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias("cough"), listOf(d7))
        assertEquals(DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias("cou"), listOf(d7, d4))
        assertEquals(DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias("prag"), listOf(d6, d5))
        assertEquals(DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias("urti"), listOf(d3, d2, d10))
        assertEquals(DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias("itch runn"), listOf(d8))
    }

    @Test
    fun findBySearchAliases() {
        val d1 = createDiagnosis(1, "Severe Malaria", "mal s mal smal s. mal")
        val d2 = createDiagnosis(2, "Urinary Tract Infection", "UTI")
        val d3 = createDiagnosis(3, "Upper respiratory tract infection", "URTI")
        val d4 = createDiagnosis(4, "Cushing's syndrome", null)
        val d5 = createDiagnosis(5, "Severe Malaria in Pregnancy", "mal s mal smal s. mal")
        val d6 = createDiagnosis(6, "Malaria in Pregnancy", "MAL")

        assertEquals(DiagnosisDao.findBySearchAliases("s. mal"), listOf(d1, d5))
        assertEquals(DiagnosisDao.findBySearchAliases("s mal"), listOf(d1, d5))
        assertEquals(DiagnosisDao.findBySearchAliases("smal"), listOf(d1, d5))
        assertEquals(DiagnosisDao.findBySearchAliases("SMAL"), listOf(d1, d5))
        assertEquals(DiagnosisDao.findBySearchAliases("UTI"), listOf(d2))
        assertEquals(DiagnosisDao.findBySearchAliases("URTI"), listOf(d3))
        assertEquals(DiagnosisDao.findBySearchAliases("MAL"), listOf(d6, d1, d5))
        assertEquals(DiagnosisDao.findBySearchAliases("pregnancy"), listOf<String>())
        assertEquals(DiagnosisDao.findBySearchAliases("Severe Malaria"), listOf<String>())
    }

    @Test(expected = SQLException::class)
    fun findByDescription() {
        val d1 = createDiagnosis(1, "Malaria", null)
        val d2 = createDiagnosis(2, "Salmanella Malaria", null)
        val d3 = createDiagnosis(3, "Ice cream", null)
        val d4 = createDiagnosis(4, "Cushing's syndrome", null)

        assertEquals(DiagnosisDao.findDiagnosisByDescription("Malaria"), d1)
        assertEquals(DiagnosisDao.findDiagnosisByDescription("Ice cream"), d3)
        assertEquals(DiagnosisDao.findDiagnosisByDescription("Cushing's syndrome"), d4)

        DiagnosisDao.findDiagnosisByDescription("Not found in list")
    }

    @Test
    fun allUniqueDiagnosisDescriptions() {
        val d1 = createDiagnosis(1, "Malaria", null)
        val d2 = createDiagnosis(2, "Salmanella malaria", null)
        val d3 = createDiagnosis(3, "Ice cream", null)

        val result = DiagnosisDao.allUniqueDiagnosisDescriptions()

        assertTrue(result.contains(d1.description))
        assertTrue(result.contains(d2.description))
        assertTrue(result.contains(d3.description))
    }
}
