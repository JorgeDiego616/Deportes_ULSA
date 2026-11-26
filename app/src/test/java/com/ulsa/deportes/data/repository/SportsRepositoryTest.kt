package com.ulsa.deportes.data.repository

import com.google.gson.JsonSyntaxException
import com.ulsa.deportes.ui.homeSection.homeHome.model.HomeData
import com.ulsa.deportes.ui.homeSection.homeHome.network.HomeService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SportsRepositoryTest {

    class FakeHomeService(
        val action: suspend () -> HomeData
    ) : HomeService {
        override suspend fun getHomeData(): HomeData = action()
    }

    @Test
    fun testFallbackOnIoException() = runTest {
        val service = FakeHomeService { throw IOException("Network timeout") }
        val repository = SportsRepositoryImpl(service)
        val data = repository.getHomeData()

        assertNotNull(data)
        assertFalse(data.scores.isEmpty())
        assertTrue(data.scores[0].summary.contains("demostración", ignoreCase = true))
    }

    @Test
    fun testFallbackOnHttp503() = runTest {
        val response = Response.error<HomeData>(503, "".toResponseBody(null))
        val service = FakeHomeService { throw HttpException(response) }
        val repository = SportsRepositoryImpl(service)
        val data = repository.getHomeData()

        assertNotNull(data)
        assertFalse(data.scores.isEmpty())
    }

    @Test(expected = HttpException::class)
    fun testPropagatesHttp404() = runTest {
        val response = Response.error<HomeData>(404, "".toResponseBody(null))
        val service = FakeHomeService { throw HttpException(response) }
        val repository = SportsRepositoryImpl(service)
        repository.getHomeData()
    }

    @Test(expected = JsonSyntaxException::class)
    fun testPropagatesJsonSyntaxException() = runTest {
        val service = FakeHomeService { throw JsonSyntaxException("Malformed JSON") }
        val repository = SportsRepositoryImpl(service)
        repository.getHomeData()
    }
}
