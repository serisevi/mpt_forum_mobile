package com.example.forummpt

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Rule
import org.junit.runner.RunWith
import androidx.test.rule.ActivityTestRule
import org.junit.Test

@RunWith(AndroidJUnit4ClassRunner::class)
class NewTest {

    @Rule
    @JvmField
    var main = ActivityTestRule<MainActivity>(MainActivity::class.java)


    @Test
    fun testUserInputScenario() {
        // Exit your account before running test
        onView(withId(R.id.activityMain)).check(matches(isDisplayed()))
        onView(withId(R.id.btnRegister)).perform(click())
        onView(withId(R.id.activityRegistration)).check(matches(isDisplayed()))
        Espresso.pressBack()
        onView(withId(R.id.btnAuth)).perform(click())
        onView(withId(R.id.activityAuthorization)).check(matches(isDisplayed()))
        onView(withId(R.id.tvAuthRestore)).perform(click())
        onView(withId(R.id.activityRestoration)).check(matches(isDisplayed()))
        Espresso.pressBack()
        onView(withId(R.id.editAuthLogin)).perform(typeText("serisevi"))
        Espresso.pressBack()
        onView(withId(R.id.editAuthPassword)).perform(typeText("Sardo243"))
        Espresso.pressBack()
        onView(withId(R.id.btnAuthOk)).perform(click())
        onView(withId(R.id.activityThreads)).check(matches(isDisplayed()))
        onView(withId(R.id.btnThreadsAdd)).perform(click())
        onView(withId(R.id.thread_add_name)).inRoot(isDialog()).perform(replaceText("Тестовое обсуждение"))
        onView(withId(R.id.thread_add_desc)).inRoot(isDialog()).perform(replaceText("Создано espresso"))
        onView(withText("Создать")).inRoot(isDialog()).perform(click())
        onView(withId(R.id.btnThreadsCancel)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.threads_recycler)).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.activityMessages)).check(matches(isDisplayed()))
        onView(withId(R.id.btnMessagesAdd)).perform(click())
        onView(withId(R.id.message_add_text)).inRoot(isDialog()).perform(replaceText("Тестовый текст"))
        onView(withText("Отправить")).inRoot(isDialog()).perform(click())
        Thread.sleep(500)
        Espresso.pressBack()
        onView(withId(R.id.ivMenuSettings)).perform(click())
        onView(withId(R.id.cmbSettingsTheme)).perform(click())
        onView(withText("Системная")).perform(click())
        onView(withId(R.id.cmbSettingsAccent)).perform(click())
        onView(withText("Мятный")).perform(click())
        onView(withId(R.id.ivMenuAccount)).perform(click())
        onView(withId(R.id.btnAccountChangePassword)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.btnAccountEdit)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.btnAccountNotifications)).perform(click())
        onView(withId(R.id.activityNotifications)).check(matches(isDisplayed()))
        Espresso.pressBack()
        onView(withId(R.id.btnAccountExit)).perform(click())
        onView(withId(R.id.activityAuthorization)).check(matches(isDisplayed()))
        Thread.sleep(500)
    }

}
