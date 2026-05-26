// SPDX-License-Identifier: AGPL-3.0-or-later

package com.kineticgain.fieldaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FieldAuditServiceTest {
    private val service = FieldAuditService()

    @Test
    fun `summary counts align with lane states`() {
        val summary = service.summary()
        assertEquals(service.auditLane().size, summary.activeAudits)
        assertEquals(service.auditLane().count { it.status == "blocked" }, summary.blockedAudits)
        assertEquals(service.capturePackets().count { it.syncState != "synced" }, summary.offlinePackets)
    }

    @Test
    fun `payload routes include verification route`() {
        val payload = service.payload()
        val routes = payload["routes"] as List<*>
        assertTrue("/verification" in routes)
    }
}
