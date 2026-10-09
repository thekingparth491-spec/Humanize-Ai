package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.HistoryDao
import com.example.data.database.HistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HistoryDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: HistoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.historyDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndGetHistory() = runBlocking {
        val entry = HistoryEntity(
            toolType = "CHECKER",
            title = "Sample Check",
            inputText = "This is a test prompt.",
            outputText = "Likely Human",
            styleSelected = "Standard",
            aiScore = 15,
            humanScore = 85,
            wordCount = 5
        )

        val id = dao.insert(entry)
        assertTrue(id > 0)

        val list = dao.getAllHistory().first()
        assertEquals(1, list.size)
        assertEquals("Sample Check", list[0].title)
        assertEquals(15, list[0].aiScore)
        assertEquals(85, list[0].humanScore)
    }

    @Test
    fun testDeleteAndClearHistory() = runBlocking {
        val e1 = HistoryEntity(toolType = "HUMANIZE", title = "H1", inputText = "In1", outputText = "Out1", styleSelected = "Natural")
        val e2 = HistoryEntity(toolType = "AI_STYLE", title = "H2", inputText = "In2", outputText = "Out2", styleSelected = "Formal")

        val id1 = dao.insert(e1)
        dao.insert(e2)

        var list = dao.getAllHistory().first()
        assertEquals(2, list.size)

        dao.deleteById(id1)
        list = dao.getAllHistory().first()
        assertEquals(1, list.size)
        assertEquals("H2", list[0].title)

        dao.clearAll()
        list = dao.getAllHistory().first()
        assertTrue(list.isEmpty())
    }
}
