# Architecture

`field-audit-mobile` is a Kotlin/JVM operator surface with a mobile-first layout and static Pages deploy.

## Layers

1. `FieldAuditService`
   - serves the synthetic audit queue, capture packets, sync posture, and verification gates
2. `SiteRenderer`
   - renders a touch-friendly control plane designed to read like a mobile operator app rather than a desktop dashboard squeezed into a phone frame
3. `Main.kt`
   - supports:
     - `server`
     - `demo`
     - `prerender`
     - `smoke`

## Why this shape works

The repo keeps the Kotlin lane honest while still shipping a live subdomain:

- Kotlin is the actual implementation language
- the HTML is generated from Kotlin
- local smoke, prerender, and JSON outputs all come from the same service model
- the public site stays static and crawlable for the portfolio estate
