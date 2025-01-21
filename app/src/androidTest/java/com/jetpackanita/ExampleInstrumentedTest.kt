package com.jetpackanita

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.jetpackanita.ui.theme.JetpackAnitaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.jetpackanita", appContext.packageName)
//    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginAndGetSingleUser() {
        composeTestRule.setContent {
            var isLoggedIn by remember { mutableStateOf(false) }

            JetpackAnitaTheme {
                if (isLoggedIn) {
                    HomePageScreen(onLogout = { isLoggedIn = false })
                } else {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                }
            }
        }

        val userNameData = "eve.holt@reqres.in"
        val passwordData = "cityslicka"
        val expectedFullName = "Janet Weaver"
        val expectedEmail = "janet.weaver@reqres.in"

        composeTestRule.onNodeWithText("Username").performTextInput(userNameData)
        composeTestRule.onNodeWithText("Password").performTextInput(passwordData)
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Welcome to the Home Page!")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Welcome to the Home Page!").assertIsDisplayed()

        composeTestRule.onNodeWithText("Get Single User").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Janet Weaver").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(expectedFullName).assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedEmail).assertIsDisplayed()
    }

    @Test
    fun testLoginFailureEmptyFields() {
        composeTestRule.setContent {
            JetpackAnitaTheme {
                LoginScreen(onLoginSuccess = { /* Mock success */ })
            }
        }

        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Please enter both username and password.")
            .assertIsDisplayed()
    }

    @Test
    fun testLoginFailureInvalidCredentials() {
        composeTestRule.setContent {
            JetpackAnitaTheme {
                LoginScreen(onLoginSuccess = { /* Mock success */ })
            }
        }

        composeTestRule.onNodeWithText("Username").performTextInput("wrong@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpassword")

        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Invalid username or password.").assertIsDisplayed()
    }
}