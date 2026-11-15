package com.nuvio.app.features.cloudstream

/**
 * CloudStream extensions sometimes return the generic section name "Raw Catalog" for every
 * MainPageData request. Prefer the request's declared name in that case so Home can expose the
 * actual categories instead of a stack of indistinguishable rows.
 */
internal fun cloudStreamMainPageSectionTitle(
    apiName: String,
    requestName: String,
    sectionName: String,
    multipleApis: Boolean,
): String {
    val normalizedRequest = requestName.trim()
    val normalizedSection = sectionName.trim()
    val displayName = when {
        normalizedSection.isBlank() -> normalizedRequest
        normalizedSection.equals("Raw Catalog", ignoreCase = true) && normalizedRequest.isNotBlank() ->
            normalizedRequest
        else -> normalizedSection
    }.ifBlank { "Raw Catalog" }

    val normalizedApiName = apiName.trim()
    return if (multipleApis && normalizedApiName.isNotBlank() &&
        !displayName.equals(normalizedApiName, ignoreCase = true)
    ) {
        "$normalizedApiName · $displayName"
    } else {
        displayName.ifBlank { normalizedApiName }
    }
}
