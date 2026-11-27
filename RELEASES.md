# Android release setup

## Trakt

Create a Trakt API application and use this redirect URI:

```text
nuvioenhanced://auth/trakt
```

For local builds, add these values to `local.properties`:

```properties
TRAKT_CLIENT_ID=your_client_id
TRAKT_CLIENT_SECRET=your_client_secret
TRAKT_REDIRECT_URI=nuvioenhanced://auth/trakt
```

For GitHub Actions releases, add repository secrets named:

- `TRAKT_CLIENT_ID`
- `TRAKT_CLIENT_SECRET`

The workflow passes the secrets directly to Gradle and does not print their values.
Forks do not inherit Actions secrets, so every fork that publishes an APK must
configure its own credentials.

## Simkl

Create a Simkl API application and use this redirect URI:

```text
nuvioenhanced://auth/simkl
```

For local builds, add these values to `local.properties`:

```properties
SIMKL_CLIENT_ID=your_client_id
SIMKL_REDIRECT_URI=nuvioenhanced://auth/simkl
SIMKL_APP_NAME=Mew
```

For GitHub Actions releases, add the repository secret `SIMKL_CLIENT_ID`.
Simkl authentication uses PKCE and does not embed a client secret in the app.

## Android debug signing

For local debug builds, use the same dedicated Mew debug keystore as CI so a local APK can update a CI debug APK without uninstalling. Keep the keystore under the ignored `keystore/` directory and never commit it.

Add these entries to the local, ignored `local.properties` file:

```properties
MEW_DEBUG_KEYSTORE_FILE=keystore/mew-debug.keystore
MEW_DEBUG_KEYSTORE_PASSWORD=your_debug_keystore_password
MEW_DEBUG_KEY_ALIAS=your_debug_key_alias
MEW_DEBUG_KEY_PASSWORD=your_debug_key_password
```

The Gradle configuration reads these values from `local.properties` (environment variables take precedence). CI restores the same keystore from GitHub Actions secrets and verifies the generated APK certificate before uploading it.

## Android signing

An APK can update an existing Mew installation only when both APKs
use the same application ID and signing certificate. The established Android
certificate SHA-256 digest is:

```text
4d87e3d92c54ae0efcdebb75dd08b8cfca1eace052198ed3b8f3f552533a21e3
```

The repository owner must configure the Mew signing material through
`MEW_RELEASE_KEYSTORE_BASE64`, `MEW_RELEASE_KEYSTORE_PASSWORD`,
`MEW_RELEASE_KEY_ALIAS`, and `MEW_RELEASE_KEY_PASSWORD`. Never commit the
keystore or its passwords, and do not send them through chat.

Legacy `NUVIO_*` signing/update identifiers may remain in compatibility paths
where changing them would break existing installations or release tooling.

The release workflow verifies every generated APK against this digest before
uploading artifacts or creating a GitHub Release. A mismatch stops the workflow
because that APK would require users to uninstall the existing app.

## Update source

GitHub Actions builds automatically use the owner of the repository running the
workflow. A local build can override the update source in `local.properties`:

```properties
NUVIO_UPDATE_GITHUB_OWNER=somilkhan
NUVIO_UPDATE_GITHUB_REPO=MewMobile
```

These property names are retained as internal compatibility identifiers; their
current defaults target `somilkhan/MewMobile` for Mew releases.

The app checks the 20 most recent GitHub Releases at startup. On the Stable
channel it ignores pre-releases and prefers releases cut from `enhanced`, the
only long-lived branch. Installed 0.5.x apps accept nothing else, so every
release must be dispatched from `enhanced`. On the Beta channel it also considers pre-releases. A release is
offered only when it contains a compatible APK and has a newer version than the
installed app, so `0.6.0` outranks `0.6.0-beta.2`, which outranks `0.6.0-beta.1`.
The GitHub release body is displayed as the in-app changelog.

Release notes are the matching `CHANGELOG.md` section followed by a commit list.
The list only follows this repository's first-parent history, so upstream
commits that arrive through merges are left out and should be summarised in the
changelog by hand. Reverts, merge-repair commits and work that was undone later
in the same range are dropped too.

## Release checklist

1. Merge the release branch into `enhanced` and dispatch releases from `enhanced`.
2. Add a `CHANGELOG.md` section whose heading starts with the exact version,
   for example `## 0.6.0-beta.1 - 2026-11-27`.
3. Increment `MARKETING_VERSION` and `CURRENT_PROJECT_VERSION` in
   `iosApp/Configuration/Version.xcconfig`. Use a `-beta.N` suffix for betas.
   This must be the last application change; only the changelog, release
   workflow and release scripts may change after it.
4. Run the Android release workflow in `dry-run` mode and read the generated
   notes in the run summary.
5. Confirm the release contents with the repository owner.
6. Run the workflow in `publish` mode. For a beta, set `channel` to `beta`
   so it is published as a GitHub pre-release.
