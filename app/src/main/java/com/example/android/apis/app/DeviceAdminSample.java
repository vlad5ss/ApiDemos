/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import androidx.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.android.apis.R;

import java.util.List;

/**
 * This activity provides a comprehensive UI for exploring and operating the DevicePolicyManager
 * api.  It consists of two primary modules:
 * <p>
 * 1:  A device policy controller, implemented here as a series of preference fragments.  Each
 *     one contains code to monitor and control a particular subset of device policies.
 * <p>
 * 2:  A DeviceAdminReceiver, to receive updates from the DevicePolicyManager when certain aspects
 *     of the device security status have changed.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class DeviceAdminSample extends PreferenceActivity {

    // Miscellaneous utilities and definitions
    /**
     * TAG used for logging
     */
    private static final String TAG = "DeviceAdminSample";

    /**
     * Request code used when starting the activity to have the user enable our admin.
     */
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    /**
     * Request code used when starting the activity to activate encryption.
     */
    private static final int REQUEST_CODE_START_ENCRYPTION = 2;

    /**
     * Number of milliseconds in a minute
     */
    private static final long MS_PER_MINUTE = 60 * 1000;
    /**
     * Number of milliseconds in an hour
     */
    private static final long MS_PER_HOUR = 60 * MS_PER_MINUTE;
    /**
     * Number of milliseconds in a day
     */
    private static final long MS_PER_DAY = 24 * MS_PER_HOUR;

    // The following keys are used to find each preference item
    /**
     * android:key for the "Enable admin" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_ENABLE_ADMIN = "key_enable_admin";
    /**
     * android:key for the "Disable all device cameras" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_DISABLE_CAMERA = "key_disable_camera";
    /**
     * android:key for the "Disable keyguard notifications" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_DISABLE_NOTIFICATIONS = "key_disable_notifications";
    /**
     * android:key for the "Disable keyguard unredacted notifications" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_DISABLE_UNREDACTED = "key_disable_unredacted";
    /**
     * android:key for the "Disable keyguard Trust Agents" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_DISABLE_TRUST_AGENTS = "key_disable_trust_agents";
    /**
     * android:key for the "Enabled Component Name" EditTextPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_TRUST_AGENT_COMPONENT = "key_trust_agent_component";
    /**
     * android:key for the "Enabled Features (comma-separated)" EditTextPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_TRUST_AGENT_FEATURES = "key_trust_agent_features";
    /**
     * android:key for the "Disable keyguard widgets" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_DISABLE_KEYGUARD_WIDGETS = "key_disable_keyguard_widgets";
    /**
     * android:key for the "Disable keyguard secure camera" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_DISABLE_KEYGUARD_SECURE_CAMERA = "key_disable_keyguard_secure_camera";
    /**
     * android:key for the "Disable keyguard Fingerprint" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_DISABLE_FINGERPRINT = "key_disable_fingerprint";
    /**
     * android:key for the "Disable keyguard Remote Input" CheckBoxPreference in the xml/device_admin_general.xml PreferenceScreen
     */
    private static final String KEY_DISABLE_REMOTE_INPUT = "key_disable_remote_input";

    /**
     * android:key for the "Password quality" PreferenceCategory in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_CATEGORY_QUALITY = "key_category_quality";
    /**
     * android:key for the "Set password (user)" PreferenceScreen in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_SET_PASSWORD = "key_set_password";
    /**
     * android:key for the "Set password (via API)" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_RESET_PASSWORD = "key_reset_password";
    /**
     * android:key for the "Password quality" ListPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_QUALITY = "key_quality";
    /**
     * android:key for the "Minimum length" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_MIN_LENGTH = "key_minimum_length";
    /**
     * android:key for the "Minimum letters" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_MIN_LETTERS = "key_minimum_letters";
    /**
     * android:key for the "Minimum numeric" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_MIN_NUMERIC = "key_minimum_numeric";
    /**
     * android:key for the "Minimum lower case" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_MIN_LOWER_CASE = "key_minimum_lower_case";
    /**
     * android:key for the "Minimum upper case" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_MIN_UPPER_CASE = "key_minimum_upper_case";
    /**
     * android:key for the "Minimum symbols" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_MIN_SYMBOLS = "key_minimum_symbols";
    /**
     * android:key for the "Minimum non-letter" EditTextPreference in the xml/device_admin_quality.xml PreferenceScreen
     */
    private static final String KEY_MIN_NON_LETTER = "key_minimum_non_letter";

    /**
     * android:key for the "Password history / Expiration" PreferenceCategory in the xml/device_admin_expiration.xml PreferenceScreen
     */
    private static final String KEY_CATEGORY_EXPIRATION = "key_category_expiration";
    /**
     * android:key for the "Password history depth" EditTextPreference in the xml/device_admin_expiration.xml PreferenceScreen
     */
    private static final String KEY_HISTORY = "key_history";
    /**
     * android:key for the "Password expiration timeout (minutes)" EditTextPreference in the xml/device_admin_expiration.xml PreferenceScreen
     */
    private static final String KEY_EXPIRATION_TIMEOUT = "key_expiration_timeout";
    /**
     * android:key for the "Password expiration status" PreferenceScreen in the xml/device_admin_expiration.xml PreferenceScreen
     */
    private static final String KEY_EXPIRATION_STATUS = "key_expiration_status";

    /**
     * android:key for the "Lock screen / Wipe" PreferenceCategory in the xml/device_admin_lock_wipe.xml PreferenceScreen
     */
    private static final String KEY_CATEGORY_LOCK_WIPE = "key_category_lock_wipe";
    /**
     * android:key for the "Max time to screen lock (minutes)" EditTextPreference in the xml/device_admin_lock_wipe.xml PreferenceScreen
     */
    private static final String KEY_MAX_TIME_SCREEN_LOCK = "key_max_time_screen_lock";
    /**
     * android:key for the "Max password failures for local wipe" EditTextPreference in the xml/device_admin_lock_wipe.xml PreferenceScreen
     */
    private static final String KEY_MAX_FAILS_BEFORE_WIPE = "key_max_fails_before_wipe";
    /**
     * android:key for the "Lock screen now" PreferenceScreen in the xml/device_admin_lock_wipe.xml PreferenceScreen
     */
    private static final String KEY_LOCK_SCREEN = "key_lock_screen";
    /**
     * android:key for the "Wipe data" PreferenceScreen in the xml/device_admin_lock_wipe.xml PreferenceScreen
     */
    private static final String KEY_WIPE_DATA = "key_wipe_data";
    /**
     * android:key for the "Wipe all data" PreferenceScreen in the xml/device_admin_lock_wipe.xml PreferenceScreen
     */
    private static final String KEY_WIP_DATA_ALL = "key_wipe_data_all";

    /**
     * android:key for the "Encryption" PreferenceCategory in the xml/device_admin_encryption.xml PreferenceScreen
     */
    private static final String KEY_CATEGORY_ENCRYPTION = "key_category_encryption";
    /**
     * android:key for the "Require encryption" CheckBoxPreference in the xml/device_admin_encryption.xml PreferenceScreen
     */
    private static final String KEY_REQUIRE_ENCRYPTION = "key_require_encryption";
    /**
     * android:key for the "Activate encryption" PreferenceScreen in the xml/device_admin_encryption.xml PreferenceScreen
     */
    private static final String KEY_ACTIVATE_ENCRYPTION = "key_activate_encryption";

    // Interaction with the DevicePolicyManager
    /**
     * Handle to the DEVICE_POLICY_SERVICE system level service.
     */
    DevicePolicyManager mDPM;
    /**
     * {@code ComponentName} of the class of {@code DeviceAdminSampleReceiver}
     */
    ComponentName mDeviceAdminSample;

    /**
     * Called when the PreferenceActivity is starting. First we call our super's implementation of
     * {@code onCreate}. Then we initialize our field {@code DevicePolicyManager mDPM} with a handle
     * to the DEVICE_POLICY_SERVICE system level service (the public interface for managing policies
     * enforced on a device). Finally we initialize our field {@code ComponentName mDeviceAdminSample}
     * with an instance for the class of {@code DeviceAdminSampleReceiver} (we will later use this
     * when we need to supply the name of the admin component to {@code DevicePolicyManager} methods).
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare to work with the DPM
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminSample = new ComponentName(this, DeviceAdminSampleReceiver.class);
    }

    /**
     * Called when the activity needs its list of headers built. By implementing this and adding at
     * least one item to the list, you will cause the activity to run in its modern fragment mode.
     * Note that this function may not always be called; for example, if the activity has been asked
     * to display a particular fragment without the header list, there is no need to build the headers.
     * <p>
     * Typical implementations will use {@link #loadHeadersFromResource} to fill in the list from a
     * resource as we do, loading them from the file R.xml.device_admin_headers into our parameter
     * {@code List<Header> target}.
     *
     * @param target The list in which to place the headers.
     * We override this method to provide PreferenceActivity with the top-level preference headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.device_admin_headers, target);
    }

    /**
     * Helper to determine if we are an active admin, returns true if {@code ComponentName mDeviceAdminSample}
     * is currently active (enabled) in the system. We just return the value returned by the
     * {@code isAdminActive} method of our field {@code DevicePolicyManager mDPM} for our field
     * {@code ComponentName mDeviceAdminSample}.
     *
     * @return {@code true} if {@code ComponentName mDeviceAdminSample} is currently enabled in the
     * system, {@code false} otherwise.
     */
    private boolean isActiveAdmin() {
        return mDPM.isAdminActive(mDeviceAdminSample);
    }

    /**
     * Subclasses should override this method and verify that the given fragment is a valid type
     * to be attached to this activity. We return true if our parameter {@code String fragmentName}
     * matches one of five the class names: {@code GeneralFragment}, {@code QualityFragment},
     * {@code ExpirationFragment}, {@code LockWipeFragment}, or {@code EncryptionFragment}.
     *
     * @param fragmentName the class name of the Fragment about to be attached to this activity.
     * @return true if the fragment class name is valid for this Activity and false otherwise.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralFragment.class.getName().equals(fragmentName)
                || QualityFragment.class.getName().equals(fragmentName)
                || ExpirationFragment.class.getName().equals(fragmentName)
                || LockWipeFragment.class.getName().equals(fragmentName)
                || EncryptionFragment.class.getName().equals(fragmentName);
    }

    /**
     * Common fragment code for DevicePolicyManager access. Provides two shared elements:
     * <ul>
     *     <li>
     *         1. Provides instance variables to access activity/context, DevicePolicyManager, etc.
     *     </li>
     *     <li>
     *         2. Provides support for the "set password" button(s) shared by multiple fragments.
     *     </li>
     * </ul>
     */
    public static class AdminSampleFragment extends PreferenceFragment
            implements OnPreferenceChangeListener, OnPreferenceClickListener{

        // Useful instance variables
        /**
         * Cached reference to the Activity this fragment is currently associated with, as returned
         * by {@code getActivity}. Used as the context for retrieving resources, and accessing fields
         * and methods of our parent Activity.
         */
        protected DeviceAdminSample mActivity;
        /**
         * Cached reference to the {@code DevicePolicyManager mDPM} field of {@code mActivity}
         * (saves typing?).
         */
        protected DevicePolicyManager mDPM;
        /**
         * Cached reference to the {@code ComponentName mDeviceAdminSample} field of {@code mActivity}
         * (saves typing?).
         */
        protected ComponentName mDeviceAdminSample;
        /**
         * Cached value of a call to the {@code isActiveAdmin} method of {@code mActivity}, saves
         * repeated calls to the {@code DevicePolicyManager.isAdminActive} method.
         */
        protected boolean mAdminActive;

        // Optional shared UI
        /**
         * Reference to the KEY_SET_PASSWORD ("key_set_password") {@code PreferenceScreen} in our UI
         * (if it exists).
         */
        private PreferenceScreen mSetPassword;
        /**
         * Reference to the KEY_RESET_PASSWORD ("key_reset_password") {@code EditTextPreference} in
         * our UI (if it exists).
         */
        private EditTextPreference mResetPassword;

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call our super's implementation of {@code onActivityCreated}. We
         * initialize our field {@code DeviceAdminSample mActivity} with the Activity this fragment
         * is currently associated with (as returned by the {@code getActivity} method), and then use
         * it to set our field {@code DevicePolicyManager mDPM} to the value of the {@code mDPM} field
         * of {@code mActivity}, our field {@code ComponentName mDeviceAdminSample} to the value of
         * field {@code mDeviceAdminSample} of {@code mActivity}, and our field {@code boolean mAdminActive}
         * to the value returned by the {@code isActiveAdmin} method of {@code mActivity}. We initialize
         * our field {@code EditTextPreference mResetPassword} by finding the {@link Preference} with
         * the key KEY_RESET_PASSWORD ("key_reset_password"), and our field {@code EditTextPreference mResetPassword}
         * by finding the {@link Preference} with the key KEY_SET_PASSWORD ("key_set_password"). If
         * {@code mResetPassword} is not null we set its {@code OnPreferenceChangeListener} to this
         * (the callback to be invoked when the Preference is changed by the user will be our override
         * of {@code onPreferenceChange}). If {@code mSetPassword} is not null we set its
         * {@code OnPreferenceClickListener} to this (the callback to be invoked when this Preference
         * is clicked, our {@code onPreferenceClick} override will be called).
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Retrieve the useful instance variables
            mActivity = (DeviceAdminSample) getActivity();
            mDPM = mActivity.mDPM;
            mDeviceAdminSample = mActivity.mDeviceAdminSample;
            mAdminActive = mActivity.isActiveAdmin();

            // Configure the shared UI elements (if they exist)
            mResetPassword = (EditTextPreference) findPreference(KEY_RESET_PASSWORD);
            mSetPassword = (PreferenceScreen) findPreference(KEY_SET_PASSWORD);

            if (mResetPassword != null) {
                mResetPassword.setOnPreferenceChangeListener(this);
            }
            if (mSetPassword != null) {
                mSetPassword.setOnPreferenceClickListener(this);
            }
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of {@code onResume}. We set our {@code boolean mAdminActive} to the
         * value returned by the {@code isActiveAdmin} method of our parent activity {@code mActivity}
         * (returns true if we are currently an active admin). We then call our method {@code reloadSummaries}
         * which will display text describing the sufficiency of our password if it is appropriate to
         * do so. If our field {@code EditTextPreference mResetPassword} is not null we call its
         * {@code setEnabled} method with {@code mAdminActive}, disabling the preference if we are
         * not an active admin.
         */
        @Override
        public void onResume() {
            super.onResume();
            mAdminActive = mActivity.isActiveAdmin();
            reloadSummaries();
            // Resetting the password via API is available only to active admins
            if (mResetPassword != null) {
                mResetPassword.setEnabled(mAdminActive);
            }
        }

        /**
         * Called automatically at every onResume. Should also call explicitly any time a  policy
         * changes that may affect other policy values. If our field {@code PreferenceScreen mSetPassword}
         * is null we do nothing, if it is not branch on the value of {@code mAdminActive}:
         * <ul>
         *     <li>
         *         true (we are an active admin): we initialize {@code boolean sufficient} with the
         *         value returned by the {@code isActivePasswordSufficient} method of {@code mDPM}
         *         (Returns true if the password meets the current requirements, else false). If
         *         {@code sufficient} is true we set the summary of {@code mSetPassword} to the string
         *         with resource ID R.string.password_sufficient ("Current password meets policy
         *         requirements"), if false we set its summary to the string with resource ID
         *         R.string.password_insufficient ("Current password does not meet policy requirements").
         *     </li>
         *     <li>
         *         false (we are not an active admin): we set the summary of {@code mSetPassword} to null.
         *     </li>
         * </ul>
         */
        protected void reloadSummaries() {
            if (mSetPassword != null) {
                if (mAdminActive) {
                    // Show password-sufficient status under Set Password button
                    boolean sufficient = mDPM.isActivePasswordSufficient();
                    mSetPassword.setSummary(sufficient ?
                            R.string.password_sufficient : R.string.password_insufficient);
                } else {
                    mSetPassword.setSummary(null);
                }
            }
        }

        /**
         * Delays a call to {@code reloadSummaries} until after preference changes have been applied
         * upon return from the various {@code onPreferenceChange} overrides.
         */
        protected void postReloadSummaries() {
            //noinspection ConstantConditions
            getView().post(new Runnable() {
                @Override
                public void run() {
                    reloadSummaries();
                }
            });
        }

        /**
         * Called when a Preference has been clicked. This is used only by {@code mSetPassword}, so
         * if {@code mSetPassword} is null or not equal to our parameter {@code preference} we return
         * false having done nothing. If it is {@code mSetPassword} we initialize {@code Intent intent}
         * with an instance for the action ACTION_SET_NEW_PASSWORD (have the user enter a new password
         * that meets the current requirements), launch the activity of {@code intent} and return true
         * consuming the click.
         *
         * @param preference The Preference that was clicked.
         * @return True if the click was handled.
         */
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (mSetPassword != null && preference == mSetPassword) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                startActivity(intent);
                return true;
            }
            return false;
        }

        /**
         * Called when a Preference has been changed by the user. This is called before the state of
         * the Preference is about to be updated and before the state is persisted. This is used only
         * by {@code mResetPassword}, so if {@code mResetPassword} is null or not equal to our parameter
         * {@code preference} we return false having done nothing. If it is {@code mResetPassword} we
         * call our method {@code doResetPassword} with the string cast of our parameter {@code newValue}
         * to force a new password for device unlock (no longer works for Android O and above), and
         * return true to update the state of the Preference with the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (mResetPassword != null && preference == mResetPassword) {
                doResetPassword((String)newValue);
                return true;
            }
            return false;
        }

        /**
         * Force a new password for device unlock. This is dangerous, so we prevent automated tests
         * from doing it, and we remind the user after we do it. If our method {@code alertIfMonkey}
         * returns true indicating we are being run by an automated test, we return true having done
         * nothing ({@code alertIfMonkey} will have displayed an alert dialog with the string with
         * resource id R.string.monkey_reset_password: "You can't reset my password, you are a monkey!")
         * If we are being used by a real user we call the {@code resetPassword} method of our field
         * {@code DevicePolicyManager mDPM} with our parameter {@code newPassword} as the new password
         * for the user, and RESET_PASSWORD_REQUIRE_ENTRY (don't allow other admins to change the
         * password again until the user has entered it) as the flags. We then initialize our variable
         * {@code AlertDialog.Builder builder} with a new instance, initialize {@code String message}
         * with the string resulting from formatting {@code newPassword} with the format string with
         * resource id R.string.reset_password_warning ("You have just reset your screen lock password
         * to "%1$s"). We then set the message of {@code builder} to {@code message}, set the text of
         * its positive button to the string with resource id R.string.reset_password_ok ("Don't forget
         * it") with null for the {@code OnClickListener}. Finally we create an {@link AlertDialog}
         * with the arguments supplied to {@code builder} and immediately display the dialog.
         *
         * @param newPassword new password.
         */
        private void doResetPassword(String newPassword) {
            if (alertIfMonkey(mActivity, R.string.monkey_reset_password)) {
                return;
            }
            mDPM.resetPassword(newPassword, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            String message = mActivity.getString(R.string.reset_password_warning, newPassword);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.reset_password_ok, null);
            builder.show();
        }

        /**
         * Simple helper for summaries showing local & global (aggregate) policy settings. We just
         * return a string displaying the string value of our parameters formatted using the string
         * with resource id R.string.status_local_global ("Local=%1$s / Global=%2$s").
         *
         * @param local policy setting for local
         * @param global policy setting for global
         * @return string displaying the string value of our parameters formatted using the string
         * with resource id R.string.status_local_global ("Local=%1$s / Global=%2$s")
         */
        protected String localGlobalSummary(Object local, Object global) {
            return getString(R.string.status_local_global, local, global);
        }
    }

    /**
     * PreferenceFragment for "general" preferences.
     */
    public static class GeneralFragment extends AdminSampleFragment
            implements OnPreferenceChangeListener {
        // UI elements
        /**
         * {@code CheckBoxPreference} with the key KEY_ENABLE_ADMIN ("key_enable_admin") in the
         * xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mEnableCheckbox;
        /**
         * {@code CheckBoxPreference} with the key KEY_DISABLE_CAMERA ("key_disable_camera") in the
         * xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mDisableCameraCheckbox;
        /**
         * {@code CheckBoxPreference} with the key KEY_DISABLE_KEYGUARD_WIDGETS ("key_disable_keyguard_widgets")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mDisableKeyguardWidgetsCheckbox;
        /**
         * {@code CheckBoxPreference} with the key KEY_DISABLE_KEYGUARD_SECURE_CAMERA
         * ("key_disable_keyguard_secure_camera") in the xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mDisableKeyguardSecureCameraCheckbox;
        /**
         * {@code CheckBoxPreference} with the key KEY_DISABLE_NOTIFICATIONS ("key_disable_notifications")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mDisableKeyguardNotificationCheckbox;
        /**
         * {@code CheckBoxPreference} with the key KEY_DISABLE_TRUST_AGENTS ("key_disable_trust_agents")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mDisableKeyguardTrustAgentCheckbox;
        /**
         * {@code CheckBoxPreference} with the key KEY_DISABLE_UNREDACTED ("key_disable_unredacted")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mDisableKeyguardUnredactedCheckbox;
        /**
         * {@code EditTextPreference} with the key KEY_TRUST_AGENT_COMPONENT ("key_trust_agent_component")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private EditTextPreference mTrustAgentComponent;
        /**
         * {@code EditTextPreference} with the key KEY_TRUST_AGENT_FEATURES ("key_trust_agent_features")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private EditTextPreference mTrustAgentFeatures;
        /**
         * {@code CheckBoxPreference} with the key KEY_DISABLE_FINGERPRINT ("key_disable_fingerprint")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mDisableKeyguardFingerprintCheckbox;
        /**
         * {@code CheckBoxPreference} with the key KEY_DISABLE_REMOTE_INPUT ("key_disable_remote_input")
         * in the xml/device_admin_general.xml PreferenceScreen
         */
        private CheckBoxPreference mDisableKeyguardRemoteInputCheckbox;

        /**
         * Called to do initial creation of a {@code PreferenceFragment}. First we call our super's
         * implementation of {@code onCreate}, then we call the {@code addPreferencesFromResource}
         * method to inflate our XML resource file R.xml.device_admin_general and add its preference
         * hierarchy to the current preference hierarchy. We then initialize the fields we use to
         * access the various {@link Preference} widgets in our UI by finding them using the android:key
         * strings they are identified by in the xml/device_admin_general.xml file. After doing so we
         * set their {@code OnPreferenceChangeListener} to this.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_general);

            mEnableCheckbox = (CheckBoxPreference) findPreference(KEY_ENABLE_ADMIN);
            mEnableCheckbox.setOnPreferenceChangeListener(this);

            mDisableCameraCheckbox = (CheckBoxPreference) findPreference(KEY_DISABLE_CAMERA);
            mDisableCameraCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardWidgetsCheckbox =
                (CheckBoxPreference) findPreference(KEY_DISABLE_KEYGUARD_WIDGETS);
            mDisableKeyguardWidgetsCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardSecureCameraCheckbox =
                (CheckBoxPreference) findPreference(KEY_DISABLE_KEYGUARD_SECURE_CAMERA);
            mDisableKeyguardSecureCameraCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardNotificationCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_NOTIFICATIONS);
            mDisableKeyguardNotificationCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardUnredactedCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_UNREDACTED);
            mDisableKeyguardUnredactedCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardFingerprintCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_FINGERPRINT);
            mDisableKeyguardFingerprintCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardRemoteInputCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_REMOTE_INPUT);
            mDisableKeyguardRemoteInputCheckbox.setOnPreferenceChangeListener(this);

            mDisableKeyguardTrustAgentCheckbox =
                    (CheckBoxPreference) findPreference(KEY_DISABLE_TRUST_AGENTS);
            mDisableKeyguardTrustAgentCheckbox.setOnPreferenceChangeListener(this);

            mTrustAgentComponent =
                    (EditTextPreference) findPreference(KEY_TRUST_AGENT_COMPONENT);
            mTrustAgentComponent.setOnPreferenceChangeListener(this);

            mTrustAgentFeatures =
                    (EditTextPreference) findPreference(KEY_TRUST_AGENT_FEATURES);
            mTrustAgentFeatures.setOnPreferenceChangeListener(this);
        }

        /**
         * Called when the fragment is visible to the user and actively running, this is where we
         * reload our UI with the current values of the Preferences. First we call our super's
         * implementation of {@code onResume}. We then enable or disable the {@code mEnableCheckbox}
         * {@code CheckBoxPreference} depending on whether our {@code mAdminActive} is true of false.
         * We call our {@code enableDeviceCapabilitiesArea} method with {@code mAdminActive} to do the
         * same with all the other widgets in our UI. If {@code mAdminActive} is true we call the
         * {@code setCameraDisabled} method of {@code DevicePolicyManager mDPM} to disable all cameras
         * on the device for this user if our {@code CheckBoxPreference mDisableCameraCheckbox} is
         * checked. We call the {@code setKeyguardDisabledFeatures} method of {@code mDPM} to disable
         * keyguard customizations, such as widgets according to the feature list created by our method
         * {@code createKeyguardDisabledFlag} (it reads the state of the various keyguard widgets in
         * order to build a flag bitmask enabling or disabling the feature the widget controls depending
         * on the checked/unchecked state of the widget). Finally we call our {@code reloadSummaries}
         * method to set the summary text of our widgets appropriately for their current state.
         */
        @Override
        public void onResume() {
            super.onResume();
            mEnableCheckbox.setChecked(mAdminActive);
            enableDeviceCapabilitiesArea(mAdminActive);

            if (mAdminActive) {
                mDPM.setCameraDisabled(mDeviceAdminSample, mDisableCameraCheckbox.isChecked());
                mDPM.setKeyguardDisabledFeatures(mDeviceAdminSample, createKeyguardDisabledFlag());
                reloadSummaries();
            }
        }

        /**
         * Creates a bitmask of keyguard features that are disabled by the checked state of the feature's
         * {@code CheckBoxPreference}. The seven keyguard {@code CheckBoxPreference} in our UI and the
         * feature bitmask they disable are:
         * <ul>
         *     <li>
         *         {@code mDisableKeyguardWidgetsCheckbox} ("Disable keyguard widgets"): KEYGUARD_DISABLE_WIDGETS_ALL
         *         Disable all keyguard widgets, has no effect starting from LOLLIPOP since keyguard
         *         widget is only supported on Android versions lower than 5.0.
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardSecureCameraCheckbox} ("Disable keyguard secure camera"):
         *         KEYGUARD_DISABLE_SECURE_CAMERA Disable the camera on secure keyguard screens (e.g.
         *         PIN/Pattern/Password)
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardNotificationCheckbox} ("Disable keyguard notifications"):
         *         KEYGUARD_DISABLE_SECURE_NOTIFICATIONS Disable showing all notifications on secure
         *         keyguard screens (e.g. PIN/Pattern/Password)
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardUnredactedCheckbox} ("Disable keyguard unredacted notifications")
         *         KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS Only allow redacted notifications on secure
         *         keyguard screens (e.g. PIN/Pattern/Password)
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardTrustAgentCheckbox} ("Disable keyguard Trust Agents")
         *         KEYGUARD_DISABLE_TRUST_AGENTS Disable trust agents on secure keyguard screens
         *         (e.g. PIN/Pattern/Password). By setting this flag alone, all trust agents are
         *         disabled. If the admin then wants to whitelist specific features of some trust
         *         agent, {@code setTrustAgentConfiguration} can be used in conjunction to set
         *         trust-agent-specific configurations.
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardFingerprintCheckbox} ("Disable keyguard Fingerprint")
         *         KEYGUARD_DISABLE_FINGERPRINT Disable fingerprint authentication on keyguard secure
         *         screens (e.g. PIN/Pattern/Password).
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardRemoteInputCheckbox} ("Disable keyguard Remote Input")
         *         KEYGUARD_DISABLE_REMOTE_INPUT Disable text entry into notifications on secure
         *         keyguard screens (e.g. PIN/Pattern/Password).
         *     </li>
         * </ul>
         * Having created {@code flags} by or'ing the appropriate bitmasks into it we return {@code flags}
         * to the caller.
         *
         * @return bitmask of keyguard features representing the state of our keyguard widgets that
         * is appropriate for the {@code setKeyguardDisabledFeatures} method of {@code DevicePolicyManager}
         */
        int createKeyguardDisabledFlag() {
            int flags = DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE;
            flags |= mDisableKeyguardWidgetsCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL : 0;
            flags |= mDisableKeyguardSecureCameraCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA : 0;
            flags |= mDisableKeyguardNotificationCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS : 0;
            flags |= mDisableKeyguardUnredactedCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS : 0;
            flags |= mDisableKeyguardTrustAgentCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS : 0;
            flags |= mDisableKeyguardFingerprintCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT : 0;
            flags |= mDisableKeyguardRemoteInputCheckbox.isChecked() ?
                    DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT : 0;
            return flags;
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * {@code onPreferenceChange} returns true, we return true having done nothing. Otherwise we
         * branch on the value of our parameter {@code Preference preference}:
         * <ul>
         *     <li>
         *         {@code mEnableCheckbox} "Enable admin" {@code CheckBoxPreference} in the PreferenceScreen
         *         xml/device_admin_general.xml -- We initialize {@code boolean value} by casting our parameter
         *         {@code Object newValue} to boolean, and if {@code value} is not equal to our field
         *         {@code mAdminActive} we branch on the value of {@code value}
         *         <ul>
         *             <li>
         *                 true: we initialize {@code Intent intent} with an instance whose action is
         *                 ACTION_ADD_DEVICE_ADMIN (ask the user to add a new device administrator to
         *                 the system), add {@code mDeviceAdminSample} as an extra under the key EXTRA_DEVICE_ADMIN
         *                 (ComponentName of the administrator component), add the string with resource
         *                 id R.string.add_admin_extra_app_text ("Additional text explaining why this needs
         *                 to be added") as an extra under the key EXTRA_ADD_EXPLANATION (optional CharSequence
         *                 providing additional explanation for why the admin is being added) then start the
         *                 intent's activity running for a result. Finally we return false so that the
         *                 Preference will not be updated until we're really active.
         *             </li>
         *             <li>
         *                 false: we call the {@code removeActiveAdmin} method of {@code DevicePolicyManager mDPM}
         *                 to remove the administration component {@code ComponentName mDeviceAdminSample} then
         *                 call our method {@code enableDeviceCapabilitiesArea} to update the device capabilities
         *                 area of our UI by disabling the widgets.
         *             </li>
         *         </ul>
         *     </li>
         *     <li>
         *         {@code mDisableCameraCheckbox} "Disable all device cameras" {@code CheckBoxPreference} in the
         *         PreferenceScreen xml/device_admin_general.xml -- We initialize {@code boolean value} by casting
         *         our parameter {@code Object newValue} to boolean, then call the {@code setCameraDisabled}
         *         method of {@code DevicePolicyManager mDPM} to disable or enable all cameras on the device for
         *         this user depending on the value of {@code value}. Finally we call our {@code postReloadSummaries}
         *         method to delay a call to {@code reloadSummaries} until after preference changes have been applied.
         *     </li>
         *     <li>
         *         If {@code Preference preference} is one of:
         *         <ul>
         *             <li>
         *                 {@code mDisableKeyguardWidgetsCheckbox} "Disable keyguard widgets" in the
         *                 PreferenceScreen xml/device_admin_general.xml
         *             </li>
         *             <li>
         *                 {@code mDisableKeyguardSecureCameraCheckbox} "Disable keyguard secure camera"
         *                 in the PreferenceScreen xml/device_admin_general.xml
         *             </li>
         *             <li>
         *                 {@code mDisableKeyguardNotificationCheckbox} "Disable keyguard notifications"
         *                 in the PreferenceScreen xml/device_admin_general.xml
         *             </li>
         *             <li>
         *                 {@code mDisableKeyguardUnredactedCheckbox} "Disable keyguard unredacted notifications"
         *                 in the PreferenceScreen xml/device_admin_general.xml
         *             </li>
         *             <li>
         *                 {@code mDisableKeyguardTrustAgentCheckbox} "Disable keyguard Trust Agents"
         *                 in the PreferenceScreen xml/device_admin_general.xml
         *             </li>
         *             <li>
         *                 {@code mDisableKeyguardFingerprintCheckbox} "Disable keyguard Fingerprint"
         *                 in the PreferenceScreen xml/device_admin_general.xml
         *             </li>
         *             <li>
         *                 {@code mDisableKeyguardRemoteInputCheckbox} "Disable keyguard Remote Input"
         *                 in the PreferenceScreen xml/device_admin_general.xml
         *             </li>
         *             <li>
         *                 {@code mTrustAgentComponent} "Enabled Component Name" in the PreferenceScreen
         *                 xml/device_admin_general.xml
         *             </li>
         *             <li>
         *                 {@code mTrustAgentFeatures} "Enabled Features (comma-separated)" in the
         *                 PreferenceScreen xml/device_admin_general.xml
         *             </li>
         *             We call our method {@code postUpdateDpmDisableFeatures} to have the {@code DevicePolicyManager}
         *             enable, disable or set all the features controlled by these {@code Preference} widgets. Then
         *             we call our {@code postReloadSummaries} method to delay a call to {@code reloadSummaries}
         *             until after the preference change has been applied.
         *         </ul>
         *     </li>
         * </ul>
         * Finally we return true to the caller to have the state of the Preference updated with the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            if (preference == mEnableCheckbox) {
                boolean value = (Boolean) newValue;
                if (value != mAdminActive) {
                    if (value) {
                        // Launch the activity to have the user enable our admin.
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                mActivity.getString(R.string.add_admin_extra_app_text));
                        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
                        // return false - don't update checkbox until we're really active
                        return false;
                    } else {
                        mDPM.removeActiveAdmin(mDeviceAdminSample);
                        enableDeviceCapabilitiesArea(false);
                        mAdminActive = false;
                    }
                }
            } else if (preference == mDisableCameraCheckbox) {
                boolean value = (Boolean) newValue;
                mDPM.setCameraDisabled(mDeviceAdminSample, value);
                // Delay update because the change is only applied after exiting this method.
                postReloadSummaries();
            } else if (preference == mDisableKeyguardWidgetsCheckbox
                    || preference == mDisableKeyguardSecureCameraCheckbox
                    || preference == mDisableKeyguardNotificationCheckbox
                    || preference == mDisableKeyguardUnredactedCheckbox
                    || preference == mDisableKeyguardTrustAgentCheckbox
                    || preference == mDisableKeyguardFingerprintCheckbox
                    || preference == mDisableKeyguardRemoteInputCheckbox
                    || preference == mTrustAgentComponent
                    || preference == mTrustAgentFeatures) {
                postUpdateDpmDisableFeatures();
                postReloadSummaries();
            }
            return true;
        }

        /**
         * Posts a runnable to the queue of the UI thread which propagates the values set in our
         * {@code PreferenceScreen} xml/device_admin_general.xml to {@code DevicePolicyManager mDPM},
         * enabling and disabling the keyguard features controlled by the various {@code CheckBoxPreference}
         * widgets and transferring the text in the {@code EditTextPreference} widgets to their appropriate
         * destinations.
         */
        private void postUpdateDpmDisableFeatures() {
            //noinspection ConstantConditions
            getView().post(new Runnable() {
                /**
                 * Propagates the values set in our {@code PreferenceScreen} xml/device_admin_general.xml
                 * to {@code DevicePolicyManager mDPM}. First we call the {@code setKeyguardDisabledFeatures}
                 * method of {@code mDPM} to disable keyguard customizations, such as widgets according to
                 * the feature list created by our method {@code createKeyguardDisabledFlag} (it reads the
                 * state of the various keyguard widgets in order to build a flag bitmask enabling or
                 * disabling the feature the widget controls depending on the checked/unchecked state
                 * of the widget). We initialize {@code String component} to the text contained in our
                 * field {@code EditTextPreference mTrustAgentComponent} and if this is not null we
                 * initialize {@code ComponentName agent} by calling the {@code unflattenFromString}
                 * method of {@code ComponentName} to parse {@code String component}. If {@code agent}
                 * is null we just log "Invalid component:". Otherwise we initialize {@code String featureString}
                 * to the text contained in our field {@code EditTextPreference mTrustAgentFeatures}
                 * and if this is not null we initialize {@code PersistableBundle bundle} with a new
                 * instance then split {@code featureString} on the "," character in order to store the
                 * resulting string array in {@code bundle} under the key "features". Finally we call the
                 * {@code setTrustAgentConfiguration} method of {@code DevicePolicyManager mDPM} to set
                 * the list of configuration features in {@code bundle} to enable for the trust agent
                 * component {@code agent}.
                 */
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    mDPM.setKeyguardDisabledFeatures(mDeviceAdminSample, createKeyguardDisabledFlag());
                    String component = mTrustAgentComponent.getText();
                    if (component != null) {
                        ComponentName agent = ComponentName.unflattenFromString(component);
                        if (agent != null) {
                            String featureString = mTrustAgentFeatures.getText();
                            if (featureString != null) {
                                PersistableBundle bundle = new PersistableBundle();
                                bundle.putStringArray("features", featureString.split(","));
                                mDPM.setTrustAgentConfiguration(mDeviceAdminSample, agent, bundle);
                            }
                        } else {
                            Log.w(TAG, "Invalid component: " + component);
                        }
                    }
                }
            });
        }

        /**
         * Set the summaries of all our {@code Preference} widgets to strings appropriate for their
         * current state. First we call our super's implementation of {@code reloadSummaries}. Then
         * we initialize {@code String cameraSummary} to the string with id R.string.camera_disabled
         * ("Device cameras disabled") if the {@code getCameraDisabled} method of {@code mDPM} returns
         * true or the string with id R.string.camera_enabled ("Device cameras enabled") if it returns
         * false, and then we set the summary of {@code CheckBoxPreference mDisableCameraCheckbox} to
         * {@code cameraSummary}.
         * <p>
         * We initialize {@code int disabled} with the bitmap of flags for the disabled keyguard features
         * return by the {@code getKeyguardDisabledFeatures} of {@code mDPM}. Then for the following
         * keyguard feature {@code CheckBoxPreference} widgets in our UI:
         * <ul>
         *     <li>
         *         {@code mDisableKeyguardWidgetsCheckbox} "Disable keyguard widgets" -- If the KEYGUARD_DISABLE_WIDGETS_ALL
         *         bit is set in {@code disable} we initialize {@code String keyguardWidgetSummary} to the
         *         string with resource id R.string.keyguard_widgets_disabled ("Keyguard widgets disabled"),
         *         if unset we initialize {@code String keyguardWidgetSummary} to the string with resource id
         *         R.string.keyguard_widgets_enabled ("Keyguard widgets enabled"). Then we set the summary
         *         of {@code mDisableKeyguardWidgetsCheckbox} to {@code keyguardWidgetSummary}.
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardSecureCameraCheckbox} "Disable keyguard secure camera" -- If the
         *         KEYGUARD_DISABLE_SECURE_CAMERA bit is set in {@code disable} we initialize {@code String keyguardSecureCameraSummary}
         *         to the string with resource id R.string.keyguard_secure_camera_disabled ("Keyguard secure camera disabled"),
         *         if unset we initialize {@code String keyguardSecureCameraSummary} to the string with resource id
         *         R.string.keyguard_secure_camera_enabled ("Keyguard secure camera enabled"). Then we set the summary
         *         of {@code mDisableKeyguardSecureCameraCheckbox} to {@code keyguardSecureCameraSummary}.
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardNotificationCheckbox} "Disable keyguard notifications" -- If the
         *         KEYGUARD_DISABLE_SECURE_NOTIFICATIONS bit is set in {@code disable} we initialize
         *         {@code String keyguardSecureNotificationsSummary} to the string with resource id
         *         R.string.keyguard_secure_notifications_disabled ("Keyguard notifications disabled"),
         *         if unset we initialize {@code String keyguardSecureNotificationsSummary} to the string with
         *         resource id R.string.keyguard_secure_notifications_enabled ("Keyguard notifications enabled").
         *         Then we set the summary of {@code mDisableKeyguardNotificationCheckbox} to
         *         {@code keyguardSecureNotificationsSummary}.
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardUnredactedCheckbox} "Disable keyguard unredacted notifications" -- If the
         *         KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS bit is set in {@code disable} we initialize
         *         {@code String keyguardUnredactedSummary} to the string with resource id R.string.keyguard_unredacted_notifications_disabled
         *         ("Keyguard unredacted notifications disabled"), if unset we initialize {@code String keyguardUnredactedSummary}
         *         to the string with resource id R.string.keyguard_unredacted_notifications_enabled ("Keyguard unredacted notifications enabled").
         *         Then we set the summary of {@code mDisableKeyguardUnredactedCheckbox} to {@code keyguardUnredactedSummary}.
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardTrustAgentCheckbox} "Disable keyguard Trust Agents" -- If the
         *         KEYGUARD_DISABLE_TRUST_AGENTS bit is set in {@code disable} we initialize {@code String keyguardEnableTrustAgentSummary}
         *         to the string with resource id R.string.keyguard_trust_agents_disabled ("Keyguard Trust Agents disabled"), if unset we
         *         initialize {@code String keyguardEnableTrustAgentSummary} to the string with resource id R.string.keyguard_trust_agents_enabled
         *         ("Keyguard Trust Agents enabled"). Then we set the summary of {@code mDisableKeyguardTrustAgentCheckbox} to
         *         {@code keyguardEnableTrustAgentSummary}.
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardFingerprintCheckbox} "Disable keyguard Fingerprint" -- If the
         *         KEYGUARD_DISABLE_FINGERPRINT bit is set in {@code disable} we initialize
         *         {@code String keyguardEnableFingerprintSummary} to the string with resource id
         *         R.string.keyguard_fingerprint_disabled ("Keyguard Fingerprint disabled"), if unset we
         *         initialize {@code String keyguardEnableFingerprintSummary} to the string with resource
         *         id R.string.keyguard_fingerprint_enabled ("Keyguard Fingerprint enabled"). Then we set
         *         the summary of {@code mDisableKeyguardFingerprintCheckbox} to {@code keyguardEnableFingerprintSummary}.
         *     </li>
         *     <li>
         *         {@code mDisableKeyguardRemoteInputCheckbox} "Disable keyguard Remote Input" -- If the
         *         KEYGUARD_DISABLE_REMOTE_INPUT bit is set in {@code disable} we initialize {@code String keyguardEnableRemoteInputSummary}
         *         to the string with resource id R.string.keyguard_remote_input_disabled ("Keyguard Remote Input disabled"),
         *         if unset we initialize {@code String keyguardEnableRemoteInputSummary} to the string with resource
         *         id R.string.keyguard_remote_input_enabled ("Keyguard Remote Input enabled"). Then we set
         *         the summary of {@code mDisableKeyguardRemoteInputCheckbox} to {@code keyguardEnableRemoteInputSummary}.
         *     </li>
         * </ul>
         * Now we deal with the two trust agent {@code EditTextPreference} widgets. We initialize {@code SharedPreferences prefs}
         * with an instance that the {@code getSharedPreferences} method of the {@code PreferenceManager} used by this fragment
         * returns for us to use. We initialize {@code boolean trustDisabled} to true if the KEYGUARD_DISABLE_TRUST_AGENTS
         * bit of {@code disabled} is set, and false if not. We initialize {@code String component} with the string stored in
         * {@code SharedPreferences prefs} under the key used by {@code EditTextPreference mTrustAgentComponent} defaulting
         * to null. We then set the summary of {@code mTrustAgentComponent} to {@code component}, and enable or disable it
         * depending on the value of {@code trustDisabled}. We initialize {@code String features} with the string stored in
         * {@code SharedPreferences prefs} under the key used by {@code EditTextPreference mTrustAgentFeatures} defaulting
         * to null. We then set the summary of {@code mTrustAgentFeatures} to {@code features}, and enable or disable it
         * depending on the value of {@code trustDisabled}.
         */
        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();
            String cameraSummary = getString(mDPM.getCameraDisabled(mDeviceAdminSample)
                    ? R.string.camera_disabled : R.string.camera_enabled);
            mDisableCameraCheckbox.setSummary(cameraSummary);

            int disabled = mDPM.getKeyguardDisabledFeatures(mDeviceAdminSample);

            String keyguardWidgetSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL) != 0 ?
                            R.string.keyguard_widgets_disabled : R.string.keyguard_widgets_enabled);
            mDisableKeyguardWidgetsCheckbox.setSummary(keyguardWidgetSummary);

            String keyguardSecureCameraSummary = getString(
                (disabled & DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA) != 0 ?
                R.string.keyguard_secure_camera_disabled : R.string.keyguard_secure_camera_enabled);
            mDisableKeyguardSecureCameraCheckbox.setSummary(keyguardSecureCameraSummary);

            String keyguardSecureNotificationsSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS) != 0 ?
                        R.string.keyguard_secure_notifications_disabled
                        : R.string.keyguard_secure_notifications_enabled);
            mDisableKeyguardNotificationCheckbox.setSummary(keyguardSecureNotificationsSummary);

            String keyguardUnredactedSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS) != 0
                        ? R.string.keyguard_unredacted_notifications_disabled
                        : R.string.keyguard_unredacted_notifications_enabled);
            mDisableKeyguardUnredactedCheckbox.setSummary(keyguardUnredactedSummary);

            String keyguardEnableTrustAgentSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS) != 0 ?
                        R.string.keyguard_trust_agents_disabled
                        : R.string.keyguard_trust_agents_enabled);
            mDisableKeyguardTrustAgentCheckbox.setSummary(keyguardEnableTrustAgentSummary);

            String keyguardEnableFingerprintSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT) != 0 ?
                        R.string.keyguard_fingerprint_disabled
                        : R.string.keyguard_fingerprint_enabled);
            mDisableKeyguardFingerprintCheckbox.setSummary(keyguardEnableFingerprintSummary);

            String keyguardEnableRemoteInputSummary = getString(
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT) != 0 ?
                        R.string.keyguard_remote_input_disabled
                        : R.string.keyguard_remote_input_enabled);
            mDisableKeyguardRemoteInputCheckbox.setSummary(keyguardEnableRemoteInputSummary);

            final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
            final boolean trustDisabled =
                    (disabled & DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS) != 0;
            String component = prefs.getString(mTrustAgentComponent.getKey(), null);
            mTrustAgentComponent.setSummary(component);
            mTrustAgentComponent.setEnabled(trustDisabled);

            String features = prefs.getString(mTrustAgentFeatures.getKey(), null);
            mTrustAgentFeatures.setSummary(features);
            mTrustAgentFeatures.setEnabled(trustDisabled);
        }

        /** Updates the device capabilities area (dis/enabling) as the admin is (de)activated
         *
         * @param enabled if true all the device capability {@code CheckBoxPreference} widgets are
         *                enabled, if false they are disabled.
         */
        private void enableDeviceCapabilitiesArea(boolean enabled) {
            mDisableCameraCheckbox.setEnabled(enabled);
            mDisableKeyguardWidgetsCheckbox.setEnabled(enabled);
            mDisableKeyguardSecureCameraCheckbox.setEnabled(enabled);
            mDisableKeyguardNotificationCheckbox.setEnabled(enabled);
            mDisableKeyguardUnredactedCheckbox.setEnabled(enabled);
            mDisableKeyguardTrustAgentCheckbox.setEnabled(enabled);
            mTrustAgentComponent.setEnabled(enabled);
            mTrustAgentFeatures.setEnabled(enabled);
        }
    }

    /**
     * PreferenceFragment for "password quality" preferences.
     */
    public static class QualityFragment extends AdminSampleFragment
            implements OnPreferenceChangeListener {

        /**
         * Password quality values. This list must match the list found in res/values/arrays.xml
         * for the "password_qualities" string-array.
         */
        final static int[] mPasswordQualityValues = new int[] {
            DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED,
            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING,
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
        };

        /**
         * Password quality values (as strings, for the ListPreference entryValues). This list must
         * match the list found in res/values/arrays.xml for the "password_qualities" string-array.
         */
        final static String[] mPasswordQualityValueStrings = new String[] {
            String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED),
            String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_SOMETHING),
            String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_NUMERIC),
            String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX),
            String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC),
            String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC),
            String.valueOf(DevicePolicyManager.PASSWORD_QUALITY_COMPLEX)
        };

        // UI elements
        /**
         * {@code PreferenceCategory} "Password quality" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_category_quality"
         */
        private PreferenceCategory mQualityCategory;
        /**
         * {@code ListPreference} "Password quality" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_quality"
         */
        private ListPreference mPasswordQuality;
        /**
         * {@code EditTextPreference} "Minimum length" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_minimum_length"
         */
        private EditTextPreference mMinLength;
        /**
         * {@code EditTextPreference} "Minimum letters" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_minimum_letters"
         */
        private EditTextPreference mMinLetters;
        /**
         * {@code EditTextPreference} "Minimum numeric" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_minimum_numeric"
         */
        private EditTextPreference mMinNumeric;
        /**
         * {@code EditTextPreference} "Minimum lower case" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_minimum_lower_case"
         */
        private EditTextPreference mMinLowerCase;
        /**
         * {@code EditTextPreference} "Minimum upper case" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_minimum_upper_case"
         */
        private EditTextPreference mMinUpperCase;
        /**
         * {@code EditTextPreference} "Minimum symbols" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_minimum_symbols"
         */
        private EditTextPreference mMinSymbols;
        /**
         * {@code EditTextPreference} "Minimum non-letter" in the {@code PreferenceScreen}
         * xml/device_admin_quality.xml with the key "key_minimum_non_letter"
         */
        private EditTextPreference mMinNonLetter;

        /**
         * Called to do initial creation of a {@code PreferenceFragment}. First we call our super's
         * implementation of {@code onCreate}, then we call the {@code addPreferencesFromResource}
         * method to inflate our XML resource file R.xml.device_admin_quality and add its preference
         * hierarchy to the current preference hierarchy. We then initialize the fields we use to
         * access the various {@link Preference} widgets in our UI by finding them using the android:key
         * strings they are identified by in the xml/device_admin_general.xml file. After doing so we
         * set their {@code OnPreferenceChangeListener} to this. Finally we call the {@code setEntryValues}
         * method of {@code ListPreference mPasswordQuality} to set the values to save for the preferences
         * when an entry is selected to our array {@code String[] mPasswordQualityValueStrings}.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_quality);

            mQualityCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_QUALITY);
            mPasswordQuality = (ListPreference) findPreference(KEY_QUALITY);
            mMinLength = (EditTextPreference) findPreference(KEY_MIN_LENGTH);
            mMinLetters = (EditTextPreference) findPreference(KEY_MIN_LETTERS);
            mMinNumeric = (EditTextPreference) findPreference(KEY_MIN_NUMERIC);
            mMinLowerCase = (EditTextPreference) findPreference(KEY_MIN_LOWER_CASE);
            mMinUpperCase = (EditTextPreference) findPreference(KEY_MIN_UPPER_CASE);
            mMinSymbols = (EditTextPreference) findPreference(KEY_MIN_SYMBOLS);
            mMinNonLetter = (EditTextPreference) findPreference(KEY_MIN_NON_LETTER);

            mPasswordQuality.setOnPreferenceChangeListener(this);
            mMinLength.setOnPreferenceChangeListener(this);
            mMinLetters.setOnPreferenceChangeListener(this);
            mMinNumeric.setOnPreferenceChangeListener(this);
            mMinLowerCase.setOnPreferenceChangeListener(this);
            mMinUpperCase.setOnPreferenceChangeListener(this);
            mMinSymbols.setOnPreferenceChangeListener(this);
            mMinNonLetter.setOnPreferenceChangeListener(this);

            // Finish setup of the quality dropdown
            mPasswordQuality.setEntryValues(mPasswordQualityValueStrings);
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of {@code onResume}. Then we enable or disable our UI element
         * {@code PreferenceCategory mQualityCategory} depending on the value of {@code mAdminActive},
         * enabling it if we are in an active admin capacity and disabling it if we are not.
         */
        @Override
        public void onResume() {
            super.onResume();
            mQualityCategory.setEnabled(mAdminActive);
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting.
         */
        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();
            // Show numeric settings for each policy API
            int local, global;
            local = mDPM.getPasswordQuality(mDeviceAdminSample);
            global = mDPM.getPasswordQuality(null);
            mPasswordQuality.setSummary(
                    localGlobalSummary(qualityValueToString(local), qualityValueToString(global)));
            local = mDPM.getPasswordMinimumLength(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumLength(null);
            mMinLength.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumLetters(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumLetters(null);
            mMinLetters.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumNumeric(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumNumeric(null);
            mMinNumeric.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumLowerCase(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumLowerCase(null);
            mMinLowerCase.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumUpperCase(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumUpperCase(null);
            mMinUpperCase.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumSymbols(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumSymbols(null);
            mMinSymbols.setSummary(localGlobalSummary(local, global));
            local = mDPM.getPasswordMinimumNonLetter(mDeviceAdminSample);
            global = mDPM.getPasswordMinimumNonLetter(null);
            mMinNonLetter.setSummary(localGlobalSummary(local, global));
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * {@code onPreferenceChange} returns true, we return true having done nothing. Otherwise we
         * initialize {@code String valueString} by casting our parameter {@code Object newValue} to
         * string, and if it is empty we return false so that the preference is not updated. Next we
         * initialize {@code int value} to 0, and wrapped in a try block intended to catch NumberFormatException
         * in order to toast a "Bad value" error message we set {@code value} to the integer value of
         * {@code valueString}. Now we branch depending on which of our preference widgets match our
         * parameter {@code Preference preference}:
         * <ul>
         *     <li>
         *         {@code mPasswordQuality} "Password quality" ListPreference -- we call the {@code setPasswordQuality}
         *         method of {@code DevicePolicyManager mDPM} to set the password restrictions we are imposing
         *         to {@code value}.
         *     </li>
         *     <li>
         *         {@code mMinLength} "Minimum length" EditTextPreference -- we call the {@code setPasswordMinimumLength}
         *         method of {@code DevicePolicyManager mDPM} to set the minimum allowed password length to {@code value}.
         *     </li>
         *     <li>
         *         {@code mMinLetters} "Minimum letters" EditTextPreference -- we call the {@code setPasswordMinimumLetters}
         *         method of {@code DevicePolicyManager mDPM} to set the minimum number of letters required in the password
         *         to {@code value}.
         *     </li>
         *     <li>
         *         {@code mMinNumeric} "Minimum numeric" EditTextPreference -- we call the {@code setPasswordMinimumNumeric}
         *         method of {@code DevicePolicyManager mDPM} to set the minimum number of numerical digits required in the
         *         password to {@code value}.
         *     </li>
         *     <li>
         *         {@code mMinLowerCase} "Minimum lower case" EditTextPreference -- we call the {@code setPasswordMinimumLowerCase}
         *         method of {@code DevicePolicyManager mDPM} to set the minimum number of lower case letters required in the
         *         password to {@code value}.
         *     </li>
         *     <li>
         *         {@code mMinUpperCase} "Minimum upper case" EditTextPreference -- we call the {@code setPasswordMinimumUpperCase}
         *         method of {@code DevicePolicyManager mDPM} to set the minimum number of upper case letters required
         *         in the password to {@code value}.
         *     </li>
         *     <li>
         *         {@code mMinSymbols} "Minimum symbols" EditTextPreference -- we call the {@code setPasswordMinimumSymbols}
         *         method of {@code DevicePolicyManager mDPM} to set the minimum number of symbols required in the password
         *         to {@code value}.
         *     </li>
         *     <li>
         *         {@code mMinNonLetter} "Minimum non-letter" EditTextPreference -- we call the {@code setPasswordMinimumNonLetter}
         *         method of {@code DevicePolicyManager mDPM} to set the minimum number of non-letters required in the password
         *         to {@code value}.
         *     </li>
         * </ul>
         * We then call our method {@code postReloadSummaries} to post a call to {@code reloadSummaries}
         * on the UI queue so that it won't run until after the preference change has been applied
         * upon exiting this method. Finally we return true to update the state of the Preference with
         * the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            String valueString = (String)newValue;
            if (TextUtils.isEmpty(valueString)) {
                return false;
            }
            int value = 0;
            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException nfe) {
                String warning = mActivity.getString(R.string.number_format_warning, valueString);
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show();
            }
            if (preference == mPasswordQuality) {
                mDPM.setPasswordQuality(mDeviceAdminSample, value);
            } else if (preference == mMinLength) {
                mDPM.setPasswordMinimumLength(mDeviceAdminSample, value);
            } else if (preference == mMinLetters) {
                mDPM.setPasswordMinimumLetters(mDeviceAdminSample, value);
            } else if (preference == mMinNumeric) {
                mDPM.setPasswordMinimumNumeric(mDeviceAdminSample, value);
            } else if (preference == mMinLowerCase) {
                mDPM.setPasswordMinimumLowerCase(mDeviceAdminSample, value);
            } else if (preference == mMinUpperCase) {
                mDPM.setPasswordMinimumUpperCase(mDeviceAdminSample, value);
            } else if (preference == mMinSymbols) {
                mDPM.setPasswordMinimumSymbols(mDeviceAdminSample, value);
            } else if (preference == mMinNonLetter) {
                mDPM.setPasswordMinimumNonLetter(mDeviceAdminSample, value);
            }
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries();
            return true;
        }

        /**
         * Translates the {@code DevicePolicyManager} integer constant that is used to specify a password
         * quality into a string to display. We loop through our field {@code int[] mPasswordQualityValues}
         * using {@code int i} as the index, and if {@code mPasswordQualityValues[i]} is equal to our
         * parameter {@code int quality} we initialize {@code String[] qualities} with the resource
         * string array with id R.array.password_qualities and return {@code qualities[i]} to the caller.
         * If we do not find {@code quality} in the {@code mPasswordQualityValues} array we format the
         * radix 16 string value of {@code quality} into a string which we return to the caller.
         *
         * @param quality {@code DevicePolicyManager} integer constant that is used to specify a password
         *                quality that is used by {@code getPasswordQuality} and {@code setPasswordQuality}
         * @return a string describing the password quality corresponding to our parameter {@code int quality}
         */
        private String qualityValueToString(int quality) {
            for (int i=  0; i < mPasswordQualityValues.length; i++) {
                if (mPasswordQualityValues[i] == quality) {
                    String[] qualities =
                        mActivity.getResources().getStringArray(R.array.password_qualities);
                    return qualities[i];
                }
            }
            return "(0x" + Integer.toString(quality, 16) + ")";
        }
    }

    /**
     * PreferenceFragment for "password expiration" preferences, uses xml/device_admin_expiration.xml
     * as its {@code PreferenceScreen}.
     */
    public static class ExpirationFragment extends AdminSampleFragment
            implements OnPreferenceChangeListener, OnPreferenceClickListener {
        /**
         * {@code PreferenceCategory} "Password history / Expiration" with the key key_category_expiration
         * in the xml/device_admin_expiration.xml {@code PreferenceScreen}
         */
        private PreferenceCategory mExpirationCategory;
        /**
         * {@code EditTextPreference} "Password history depth" with the key key_history
         * in the xml/device_admin_expiration.xml {@code PreferenceScreen}
         */
        private EditTextPreference mHistory;
        /**
         * {@code EditTextPreference} "Password expiration timeout (minutes)" with the key key_expiration_timeout
         * in the xml/device_admin_expiration.xml {@code PreferenceScreen}
         */
        private EditTextPreference mExpirationTimeout;
        /**
         * {@code PreferenceScreen} "Password expiration status" with the key key_expiration_status
         * in the xml/device_admin_expiration.xml {@code PreferenceScreen}
         */
        private PreferenceScreen mExpirationStatus;

        /**
         * Called to do initial creation of a fragment. First we call our super's implementation of
         * {@code onCreate}, then we call the {@code addPreferencesFromResource} method to inflate
         * the XML resource R.xml.device_admin_expiration and add its preference hierarchy to the
         * current preference hierarchy. We initialize {@code PreferenceCategory mExpirationCategory}
         * by finding the preference with key KEY_CATEGORY_EXPIRATION ("key_category_expiration"),
         * initialize {@code EditTextPreference mHistory} by finding the preference with key KEY_HISTORY
         * ("key_history"), initialize {@code EditTextPreference mExpirationTimeout} by finding the
         * preference with key KEY_EXPIRATION_TIMEOUT ("key_expiration_timeout"), and initialize
         * {@code PreferenceScreen mExpirationStatus} by finding the preference with key KEY_EXPIRATION_STATUS
         * ("key_expiration_status"). We then set the {@code OnPreferenceChangeListener} of {@code mHistory},
         * {@code mExpirationTimeout} and {@code mExpirationStatus} to this.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_expiration);

            mExpirationCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_EXPIRATION);
            mHistory = (EditTextPreference) findPreference(KEY_HISTORY);
            mExpirationTimeout = (EditTextPreference) findPreference(KEY_EXPIRATION_TIMEOUT);
            mExpirationStatus = (PreferenceScreen) findPreference(KEY_EXPIRATION_STATUS);

            mHistory.setOnPreferenceChangeListener(this);
            mExpirationTimeout.setOnPreferenceChangeListener(this);
            mExpirationStatus.setOnPreferenceClickListener(this);
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of {@code onResume}, then we call the {@code setEnabled} method of
         * {@code PreferenceCategory mExpirationCategory} to enable the preference if {@code mAdminActive}
         * is true or disable it if it is false.
         */
        @Override
        public void onResume() {
            super.onResume();
            mExpirationCategory.setEnabled(mAdminActive);
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting. First
         * we call our super's implementation of {@code reloadSummaries}. Then we declare {@code local}
         * and {@code global}, set local to the local length of the password history returned by the
         * {@code getPasswordHistoryLength} method of {@code DevicePolicyManager mDPM} and {@code global}
         * to the global value it returns. We then set the summary of {@code EditTextPreference mHistory}
         * to the string that our method {@code localGlobalSummary} constructs from {@code local} and
         * {@code global}. We declare {@code long localLong} and {@code long globalLong}, set {@code localLong}
         * to the local password expiration timeout returned by the {@code getPasswordExpirationTimeout}
         * method of {@code DevicePolicyManager mDPM} and {@code globalLong} to the global value it returns.
         * We then set the summary of {@code EditTextPreference mExpirationTimeout} to the string that our
         * method {@code localGlobalSummary} constructs from {@code localLong} divided by the number
         * of milliseconds in a minute and {@code globalLong} divided by the number of milliseconds
         * in a minute. We initialize {@code String expirationStatus} with the string formed by our
         * {@code getExpirationStatus} method from the current password expiration time for both local
         * and global profiles, then set the summary of {@code PreferenceScreen mExpirationStatus}
         * to it.
         */
        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();

            int local, global;
            local = mDPM.getPasswordHistoryLength(mDeviceAdminSample);
            global = mDPM.getPasswordHistoryLength(null);
            mHistory.setSummary(localGlobalSummary(local, global));

            long localLong, globalLong;
            localLong = mDPM.getPasswordExpirationTimeout(mDeviceAdminSample);
            globalLong = mDPM.getPasswordExpirationTimeout(null);
            mExpirationTimeout.setSummary(localGlobalSummary(
                    localLong / MS_PER_MINUTE, globalLong / MS_PER_MINUTE));

            String expirationStatus = getExpirationStatus();
            mExpirationStatus.setSummary(expirationStatus);
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * {@code onPreferenceChange} returns true, we return true having done nothing. Otherwise we
         * initialize {@code String valueString} by casting our parameter {@code Object newValue} to
         * string and if it is empty we return false so the preference is not updated. Next we initialize
         * {@code int value} to 0, and wrapped in a try block intended to catch NumberFormatException
         * in order to toast a "Bad value" error message we set {@code value} to the integer value of
         * {@code valueString}. We then branch on the value of our parameter {@code Preference preference}:
         * <ul>
         *     <li>
         *         {@code EditTextPreference mHistory} "Password history depth" we call the {@code setPasswordHistoryLength}
         *         method of {@code DevicePolicyManager mDPM} to set the length of the password history
         *         to {@code value}.
         *     </li>
         *     <li>
         *         {@code EditTextPreference mExpirationTimeout} "Password expiration timeout (minutes)"
         *         we call the {@code setPasswordExpirationTimeout} method of {@code DevicePolicyManager mDPM}
         *         to set the password expiration timeout to {@code value}.
         *     </li>
         * </ul>
         * We then call our method {@code postReloadSummaries} to post a call to {@code reloadSummaries}
         * on the UI queue so that it won't run until after the preference change has been applied
         * upon exiting this method. Finally we return true to update the state of the Preference with
         * the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            String valueString = (String)newValue;
            if (TextUtils.isEmpty(valueString)) {
                return false;
            }
            int value = 0;
            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException nfe) {
                String warning = mActivity.getString(R.string.number_format_warning, valueString);
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show();
            }
            if (preference == mHistory) {
                mDPM.setPasswordHistoryLength(mDeviceAdminSample, value);
            } else if (preference == mExpirationTimeout) {
                mDPM.setPasswordExpirationTimeout(mDeviceAdminSample, value * MS_PER_MINUTE);
            }
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries();
            return true;
        }

        /**
         * Called when a Preference has been clicked. If our super's implementation of {@code onPreferenceClick}
         * returns true, we return true having done nothing. If our parameter {@code Preference preference} is
         * {@code PreferenceScreen mExpirationStatus} we initialize {@code String expirationStatus} with the
         * string formed by our {@code getExpirationStatus} method from the current password expiration time
         * for both local and global profiles, then set the summary of {@code PreferenceScreen mExpirationStatus}
         * to it and return true to the caller having consumed the click. If {@code preference} is not
         * equal to {@code mExpirationStatus} we return false to the caller.
         *
         * @param preference The Preference that was clicked.
         * @return True if the click was handled.
         */
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (super.onPreferenceClick(preference)) {
                return true;
            }
            if (preference == mExpirationStatus) {
                String expirationStatus = getExpirationStatus();
                mExpirationStatus.setSummary(expirationStatus);
                return true;
            }
            return false;
        }

        /**
         * Create a summary string describing the expiration status for the sample app, as well as
         * the global (aggregate) status. We initialize {@code long localExpiration} with the local
         * password expiration time returned by the {@code getPasswordExpiration} method of
         * {@code DevicePolicyManager mDPM} and {@code long globalExpiration} to the global value,
         * then initialize {@code long now} with the current time in milliseconds. We declare
         * {@code String local} and if {@code localExpiration} is zero we set it to the string with
         * resource id R.string.expiration_status_none ("None"), otherwise we subtract {@code now}
         * from {@code localExpiration}. We initialize {@code String dms} to the string returned by our
         * method {@code timeToDaysMinutesSeconds} creates from the absolute value of {@code localExpiration}.
         * If {@code localExpiration} is greater than or equal to 0 we set {@code local} to the formatted
         * string created from {@code dms} using the format string with resource id R.string.expiration_status_future
         * ("Password will expire %1$s from now") otherwise we set it to the formatted string created from
         * {@code dms} using the format string with resource id R.string.expiration_status_past
         * ("Password expired %1$s ago").
         * <p>
         * We then declare {@code String global} and if {@code globalExpiration} is 0 we set it to the
         * string with resource id R.string.expiration_status_none ("None"), otherwise we subtract
         * {@code now} from {@code globalExpiration}. We initialize {@code String dms} to the string
         * returned by our method {@code timeToDaysMinutesSeconds} creates from the absolute value of
         * {@code globalExpiration}. If {@code globalExpiration} is greater than or equal to 0 we set
         * {@code global} to the formatted string created from {@code dms} using the format string with
         * resource id R.string.expiration_status_future ("Password will expire %1$s from now") otherwise
         * we set it to the formatted string created from {@code dms} using the format string with resource
         * id R.string.expiration_status_past ("Password expired %1$s ago"). Finally we return the formatted
         * string created from {@code local} and {@code global} using the format string with resource id
         * R.string.status_local_global ("Local=%1$s / Global=%2$s").
         */
        private String getExpirationStatus() {
            // expirations are absolute;  convert to relative for display
            long localExpiration = mDPM.getPasswordExpiration(mDeviceAdminSample);
            long globalExpiration = mDPM.getPasswordExpiration(null);
            long now = System.currentTimeMillis();

            // local expiration
            String local;
            if (localExpiration == 0) {
                local = mActivity.getString(R.string.expiration_status_none);
            } else {
                localExpiration -= now;
                String dms = timeToDaysMinutesSeconds(mActivity, Math.abs(localExpiration));
                if (localExpiration >= 0) {
                    local = mActivity.getString(R.string.expiration_status_future, dms);
                } else {
                    local = mActivity.getString(R.string.expiration_status_past, dms);
                }
            }

            // global expiration
            String global;
            if (globalExpiration == 0) {
                global = mActivity.getString(R.string.expiration_status_none);
            } else {
                globalExpiration -= now;
                String dms = timeToDaysMinutesSeconds(mActivity, Math.abs(globalExpiration));
                if (globalExpiration >= 0) {
                    global = mActivity.getString(R.string.expiration_status_future, dms);
                } else {
                    global = mActivity.getString(R.string.expiration_status_past, dms);
                }
            }
            return mActivity.getString(R.string.status_local_global, local, global);
        }
    }

    /**
     * PreferenceFragment for "lock screen & wipe" preferences, uses the xml/device_admin_lock_wipe.xml
     * file as its {@code PreferenceScreen}.
     */
    public static class LockWipeFragment extends AdminSampleFragment
            implements OnPreferenceChangeListener, OnPreferenceClickListener {
        /**
         * {@code PreferenceCategory} "Lock screen / Wipe" with the key key_category_lock_wipe in the
         * xml/device_admin_lock_wipe.xml {@code PreferenceScreen}
         */
        private PreferenceCategory mLockWipeCategory;
        /**
         * {@code EditTextPreference} "Max time to screen lock (minutes)" with the key key_max_time_screen_lock
         * in the xml/device_admin_lock_wipe.xml {@code PreferenceScreen}
         */
        private EditTextPreference mMaxTimeScreenLock;
        /**
         * {@code EditTextPreference} "Max password failures for local wipe" with the key key_max_fails_before_wipe
         * in the xml/device_admin_lock_wipe.xml {@code PreferenceScreen}
         */
        private EditTextPreference mMaxFailures;
        /**
         * {@code PreferenceScreen} "Lock screen now" with the key key_lock_screen in the
         * xml/device_admin_lock_wipe.xml {@code PreferenceScreen}
         */
        private PreferenceScreen mLockScreen;
        /**
         * {@code PreferenceScreen} "Wipe data" with the key key_wipe_data in the
         * xml/device_admin_lock_wipe.xml {@code PreferenceScreen}
         */
        private PreferenceScreen mWipeData;
        /**
         * {@code PreferenceScreen} "Wipe all data" with the key key_wipe_data_all in the
         * xml/device_admin_lock_wipe.xml {@code PreferenceScreen}
         */
        private PreferenceScreen mWipeAppData;

        /**
         * Called to do initial creation of a {@code PreferenceFragment}. First we call our super's
         * implementation of {@code onCreate}, then we call the {@code addPreferencesFromResource}
         * method to inflate our XML resource file R.xml.device_admin_lock_wipe and add its preference
         * hierarchy to the current preference hierarchy. We then initialize the fields we use to
         * access the various {@link Preference} widgets in our UI by finding them using the android:key
         * strings they are identified by in the xml/device_admin_lock_wipe.xml file. After doing so we
         * set their {@code OnPreferenceChangeListener} to this.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_lock_wipe);

            mLockWipeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_LOCK_WIPE);
            mMaxTimeScreenLock = (EditTextPreference) findPreference(KEY_MAX_TIME_SCREEN_LOCK);
            mMaxFailures = (EditTextPreference) findPreference(KEY_MAX_FAILS_BEFORE_WIPE);
            mLockScreen = (PreferenceScreen) findPreference(KEY_LOCK_SCREEN);
            mWipeData = (PreferenceScreen) findPreference(KEY_WIPE_DATA);
            mWipeAppData = (PreferenceScreen) findPreference(KEY_WIP_DATA_ALL);

            mMaxTimeScreenLock.setOnPreferenceChangeListener(this);
            mMaxFailures.setOnPreferenceChangeListener(this);
            mLockScreen.setOnPreferenceClickListener(this);
            mWipeData.setOnPreferenceClickListener(this);
            mWipeAppData.setOnPreferenceClickListener(this);
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of {@code onResume}. Then we enable or disable our UI element
         * {@code PreferenceCategory mLockWipeCategory} depending on the value of {@code mAdminActive},
         * enabling it if we are in an active admin capacity and disabling it if we are not.
         */
        @Override
        public void onResume() {
            super.onResume();
            mLockWipeCategory.setEnabled(mAdminActive);
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting. First
         * we call our super's implementation of {@code reloadSummaries}. We then declare {@code long localLong}
         * and {@code long globalLong}. Then we set {@code localLong} to the current local maximum time to unlock
         * returned by the {@code getMaximumTimeToLock} method of {@code DevicePolicyManager mDPM} and
         * {@code globalLong} to the global value returned. We then set the summary of {@code EditTextPreference mMaxTimeScreenLock}
         * to the string that our method {@code localGlobalSummary} constructs from {@code localLong}
         * divided by the number of milliseconds in a minute and {@code globalLong} divided by the number
         * of milliseconds in a minute. Then we declare {@code int local} and {@code int global}, set {@code local}
         * to the local current maximum number of login attempts that are allowed before the device or
         * profile is wiped returned by the {@code getMaximumFailedPasswordsForWipe} method of
         * {@code DevicePolicyManager mDPM} and {@code global} to the global value it returns. We then
         * set the summary of {@code EditTextPreference mMaxFailures} to the string that our method
         * {@code localGlobalSummary} constructs from {@code local} and {@code global}.
         */
        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();

            long localLong, globalLong;
            localLong = mDPM.getMaximumTimeToLock(mDeviceAdminSample);
            globalLong = mDPM.getMaximumTimeToLock(null);
            mMaxTimeScreenLock.setSummary(localGlobalSummary(
                    localLong / MS_PER_MINUTE, globalLong / MS_PER_MINUTE));

            int local, global;
            local = mDPM.getMaximumFailedPasswordsForWipe(mDeviceAdminSample);
            global = mDPM.getMaximumFailedPasswordsForWipe(null);
            mMaxFailures.setSummary(localGlobalSummary(local, global));
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * {@code onPreferenceChange} returns true, we return true having done nothing. Otherwise we
         * initialize {@code String valueString} by casting our parameter {@code Object newValue} to
         * string, and if it is empty we return false so that the preference is not updated. Next we
         * initialize {@code int value} to 0, and wrapped in a try block intended to catch NumberFormatException
         * in order to toast a "Bad value" error message we set {@code value} to the integer value of
         * {@code valueString}. Now we branch depending on which of our preference widgets match our
         * parameter {@code Preference preference}:
         * <ul>
         *     <li>
         *         {@code EditTextPreference mMaxTimeScreenLock} "Max time to screen lock (minutes)"
         *         we call the {@code setMaximumTimeToLock} method of {@code DevicePolicyManager mDPM}
         *         to set the maximum time for user activity until the device will lock to {@code value}
         *         times the number of milliseconds in a minute.
         *     </li>
         *     <li>
         *         {@code EditTextPreference mMaxFailures} "Max password failures for local wipe" we first
         *         call our method {@code alertIfMonkey} to make sure we are not being run by an automated
         *         test, toasting the message "You can't wipe my data, you are a monkey!" if so and returning
         *         true without doing more if is returns true. If it returns false we call the
         *         {@code setMaximumFailedPasswordsForWipe} method of {@code DevicePolicyManager mDPM}
         *         to set the number of failed password attempts at which point the device or profile will
         *         be wiped to {@code value}.
         *     </li>
         * </ul>
         * We then call our method {@code postReloadSummaries} to post a call to {@code reloadSummaries}
         * on the UI queue so that it won't run until after the preference change has been applied
         * upon exiting this method. Finally we return true to update the state of the Preference with
         * the new value.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            String valueString = (String)newValue;
            if (TextUtils.isEmpty(valueString)) {
                return false;
            }
            int value = 0;
            try {
                value = Integer.parseInt(valueString);
            } catch (NumberFormatException nfe) {
                String warning = mActivity.getString(R.string.number_format_warning, valueString);
                Toast.makeText(mActivity, warning, Toast.LENGTH_SHORT).show();
            }
            if (preference == mMaxTimeScreenLock) {
                mDPM.setMaximumTimeToLock(mDeviceAdminSample, value * MS_PER_MINUTE);
            } else if (preference == mMaxFailures) {
                if (alertIfMonkey(mActivity, R.string.monkey_wipe_data)) {
                    return true;
                }
                mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdminSample, value);
            }
            // Delay update because the change is only applied after exiting this method.
            postReloadSummaries();
            return true;
        }

        /**
         * Called when a Preference has been clicked. If our super's implementation of {@code onPreferenceClick}
         * returns true, we return true having done nothing. If our parameter {@code Preference preference} is
         * {@code PreferenceScreen mLockScreen} we call our method {@code alertIfMonkey} to check if we
         * are being run by an automated test, toasting the message "You can't lock my screen, you are a monkey!"
         * if so and if it returns true we return true having done nothing. If {@code alertIfMonkey} returned
         * false (indicating we are being run by a user) we call the {@code lockNow} method of
         * {@code DevicePolicyManager mDPM} to make the device lock immediately. If our parameter
         * {@code Preference preference} is {@code PreferenceScreen mWipeData} or {@code PreferenceScreen mWipeAppData}
         * we call our method {@code alertIfMonkey} to check if we are being run by an automated test,
         * toasting the message "You can't wipe my data, you are a monkey!" if so and if it returns true
         * we return true having done nothing. If {@code alertIfMonkey} returned false (indicating we are
         * being run by a user) we call our method {@code promptForRealDeviceWipe} with true if {@code preference}
         * is equal to {@code mWipeAppData} or false if it is {@code mWipeData}. Upon return from
         * {@code promptForRealDeviceWipe} we return true consuming the click. For any other value of
         * {@code preference} we return false indicating that we did not handle the click.
         *
         * @param preference The Preference that was clicked.
         * @return True if the click was handled.         *
         */
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (super.onPreferenceClick(preference)) {
                return true;
            }
            if (preference == mLockScreen) {
                if (alertIfMonkey(mActivity, R.string.monkey_lock_screen)) {
                    return true;
                }
                mDPM.lockNow();
                return true;
            } else if (preference == mWipeData || preference == mWipeAppData) {
                if (alertIfMonkey(mActivity, R.string.monkey_wipe_data)) {
                    return true;
                }
                promptForRealDeviceWipe(preference == mWipeAppData);
                return true;
            }
            return false;
        }

        /**
         * Calls the {@code DevicePolicyManager mDPM} {@code wipeData} method to wipe data on the
         * device after displaying two warning alert dialogs. Wiping data is real, so we don't want
         * it to be easy, so we show two alerts before wiping. We initialize {@code DeviceAdminSample activity}
         * with the value of our field {@code DeviceAdminSample mActivity}, and initialize {@code AlertDialog.Builder builder}
         * with a new instance. We set the message of {@code builder} to the string with resource id
         * R.string.wipe_warning_first ("This will erase all of your data.  Are you sure?") and set the
         * text of its positive button to the string with resource id R.string.wipe_warning_first_ok
         * ("Yes") and its {@code OnClickListener} to an anonymous class which builds and launches a
         * second alert dialog when the positive button is clicked. We set the text of its negative button
         * to the string with resource id R.string.wipe_warning_first_no ("No") then show this first
         * alert dialog. The {@code OnClickListener} of the positive button of the second alert dialog
         * that is displayed when the positive button of the first alert dialog is clicked will really
         * call the {@code DevicePolicyManager mDPM} {@code wipeData} method to wipe data on the device
         * passing the flag WIPE_EXTERNAL_STORAGE to wipe the external data also if our parameter
         * {@code boolean wipeAllData} is true.
         *
         * @param wipeAllData if true, we pass the WIPE_EXTERNAL_STORAGE flag (also erase the device's
         *                    external storage, such as SD cards) to the {@code wipeData} method of
         *                    {@code DevicePolicyManager mDPM}, if false we pass 0.
         */
        private void promptForRealDeviceWipe(final boolean wipeAllData) {
            final DeviceAdminSample activity = mActivity;

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.wipe_warning_first);
            builder.setPositiveButton(R.string.wipe_warning_first_ok, new DialogInterface.OnClickListener() {
                /**
                 * This method will be invoked when the positive button in the dialog is clicked. We initialize
                 * {@code AlertDialog.Builder builder} with a new instance. If the {@code wipeAllData}
                 * parameter of the method {@code promptForRealDeviceWipe} was true we set the message
                 * of {@code builder} to the string with resource id R.string.wipe_warning_second_full
                 * ("This is not a test. This WILL erase all of your data, including external storage!
                 * Are you really absolutely sure?"), if it was false we set the  message to the string
                 * with resource id R.string.wipe_warning_second ("This is not a test. This WILL erase
                 * all of your data! Are you really absolutely sure?"). We then set the text of the
                 * positive button to the string with resource id R.string.wipe_warning_second_ok
                 * ("BOOM!") and its {@code OnClickListener} to an anonymous class which calls the
                 * {@code wipeData} method of {@code DevicePolicyManager mDPM} with the flag WIPE_EXTERNAL_STORAGE
                 * if {@code wipeAllData} was true, or 0 if it was false. We then set the text of the
                 * negative button to the string with resource id R.string.wipe_warning_second_no ("Oops, run away!")
                 * and show the dialog of {@code builder}.
                 *
                 * @param dialog the dialog that received the click
                 * @param which the button that was clicked, BUTTON_POSITIVE in our case
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    if (wipeAllData) {
                        builder.setMessage(R.string.wipe_warning_second_full);
                    } else {
                        builder.setMessage(R.string.wipe_warning_second);
                    }
                    builder.setPositiveButton(R.string.wipe_warning_second_ok, new DialogInterface.OnClickListener() {
                        /**
                         * This method will be invoked when the positive button in the dialog is clicked.
                         * We initialize {@code boolean stillActive} with the value returned by the
                         * {@code isActiveAdmin} method of {@code DeviceAdminSample mActivity} (this
                         * helper method just returns the value returned by the {@code isAdminActive}
                         * method of {@code DevicePolicyManager mDPM} for {@code ComponentName mDeviceAdminSample}
                         * (true if the given administrator component is currently active (enabled) in the system).
                         * If {@code stillActive} is true we call the {@code wipeData} method of
                         * {@code DevicePolicyManager mDPM} with the flag WIPE_EXTERNAL_STORAGE if
                         * {@code wipeAllData} was true, or 0 if it was false.
                         *
                         * @param dialog the dialog that received the click
                         * @param which the button that was clicked, BUTTON_POSITIVE in our case
                         */
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean stillActive = mActivity.isActiveAdmin();
                            if (stillActive) {
                                mDPM.wipeData(wipeAllData
                                        ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0);
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.wipe_warning_second_no, null);
                    builder.show();
                }
            });
            builder.setNegativeButton(R.string.wipe_warning_first_no, null);
            builder.show();
        }
    }

    /**
     * PreferenceFragment for "encryption" preferences, uses the xml/device_admin_encryption.xml
     * {@code PreferenceScreen} to load its preference widgets.
     */
    public static class EncryptionFragment extends AdminSampleFragment
            implements OnPreferenceChangeListener, OnPreferenceClickListener {
        /**
         * {@code PreferenceCategory} "Encryption" with the key key_category_encryption in the
         * xml/device_admin_encryption.xml {@code PreferenceScreen}
         */
        private PreferenceCategory mEncryptionCategory;
        /**
         * {@code CheckBoxPreference} "Require encryption" with the key key_require_encryption in the
         * xml/device_admin_encryption.xml {@code PreferenceScreen}
         */
        private CheckBoxPreference mRequireEncryption;
        /**
         * {@code PreferenceScreen} "Activate encryption" with the key key_activate_encryption in the
         * xml/device_admin_encryption.xml {@code PreferenceScreen}
         */
        private PreferenceScreen mActivateEncryption;

        /**
         * Called to do initial creation of a fragment. First we call our super's implementation of
         * {@code onCreate}, then we call the {@code addPreferencesFromResource} method to inflate
         * the XML resource R.xml.device_admin_encryption and add its preference hierarchy to the
         * current preference hierarchy. We initialize {@code PreferenceCategory mEncryptionCategory}
         * by finding the preference with key KEY_CATEGORY_ENCRYPTION ("key_category_encryption"),
         * initialize {@code CheckBoxPreference mRequireEncryption} by finding the preference with key
         * KEY_REQUIRE_ENCRYPTION ("key_require_encryption"), and initialize {@code PreferenceScreen mActivateEncryption}
         * by finding the preference with key KEY_ACTIVATE_ENCRYPTION ("key_activate_encryption"). We
         * then set the {@code OnPreferenceChangeListener} of {@code mRequireEncryption} to this, and
         * the {@code OnPreferenceClickListener} of {@code mActivateEncryption} to this.
         *
         * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.device_admin_encryption);

            mEncryptionCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_ENCRYPTION);
            mRequireEncryption = (CheckBoxPreference) findPreference(KEY_REQUIRE_ENCRYPTION);
            mActivateEncryption = (PreferenceScreen) findPreference(KEY_ACTIVATE_ENCRYPTION);

            mRequireEncryption.setOnPreferenceChangeListener(this);
            mActivateEncryption.setOnPreferenceClickListener(this);
        }

        /**
         * Called when the fragment is visible to the user and actively running. First we call our
         * super's implementation of {@code onResume}, then we enable {@code PreferenceCategory mEncryptionCategory}
         * if {@code mAdminActive} is true or disable it if it is false. Finally we set the checked state of
         * {@code CheckBoxPreference mRequireEncryption} to the value returned by the {@code getStorageEncryption}
         * method of {@code DevicePolicyManager mDPM} (true if the admin(s) are requesting encryption, false if not).
         */
        @Override
        public void onResume() {
            super.onResume();
            mEncryptionCategory.setEnabled(mAdminActive);
            mRequireEncryption.setChecked(mDPM.getStorageEncryption(mDeviceAdminSample));
        }

        /**
         * Update the summaries of each item to show the local setting and the global setting. First
         * we call our super's implementation of {@code reloadSummaries}. Then we declare {@code local}
         * and {@code global} to be boolean, set local to the value returned by the {@code getStorageEncryption}
         * method of {@code DevicePolicyManager mDPM} and {@code global} to the global value it returns
         * (true if the admin(s) are requesting encryption, false if not). We then set the summary of
         * {@code CheckBoxPreference mRequireEncryption} to the string that our method {@code localGlobalSummary}
         * constructs from {@code local} and {@code global}. We then initialize {@code int deviceStatusCode}
         * with the value that the {@code getStorageEncryptionStatus} method of {@code DevicePolicyManager mDPM}
         * returns (current status of encryption. The value will be one of ENCRYPTION_STATUS_UNSUPPORTED
         * (indicates that encryption is not supported), ENCRYPTION_STATUS_INACTIVE (encryption is supported,
         * but is not currently active), ENCRYPTION_STATUS_ACTIVATING (encryption is not currently active,
         * but is currently being activated), ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY (encryption is active,
         * but an encryption key has not been set by the user), ENCRYPTION_STATUS_ACTIVE (encryption is active),
         * or ENCRYPTION_STATUS_ACTIVE_PER_USER (encryption is active and the encryption key is tied to the
         * user or profile). We initialize {@code String deviceStatus} with the string that our method
         * {@code statusCodeToString} retrieves to explain {@code deviceStatusCode}, then initialize
         * {@code String status} with the string formatted using the string with resource id
         * R.string.status_device_encryption ("Device encryption status=%1$s") from {@code deviceStatus}.
         * Finally we set the summary of {@code PreferenceScreen mActivateEncryption} to {@code status}.
         */
        @Override
        protected void reloadSummaries() {
            super.reloadSummaries();

            boolean local, global;
            local = mDPM.getStorageEncryption(mDeviceAdminSample);
            global = mDPM.getStorageEncryption(null);
            mRequireEncryption.setSummary(localGlobalSummary(local, global));

            int deviceStatusCode = mDPM.getStorageEncryptionStatus();
            String deviceStatus = statusCodeToString(deviceStatusCode);
            String status = mActivity.getString(R.string.status_device_encryption, deviceStatus);
            mActivateEncryption.setSummary(status);
        }

        /**
         * Called when a Preference has been changed by the user. If our super's implementation of
         * {@code onPreferenceChange} returns true, we return true having done nothing. If our parameter
         * {@code Preference preference} is equal to {@code CheckBoxPreference mRequireEncryption} we
         * initialize {@code boolean newActive} by casting our parameter {@code Object newValue} to
         * Boolean. We then call the {@code setStorageEncryption} method of {@code DevicePolicyManager mDPM}
         * to request encryption if {@code newActive} is true or to release any previous request if it
         * is false. We then call our method {@code postReloadSummaries} to post a call to {@code reloadSummaries}
         * on the UI queue so that it won't run until after the preference change has been applied
         * upon exiting this method. Finally we return true to update the state of the Preference with
         * the new value. If our parameter {@code Preference preference} is not equal to {@code mRequireEncryption}
         * we also return true.
         *
         * @param preference The changed Preference.
         * @param newValue The new value of the Preference.
         * @return True to update the state of the Preference with the new value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (super.onPreferenceChange(preference, newValue)) {
                return true;
            }
            if (preference == mRequireEncryption) {
                boolean newActive = (Boolean) newValue;
                mDPM.setStorageEncryption(mDeviceAdminSample, newActive);
                // Delay update because the change is only applied after exiting this method.
                postReloadSummaries();
                return true;
            }
            return true;
        }

        /**
         * Called when a Preference has been clicked. If our super's implementation of {@code onPreferenceClick}
         * returns true, we return true having done nothing. If our parameter {@code Preference preference} is
         * {@code PreferenceScreen mActivateEncryption} we check whether we are being called by an automated
         * test by calling our method {@code alertIfMonkey}, which returns true if we are being run by an
         * automated test in which case we return true having done nothing ({@code alertIfMonkey} will have
         * displayed an alert dialog with the string with resource id R.string.monkey_encryption ("You can't
         * start encryption, you are a monkey!"). If the {@code getStorageEncryptionStatus} method of
         * {@code DevicePolicyManager mDPM} returns ENCRYPTION_STATUS_UNSUPPORTED (encryption is not supported)
         * we initialize {@code AlertDialog.Builder builder} with a new instance, set its message to the
         * string with resource id R.string.encryption_not_supported ("Encryption is not supported on this device."),
         * set the text of its positive button to the string with resource id R.string.encryption_not_supported_ok
         * ("OK"), show the dialog built from {@code builder} and return true to the caller. Otherwise
         * we initialize {@code Intent intent} with an instance whose action is ACTION_START_ENCRYPTION
         * (begin the process of encrypting data on the device). We then launch the activity of {@code intent}
         * for a result and return true to the caller.
         *
         * @param preference The Preference that was clicked.
         * @return True if the click was handled.
         */
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (super.onPreferenceClick(preference)) {
                return true;
            }
            if (preference == mActivateEncryption) {
                if (alertIfMonkey(mActivity, R.string.monkey_encryption)) {
                    return true;
                }
                // Check to see if encryption is even supported on this device (it's optional).
                if (mDPM.getStorageEncryptionStatus() ==
                        DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(R.string.encryption_not_supported);
                    builder.setPositiveButton(R.string.encryption_not_supported_ok, null);
                    builder.show();
                    return true;
                }
                // Launch the activity to activate encryption.  May or may not return!
                Intent intent = new Intent(DevicePolicyManager.ACTION_START_ENCRYPTION);
                startActivityForResult(intent, REQUEST_CODE_START_ENCRYPTION);
                return true;
            }
            return false;
        }

        /**
         * Translates a {@code DevicePolicyManager} encryption status code to a descriptive string.
         * We initialize {@code int newStatus} to the resource id R.string.encryption_status_unknown
         * (the string "unknown") then switch on our parameter {@code int newStatusCode}:
         * <ul>
         *     <li>
         *         ENCRYPTION_STATUS_UNSUPPORTED we set {@code newStatus} to the resource id
         *         R.string.encryption_status_unsupported (the string: "unsupported") and break.
         *     </li>
         *     <li>
         *         ENCRYPTION_STATUS_INACTIVE we set {@code newStatus} to the resource id
         *         R.string.encryption_status_inactive (the string: "inactive") and break.
         *     </li>
         *     <li>
         *         ENCRYPTION_STATUS_ACTIVATING we set {@code newStatus} to the resource id
         *         R.string.encryption_status_activating (the string: "activating") and break.
         *     </li>
         *     <li>
         *         ENCRYPTION_STATUS_ACTIVE we set {@code newStatus} to the resource id
         *         R.string.encryption_status_active (the string: "active") and break.
         *     </li>
         * </ul>
         * Finally we return the string that the {@code getString} method of {@code DeviceAdminSample mActivity}
         * returns for the resource id in {@code newStatus}.
         *
         * @param newStatusCode {@code DevicePolicyManager} encryption status code
         * @return a string describing the meaning of {@code newStatusCode}
         */
        private String statusCodeToString(int newStatusCode) {
            int newStatus = R.string.encryption_status_unknown;
            switch (newStatusCode) {
                case DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED:
                    newStatus = R.string.encryption_status_unsupported;
                    break;
                case DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE:
                    newStatus = R.string.encryption_status_inactive;
                    break;
                case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING:
                    newStatus = R.string.encryption_status_activating;
                    break;
                case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE:
                    newStatus = R.string.encryption_status_active;
                    break;
            }
            return mActivity.getString(newStatus);
        }
    }

    /**
     * Simple converter used for long expiration times reported in mSec. We initialize {@code long days}
     * by dividing our parameter {@code time} by the number of milliseconds in a day, {@code long hours}
     * by dividing our parameter {@code time} by the number of milliseconds in an hour then applying
     * modulo 24 to that value, {@code long minutes} by dividing our parameter {@code time} by the
     * number of milliseconds in an minute then applying modulo 60 to that value, and {@code long minutes}
     * by dividing our parameter {@code time} by the number of milliseconds in a minute then applying
     * modulo 60 to that value. Finally we return the string formatted from {@code days}, {@code hours},
     * and {@code minutes} using the format string with resource id R.string.status_days_hours_minutes
     * ("%1$dd %2$dh %3$dm").
     *
     * @param context {@code Context} to use to access resources.
     * @param time time in milliseconds.
     * @return string representation of our parameter {@code time}
     */
    private static String timeToDaysMinutesSeconds(Context context, long time) {
        long days = time / MS_PER_DAY;
        long hours = (time / MS_PER_HOUR) % 24;
        long minutes = (time / MS_PER_MINUTE) % 60;
        return context.getString(R.string.status_days_hours_minutes, days, hours, minutes);
    }

    /**
     * If the "user" is a monkey, post an alert and notify the caller. This prevents automated test
     * frameworks from stumbling into annoying or dangerous operations. If the {@code isUserAMonkey}
     * method of {@code ActivityManager} returns true (the user interface is currently being messed
     * with by a monkey) we initialize {@code AlertDialog.Builder builder}, set its message to the
     * string with the resource id of our parameter {@code int stringId}, set the text of its positive
     * button to the string with resource id R.string.monkey_ok ("I admit defeat"), show the alert
     * dialog built from {@code builder} and return true to the caller. Otherwise we return false to
     * the caller.
     *
     * @param context {@code Context} to use to access resources.
     * @param stringId resource id of the message to use in the alert dialog.
     * @return Returns "true" if the user interface is currently being messed with by a monkey.
     */
    private static boolean alertIfMonkey(Context context, int stringId) {
        if (ActivityManager.isUserAMonkey()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(stringId);
            builder.setPositiveButton(R.string.monkey_ok, null);
            builder.show();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sample implementation of a DeviceAdminReceiver.  Your controller must provide one,
     * although you may or may not implement all of the methods shown here.
     * <p>
     * All callbacks are on the UI thread and your implementations should not engage in any
     * blocking operations, including disk I/O.
     */
    public static class DeviceAdminSampleReceiver extends DeviceAdminReceiver {
        /**
         * Convenience method to toast a {@code DeviceAdminSampleReceiver} status message. We initialize
         * {@code String status} by using the format string with resource id R.string.admin_receiver_status
         * ("Sample Device Admin: %1$s") to format our parameter {@code String msg}, then make and show
         * a toast of {@code status}.
         *
         * @param context {@code Context} to use to access resources.
         * @param msg string to format into a "Sample Device Admin: %1$s" toast message
         */
        void showToast(Context context, String msg) {
            String status = context.getString(R.string.admin_receiver_status, msg);
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
        }

        /**
         * Intercept standard device administrator broadcasts. This method is called when this
         * BroadcastReceiver is receiving an Intent broadcast. If the action of our parameter
         * {@code Intent intent} is ACTION_DEVICE_ADMIN_DISABLE_REQUESTED (Action sent to a device
         * administrator when the user has requested to disable it, but before this has actually been
         * done) we call the {@code abortBroadcast} method to set the flag indicating that this receiver
         * should abort the current broadcast (This will prevent any other broadcast receivers from
         * receiving the broadcast). Finally we call our super's implementation of {@code onReceive}.
         *
         * @param context The Context in which the receiver is running.
         * @param intent The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            //noinspection ConstantConditions
            if (intent.getAction().equals(ACTION_DEVICE_ADMIN_DISABLE_REQUESTED)) {
                abortBroadcast();
            }
            super.onReceive(context, intent);
        }

        /**
         * Called after the administrator is first enabled, as a result of receiving an intent with
         * the action {@link #ACTION_DEVICE_ADMIN_ENABLED} (This is the primary action that a device
         * administrator must implement to be allowed to manage a device. This will be sent to the
         * receiver when the user enables it for administration). We call our method {@code showToast}
         * to toast the string with resource id R.string.admin_receiver_status_enabled ("enabled").
         *
         * @param context The running context as per {@link #onReceive}.
         * @param intent The received intent as per {@link #onReceive}.
         */
        @Override
        public void onEnabled(Context context, Intent intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_enabled));
        }

        /**
         * Called when the user has asked to disable the administrator, as a result of receiving
         * {@link #ACTION_DEVICE_ADMIN_DISABLE_REQUESTED} (Action sent to a device administrator when
         * the user has requested to disable it, but before this has actually been done), giving you
         * a chance to present a warning message to them. The message is returned as the result; if
         * null is returned (the default implementation), no message will be displayed. We return the
         * string with resource id R.string.admin_receiver_status_disable_warning ("This is an optional
         * message to warn the user about disabling").
         *
         * @param context The running context as per {@link #onReceive}.
         * @param intent The received intent as per {@link #onReceive}.
         * @return Return the warning message to display to the user before being disabled; if null
         * is returned, no message is displayed.
         */
        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return context.getString(R.string.admin_receiver_status_disable_warning);
        }

        /**
         * Called prior to the administrator being disabled, as a result of receiving
         * {@link #ACTION_DEVICE_ADMIN_DISABLED}. Upon return, you can no longer use the
         * protected parts of the {@link DevicePolicyManager} API. We call our method
         * {@code showToast} with the string resource id R.string.admin_receiver_status_disabled
         * ("disabled").
         *
         * @param context The running context as per {@link #onReceive}.
         * @param intent The received intent as per {@link #onReceive}.
         */
        @Override
        public void onDisabled(Context context, Intent intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_disabled));
        }

        /**
         * Called after the user has changed their device or profile challenge password, as a result of
         * receiving {@link #ACTION_PASSWORD_CHANGED}. At this point you can use
         * {@link DevicePolicyManager#getPasswordQuality(android.content.ComponentName)}
         * to retrieve the active password characteristics. We call our method {@code showToast}
         * with the string resource id R.string.admin_receiver_status_pw_changed ("pw changed").
         *
         * @param context The running context as per {@link #onReceive}.
         * @param intent The received intent as per {@link #onReceive}.
         * @deprecated From {@code android.os.Build.VERSION_CODES.O}, use
         * {@code onPasswordChanged(Context, Intent, UserHandle)} instead.
         */
        @Override
        public void onPasswordChanged(Context context, Intent intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_changed));
        }

        /**
         * Called after the user has failed at entering their device or profile challenge password,
         * as a result of receiving {@link #ACTION_PASSWORD_FAILED}.  At this point you can use
         * {@link DevicePolicyManager#getCurrentFailedPasswordAttempts()} to retrieve the number of
         * failed password attempts. We call our method {@code showToast} with the string resource
         * id R.string.admin_receiver_status_pw_failed ("pw failed")
         *
         * @param context The running context as per {@link #onReceive}.
         * @param intent The received intent as per {@link #onReceive}.
         * @deprecated From {@link android.os.Build.VERSION_CODES#O}, use
         * {@code onPasswordFailed(Context, Intent, UserHandle)} instead.
         */
        @Override
        public void onPasswordFailed(Context context, Intent intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_failed));
        }

        /**
         * Called after the user has succeeded at entering their device or profile challenge password,
         * as a result of receiving {@link #ACTION_PASSWORD_SUCCEEDED}.  This will only be received
         * the first time they succeed after having previously failed. We call our method {@code showToast}
         * with the string resource id R.string.admin_receiver_status_pw_succeeded ("pw succeeded").
         *
         * @param context The running context as per {@link #onReceive}.
         * @param intent The received intent as per {@link #onReceive}.
         * @deprecated From {@link android.os.Build.VERSION_CODES#O}, use
         * {@code onPasswordSucceeded(Context, Intent, UserHandle)} instead.
         */
        @Override
        public void onPasswordSucceeded(Context context, Intent intent) {
            showToast(context, context.getString(R.string.admin_receiver_status_pw_succeeded));
        }

        /**
         * Called periodically when the device or profile challenge password is about to expire
         * or has expired.  It will typically be called at these times: on device boot, once per day
         * before the password expires, and at the time when the password expires.
         * <p>
         * If the password is not updated by the user, this method will continue to be called
         * once per day until the password is changed or the device admin disables password expiration.
         * <p>
         * The admin will typically post a notification requesting the user to change their password
         * in response to this call. The actual password expiration time can be obtained by calling
         * {@link DevicePolicyManager#getPasswordExpiration(ComponentName)}
         * <p>
         * The admin should be sure to take down any notifications it posted in response to this call
         * when it receives {@link DeviceAdminReceiver#onPasswordChanged(Context, Intent) }.
         * <p>
         * We initialize {@code DevicePolicyManager dpm} with a handle to the DEVICE_POLICY_SERVICE
         * system level service, then initialize {@code long expr} with the value returned by the
         * {@code getPasswordExpiration} method of {@code dpm} for the component name whose class
         * is {@code DeviceAdminSampleReceiver} ({@code getPasswordExpiration} returns the password
         * expiration time, in milliseconds since epoch). We then initialize {@code long delta} by
         * subtracting the current time in milliseconds from {@code expr}. We initialize {@code expired}
         * to true if {@code delta} is less than 0, and if {@code expired} is true we initialize
         * {@code String message} to the string with resource id R.string.expiration_status_past
         * ("Password expired %1$s ago"), if false to the string with resource id R.string.expiration_status_future
         * ("Password will expire %1$s from now"). We then call our method {@code showToast} with
         * {@code message} and log {@code message} as well.
         *
         * @param context The running context as per {@link #onReceive}.
         * @param intent The received intent as per {@link #onReceive}.
         * @deprecated From {@link android.os.Build.VERSION_CODES#O}, use
         * {@code onPasswordExpiring(Context, Intent, UserHandle)} instead.
         */
        @Override
        public void onPasswordExpiring(Context context, Intent intent) {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            //noinspection ConstantConditions
            long expr = dpm.getPasswordExpiration(new ComponentName(context, DeviceAdminSampleReceiver.class));
            long delta = expr - System.currentTimeMillis();
            boolean expired = delta < 0L;
            String message = context.getString(expired ?
                    R.string.expiration_status_past : R.string.expiration_status_future);
            showToast(context, message);
            Log.v(TAG, message);
        }
    }

    /**
     * UNUSED.
     */
    public static class DeviceAdminSampleReceiver2 extends DeviceAdminReceiver {
    }
}
