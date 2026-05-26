// SPDX-License-Identifier: AGPL-3.0-or-later

package com.kineticgain.fieldaudit

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URI
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    val command = args.firstOrNull() ?: "server"
    val service = FieldAuditService()
    when (command) {
        "server" -> runServer(service, 5572, true)
        "demo" -> println(toJson(service))
        "prerender" -> prerender(service)
        "smoke" -> runSmoke(service)
        else -> error("Unknown command: $command")
    }
}

private fun buildPage(service: FieldAuditService, path: String): String? = when (path) {
    "/" -> SiteRenderer.overview(service)
    "/audit-lane" -> SiteRenderer.auditLane(service)
    "/capture-packets" -> SiteRenderer.capturePackets(service)
    "/sync-posture" -> SiteRenderer.syncPosture(service)
    "/verification" -> SiteRenderer.verification(service)
    "/docs" -> SiteRenderer.docs(service)
    else -> null
}

private fun jsonPayload(service: FieldAuditService, path: String): String? = when (path) {
    "/api/dashboard/summary" -> summaryJson(service.summary())
    "/api/audit-lane" -> auditLaneJson(service.auditLane())
    "/api/capture-packets" -> capturePacketsJson(service.capturePackets())
    "/api/sync-posture" -> syncPostureJson(service.syncPosture())
    "/api/verification" -> verificationJson(service.verification())
    "/api/sample" -> payloadJson(service.payload())
    else -> null
}

private fun runServer(service: FieldAuditService, port: Int, block: Boolean) {
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
    server.createContext("/") { exchange -> handleRequest(exchange, service) }
    server.executor = null
    server.start()
    if (block) {
        println("Field Audit Mobile listening on http://127.0.0.1:$port")
        while (true) {
            Thread.sleep(1_000)
        }
    }
}

