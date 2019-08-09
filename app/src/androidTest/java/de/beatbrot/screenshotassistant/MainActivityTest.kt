package de.beatbrot.screenshotassistant

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import de.beatbrot.screenshotassistant.custom.checkCropSuccessful
import de.beatbrot.screenshotassistant.custom.containsImage
import de.beatbrot.screenshotassistant.custom.cropImageByHalf
import de.beatbrot.screenshotassistant.custom.launchesActivity
import org.hamcrest.Matchers.*
import org.junit.Before
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
        onView(withId(R.id.screenShot))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testImageIsLoaded() {
        onView(withId(R.id.screenShot))
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
    fun testCroppingWorks() {
        onView(withId(R.id.screenShot))
            .perform(cropImageByHalf())
            .check(checkCropSuccessful())
    }

    @Test
    fun testShareOpens() {
        onView(withId(R.id.button))
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

            outputStream().apply {
                write(buffer)
                close()
            }
        }

        return Uri.fromFile(scrFile)
    }
}