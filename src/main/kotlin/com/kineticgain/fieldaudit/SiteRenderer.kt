// SPDX-License-Identifier: AGPL-3.0-or-later

package com.kineticgain.fieldaudit

import java.lang.StringBuilder

object SiteRenderer {
    private val style = """
      :root{
        --bg:#070a0f; --panel:#0b1220; --panel2:#0a1426;
        --line:rgba(120,255,170,.18); --line2:rgba(120,255,170,.10);
        --text:#e9f3ff; --muted:rgba(233,243,255,.72); --muted2:rgba(233,243,255,.55);
        --bert:#37ff8b; --bert2:#19c7ff;
        --warn:#ffcc66; --bad:#ff5c7a; --good:#37ff8b; --plum:#b88cff;
        --shadow: 0 18px 60px rgba(0,0,0,.55);
        --mono: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
        --sans: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Apple Color Emoji", "Segoe UI Emoji";
      }
      *{box-sizing:border-box} html,body{height:100%}
      body{
        margin:0; font-family:var(--sans); color:var(--text);
        background:
          radial-gradient(1200px 600px at 20% -10%, rgba(55,255,139,.18), transparent 60%),
          radial-gradient(900px 520px at 90% 0%, rgba(25,199,255,.16), transparent 55%),
          radial-gradient(1000px 600px at 50% 110%, rgba(55,255,139,.10), transparent 60%),
          linear-gradient(180deg, #05070c 0%, #070a0f 35%, #05070c 100%);
      }
      .grid-bg{
        position:fixed; inset:0; pointer-events:none; opacity:.12; z-index:-1;
        background-image:
          linear-gradient(to right, rgba(55,255,139,.14) 1px, transparent 1px),
          linear-gradient(to bottom, rgba(55,255,139,.10) 1px, transparent 1px);
        background-size: 46px 46px;
        mask-image: radial-gradient(900px 600px at 40% 10%, #000 60%, transparent 100%);
      }
      .wrap{max-width:1200px; margin:0 auto; padding:24px 18px 80px}
      .topbar{
        display:flex; justify-content:space-between; align-items:flex-start; gap:14px;
        border-bottom:1px solid var(--line2); padding-bottom:14px; margin-bottom:22px;
        font-family:var(--mono); font-size:11px; letter-spacing:.16em; color:var(--muted);
        text-transform:uppercase;
      }
      .topbar .left{color:var(--bert)}
      .topbar .right{text-align:right}
      .hero{
        background: linear-gradient(180deg, rgba(11,18,32,.95), rgba(8,14,26,.92));
        border:1px solid var(--line); border-radius:24px; padding:26px;
        box-shadow: var(--shadow); overflow:hidden; border-top:2px solid var(--bert2);
      }
      .phone-row{display:grid; grid-template-columns:1.2fr .8fr; gap:18px}
      @media (max-width:960px){.phone-row{grid-template-columns:1fr}}
      h1{font-size:58px; line-height:.95; margin:0 0 16px; letter-spacing:-.04em; font-weight:800}
      @media (max-width:700px){h1{font-size:40px}}
      .eyebrow,.note,.chip,.micro{font-family:var(--mono); letter-spacing:.16em; text-transform:uppercase}
      .eyebrow{font-size:11px; color:var(--bert); margin-bottom:12px}
      .hero p{font-size:15px; color:var(--muted); line-height:1.55; margin:0 0 16px}
      .nav{display:flex; flex-wrap:wrap; gap:8px; margin-top:16px}
      .chip{
        font-size:11px; padding:7px 11px; border-radius:999px; border:1px solid var(--line); color:var(--muted);
        background:rgba(6,10,18,.4); text-decoration:none;
      }
      .chip.active{color:#081018; background:var(--bert); border-color:transparent; font-weight:700}
      .hero-note{
        margin-top:18px; border:1px solid rgba(255,204,102,.2); border-left:4px solid var(--warn);
        background:linear-gradient(180deg, rgba(255,204,102,.06), rgba(11,18,32,.92));
        border-radius:14px; padding:16px 18px;
      }
      .hero-note .micro{font-size:10px; color:var(--warn)}
      .side{display:flex; flex-direction:column; gap:12px}
      .sidecard,.section,.kpi,.listcard,.phonecard{
        border:1px solid var(--line); border-radius:16px; background:linear-gradient(180deg, rgba(11,18,32,.85), rgba(8,14,26,.65));
      }
      .sidecard{padding:16px 18px}
      .sidecard .micro{font-size:10px; color:var(--bert2)}
      .sidecard strong{display:block; font-size:15px; margin:6px 0 4px}
      .sidecard p{margin:0; color:var(--muted); font-size:13px; line-height:1.5}
      .section{margin-top:24px; padding:18px}
      .section-head{display:flex; justify-content:space-between; gap:10px; align-items:baseline; margin-bottom:14px; border-bottom:1px solid var(--line2); padding-bottom:10px}
      .section-head h2{margin:0; font-size:24px}
      .note{font-size:10px; color:var(--muted2)}
      .kpis{display:grid; grid-template-columns:repeat(3,1fr); gap:12px}
      @media (max-width:760px){.kpis{grid-template-columns:1fr}}
      .kpi{padding:14px}
      .kpi .value{font-family:var(--mono); font-size:26px; font-weight:700}
      .kpi .label{font-family:var(--mono); font-size:10px; color:var(--muted); margin-top:6px; text-transform:uppercase; letter-spacing:.16em}
      .kpi .copy{font-size:12px; color:var(--muted); line-height:1.45; margin-top:8px}
      .value.green{color:var(--bert)} .value.amber{color:var(--warn)} .value.red{color:var(--bad)} .value.cyan{color:var(--bert2)} .value.plum{color:var(--plum)} .value.white{color:var(--text)}
      .phone-stack{display:grid; grid-template-columns:repeat(2,1fr); gap:14px}
      @media (max-width:900px){.phone-stack{grid-template-columns:1fr}}
      .phonecard{padding:18px}
      .phonecard .top{display:flex; justify-content:space-between; align-items:center; gap:10px; margin-bottom:8px}
      .phonecard .id{font-family:var(--mono); font-size:22px; color:var(--bert)}
      .pill{font-family:var(--mono); font-size:10px; padding:4px 8px; border:1px solid currentColor; border-radius:999px; text-transform:uppercase; letter-spacing:.12em}
      .pill.good{color:var(--bert)} .pill.watch{color:var(--warn)} .pill.blocked{color:var(--bad)}
      .phonecard h3{margin:0 0 8px; font-size:22px}
      .phonecard p{margin:0 0 10px; color:var(--muted); line-height:1.5}
      .list{list-style:none; padding:0; margin:0}
      .list li{display:grid; grid-template-columns:16px 1fr; gap:10px; padding:6px 0; color:var(--muted); line-height:1.45}
      .list li:before{content:""; width:12px; height:12px; border:1px solid var(--line); border-radius:3px; margin-top:4px}
      table{width:100%; border-collapse:separate; border-spacing:0}
      th,td{padding:13px 12px; text-align:left; font-size:13px; vertical-align:top}
      thead th{font-family:var(--mono); font-size:10px; text-transform:uppercase; letter-spacing:.16em; color:var(--muted2); border-bottom:1px solid var(--line)}
      tbody td{color:var(--muted); border-bottom:1px solid var(--line2)}
      tbody tr:last-child td{border-bottom:none}
      tbody tr:hover{background:rgba(55,255,139,.03)}
      b{color:var(--text)}
      footer{
        margin-top:30px; padding-top:14px; border-top:1px dashed var(--line2);
        display:flex; justify-content:space-between; gap:10px; flex-wrap:wrap;
        font-family:var(--mono); font-size:11px; color:var(--muted2); letter-spacing:.08em;
      }
    """.trimIndent()

