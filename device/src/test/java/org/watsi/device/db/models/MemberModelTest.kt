package org.watsi.device.db.models

import org.junit.Test
import org.watsi.device.factories.MemberModelFactory

class MemberModelTest {

    @Test(expected = ModelValidationException::class)
    fun validations_nameEmpty() {
        MemberModelFactory.build(name = "")
    }

    @Test(expected = ModelValidationException::class)
    fun validations_nameWithWhiteSpace() {
        MemberModelFactory.build(name = "    ")
    }
}