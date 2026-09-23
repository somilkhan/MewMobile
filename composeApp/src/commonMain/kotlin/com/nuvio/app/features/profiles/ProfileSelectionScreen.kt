@Composable
private fun ProfileAvatarCard(
    profile: NuvioProfile,
    avatars: List<AvatarCatalogItem>,
    isEditMode: Boolean,
    animDelay: Int,
    onClick: () -> Unit,
) {
    val avatarColor = remember(profile.avatarColorHex) {
        parseHexColor(profile.avatarColorHex)
    }
    val avatarItem = remember(profile.avatarId, avatars) {
        profile.avatarId?.let { id -> avatars.find { it.id == id } }
    }
    val avatarImageUrl = remember(profile.avatarUrl, avatarItem) {
        profileAvatarImageUrl(profile, avatarItem)
    }
    val context = LocalPlatformContext.current
    val imageRequest = remember(context, avatarImageUrl) {
        avatarImageUrl?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(220)
                .crossfade(false)
                .memoryCacheKey("profile-avatar:$it")
                .diskCacheKey("profile-avatar:$it")
                .build()
        }
    }

    val animProgress = remember { Animatable(0f) }