    fun overview(service: FieldAuditService): String {
        val s = service.summary()
        val cards = service.auditLane().take(4).mapIndexed { index, item ->
            phoneCard(
                id = "F-0${index + 1}",
                status = item.status,
                title = item.site,
                desc = "${item.window} · inspector: ${item.inspector}",
                bullets = listOf(item.risk, "Next action: ${item.nextAction}")
            )
        }.joinToString("\n")

        val body = """
          <section class="section">
            <div class="section-head"><h2>Overview</h2><div class="note">field packet pressure + supervisor trust</div></div>
            <div class="kpis">
              ${kpi(s.activeAudits.toString(), "green", "Active audits", "Audits currently in motion across field teams and supervisors.")}
              ${kpi(s.blockedAudits.toString(), "red", "Blocked audits", "Audit flows unsafe to export until evidence or checklist drift is fixed.")}
              ${kpi(s.offlinePackets.toString(), "amber", "Offline packets", "Packets still relying on device-side capture before sync completion.")}
              ${kpi(s.syncWarnings.toString(), "cyan", "Sync warnings", "Signals that packet state and cloud truth may be drifting apart.")}
              ${kpi(s.supervisorEscalations.toString(), "plum", "Escalations", "Supervisor or compliance interventions still open.")}
              ${kpi(s.operatorPosture, "white", "Operator posture", "Mobile evidence capture treated like a governed operating system, not just a checklist app.")}
            </div>
          </section>
          <section class="section">
            <div class="section-head"><h2>Audit lane</h2><div class="note">highest-risk packets first</div></div>
            <div class="phone-stack">$cards</div>
          </section>
        """.trimIndent()

        return page(
            "Field Audit Mobile",
            "/",
            "field audit mobile control plane",
            "Capture mobile evidence safely before sync gaps become permit, safety, or compliance failures.",
            "This Kotlin operator surface makes the field packet visible: which audits are safe, which media bundles are incomplete, and where supervisor sign-off is being asked to outrun the actual evidence trail.",
            service,
            body,
            listOf(
                Triple("Core offer", "Mobile-first audit command surface", "Offline capture, sync trust, remediation ownership, and supervisor review in one lane."),
                Triple("Buyer fit", "Field ops + compliance teams", "For construction, facilities, utilities, inspections, and any team that moves through governed site visits."),
                Triple("Execution style", "Evidence before approvals", "Treat mobile packets like production artifacts, not disposable uploads.")
            )
        )
    }

