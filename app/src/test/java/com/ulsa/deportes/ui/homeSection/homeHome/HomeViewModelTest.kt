package com.ulsa.deportes.ui.homeSection.homeHome

import com.ulsa.deportes.domain.repository.SportsRepository
import com.ulsa.deportes.ui.homeSection.homeHome.model.*
import com.ulsa.deportes.ui.homeSection.homeHome.viewmodel.HomeUiState
import com.ulsa.deportes.ui.homeSection.homeHome.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSportsRepository
    private lateinit var viewModel: HomeViewModel

    class FakeSportsRepository(
        var resultProvider: suspend () -> HomeData
    ) : SportsRepository {
        override suspend fun getHomeData(): HomeData = resultProvider()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadHomeSuccess() = runTest {
        val sampleData = HomeData(
            scores = listOf(MatchScore(1, "Fútbol", "En curso", "2026-03-01T15:00:00Z", Team(1, "A", "C", "A", ""), Team(2, "B", "C", "B", ""), ScoreResult(1, 0), "Summary")),
            news = listOf(NewsItem(1, "Title", "Desc", "Content", "img", "author", "date")),
            events = listOf(SportEvent(1, "Event", "Fútbol", "img", "start", "end", "venue", "ann", emptyList()))
        )
        fakeRepository = FakeSportsRepository { sampleData }
        viewModel = HomeViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(sampleData, (state as HomeUiState.Success).data)
    }

    @Test
    fun testLoadHomeErrorOnIoException() = runTest {
        fakeRepository = FakeSportsRepository { throw IOException("Network down") }
        viewModel = HomeViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals(com.ulsa.deportes.R.string.error_no_connection, (state as HomeUiState.Error).messageRes)
    }

    @Test
    fun testLoadHomeErrorOnHttp401() = runTest {
        val response = Response.error<HomeData>(401, "".toResponseBody(null))
        fakeRepository = FakeSportsRepository { throw HttpException(response) }
        viewModel = HomeViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Error)
        assertEquals(com.ulsa.deportes.R.string.error_server, (state as HomeUiState.Error).messageRes)
    }

    @Test(expected = NullPointerException::class)
    fun testLoadHomeRethrowsProgrammingException() = runTest {
        fakeRepository = FakeSportsRepository { throw NullPointerException("Null pointer bug") }
        viewModel = HomeViewModel(fakeRepository)
        advanceUntilIdle()
    }
}
