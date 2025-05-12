package com.motor.diagnostic.data.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;

import com.motor.diagnostic.R;

/**
 * Utility class to manage app theme
 */
public class ThemeManager {
    private static final String TAG = "ThemeManager";

    /**
     * Apply the saved theme setting
     *
     * @param activity The activity to apply the theme to
     */
    public static void applyTheme(Activity activity) {
        try {
            boolean isDarkTheme = PreferencesManager.getInstance(activity).isDarkThemeEnabled();
            applyTheme(activity, isDarkTheme);
        } catch (Exception e) {
            Log.e(TAG, "Error applying saved theme", e);
        }
    }

    /**
     * Apply specified theme setting
     *
     * @param activity The activity to apply the theme to
     * @param isDarkTheme Whether to apply dark theme
     */
    public static void applyTheme(Activity activity, boolean isDarkTheme) {
        try {
            // First set the AppCompatDelegate mode - this is the proper order
            if (activity instanceof AppCompatActivity) {
                // Set the night mode before applying the theme
                int nightMode = isDarkTheme ? 
                    AppCompatDelegate.MODE_NIGHT_YES : 
                    AppCompatDelegate.MODE_NIGHT_NO;
                
                AppCompatDelegate.setDefaultNightMode(nightMode);
            }
            
            // Then set the theme resource
            if (isDarkTheme) {
                activity.setTheme(R.style.Theme_MotorDiagnostic_Dark);
            } else {
                activity.setTheme(R.style.Theme_MotorDiagnostic);
            }
            
            Log.d(TAG, "Applied theme: " + (isDarkTheme ? "dark" : "light"));
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme", e);
        }
    }

    /**
     * Toggle between light and dark theme
     *
     * @param activity The activity to apply the theme to
     * @return true if the theme is now dark, false if it's light
     */
    public static boolean toggleTheme(Activity activity) {
        try {
            PreferencesManager prefsManager = PreferencesManager.getInstance(activity);
            boolean newThemeIsDark = !prefsManager.isDarkThemeEnabled();
            
            // Save the new theme preference
            prefsManager.setDarkThemeEnabled(newThemeIsDark);
            
            // Apply the new theme
            applyTheme(activity, newThemeIsDark);
            
            // Recreate the activity to apply the theme change
            if (activity instanceof FragmentActivity) {
                activity.recreate();
            }
            
            return newThemeIsDark;
        } catch (Exception e) {
            Log.e(TAG, "Error toggling theme", e);
            return false;
        }
    }

    /**
     * Safely restarts the app to apply theme changes
     * 
     * @param activity The current activity
     */
    public static void restartApp(Activity activity) {
        try {
            Intent intent = activity.getPackageManager()
                .getLaunchIntentForPackage(activity.getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restarting app", e);
        }
    }
} 