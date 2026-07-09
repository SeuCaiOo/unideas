# Running the app & local git hooks

Local-dev reference. Not needed on every turn — see `CLAUDE.md` for the always-on essentials.

## Git hooks

Hooks live in `.githooks/` and are activated per clone with:

```bash
./gradlew installGitHooks
```

| Hook | When | What it does |
|---|---|---|
| `pre-commit` | Before commit | Detekt (autoCorrect) on staged `.kt` files + blocks `google-services.json` |
| `commit-msg` | On commit | Validates Conventional Commits format |
| `pre-push` | Before push | Kotlin compilation check + Detekt + blocks push to `main` |

## Running & inspecting the app (`android` CLI)

Prefer the `android` CLI over raw `adb` for deploying and inspecting the app — it wraps `adb`/`aapt`/emulator control with commands purpose-built for this workflow.

```
android emulator list                              # list AVDs (add --long for status/API level)
android emulator start <avd-name>                  # boot an emulator, blocks until ready
android emulator stop <avd-name>                    # graceful shutdown (omit name if only one is running)
android run --apks=<path-to-apk> --activity=<pkg>.<Activity>   # install + launch (activity name WITHOUT the debug applicationIdSuffix)
android screen capture -o <path.png>                # screenshot; -a annotates UI elements with bounding boxes
android layout -p                                    # dump the current on-screen view tree as JSON (text + coordinates)
android describe                                     # locate build artifacts (APK paths, etc.) for a project
```

Notes learned by using it on this project:
- `--activity` takes the manifest-declared component name (e.g. `com.seucaio.unideas.MainActivity`), not the suffixed `applicationId` — the debug variant still installs as `com.seucaio.unideas.debug`, but the activity class name is unaffected.
- The local AVD (`Resizable_Experimental_API_34`) needs a few GB of free disk to boot; if `android emulator start` fails with "not enough disk space", check `df -h /home` before assuming it's a CLI bug — the culprit here was stale `~/.gradle/caches/<old-version>` folders (safe to delete; Gradle re-downloads on demand).
