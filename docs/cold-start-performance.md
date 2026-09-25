# Cold-start performance

The backend runs behind Sablier scale-to-zero on a self-hosted host, so every request after ~15 minutes idle pays a full cold start. Sablier holds the request open for up to 2 minutes while the container boots, which means cold start *is* the dominant latency in normal use.

Measured on the deployment host:

```
Started RecipleaseApplication in 19.429 seconds (process running for 23.214)
```

The Spring context refresh dominates (`Root WebApplicationContext: initialization completed in 7045 ms` of the ~19s). Spring Data Mongo repository scanning is only 260 ms, so the repository layer is not where to look.

## JEP 483 AOT cache — removed, do not reintroduce as it was

An AOT cache was baked into the Docker image between 2026-09-22 and 2026-09-25 to cut this startup time. It crash-looped the container in production for those three days and was removed in `3812f87`.

| Commit | Change |
|---|---|
| `782c185` | Baked a JEP 483 AOT cache into the image, to cut cold-start boot time |
| `d358ffd` | Tolerated the training run's flaky exit code, verified with `cloud,prod` profiles |
| `9249d2a` | Trained the cache in place instead of against a separate jar copy |
| `3812f87` | Removal — dropped the training step and the `-XX:AOTCache` entrypoint flag |

### Why it failed

AOT caches are **not portable across CPUs with different instruction sets**. CI trained the cache on an `ubuntu-latest` GitHub Actions runner (AVX2 available); the image was then loaded on the deployment host, whose CPU has **no AVX support at all** — `grep -oE 'avx[0-9a-z_]*' /proc/cpuinfo` returns nothing on that machine. The JVM does not verify per-CPU compatibility when loading the cache, so every startup executed AVX-encoded methods and died:

```
# Problematic frame:
# v  ~AdapterBlob
# SIGILL (0x4) at pc=0x00007f02d7a6f780, pid=1, tid=7
# JRE version: OpenJDK Runtime Environment Temurin-25.0.4+7
```

`~AdapterBlob` is the AOT adapter stub, so the crash frame confirms it died executing AOT-cached code rather than application code.

That same lack of AVX is the reason MongoDB is not self-hosted on the deployment host — MongoDB 5.0+ requires AVX — so the database runs on a managed service instead. **The missing AVX support is the constraint behind two separate decisions in this project**: the database topology and the AOT cache.

### Diagnosing it if it ever recurs

The symptom is misleading. It does **not** look like a crash:

- The container crash-loops, Sablier holds the incoming request open, the 2-minute blocking timeout expires, and external callers get a **504 after roughly 60 seconds** — indistinguishable from a network timeout, a DNS problem, or a misconfigured port forward.
- DNS, TLS, the reverse proxy, and the port forwarding are all genuinely fine. Do not go chasing the network.
- The give-away is `docker ps` showing `Restarting (139)` rather than `Up`, plus `docker logs` reporting `SIGILL`.

### If it's ever reintroduced on AVX-capable hardware

The rule is: **the cache must be trained on the same CPU model that will load it.** In order of preference:

1. **Train at first boot on the target host**, writing the cache to a volume that persists across container replacement (a named volume or host bind mount for `/application`), with the entrypoint loading it if present and training it if absent. Self-correcting by construction — it cannot drift out of sync with the CPU it is running on, because it *is* that CPU. The cache must not live in the image layer, or it is lost on every rebuild.
2. **Build the image on the target hardware** rather than in CI. Simple, but ties builds to a specific machine and gives up CI reproducibility.
3. **Keep training in CI only if CI and the target host are pinned to the same CPU model.** Brittle, and the failure mode is a multi-day silent crash loop rather than a build failure. This needs an automated guard, not a comment.

Whichever route is taken, **test on the actual target before deploying**. A green CI build proves nothing here, since the crash only happens on the machine that *loads* the cache.

There is a second gotcha independent of CPU: the cache fingerprints `application.jar`'s mtime, so it is invalidated unless training runs against the exact byte-identical jar that will later be launched. Training must therefore be the final build step, after layer assembly.

## Portable alternatives

Check these before reaching for AOT again:

- **Class Data Sharing / AppCDS** — `-XX:ArchiveClassesAtExit=app.jsa` on a first run, then `-XX:SharedArchiveFile=app.jsa` on subsequent runs. Shares loaded class metadata rather than compiled code, so it is not ISA-bound in the same way. The JDK validates archives and can still reject them, so verify on the target.
- **`-XX:TieredStopAtLevel=1`** — caps JIT at C1, trading peak throughput for startup time. Reasonable for a small always-idle service; wrong if throughput ever matters.
- **Trim the eager context refresh** — since it accounts for roughly 7 of the 19 seconds, reducing eagerly-created beans is the highest-leverage JVM-independent change.
- **Stop scaling to zero.** If a 23-second cold start on the first request is unacceptable, that is a deployment-policy problem rather than a JVM problem. Raising the Sablier session duration, or keeping the container warm, makes startup time irrelevant.

## Post-startup work

Both of these run *after* `Started RecipleaseApplication ...` and therefore still affect first-request latency:

- **Mongock** — on the boot observed here all change sets were `PASSED OVER` (already executed), so it was cheap. It will get more expensive as migrations are added, and `Reflections` classpath scans are visible in the log.
- **springdoc** — the first `/openapi` request took 1964 ms to initialise, lazy-loaded on demand.