private fun handleRequest(exchange: HttpExchange, service: FieldAuditService) {
    val path = exchange.requestURI.path.removeSuffix("/").ifEmpty { "/" }
    val response = when {
        path.startsWith("/api/") -> jsonPayload(service, path)
        else -> buildPage(service, path)
    }

    if (response == null) {
        val bytes = "Not found".toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
        exchange.sendResponseHeaders(404, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
        return
    }

    val bytes = response.toByteArray(StandardCharsets.UTF_8)
    val contentType = if (path.startsWith("/api/")) "application/json; charset=utf-8" else "text/html; charset=utf-8"
    exchange.responseHeaders.add("Content-Type", contentType)
    exchange.sendResponseHeaders(200, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
}

private fun prerender(service: FieldAuditService) {
    val root = File("site")
    if (root.exists()) {
        root.deleteRecursively()
    }
    root.mkdirs()

    val pages = mapOf(
        "index.html" to SiteRenderer.overview(service),
        "audit-lane/index.html" to SiteRenderer.auditLane(service),
        "capture-packets/index.html" to SiteRenderer.capturePackets(service),
        "sync-posture/index.html" to SiteRenderer.syncPosture(service),
        "verification/index.html" to SiteRenderer.verification(service),
        "docs/index.html" to SiteRenderer.docs(service)
    )
    pages.forEach { (path, html) ->
        val target = File(root, path)
        target.parentFile.mkdirs()
        target.writeText(html)
    }

    val api = mapOf(
        "api/dashboard/summary/index.json" to summaryJson(service.summary()),
        "api/audit-lane.json" to auditLaneJson(service.auditLane()),
        "api/capture-packets.json" to capturePacketsJson(service.capturePackets()),
        "api/sync-posture.json" to syncPostureJson(service.syncPosture()),
        "api/verification.json" to verificationJson(service.verification()),
        "api/sample.json" to payloadJson(service.payload())
    )
    api.forEach { (path, json) ->
        val target = File(root, path)
        target.parentFile.mkdirs()
        target.writeText(json)
    }

    File(root, "CNAME").writeText(File("CNAME").readText().trim())
}

private fun runSmoke(service: FieldAuditService) {
    val port = 5572
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
    server.createContext("/") { exchange -> handleRequest(exchange, service) }
    server.start()
    try {
        val routes = listOf(
            "/",
            "/audit-lane",
            "/capture-packets",
            "/sync-posture",
            "/verification",
            "/docs",
            "/api/dashboard/summary",
            "/api/audit-lane",
            "/api/capture-packets",
            "/api/sync-posture",
            "/api/verification",
            "/api/sample"
        )
        for (route in routes) {
            val conn = URI("http://127.0.0.1:$port$route").toURL().openConnection() as HttpURLConnection
            conn.connectTimeout = 5_000
            conn.readTimeout = 5_000
            conn.requestMethod = "GET"
            val code = conn.responseCode
            require(code == 200) { "Unexpected status $code for $route" }
            conn.inputStream.use { it.readBytes() }
            conn.disconnect()
        }
        println("Smoke checks passed for Field Audit Mobile routes.")
    } finally {
        server.stop(0)
    }
}

private fun toJson(service: FieldAuditService): String = buildString {
    append("{\n")
    append("  \"dashboard\": ").append(summaryJson(service.summary())).append(",\n")
    append("  \"blockedAudits\": ").append(auditLaneJson(service.auditLane().filter { it.status == "blocked" || it.status == "watch" })).append(",\n")
    append("  \"syncWarnings\": ").append(syncPostureJson(service.syncPosture().filter { it.status != "healthy" })).append("\n")
    append("}\n")
}

private fun summaryJson(summary: Summary) = """
{
  "activeAudits": ${summary.activeAudits},
  "blockedAudits": ${summary.blockedAudits},
  "offlinePackets": ${summary.offlinePackets},
  "syncWarnings": ${summary.syncWarnings},
  "supervisorEscalations": ${summary.supervisorEscalations},
  "operatorPosture": "${SiteRenderer.esc(summary.operatorPosture)}",
  "leadRecommendation": "${SiteRenderer.esc(summary.leadRecommendation)}"
}
""".trimIndent()

private fun auditLaneJson(items: List<AuditLaneItem>) = items.joinToString(
    prefix = "[\n",
    postfix = "\n]",
    separator = ",\n"
) { item ->
    """
  {
    "auditId": "${SiteRenderer.esc(item.auditId)}",
    "site": "${SiteRenderer.esc(item.site)}",
    "status": "${SiteRenderer.esc(item.status)}",
    "inspector": "${SiteRenderer.esc(item.inspector)}",
    "window": "${SiteRenderer.esc(item.window)}",
    "risk": "${SiteRenderer.esc(item.risk)}",
    "nextAction": "${SiteRenderer.esc(item.nextAction)}"
  }
    """.trimIndent()
}

private fun capturePacketsJson(items: List<CapturePacket>) = items.joinToString(
    prefix = "[\n",
    postfix = "\n]",
    separator = ",\n"
) { item ->
    """
  {
    "packetId": "${SiteRenderer.esc(item.packetId)}",
    "packetType": "${SiteRenderer.esc(item.packetType)}",
    "status": "${SiteRenderer.esc(item.status)}",
    "evidenceCount": ${item.evidenceCount},
    "syncState": "${SiteRenderer.esc(item.syncState)}",
    "owner": "${SiteRenderer.esc(item.owner)}",
    "note": "${SiteRenderer.esc(item.note)}"
  }
    """.trimIndent()
}

private fun syncPostureJson(items: List<SyncPosture>) = items.joinToString(
    prefix = "[\n",
    postfix = "\n]",
    separator = ",\n"
) { item ->
    """
  {
    "channel": "${SiteRenderer.esc(item.channel)}",
    "status": "${SiteRenderer.esc(item.status)}",
    "symptom": "${SiteRenderer.esc(item.symptom)}",
    "risk": "${SiteRenderer.esc(item.risk)}",
    "requiredAction": "${SiteRenderer.esc(item.requiredAction)}"
  }
    """.trimIndent()
}

private fun verificationJson(items: List<VerificationGate>) = items.joinToString(
    prefix = "[\n",
    postfix = "\n]",
    separator = ",\n"
) { item ->
    """
  {
    "gate": "${SiteRenderer.esc(item.gate)}",
    "status": "${SiteRenderer.esc(item.status)}",
    "detail": "${SiteRenderer.esc(item.detail)}"
  }
    """.trimIndent()
}

private fun payloadJson(payload: Map<String, Any>) = buildString {
    val routes = payload["routes"] as List<*>
    val priorities = payload["priorities"] as List<*>
    append("{\n")
    append("  \"product\": \"").append(SiteRenderer.esc(payload["product"].toString())).append("\",\n")
    append("  \"purpose\": \"").append(SiteRenderer.esc(payload["purpose"].toString())).append("\",\n")
    append("  \"routes\": [").append(routes.joinToString(", ") { "\"${SiteRenderer.esc(it.toString())}\"" }).append("],\n")
    append("  \"priorities\": [").append(priorities.joinToString(", ") { "\"${SiteRenderer.esc(it.toString())}\"" }).append("]\n")
    append("}")
}
