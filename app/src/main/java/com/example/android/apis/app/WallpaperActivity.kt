/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.app

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Wallpaper Activity
 *
 * This demonstrates the how to write an activity that has the system wallpaper behind it.
 *
 * This is an app which has the system wallpaper behind it. It does this by setting the theme
 * on the window in AndroidManifest to Theme.MyThemeWallpaper, which copies attributes from
 * the system theme android:style/Theme.Wallpaper, which has android:windowShowWallpaper = "true":
 *
 *    <style name="Theme.MyThemeWallpaper"
 *        parent="Theme.AppCompat">
 *        <item name="android:windowBackground">@android:color/transparent</item>
 *        <item name="android:colorBackgroundCacheHint">@null</item>
 *        <item name="android:windowShowWallpaper">true</item>
 *        <item name="android:colorForeground">#fff</item>
 *    </style>
 *
 * Our style/Theme.MyThemeWallpaper also defines the element:
 *    <item name="android:colorForeground">#fff</item>
 */
class WallpaperActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.translucent_background.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState)

        // See assets/res/any/layout/translucent_background.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.translucent_background)
    }
}
