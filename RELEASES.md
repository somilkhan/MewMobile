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

For GitHub Actions releases, `TRAKT_CLIENT_ID` may be provided as a repository secret. The public Android release workflow deliberately removes `TRAKT_CLIENT_SECRET` before the application is built, so a Trakt client secret is not embedded in the published APK.

## Simkl

Create a Simkl API application and use this redirect URI:

```text
nuvioenhanced://auth/simkl
```

For local builds, add these values to `local.properties`:

```properties
SIMKL_CLIENT_ID=your_client_id
SIMKL_REDIRECT_URI=nuvioenhanced://auth/simkl
SIMKL_APP_NAME=MewMobile
```

For GitHub Actions releases, add the repository secret `SIMKL_CLIENT_ID`.
Simkl authentication uses PKCE and does not embed a client secret in the app.

## Android signing

An APK can update an existing MewMobile installation only when both APKs
use the same application ID and signing certificate. The established Android
certificate SHA-256 digest is:

```text
4d87e3d92c54ae0efcdebb75dd08b8cfca1eace052198ed3b8f3f552533a21e3
```

The repository owner must configure signing material through the preferred `MEW_RELEASE_KEYSTORE_BASE64` and `MEW_LOCAL_PROPERTIES_BASE64` secrets. The workflow retains `NUVIO_*` secret names only as a temporary compatibility fallback. Never
commit the keystore or its passwords, and do not send them through chat.

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

Stable builds check published, non-prerelease GitHub Releases at startup. Beta builds use the prerelease channel. A release is
offered only when it contains a compatible APK and has a version newer than the
installed app. The GitHub release body is displayed as the in-app changelog.

## Release checklist

1. Update `CHANGELOG.md`.
2. Increment `MARKETING_VERSION` and `CURRENT_PROJECT_VERSION` in
   `iosApp/Configuration/Version.xcconfig`.
3. Build and test `fullRelease`.
4. Confirm the release contents with the repository owner.
5. Run the Stable workflow in `publish` mode, or the Beta workflow for prerelease validation.