    fun auditLane(service: FieldAuditService): String {
        val rows = service.auditLane().joinToString("\n") { item ->
            """
            <tr>
              <td><b>${esc(item.site)}</b><br>${esc(item.auditId)}</td>
              <td>${esc(item.window)}</td>
              <td>${statusPill(item.status)}</td>
              <td>${esc(item.inspector)}</td>
              <td>${esc(item.risk)}</td>
              <td>${esc(item.nextAction)}</td>
            </tr>
            """.trimIndent()
        }
        val body = """
          <section class="section">
            <div class="section-head"><h2>Audit lane</h2><div class="note">queue ownership + field pressure</div></div>
            <table>
              <thead>
                <tr><th>Audit</th><th>Window</th><th>Status</th><th>Inspector</th><th>Risk</th><th>Next action</th></tr>
              </thead>
              <tbody>$rows</tbody>
            </table>
          </section>
        """.trimIndent()
        return page(
            "Audit lane | Field Audit Mobile",
            "/audit-lane",
            "audit lane",
            "See which site packets are safe, which are drifting, and which should block export.",
            "The audit lane keeps the field queue readable to supervisors and compliance reviewers before a half-synced packet becomes the system of record.",
            service,
            body,
            listOf(
                Triple("Signal", "Field pressure", "Mobile capture is only safe when window, packet, and checklist all line up."),
                Triple("Control", "Named owner + next step", "Every risky packet has a single operator and explicit remediation move."),
                Triple("Buyer value", "Fewer evidence disputes", "Teams can prove what was actually observed instead of reconstructing it later.")
            )
        )
    }

    fun capturePackets(service: FieldAuditService): String {
        val cards = service.capturePackets().mapIndexed { index, packet ->
            phoneCard(
                id = "P-0${index + 1}",
                status = packet.status,
                title = packet.packetType,
                desc = "${packet.packetId} · owner: ${packet.owner}",
                bullets = listOf("${packet.evidenceCount} evidence items", "Sync state: ${packet.syncState}", packet.note)
            )
        }.joinToString("\n")
        val body = """
          <section class="section">
            <div class="section-head"><h2>Capture packets</h2><div class="note">mobile evidence readiness</div></div>
            <div class="phone-stack">$cards</div>
          </section>
        """.trimIndent()
        return page(
            "Capture packets | Field Audit Mobile",
            "/capture-packets",
            "capture packets",
            "Keep the media bundle, route note, checklist version, and owner tied to the same packet.",
            "This route focuses on the packet itself: whether the evidence is complete, whether sync state is trustworthy, and whether the owner can safely hand it to a supervisor or regulator.",
            service,
            body,
            listOf(
                Triple("Packet model", "Offline-first evidence", "Photos, notes, defects, and closeout context remain bound even before sync completes."),
                Triple("Review posture", "Supervisor-safe export", "A packet should not clear review if the media or checklist state is stale."),
                Triple("Field reality", "Weak signal survives", "Capture still works in the field without turning sync into guesswork.")
            )
        )
    }

