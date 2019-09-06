package de.beatbrot.screenshotassistant.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.beatbrot.screenshotassistant.R
import kotlinx.android.synthetic.main.titled_modal_sheet.*

class ModalSettingsSheet : BottomSheetDialogFragment() {

    private val settingsSheet = SettingsSheet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.titled_modal_sheet, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title.text = getString(R.string.title_sheet_settings)

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