package de.beatbrot.screenshotassistant.sheets

interface IBottomSheet {
    val title: String?
        get() = null

    val isHideable: Boolean
}