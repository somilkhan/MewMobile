package com.nuvio.app

// Mew hook: luq settings root uses AppLoadingProfileAvatar from luq AppShellComponents.kt.
// Mew keeps its own app shell, so the composable is provided here.

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.NuvioAsyncImage
import com.nuvio.app.features.profiles.AvatarRepository
import com.nuvio.app.features.profiles.NuvioProfile
import com.nuvio.app.features.profiles.parseHexColor
import com.nuvio.app.features.profiles.profileAvatarImageUrl

@Composable
internal fun AppLoadingProfileAvatar(
    profile: NuvioProfile,
    modifier: Modifier = Modifier,
) {
    val avatarColor = remember(profile.avatarColorHex) { parseHexColor(profile.avatarColorHex) }
    val avatars by remember { AvatarRepository.avatars }.collectAsStateWithLifecycle()
    val avatarItem = remember(profile.avatarId, avatars) {
        profile.avatarId?.let { id -> avatars.find { it.id == id } }
    }
    val avatarImageUrl = remember(profile.avatarUrl, avatarItem) {
        profileAvatarImageUrl(profile, avatarItem)
    }
    val backgroundColor = avatarItem?.bgColor?.let(::parseHexColor) ?: avatarColor

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (avatarImageUrl != null) backgroundColor else backgroundColor.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            avatarImageUrl != null -> NuvioAsyncImage(
                imageUrl = avatarImageUrl,
                contentDescription = avatarItem?.displayName ?: profile.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            profile.name.isNotBlank() -> Text(
                text = profile.name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                color = avatarColor,
                fontWeight = FontWeight.Bold,
            )
            else -> Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = avatarColor,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}
