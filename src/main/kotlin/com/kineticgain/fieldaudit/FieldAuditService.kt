// SPDX-License-Identifier: AGPL-3.0-or-later

package com.kineticgain.fieldaudit

data class Summary(
    val activeAudits: Int,
    val blockedAudits: Int,
    val offlinePackets: Int,
    val syncWarnings: Int,
    val supervisorEscalations: Int,
    val operatorPosture: String,
    val leadRecommendation: String
)

data class AuditLaneItem(
    val auditId: String,
    val site: String,
    val status: String,
    val inspector: String,
    val window: String,
    val risk: String,
    val nextAction: String
)

data class CapturePacket(
    val packetId: String,
    val packetType: String,
    val status: String,
    val evidenceCount: Int,
    val syncState: String,
    val owner: String,
    val note: String
)

data class SyncPosture(
    val channel: String,
    val status: String,
    val symptom: String,
    val risk: String,
    val requiredAction: String
)

data class VerificationGate(
    val gate: String,
    val status: String,
    val detail: String
)

class FieldAuditService {
    fun summary() = Summary(
        activeAudits = 5,
        blockedAudits = 1,
        offlinePackets = 2,
        syncWarnings = 2,
        supervisorEscalations = 2,
        operatorPosture = "offline-first mobile capture + governed sync + supervisor sign-off",
        leadRecommendation = "Clear the rooftop safety packet sync warning before the next permit submission, or supervisor sign-off will outrun the evidence trail."
    )

    fun auditLane() = listOf(
        AuditLaneItem("FA-17", "North garage retrofit", "watch", "M. Torres", "today · 4:30 PM", "Photo packet missing one remediation closeout image.", "Re-open packet and capture the final barrier photo before upload."),
        AuditLaneItem("FA-21", "Utility trench inspection", "healthy", "J. Reed", "today · 6:00 PM", "All required observation points are present and timestamped.", "Hold for supervisor sign-off after sync completes."),
        AuditLaneItem("FA-24", "Clinic generator room", "blocked", "A. Shah", "tomorrow · 8:00 AM", "Offline packet has not synced and the supervisor note references a newer checklist revision.", "Reconcile checklist version and sync before the compliance packet is exported."),
        AuditLaneItem("FA-29", "Distribution yard walk", "watch", "C. Evans", "tomorrow · 11:00 AM", "Temperature log attachment uploaded, but route note is still draft-only.", "Promote the route note into the evidence packet so the audit record is complete."),
        AuditLaneItem("FA-31", "Solar inverter enclosure", "healthy", "R. Kim", "tomorrow · 1:30 PM", "Evidence, defects, and remediation owner are aligned.", "No action beyond scheduled closeout review.")
    )

    fun capturePackets() = listOf(
        CapturePacket("PK-08", "Barrier remediation", "watch", 6, "offline cached", "Field ops", "One closeout image still missing from the packet."),
        CapturePacket("PK-11", "Confined space checklist", "healthy", 8, "synced", "Safety lead", "Checklist and supervisor note are both current."),
        CapturePacket("PK-14", "Generator room compliance", "blocked", 5, "sync mismatch", "Compliance manager", "Packet revision is older than the current checklist template."),
        CapturePacket("PK-18", "Trench depth evidence", "healthy", 7, "synced", "Site supervisor", "Depth markers and route notes are complete.")
    )

    fun syncPosture() = listOf(
        SyncPosture("Offline packet store", "watch", "Queued media is intact but one packet has not reconciled to the latest checklist version.", "Supervisor sign-off could approve a stale record.", "Force a checklist-version compare before export."),
        SyncPosture("Supervisor approval feed", "healthy", "Approvals are arriving with packet hashes and route references.", "Low.", "Continue normal monitoring."),
        SyncPosture("Media upload channel", "blocked", "A large rooftop photo set exceeded the mobile retry window and is waiting for user action.", "Permit package can ship without the final visual evidence.", "Retry upload on stable connection and lock export until packet is whole."),
        SyncPosture("Remediation queue sync", "healthy", "Defect ownership and closeout dates are consistent across app and export.", "Low.", "Continue nightly verification.")
    )

    fun verification() = listOf(
        VerificationGate("Packet completeness", "watch", "Three active packets are safe, but one blocked audit still carries an incomplete media bundle."),
        VerificationGate("Checklist version parity", "blocked", "Generator-room packet references an older checklist revision than the supervisor workflow."),
        VerificationGate("Supervisor approval provenance", "healthy", "Approvals carry packet hash, owner, and timestamp context."),
        VerificationGate("Export-safe evidence trail", "healthy", "Signed packets can be exported with observation, remediation, and closeout context intact.")
    )

    fun payload() = mapOf(
        "product" to "Field Audit Mobile",
        "purpose" to "Kotlin mobile-first field audit control plane for offline evidence capture, sync posture, and remediation-safe supervisor review.",
        "routes" to listOf("/", "/audit-lane", "/capture-packets", "/sync-posture", "/verification", "/docs"),
        "priorities" to listOf(
            "Keep offline evidence packets review-safe before they sync.",
            "Show which audits are blocked by packet completeness or checklist drift.",
            "Tie supervisor approvals to evidence hashes and remediation ownership.",
            "Treat mobile audit capture as governed operations, not just a form fill."
        )
    )
}
