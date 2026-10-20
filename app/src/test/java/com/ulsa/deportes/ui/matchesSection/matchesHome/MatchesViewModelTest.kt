package com.ulsa.deportes.ui.matchesSection.matchesHome

import com.ulsa.deportes.domain.repository.SportsRepository
import com.ulsa.deportes.ui.homeSection.homeHome.model.*
import com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel.MatchSortOption
import com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel.MatchStatusFilter
import com.ulsa.deportes.ui.matchesSection.matchesHome.viewmodel.MatchesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSportsRepository
    private lateinit var viewModel: MatchesViewModel

    class FakeSportsRepository(var homeData: HomeData) : SportsRepository {
        override suspend fun getHomeData(): HomeData = homeData
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createSampleMatches(): List<MatchScore> {
        val teamA = Team(1, "Águilas ULSA", "CDMX", "AGU", "")
        val teamB = Team(2, "Búhos La Salle", "Monterrey", "BUH", "")
        val teamC = Team(3, "Tigres", "Guadalajara", "TIG", "")

        return listOf(
            MatchScore(
                id = 1,
                sport = "Básquetbol",
                status = "Finalizado",
                playedAt = "2026-03-01T15:00:00Z",
                homeTeam = teamA,
                awayTeam = teamB,
                score = ScoreResult(85, 80),
                summary = "Gimnasio La Salle"
            ),
            MatchScore(
                id = 2,
                sport = "Fútbol",
                status = "En curso",
                playedAt = "2026-03-10T18:00:00Z",
                homeTeam = teamB,
                awayTeam = teamC,
                score = ScoreResult(2, 2),
                summary = "Estadio Principal"
            ),
            MatchScore(
                id = 3,
                sport = "Voleibol",
                status = "Próximo",
                playedAt = "2026-03-20T12:00:00Z",
                homeTeam = teamC,
                awayTeam = teamA,
                score = ScoreResult(0, 0),
                summary = "Auditorio"
            )
        )
    }

    @Test
    fun testSearchByTeam() = runTest {
        val data = HomeData(scores = createSampleMatches(), news = emptyList(), events = emptyList())
        fakeRepository = FakeSportsRepository(data)
        viewModel = MatchesViewModel(fakeRepository, testDispatcher)
        advanceUntilIdle()

        viewModel.setSearchQuery("Águilas")
        advanceUntilIdle()

        val filtered = viewModel.filteredMatches.value
        assertEquals(2, filtered.size)
    }

    @Test
    fun testSearchBySportAndVenue() = runTest {
        val data = HomeData(scores = createSampleMatches(), news = emptyList(), events = emptyList())
        fakeRepository = FakeSportsRepository(data)
        viewModel = MatchesViewModel(fakeRepository, testDispatcher)
        advanceUntilIdle()

        viewModel.setSearchQuery("Gimnasio")
        advanceUntilIdle()
        assertEquals(1, viewModel.filteredMatches.value.size)
        assertEquals("Básquetbol", viewModel.filteredMatches.value[0].sport)
    }

    @Test
    fun testSearchAccentInsensitiveAndCaseInsensitive() = runTest {
        val data = HomeData(scores = createSampleMatches(), news = emptyList(), events = emptyList())
        fakeRepository = FakeSportsRepository(data)
        viewModel = MatchesViewModel(fakeRepository, testDispatcher)
        advanceUntilIdle()

        viewModel.setSearchQuery("basquetbol")
        advanceUntilIdle()

        val filtered = viewModel.filteredMatches.value
        assertEquals(1, filtered.size)
        assertEquals("Básquetbol", filtered[0].sport)
    }

    @Test
    fun testCombinedSearchAndFilters() = runTest {
        val data = HomeData(scores = createSampleMatches(), news = emptyList(), events = emptyList())
        fakeRepository = FakeSportsRepository(data)
        viewModel = MatchesViewModel(fakeRepository, testDispatcher)
        advanceUntilIdle()

        viewModel.setSelectedSport("Fútbol")
        viewModel.setSelectedStatus(MatchStatusFilter.LIVE)
        viewModel.setSearchQuery("Tigres")
        advanceUntilIdle()

        val filtered = viewModel.filteredMatches.value
        assertEquals(1, filtered.size)
        assertEquals("Fútbol", filtered[0].sport)
    }

    @Test
    fun testSortingCriteria() = runTest {
        val data = HomeData(scores = createSampleMatches(), news = emptyList(), events = emptyList())
        fakeRepository = FakeSportsRepository(data)
        viewModel = MatchesViewModel(fakeRepository, testDispatcher)
        advanceUntilIdle()

        viewModel.setSelectedSort(MatchSortOption.RECENT)
        advanceUntilIdle()
        assertEquals(3, viewModel.filteredMatches.value[0].id)

        viewModel.setSelectedSort(MatchSortOption.UPCOMING)
        advanceUntilIdle()
        assertEquals(1, viewModel.filteredMatches.value[0].id)

        viewModel.setSelectedSort(MatchSortOption.SCORE_DIFFERENCE)
        advanceUntilIdle()
        assertEquals(1, viewModel.filteredMatches.value[0].id)
    }

    @Test
    fun testEmptyStateAndClearFilters() = runTest {
        val data = HomeData(scores = createSampleMatches(), news = emptyList(), events = emptyList())
        fakeRepository = FakeSportsRepository(data)
        viewModel = MatchesViewModel(fakeRepository, testDispatcher)
        advanceUntilIdle()

        viewModel.setSearchQuery("NonExistentTeam")
        advanceUntilIdle()
        assertTrue(viewModel.filteredMatches.value.isEmpty())

        viewModel.clearFilters()
        advanceUntilIdle()
        assertEquals(3, viewModel.filteredMatches.value.size)
        assertEquals("", viewModel.searchQuery.value)
    }
}
