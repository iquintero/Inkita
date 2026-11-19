package net.dom53.inkita.ui.library

import net.dom53.inkita.domain.model.Collection
import net.dom53.inkita.domain.model.Library
import net.dom53.inkita.domain.model.Person
import net.dom53.inkita.domain.model.ReadingList
import net.dom53.inkita.ui.common.DownloadState

/** Fraction of a series that has been read, or null when the server didn't report page counts. */
fun seriesProgress(
    pagesRead: Int?,
    pages: Int?,
): Float? {
    val total = pages?.takeIf { it > 0 } ?: return null
    return ((pagesRead ?: 0).toFloat() / total).coerceIn(0f, 1f)
}

enum class LibraryV2Section {
    Home,
    WantToRead,
    Collections,
    ReadingList,
    BrowsePeople,
    LibrarySeries,
}

data class LibraryV2UiState(
    val libraries: List<Library> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Every series, most recently read first. */
    val homeSeries: List<net.dom53.inkita.domain.model.Series> = emptyList(),
    val isHomeLoading: Boolean = true,
    val isHomeLoadingMore: Boolean = false,
    val homeLoadFailed: Boolean = false,
    val homeSeriesPage: Int = 1,
    val canLoadMoreHomeSeries: Boolean = true,
    val wantToRead: List<net.dom53.inkita.domain.model.Series> = emptyList(),
    val isWantToReadLoading: Boolean = false,
    val wantToReadError: String? = null,
    val collections: List<Collection> = emptyList(),
    val isCollectionsLoading: Boolean = false,
    val collectionsError: String? = null,
    val selectedCollectionId: Int? = null,
    val selectedCollectionName: String? = null,
    val collectionSeries: List<net.dom53.inkita.domain.model.Series> = emptyList(),
    val isCollectionSeriesLoading: Boolean = false,
    val collectionSeriesError: String? = null,
    val readingLists: List<ReadingList> = emptyList(),
    val isReadingListsLoading: Boolean = false,
    val readingListsError: String? = null,
    val people: List<Person> = emptyList(),
    val isPeopleLoading: Boolean = false,
    val peopleError: String? = null,
    val peoplePage: Int = 1,
    val canLoadMorePeople: Boolean = true,
    val isPeopleLoadingMore: Boolean = false,
    val selectedLibraryId: Int? = null,
    val selectedLibraryName: String? = null,
    val librarySeries: List<net.dom53.inkita.domain.model.Series> = emptyList(),
    val isLibrarySeriesLoading: Boolean = false,
    val isLibrarySeriesLoadingMore: Boolean = false,
    val librarySeriesError: String? = null,
    val librarySeriesPage: Int = 1,
    val canLoadMoreLibrarySeries: Boolean = true,
    val libraryAccessDenied: Boolean = false,
    val selectedSection: LibraryV2Section = LibraryV2Section.Home,
    val downloadStates: Map<Int, DownloadState> = emptyMap(),
)
