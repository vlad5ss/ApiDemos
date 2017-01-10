/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.example.android.apis.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.example.android.apis.R;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Demonstration of the implementation of a custom Loader. Shows how to implement a custom
 * AsyncTaskLoader, it uses the system function PackageManager.getInstalledApplications to
 * retrieve a {@code List<ApplicationInfo>} containing the AndroidManifest information for
 * all the installed apps.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class LoaderCustom extends Activity {
    String TAG = "LoaderCustom";

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, Then we retrieve a handle for the FragmentManager for interacting with fragments
     * associated with this activity to {@code FragmentManager fm}. Then if when using {@code fm} to
     * find a Fragment with the ID android.R.id.content we find none (first time running), we create
     * a new instance of our Fragment {@code AppListFragment list}, use {@code fm} to start a new
     * {@code FragmentTransaction} which we use to add {@code list} to the activity state with the
     * ID android.R.id.content, and then commit the {@code FragmentTransaction}. If {@code fm} did
     * find a Fragment with ID android.R.id.content then we are being recreated after an orientation
     * change and need do nothing.
     *
     * @param savedInstanceState we do not override onSaveInstanceState so do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            AppListFragment list = new AppListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        } else {
            Log.i(TAG, "There is already an android.R.id.content Fragment");
        }
    }

    /**
     * This class holds the per-item data in our Loader.
     */
    @SuppressWarnings("WeakerAccess")
    public static class AppEntry {
        private final AppListLoader mLoader; // AppListLoader which created us using "this"
        private final ApplicationInfo mInfo; // ApplicationInfo for package we are assigned to
        private final File mApkFile; // Full path to the base APK for the package
        private String mLabel; // Application label as discovered by the method loadLabel
        private Drawable mIcon; // Icon loaded from application apk by the method getIcon
        private boolean mMounted; // Flag indicating whether we found an apk for the package

        /**
         * Constructor for an instance holding {@code ApplicationInfo info} describing a particular
         * package. We initialize our field {@code AppListLoader mLoader} with the value of our
         * parameter {@code AppListLoader loader}, and our field {@code ApplicationInfo mInfo} with
         * the value of our parameter {@code ApplicationInfo info}. Finally we initialize our field
         * {@code File mApkFile} with a new {@code File} instance derived from the pathname given in
         * the field {@code info.sourceDir} (full path to the base APK for the application).
         *
         * @param loader "this" when called in our {@code AppListLoader} background thread
         * @param info   one of the {@code ApplicationInfo} instances of the list that is returned
         *               from the call to {@code PackageManager.getInstalledApplications}
         */
        public AppEntry(AppListLoader loader, ApplicationInfo info) {
            mLoader = loader;
            mInfo = info;
            mApkFile = new File(info.sourceDir);
        }

        /**
         * Getter method for our field {@code ApplicationInfo mInfo}, simply returns {@code mInfo}.
         *
         * @return contents of our field {@code ApplicationInfo mInfo}
         */
        @SuppressWarnings("unused")
        public ApplicationInfo getApplicationInfo() {
            return mInfo;
        }

        /**
         * Getter method for our field {@code String mLabel}, simply returns {@code mLabel}.
         *
         * @return contents of our field {@code String mLabel}
         */
        public String getLabel() {
            return mLabel;
        }

        /**
         * Getter method for our field {@code Drawable mIcon} (loading it from the apk or supplying
         * a default icon if necessary.)
         * <p>
         * If the current value of {{@code Drawable mIcon}} is null (our first time being called, or
         * the apk was not found to load an Icon from) we check to see if our {@code File mApkFile}
         * exists, and if it does we set {@code mIcon} to the drawable returned by calling the method
         * {@code loadIcon} using our instance of {@code ApplicationInfo mInfo} and return
         * {@code  Drawable mIcon} to the caller. If the {@code File mApkFile} does not exist we set
         * our flag {@code boolean mMounted} to false and fall through to return the system drawable
         * android.R.drawable.sym_def_app_icon. (Never setting {@code mIcon} notice, so the same code
         * path will likely be followed again -- might be more efficient to set {@code mIcon} to the
         * default icon for the next time, or does this logic allow for an apk file to suddenly
         * appear between calls to this method?)
         * <p>
         * If {@code mIcon} is not null, we check to see if our flag {@code boolean mMounted} is false
         * and if so we check to see if our {@code File mApkFile} exists, and if it does we set our
         * flag {@code boolean mMounted} to true, set  {@code mIcon} to the drawable returned by
         * calling the method {@code loadIcon} using our instance of {@code ApplicationInfo mInfo}
         * and return {@code  Drawable mIcon} to the caller. If {@code mMounted} was true we simply
         * return {@code mIcon} to the caller.
         *
         * @return either the contents of our field {@code Drawable mIcon} or the system default app
         * icon android.R.drawable.sym_def_app_icon
         */
        public Drawable getIcon() {
            if (mIcon == null) {
                if (mApkFile.exists()) {
                    mIcon = mInfo.loadIcon(mLoader.mPm);
                    return mIcon;
                } else {
                    mMounted = false;
                }
            } else if (!mMounted) {
                // If the app wasn't mounted but is now mounted, reload
                // its icon.
                if (mApkFile.exists()) {
                    mMounted = true;
                    mIcon = mInfo.loadIcon(mLoader.mPm);
                    return mIcon;
                }
            } else {
                return mIcon;
            }

            //noinspection deprecation
            return mLoader.getContext()
                    .getResources()
                    .getDrawable(android.R.drawable.sym_def_app_icon);
        }

        /**
         * Returns a string containing a concise, human-readable description of this object, which
         * in our case is the field {@code String mLabel} which is set by our method {@code loadLabel}.
         *
         * @return a printable representation of this object, in our case the field {@code String mLabel}
         * which is the current textual label associated with the application we describe, or the
         * packageName if none could be loaded.
         */
        @Override
        public String toString() {
            return mLabel;
        }

        /**
         * Makes sure our field {@code String mLabel} is loaded, either from the apk, or the contents
         * of the field {@code packageName} of our field {@code ApplicationInfo mInfo}.
         * <p>
         * If {@code String mLabel} is currently null, or the apk has not been mounted (our flag
         * {@code boolean mMounted} is false) we check to see if the {@code File mApkFile} exists
         * and if it does not we set {@code mMounted} to false and set {@code mLabel} to the contents
         * of the {@code packageName} field our our field {@code ApplicationInfo mInfo}. If the apk
         * file does exist we set {@code mMounted} to true and try to load the {@code CharSequence label}
         * from the apk. If successful we set {@code mLabel} to the String value of {@code label},
         * otherwise we set {@code mLabel} to the contents of the {@code packageName} field our our
         * field {@code ApplicationInfo mInfo}.
         * <p>
         * If {@code String mLabel} is currently not null, and the apk has been mounted (our flag
         * {@code boolean mMounted} is true) we do nothing.
         *
         * @param context traces back to the an application context retrieved from the Context
         *                passed to the constructor, which is called with {@code getActivity()} in
         *                our case
         */
        void loadLabel(Context context) {
            if (mLabel == null || !mMounted) {
                if (!mApkFile.exists()) {
                    mMounted = false;
                    mLabel = mInfo.packageName;
                } else {
                    mMounted = true;
                    CharSequence label = mInfo.loadLabel(context.getPackageManager());
                    mLabel = label != null ? label.toString() : mInfo.packageName;
                }
            }
        }
    }

    /**
     * Perform alphabetical comparison of application {@code AppEntry} objects.
     */
    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        /**
         *  Collator object for the default locale. 
         */
        private final Collator sCollator = Collator.getInstance();

        /**
         * Compares the two specified {@code AppEntry} Objects to determine their relative ordering.
         * The ordering implied by the return value of this method for all possible pairs of
         * {@code (object1, object2)} should form an <i>equivalence relation</i>.
         * This means that
         * <ul>
         * <li>{@code compare(a,a)} returns zero for all {@code a}</li>
         * <li>the sign of {@code compare(a,b)} must be the opposite of the sign of {@code compare(b,a)}
         * for all pairs of (a,b)</li>
         * <li>From {@code compare(a,b) > 0} and {@code compare(b,c) > 0} it must follow
         * {@code compare(a,c) > 0} for all possible combinations of {@code (a,b,c)}</li>
         * </ul>
         *
         * We simply return the results of applying our {@code Collator sCollator} to the labels
         * fetched from {@code object1} and {@code object2} respectively, thereby defining an
         * alphabetical ordering or {@code AppEntry} Objects based on the alphabetical ordering of
         * their labels as fetched from their field {@code String mLabel}.
         *
         * @param object1 an {@code AppEntry}.
         * @param object2 a second {@code AppEntry} to compare with {@code object1}.
         * @return an integer < 0 if {@code object1} is less than {@code object2}, 0 if they are
         *         equal, and > 0 if {@code object1} is greater than {@code object2}.
         * @throws ClassCastException if objects are not of the correct type.
         */
        @Override
        public int compare(AppEntry object1, AppEntry object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };

    /**
     * Helper for determining if the configuration has changed in an interesting
     * way so we need to rebuild the app list.
     */
    public static class InterestingConfigChanges {
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        boolean applyNewConfig(Resources res) {
            int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
            boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
            if (densityChanged || (configChanges & (ActivityInfo.CONFIG_LOCALE
                    | ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }

    /**
     * Helper class to look for interesting changes to the installed apps
     * so that the loader can be updated.
     */
    public static class PackageIntentReceiver extends BroadcastReceiver {
        final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader loader) {
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mLoader.getContext().registerReceiver(this, sdFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Tell the loader about the change.
            mLoader.onContentChanged();
        }
    }

    /**
     * A custom Loader that loads all of the installed applications.
     */
    @SuppressWarnings("WeakerAccess")
    public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        final PackageManager mPm;

        List<AppEntry> mApps;
        PackageIntentReceiver mPackageObserver;

        public AppListLoader(Context context) {
            super(context);

            // Retrieve the package manager for later use; note we don't
            // use 'context' directly but instead the save global application
            // context returned by getContext().
            mPm = getContext().getPackageManager();
        }

        /**
         * This is where the bulk of our work is done.  This function is
         * called in a background thread and should generate a new set of
         * data to be published by the loader.
         */
        @Override
        public List<AppEntry> loadInBackground() {
            // Retrieve all known applications.
            //noinspection WrongConstant
            List<ApplicationInfo> apps = mPm.getInstalledApplications(
                    PackageManager.GET_UNINSTALLED_PACKAGES |
                            PackageManager.GET_DISABLED_COMPONENTS);
            if (apps == null) {
                apps = new ArrayList<>();
            }

            final Context context = getContext();

            // Create corresponding array of entries and load their labels.
            List<AppEntry> entries = new ArrayList<>(apps.size());
            for (int i = 0; i < apps.size(); i++) {
                AppEntry entry = new AppEntry(this, apps.get(i));
                entry.loadLabel(context);
                entries.add(entry);
            }

            // Sort the list.
            Collections.sort(entries, ALPHA_COMPARATOR);

            // Done!
            return entries;
        }

        /**
         * Called when there is new data to deliver to the client.  The
         * super class will take care of delivering it; the implementation
         * here just adds a little more logic.
         */
        @Override
        public void deliverResult(List<AppEntry> apps) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (apps != null) {
                    onReleaseResources(apps);
                }
            }
            List<AppEntry> oldApps = mApps;
            mApps = apps;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(apps);
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldApps != null) {
                onReleaseResources(oldApps);
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override
        protected void onStartLoading() {
            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            // Start watching for changes in the app data.
            if (mPackageObserver == null) {
                mPackageObserver = new PackageIntentReceiver(this);
            }

            // Has something interesting in the configuration changed since we
            // last built the app list?
            boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

            if (takeContentChanged() || mApps == null || configChange) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override
        public void onCanceled(List<AppEntry> apps) {
            super.onCanceled(apps);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(apps);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                onReleaseResources(mApps);
                mApps = null;
            }

            // Stop monitoring for changes.
            if (mPackageObserver != null) {
                getContext().unregisterReceiver(mPackageObserver);
                mPackageObserver = null;
            }
        }

        /**
         * Helper function to take care of releasing resources associated
         * with an actively loaded data set.
         */
        @SuppressWarnings("UnusedParameters")
        protected void onReleaseResources(List<AppEntry> apps) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }


    @SuppressWarnings("WeakerAccess")
    public static class AppListAdapter extends ArrayAdapter<AppEntry> {
        private final LayoutInflater mInflater;

        public AppListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<AppEntry> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        /**
         * Populate new items in the list.
         */
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_icon_text, parent, false);
            } else {
                view = convertView;
            }

            AppEntry item = getItem(position);
            //noinspection ConstantConditions
            ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
            ((TextView) view.findViewById(R.id.text)).setText(item.getLabel());

            return view;
        }
    }

    public static class AppListFragment extends ListFragment
            implements OnQueryTextListener, OnCloseListener,
            LoaderManager.LoaderCallbacks<List<AppEntry>> {

        // This is the Adapter being used to display the list's data.
        AppListAdapter mAdapter;

        // The SearchView for doing filtering.
        SearchView mSearchView;

        // If non-null, this is the current filter the user has provided.
        String mCurFilter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No applications");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new AppListAdapter(getActivity());
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }

        public static class MySearchView extends SearchView {
            public MySearchView(Context context) {
                super(context);
            }

            // The normal SearchView doesn't clear its search text when
            // collapsed, so we will do this for it.
            @Override
            public void onActionViewCollapsed() {
                setQuery("", false);
                super.onActionViewCollapsed();
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
            MenuItem item = menu.add("Search");
            item.setIcon(android.R.drawable.ic_menu_search);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            mSearchView = new MySearchView(getActivity());
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setIconifiedByDefault(true);
            item.setActionView(mSearchView);
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed.  Since this
            // is a simple array adapter, we can just have it do the filtering.
            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
            mAdapter.getFilter().filter(mCurFilter);
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            // Don't care about this.
            return true;
        }

        @Override
        public boolean onClose() {
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            return true;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            // Insert desired behavior here.
            Log.i("LoaderCustom", "Item clicked: " + id);
        }

        @Override
        public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader with no arguments, so it is simple.
            return new AppListLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
            // Set the new data in the adapter.
            mAdapter.setData(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<AppEntry>> loader) {
            // Clear the data in the adapter.
            mAdapter.setData(null);
        }
    }

}