    fun syncPosture(service: FieldAuditService): String {
        val rows = service.syncPosture().joinToString("\n") { item ->
            """
            <tr>
              <td><b>${esc(item.channel)}</b></td>
              <td>${statusPill(item.status)}</td>
              <td>${esc(item.symptom)}</td>
              <td>${esc(item.risk)}</td>
              <td>${esc(item.requiredAction)}</td>
            </tr>
            """.trimIndent()
        }
        val body = """
          <section class="section">
            <div class="section-head"><h2>Sync posture</h2><div class="note">offline trust + upload recovery</div></div>
            <table>
              <thead>
                <tr><th>Channel</th><th>Status</th><th>Symptom</th><th>Risk</th><th>Required action</th></tr>
              </thead>
              <tbody>$rows</tbody>
            </table>
          </section>
        """.trimIndent()
        return page(
            "Sync posture | Field Audit Mobile",
            "/sync-posture",
            "sync posture",
            "Treat upload and checklist reconciliation as first-class operational signals, not hidden mobile plumbing.",
            "Sync confidence determines whether a field packet is believable. This route surfaces the channels where mobile reality and cloud truth are still drifting apart.",
            service,
            body,
            listOf(
                Triple("Core offer", "Sync trust map", "See where mobile capture is healthy, watch-level, or blocked."),
                Triple("Field ops", "Fewer stale approvals", "Supervisors stop approving packets that are incomplete or mismatched."),
                Triple("Evidence trail", "Hash-safe handoff", "Keep sync and review provenance intact across weak connectivity.")
            )
        )
    }

    fun verification(service: FieldAuditService): String {
        val cards = service.verification().mapIndexed { index, gate ->
            phoneCard(
                id = "V-0${index + 1}",
                status = gate.status,
                title = gate.gate,
                desc = gate.detail,
                bullets = listOf("Release gate: ${gate.status}")
            )
        }.joinToString("\n")
        val body = """
          <section class="section">
            <div class="section-head"><h2>Verification</h2><div class="note">buyer-safe mobile release gate</div></div>
            <div class="phone-stack">$cards</div>
          </section>
        """.trimIndent()
        return page(
            "Verification | Field Audit Mobile",
            "/verification",
            "verification gate",
            "Block export when packet completeness, checklist parity, or approval provenance is still unreliable.",
            "The verification route decides whether the mobile workflow is safe to trust in audits, inspections, permit packages, and supervisor review.",
            service,
            body,
            listOf(
                Triple("Signal", "No blind exports", "Stop incomplete or stale field packets before they turn into official records."),
                Triple("Proof", "Supervisor parity", "Every approval should carry packet context, not just a thumbs-up."),
                Triple("Buyer value", "Safer site execution", "Teams get faster closeout with fewer evidence disputes.")
            )
        )
    }

    fun docs(service: FieldAuditService): String {
        val priorities = (service.payload()["priorities"] as List<*>).joinToString("") { "<li>${esc(it.toString())}</li>" }
        val body = """
          <section class="section">
            <div class="section-head"><h2>Docs</h2><div class="note">implementation notes</div></div>
            <div class="phone-stack">
              <article class="phonecard">
                <div class="top"><div class="id">K1</div><div class="pill good">Kotlin</div></div>
                <h3>JVM control plane</h3>
                <p>This repo keeps the language atlas honest: Kotlin owns the service model, route renderer, prerender output, and smoke flow.</p>
                <ul class="list">
                  <li>Single language across demo, prerender, and server modes.</li>
                  <li>Static bundle keeps the public estate crawlable.</li>
                </ul>
              </article>
              <article class="phonecard">
                <div class="top"><div class="id">K2</div><div class="pill watch">Priorities</div></div>
                <h3>Operator priorities</h3>
                <p>Field evidence needs the same governance discipline as any other regulated workflow.</p>
                <ul class="list">$priorities</ul>
              </article>
            </div>
          </section>
        """.trimIndent()
        return page(
            "Docs | Field Audit Mobile",
            "/docs",
            "operator docs",
            "Document the mobile evidence path so the release system can enforce it.",
            "This route explains why the Kotlin lane matters, how the mobile-first control plane is shaped, and where field capture fits into the broader Kinetic Gain operating system.",
            service,
            body,
            listOf(
                Triple("Language atlas", "Kotlin surface", "This opens the Kotlin lane without pretending mobile governance is just another web dashboard."),
                Triple("Deploy", "Static Pages + local server", "The same codebase powers local review and a crawlable public demo."),
                Triple("Embedded tie-back", "In-product field evidence", "The primitive can live inside inspection or operations software without losing audit trust.")
            )
        )
    }

