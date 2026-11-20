# Contributing to Kavern

Kavern is a fork of [Inkita](https://github.com/dom-53/Inkita). Contributions are welcome.

## Getting started
- Use Android Studio Flamingo+ and JDK 17.
- Build/test: `./gradlew assembleDebug`, then `./gradlew detekt spotlessApply` before opening a PR.
- The app needs your own Kavita server: set the server URL and API key in Settings → Kavita (HTTPS recommended).

## Workflow
- Commits: keep them scoped and descriptive; prefer present tense.
- Run `detekt` and `spotlessApply`; fix warnings where feasible.
- For UI changes, add a brief description or screenshots in the PR.

## Code style
- Kotlin + Compose: keep UI stateless where possible; move logic to ViewModels/repositories.
- Comments should explain intent for non-obvious code; avoid noise.

## Reporting issues
- Include device, app version, and reproduction steps.
- For crashes: share logs/stack traces if possible.
- Network issues: note server URL scheme/port (HTTP vs HTTPS) and network environment.
