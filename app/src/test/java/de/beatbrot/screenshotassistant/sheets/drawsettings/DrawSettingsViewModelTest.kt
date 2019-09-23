package de.beatbrot.screenshotassistant.sheets.drawsettings

import android.graphics.Color
import android.graphics.Paint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DrawSettingsViewModelTest {

    private lateinit var viewModel: DrawSettingsViewModel

    private val alibiObserver: Observer<Any> = Observer { }

    init {
        ArchTaskExecutor.getInstance().setDelegate(TestExecutor())
    }

    @Before
    fun initVM() {
        // We need to observe everything we want to access with .value
        viewModel = DrawSettingsViewModel().apply {
            strokeColor.observeForever(alibiObserver)
            editingMode.observeForever(alibiObserver)
            markerColor.observeForever(alibiObserver)
            penColor.observeForever(alibiObserver)
            strokeWidth.observeForever(alibiObserver)
            strokeCap.observeForever(alibiObserver)
        }
    }

    @Test
    fun testInitialValue() {
        assertLDEquals(Color.BLACK, viewModel.penColor)
        assertLDEquals(-0x14C5, viewModel.markerColor)
        assertLDEquals(Color.BLACK, viewModel.strokeColor)
    }

    @Test
    fun testModeSwitch() {
        viewModel.penColor.value = Color.RED
        viewModel.markerColor.value = Color.MAGENTA
        val penStrokeWidth = viewModel.strokeWidth.value!!
        assertLDEquals(Color.RED, viewModel.penColor)
        assertLDEquals(Color.MAGENTA, viewModel.markerColor)
        assertLDEquals(Color.RED, viewModel.strokeColor)
        assertLDEquals(Paint.Cap.ROUND, viewModel.strokeCap)

        viewModel.editingMode.value = DrawMode.MARKER

        assertLDEquals(Color.MAGENTA, viewModel.strokeColor)
        assertLDEquals(Paint.Cap.SQUARE, viewModel.strokeCap)
        assert(penStrokeWidth < viewModel.strokeWidth.value!!)
    }

    @Test
    fun testOnlyRelevantColorIsApplied() {
        assertLDEquals(Color.BLACK, viewModel.penColor)
        viewModel.markerColor.value = Color.MAGENTA
        assertLDEquals(Color.BLACK, viewModel.penColor)
        assertLDEquals(Color.BLACK, viewModel.penColor)

        viewModel.editingMode.value = DrawMode.MARKER
        assertLDEquals(Color.MAGENTA, viewModel.markerColor)
        viewModel.penColor.value = Color.RED
        assertLDEquals(Color.MAGENTA, viewModel.markerColor)
    }

    private fun <T> assertLDEquals(expected: T, actual: LiveData<T>) {
        try {
            assertEquals(expected, actual.value)
        } catch (err: AssertionError) {
            throw AssertionError(err.message, err.cause)
        }
    }

    private class TestExecutor : TaskExecutor() {
        override fun executeOnDiskIO(r: Runnable) = r.run()

        override fun isMainThread() = true

        override fun postToMainThread(r: Runnable) = r.run()
    }
}