    private fun page(title: String, active: String, eyebrow: String, hero: String, intro: String, service: FieldAuditService, body: String, sideCards: List<Triple<String, String, String>>): String {
        val summary = service.summary()
        val nav = listOf(
            "/" to "Overview",
            "/audit-lane" to "Audit Lane",
            "/capture-packets" to "Capture Packets",
            "/sync-posture" to "Sync Posture",
            "/verification" to "Verification",
            "/docs" to "Docs"
        ).joinToString("") { (href, label) ->
            val cls = if (href == active) "chip active" else "chip"
            "<a class=\"$cls\" href=\"$href\">${esc(label)}</a>"
        }
        val side = sideCards.joinToString("") { (label, titleText, copy) ->
            "<article class=\"sidecard\"><div class=\"micro\">${esc(label)}</div><strong>${esc(titleText)}</strong><p>${esc(copy)}</p></article>"
        }

        return """
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>${esc(title)}</title>
          <style>$style</style>
        </head>
        <body>
          <div class="grid-bg"></div>
          <div class="wrap">
            <div class="topbar">
              <div class="left">KINETIC GAIN · field audit mobile</div>
              <div class="right">
                <div>Kotlin mobile-first operator surface</div>
                <div>offline evidence · sync trust · supervisor review</div>
              </div>
            </div>
            <div class="phone-row">
              <section class="hero">
                <div class="eyebrow">${esc(eyebrow)}</div>
                <h1>${esc(hero)}</h1>
                <p>${esc(intro)}</p>
                <div class="hero-note">
                  <div class="micro">Lead recommendation</div>
                  <p><strong>Reviewer-first posture</strong><br>${esc(summary.leadRecommendation)}</p>
                </div>
                <div class="nav">$nav</div>
              </section>
              <aside class="side">$side</aside>
            </div>
            $body
            <footer>
              <div>field-audit-mobile · AGPL-3.0-or-later · synthetic demonstration data only</div>
              <div>Routes: / · /audit-lane · /capture-packets · /sync-posture · /verification · /docs</div>
            </footer>
          </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun kpi(value: String, cls: String, label: String, copy: String) = """
      <div class="kpi">
        <div class="value $cls">${esc(value)}</div>
        <div class="label">${esc(label)}</div>
        <div class="copy">${esc(copy)}</div>
      </div>
    """.trimIndent()

    private fun phoneCard(id: String, status: String, title: String, desc: String, bullets: List<String>): String {
        val statusClass = when (status.lowercase()) {
            "healthy" -> "good"
            "blocked", "critical" -> "blocked"
            else -> "watch"
        }
        val bulletHtml = bullets.joinToString("") { "<li>${esc(it)}</li>" }
        return """
          <article class="phonecard">
            <div class="top"><div class="id">${esc(id)}</div><div class="pill $statusClass">${esc(status)}</div></div>
            <h3>${esc(title)}</h3>
            <p>${esc(desc)}</p>
            <ul class="list">$bulletHtml</ul>
          </article>
        """.trimIndent()
    }

    private fun statusPill(status: String): String {
        val statusClass = when (status.lowercase()) {
            "healthy" -> "good"
            "blocked", "critical" -> "blocked"
            else -> "watch"
        }
        return "<span class=\"pill $statusClass\">${esc(status)}</span>"
    }

    fun esc(value: String): String = buildString {
        value.forEach { ch ->
            append(
                when (ch) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&#39;"
                    else -> ch
                }
            )
        }
    }
}
