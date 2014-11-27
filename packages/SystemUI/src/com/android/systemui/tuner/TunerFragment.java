/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.systemui.tuner;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.PreferenceFragment;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.du.Utils;
import com.android.systemui.R;

public class TunerFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "TunerFragment";

    private static final String KEY_SHOW_LTE_FOURGEE = "show_lte_fourgee";
    private static final String KEY_STATUS_BAR_LOGO = "status_bar_logo";
    private static final String BATTERY_STYLE = "battery_style";

    private SwitchPreference mShowLteFourGee;
    private SwitchPreference mShowDuLogo;
    private ListPreference mBatteryIconStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();

        final ContentResolver resolver = getActivity().getContentResolver();

        mShowLteFourGee = (SwitchPreference) findPreference(KEY_SHOW_LTE_FOURGEE);
        if (Utils.isWifiOnly(getActivity())) {
            prefSet.removePreference(mShowLteFourGee);
        } else {
            mShowLteFourGee.setChecked((Settings.System.getInt(resolver,
                    Settings.System.SHOW_LTE_FOURGEE, 0) == 1));
        }

        mShowDuLogo = (SwitchPreference) findPreference(KEY_STATUS_BAR_LOGO);
        mShowDuLogo.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_LOGO, 0) == 1));

        mBatteryIconStyle = (ListPreference) findPreference(BATTERY_STYLE);
        mBatteryIconStyle.setValue(Integer.toString(Settings.Secure.getInt(resolver,
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, 0)));
        mBatteryIconStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.tuner_prefs);
    }

    private boolean alwaysOnAvailable() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.systemui_tuner_icon_manager_title);

        MetricsLogger.visibility(getContext(), MetricsEvent.TUNER, true);
    }

    @Override
    public void onPause() {
        super.onPause();

        MetricsLogger.visibility(getContext(), MetricsEvent.TUNER, false);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if  (preference == mShowLteFourGee) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_LTE_FOURGEE, checked ? 1:0);
            return true;
        } else if  (preference == mShowDuLogo) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO, checked ? 1:0);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryIconStyle) {
            int value = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUS_BAR_BATTERY_STYLE, value);
            return true;
        }
        return false;
    }
}
