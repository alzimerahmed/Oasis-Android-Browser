package com.alzimerahmed.oasisbrowser.browser.tab

import com.alzimerahmed.oasisbrowser.R
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * The view holder for a tab group header.
 *
 * @param view The root view for the group header.
 * @param onHeaderClick Invoked when the header is clicked (toggles collapse).
 * @param onCloseClick Invoked when the header's close button is clicked.
 */
class TabGroupViewHolder(
    view: View,
    private val onHeaderClick: (Int) -> Unit,
    private val onCloseClick: (Int) -> Unit,
) : RecyclerView.ViewHolder(view), View.OnClickListener {

    val title: TextView = view.findViewById(R.id.group_title)
    val tabCount: TextView = view.findViewById(R.id.group_tab_count)
    val collapseIcon: ImageView = view.findViewById(R.id.group_collapse_icon)
    val closeButton: View = view.findViewById(R.id.group_close)

    init {
        view.setOnClickListener(this)
        closeButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v === closeButton) {
            onCloseClick.invoke(adapterPosition)
        } else {
            onHeaderClick.invoke(adapterPosition)
        }
    }
}
