package com.nuvio.app.features.streams

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.nuvio.app.features.party.PartyRepository
import com.nuvio.app.features.party.PartySource
import com.nuvio.app.features.party.PartyStream
import com.nuvio.app.features.party.partyMediaKey
import com.nuvio.app.features.party.matches
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.party_shared_by
import nuvio.composeapp.generated.resources.party_source
import org.jetbrains.compose.resources.stringResource

internal data class SharedPartyStream(val label: String, val stream: StreamItem)

internal val LocalSharedStream = staticCompositionLocalOf<SharedPartyStream?> { null }
internal val LocalStreamHighlight = staticCompositionLocalOf<((StreamItem) -> String?)?> { null }

@Composable
internal fun PartyStreamsProvider(
    metaType: String,
    metaId: String,
    season: Int?,
    episode: Int?,
    content: @Composable (SharedPartyStream?) -> Unit,
) {
    val state by PartyRepository.state.collectAsState()
    val partyStream by PartyRepository.sharedStream.collectAsState()
    val party = state.party
    val contentKey = partyMediaKey(metaType, metaId, season, episode)
    val source: PartySource? = party?.media?.takeIf { it.key == contentKey }?.source
    val sourceLabel = stringResource(Res.string.party_source)
    val highlight: (StreamItem) -> String? = remember(source, sourceLabel) {
        { stream -> if (source?.matches(stream) == true) sourceLabel else null }
    }
    val sharedLabel = stringResource(Res.string.party_shared_by, party?.member(party.hostMemberId)?.displayName.orEmpty())
    val isHost = party?.isHost == true
    val shared = remember(partyStream, sharedLabel, isHost, contentKey) {
        partyStream
            ?.takeIf { !isHost && it.mediaKey == contentKey }
            ?.let { SharedPartyStream(sharedLabel, it.toStreamItem()) }
    }
    CompositionLocalProvider(
        LocalStreamHighlight provides highlight,
        LocalSharedStream provides shared,
    ) { content(shared) }
}
