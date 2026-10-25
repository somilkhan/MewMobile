package com.nuvio.app.features.home

import kotlin.test.Test
import kotlin.test.assertEquals

class HomePinnedCollectionOrderTest {
    @Test
    fun pinnedCollectionsStayFirstAfterManualReorder() {
        assertEquals(
            listOf("collection-a", "collection-b", "catalog-c", "catalog-d"),
            pinnedCollectionsFirst(
                orderedKeys = listOf("catalog-c", "collection-a", "catalog-d", "collection-b"),
                pinnedCollectionKeys = setOf("collection-a", "collection-b"),
            ),
        )
    }

    @Test
    fun pinnedAndUnpinnedRelativeOrderIsPreserved() {
        assertEquals(
            listOf("collection-b", "catalog-a", "catalog-c"),
            pinnedCollectionsFirst(
                orderedKeys = listOf("catalog-a", "collection-b", "catalog-c"),
                pinnedCollectionKeys = setOf("collection-b"),
            ),
        )
    }

    @Test
    fun noPinnedCollectionsLeavesOrderUnchanged() {
        val order = listOf("catalog-a", "catalog-b")
        assertEquals(order, pinnedCollectionsFirst(order, emptySet()))
    }
}
