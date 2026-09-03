package com.alzimerahmed.oasisbrowser.browser.ui

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.preference.IntEnum

/** The action exposed by the configurable OasisBrowser rail utility button. */
enum class RailUtilityAction(
    override val value: Int,
    val iconRes: Int,
    val labelRes: Int
) : IntEnum {
    QR(0, R.drawable.ic_action_qr_code, R.string.action_scan_qr),
    VAULT(1, R.drawable.ic_action_vault, R.string.action_vault),
    SCREENSHOT(2, R.drawable.ic_action_screenshot, R.string.action_screenshot)
}
