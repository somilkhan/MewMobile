# Security and credential handling

MewMobile keeps CI signing material and private build credentials out of source control.

## Credential matrix

| Credential | Used by | Required for | Safe in APK? | Action |
|---|---|---|---|---|
| `MEW_LOCAL_PROPERTIES_BASE64` | Android release/beta workflows | Release signing properties and release-only configuration | No | Keep as GitHub Actions secret. |
| `MEW_RELEASE_KEYSTORE_BASE64` | Android release/beta workflows | APK signing | No | Keep as GitHub Actions secret. |
| `NUVIO_LOCAL_PROPERTIES_BASE64` | Release compatibility fallback | Existing CI installations using legacy secret names | No | Keep only as a migration fallback; prefer `MEW_*`. |
| `NUVIO_RELEASE_KEYSTORE_BASE64` | Release compatibility fallback | APK signing | No | Keep only as a migration fallback; prefer `MEW_*`. |
| `TRAKT_CLIENT_ID` | Release workflow | Trakt OAuth client identification | Yes, as a client identifier | May be supplied to the client; never treat it as a secret. |
| `SIMKL_CLIENT_ID` | Release/beta workflows | Simkl OAuth client identification | Yes, as a client identifier | May be supplied to the client. |
| `ANILIST_CLIENT_ID` | Release/beta workflows | AniList OAuth client identification | Yes, as a client identifier | May be supplied to the client. |
| `MAL_CLIENT_ID` | Release/beta workflows | MyAnimeList OAuth client identification | Yes, as a client identifier | May be supplied to the client. |
| `PREMIUMIZE_CLIENT_ID` | Release/beta workflows | Premiumize client identification | Yes, as a client identifier | May be supplied to the client. |
| `EXPECTED_ANDROID_SIGNING_SHA256` | Release/beta workflows | Signing verification | Yes | Public certificate fingerprint; not a secret. |

## Private credentials

The stable release workflow deliberately removes `TRAKT_CLIENT_SECRET` from the CI-generated `local.properties` before building the public APK. A client secret must not be embedded in an Android application.

The same rule applies to any future credential described as private by its provider. If a service requires a genuinely confidential server-side credential, MewMobile must use a trusted backend boundary rather than embedding the credential in the APK.

## KyBox audit

No KyBox/`KYBOX`/`KY_BOX` references were found by the available repository code search during this audit. This is not treated as proof that no historical or unindexed reference exists; before introducing KyBox credentials, inspect the exact integration and classify every value as public client configuration or server-side secret.

No KyBox secret has been added to source, workflows, or documentation by this mission.

## Signing

The release workflows verify every generated universal APK against the established Android signing certificate fingerprint before publication.

Never commit:

- keystores;
- keystore passwords;
- private API keys;
- OAuth client secrets;
- access tokens;
- decoded CI `local.properties` files.

