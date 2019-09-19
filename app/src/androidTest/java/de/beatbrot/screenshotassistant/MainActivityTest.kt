package de.beatbrot.screenshotassistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import de.beatbrot.imagepainter.view.ImagePainterView
import de.beatbrot.screenshotassistant.custom.*
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    @get:Rule
    val activityRule = IntentsTestRule(MainActivity::class.java, false, false)

    @Before
    fun launchActivity() {
        val startIntent = Intent(context, MainActivity::class.java)
        startIntent.putExtra("screenshot", copyScreenshotInCache(context))
        activityRule.launchActivity(startIntent)
    }

    @Test
    fun testActivityLaunches() {
        onView(withId(R.id.cropView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testImageIsLoaded() {
        onView(withId(R.id.cropView))
            .check(matches(containsImage()))
    }

    @Test
    fun testAboutLaunches() {
        onView(withId(R.id.menuButton))
            .perform(click())

        onView(withText(R.string.about))
            .perform(click())

        intended(launchesActivity(AboutActivity::class.java))
    }

    @Test
    fun testSettingsLoad() {
        onView(withId(R.id.menuButton))
            .perform(click())

        onView(withText(R.string.title_sheet_settings))
            .perform(click())

        onView(withText(R.string.image_quality))
            .check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun testCroppingWorks() {
        onView(withId(R.id.cropView))
            .perform(cropImageByHalf())
            .check(checkCropSuccessful())
    }

    @Test
    fun testImageEditorLoads() {
        onView(withId(R.id.drawButton))
            .perform(click())

        onView(withId(R.id.imagePainter))
            .check(matches(isCompletelyDisplayed()))
    }

    @Test
    @Ignore("Messes with other tests")
    fun testShareOpens() {
        onView(withId(R.id.shareButton))
            .perform(click())

        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(
                    equalTo(Intent.EXTRA_INTENT), allOf(
                        hasExtra(
                            equalTo(Intent.EXTRA_STREAM),
                            hasToString<Uri>(containsString(AUTHORITY_NAME))
                        ),
                        hasType(MIME_TYPE)
                    )
                )
            )
        )

        val dev = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        dev.pressHome()
    }

    @Test
    fun testCropRectIsPersistent() {
        onView(withId(R.id.cropView))
            .perform(cropImageByHalf())
            .check(checkCropSuccessful())

        enableNightMode()

        onView(withId(R.id.cropView))
            .check(checkCropSuccessful())
    }

    @Test
    fun testDrawStackIsPersistent() {
        onView(withId(R.id.drawButton))
            .perform(click())

        onView(withId(R.id.imagePainter))
            .perform(draw())

        var painter: ImagePainterView = activityRule.activity.findViewById(R.id.imagePainter)
        assertTrue(painter.canUndo())
        assertFalse(painter.canRedo())
        val oldStack = painter.drawStack

        enableNightMode()

        painter = activityRule.activity.findViewById(R.id.imagePainter)
        assertTrue(painter.canUndo())
        assertFalse(painter.canRedo())
        val newStack = painter.drawStack
        assertEquals(oldStack, newStack)
        assertEquals(oldStack.hashCode(), newStack.hashCode())

        onView(withId(R.id.undoButton))
            .perform(click())

        assertTrue(painter.canUndo())
        assertFalse(painter.canRedo())
    }

    private fun enableNightMode() {
        activityRule.runOnUiThread {
            activityRule.activity.delegate.apply {
                localNightMode = MODE_NIGHT_YES
                applyDayNight()
            }
        }
    }

    private fun copyScreenshotInCache(context: Context): Uri {
        val scrStream = testContext.assets.open("scr.png")
        val buffer = ByteArray(scrStream.available())
        scrStream.read(buffer)

        val scrDir = File(context.cacheDir, "screenshots")
        val scrFile = File(scrDir, "screen.png")

        scrDir.mkdir()

        scrFile.apply {
            if (!exists()) {
                delete()
            }
            createNewFile()

            outputStream().use { stream ->
                stream.write(buffer)
            }
        }

        return Uri.fromFile(scrFile)
    }
}
