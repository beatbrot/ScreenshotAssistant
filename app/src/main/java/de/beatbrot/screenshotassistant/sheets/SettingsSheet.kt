package de.beatbrot.screenshotassistant.sheets

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import de.beatbrot.screenshotassistant.R

class SettingsSheet(borrowedContext: Context) : PreferenceFragmentCompat(), IBottomSheet {

    override val title: String = borrowedContext.getString(R.string.title_sheet_settings)

    override val isHideable = true

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}