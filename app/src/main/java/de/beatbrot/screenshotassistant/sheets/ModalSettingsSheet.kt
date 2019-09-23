package de.beatbrot.screenshotassistant.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.beatbrot.screenshotassistant.R
import de.beatbrot.screenshotassistant.databinding.TitledModalSheetBinding

class ModalSettingsSheet : BottomSheetDialogFragment() {
    private lateinit var v: TitledModalSheetBinding
    private val settingsSheet = SettingsSheet()

    override fun onCreateView(i: LayoutInflater, root: ViewGroup?, state: Bundle?): View {
        v = TitledModalSheetBinding.inflate(i)
        return v.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v.title.text = getString(R.string.title_sheet_settings)

        childFragmentManager.beginTransaction()
            .replace(R.id.fragContainer, settingsSheet)
            .commitNow()
    }

    class SettingsSheet : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}
