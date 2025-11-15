package com.latergator

import android.content.ContentValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.latergator.data.DatabaseHelper
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseOperationsTest {

    private lateinit var dbHelper: DatabaseHelper
    private val testPackageName = "com.latergator.testapp"

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        dbHelper = DatabaseHelper(context)
        clearTestData()
        insertTestApp()
    }

    @After
    fun tearDown() {
        clearTestData()
        dbHelper.close()
    }

    private fun clearTestData() {
        val db = dbHelper.writableDatabase
        // Clear profile table for profile tests
        db.delete("profile", null, null)

        // Clear app data for app tracking tests
        val cursor = db.rawQuery("SELECT id FROM apps WHERE package_name = ?", arrayOf(testPackageName))
        if (cursor.moveToFirst()) {
            val appId = cursor.getLong(0)
            db.delete("time_limit_prefs", "app_id = ?", arrayOf(appId.toString()))
        }
        cursor.close()
        db.delete("apps", "package_name = ?", arrayOf(testPackageName))
    }

    private fun insertTestApp() {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("package_name", testPackageName)
            put("label", "Test App")
            put("is_tracked", 0)
            put("created_at_ms", System.currentTimeMillis())
            put("updated_at_ms", System.currentTimeMillis())
        }
        db.insert("apps", null, values)
    }

    @Test
    fun testUserProfileCreationAndUpdate() {
        // 1. Initially, no profile should exist
        assertFalse("Profile should not exist at the start of the test", dbHelper.hasProfile())

        // 2. Create a user profile
        val initialName = "Test User"
        dbHelper.createUserProfile(initialName, "UTC")

        // 3. Verify profile creation and correct name
        assertTrue("Profile should exist after creation", dbHelper.hasProfile())
        assertEquals("Initial username should be correct", initialName, dbHelper.getUserProfileName())

        // 4. Update the user profile name
        val updatedName = "Updated Name"
        dbHelper.updateUserProfileName(updatedName)

        // 5. Verify the name was updated
        assertEquals("Username should be updated correctly", updatedName, dbHelper.getUserProfileName())
    }

    @Test
    fun testIgnoreBlankName() {
        // 1. Attempt to create a user with a blank name
        dbHelper.createUserProfile("   ", "UTC")
        assertFalse("Profile should not be created with a blank name", dbHelper.hasProfile())

        // 2. Attempt to create a user with an empty name
        dbHelper.createUserProfile("", "UTC")
        assertFalse("Profile should not be created with an empty name", dbHelper.hasProfile())

        // 3. Create a user with an initial, valid name
        val initialName = "Original Name"
        dbHelper.createUserProfile(initialName, "UTC")
        assertTrue("Profile should be created with a valid name", dbHelper.hasProfile())

        // 4. Attempt to update the name to a blank string
        dbHelper.updateUserProfileName("   ")

        // 5. Verify that the name has NOT changed
        assertEquals("Username should not be updated to a blank string", initialName, dbHelper.getUserProfileName())

        // 6. Attempt to update the name to an empty string
        dbHelper.updateUserProfileName("")

        // 7. Verify that the name has STILL not changed
        assertEquals("Username should not be updated to an empty string", initialName, dbHelper.getUserProfileName())
    }

    @Test
    fun testOperationsOnNonExistentApp() {
        val nonExistentPackage = "com.this.app.does.not.exist"

        // These operations should not throw an exception.
        try {
            dbHelper.setAppTrackingAndActiveStatus(nonExistentPackage, true)
            dbHelper.updateTimeLimit(nonExistentPackage, 60)
        } catch (e: Exception) {
            fail("Operations on a non-existent app should not throw an exception. Threw: $e")
        }

        // Verify that the non-existent app was not added to the tracked list
        val trackedApps = dbHelper.getTrackedApps()
        assertFalse("Non-existent app should not be in the tracked list", trackedApps.containsKey(nonExistentPackage))
    }

    @Test
    fun testUpdatingExistingTimeLimit() {
        // 1. Track the app and set an initial limit
        dbHelper.setAppTrackingAndActiveStatus(testPackageName, true)
        dbHelper.updateTimeLimit(testPackageName, 30)
        var trackedApps = dbHelper.getTrackedApps()
        assertEquals("Time limit should be initially set to 30", 30, trackedApps[testPackageName])

        // 2. Update the limit to a new value
        dbHelper.updateTimeLimit(testPackageName, 60)

        // 3. Verify the limit has been updated
        trackedApps = dbHelper.getTrackedApps()
        assertEquals("Time limit should be updated to 60", 60, trackedApps[testPackageName])
    }

    @Test
    fun testClearingTimeLimit() {
        // 1. Track the app and set an initial time limit
        dbHelper.setAppTrackingAndActiveStatus(testPackageName, true)
        val timeLimit = 45
        dbHelper.updateTimeLimit(testPackageName, timeLimit)

        // 2. Verify the time limit was set correctly
        var trackedApps = dbHelper.getTrackedApps()
        assertEquals("Time limit should be initially set", timeLimit, trackedApps[testPackageName])

        // 3. Clear the time limit by passing null
        dbHelper.updateTimeLimit(testPackageName, null)

        // 4. Verify the time limit has been cleared and is now null
        trackedApps = dbHelper.getTrackedApps()
        assertTrue("App should still be tracked after clearing limit", trackedApps.containsKey(testPackageName))
        assertNull("Time limit should be null after being cleared", trackedApps[testPackageName])
    }

    @Test
    fun testPreservedLimit() {
        // 1. Initially, no apps should be tracked
        var trackedApps = dbHelper.getTrackedApps()
        assertFalse("Test app should not be tracked initially", trackedApps.containsKey(testPackageName))

        // 2. Track the app
        dbHelper.setAppTrackingAndActiveStatus(testPackageName, true)
        trackedApps = dbHelper.getTrackedApps()
        assertTrue("Test app should now be tracked", trackedApps.containsKey(testPackageName))
        assertNull("Newly tracked app should have no limit (null)", trackedApps[testPackageName])

        // 3. Set a time limit
        val timeLimit = 30
        dbHelper.updateTimeLimit(testPackageName, timeLimit)
        trackedApps = dbHelper.getTrackedApps()
        assertEquals("Time limit should be set to 30", timeLimit, trackedApps[testPackageName])

        // 4. Untrack the app
        dbHelper.setAppTrackingAndActiveStatus(testPackageName, false)
        trackedApps = dbHelper.getTrackedApps()
        assertFalse("Test app should be gone from tracked list", trackedApps.containsKey(testPackageName))

        // 5. Re-track the app
        dbHelper.setAppTrackingAndActiveStatus(testPackageName, true)
        trackedApps = dbHelper.getTrackedApps()
        assertTrue("Test app should be tracked again", trackedApps.containsKey(testPackageName))


        // This assertion documents the limit is retained.
        assertEquals("Re-tracked app should retain its stale limit", timeLimit, trackedApps[testPackageName])
    }
}