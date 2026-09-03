package com.alzimerahmed.oasisbrowser.browser.tab

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.browser.tab.view.BackgroundDrawable
import com.alzimerahmed.oasisbrowser.extensions.desaturate
import com.alzimerahmed.oasisbrowser.extensions.inflater
import com.alzimerahmed.oasisbrowser.utils.ThemeUtils
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

/**
 * The adapter that renders tabs in the drawer list form.
 *
 * @param onClick Invoked when the tab is clicked.
 * @param onLongClick Invoked when the tab is long pressed.
 * @param onCloseClick Invoked when the tab's close button is clicked.
 */
class DrawerTabRecyclerViewAdapter(
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit,
    private val onCloseClick: (Int) -> Unit,
) : ListAdapter<TabListItem, TabViewHolder>(
    object : DiffUtil.ItemCallback<TabListItem>() {
        override fun areItemsTheSame(oldItem: TabListItem, newItem: TabListItem): Boolean =
            oldItem is TabListItem.TabItem &&
                newItem is TabListItem.TabItem &&
                oldItem.tab.id == newItem.tab.id

        override fun areContentsTheSame(oldItem: TabListItem, newItem: TabListItem): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TabViewHolder {
        val view = viewGroup.context.inflater.inflate(R.layout.tab_list_item, viewGroup, false)
        view.background = BackgroundDrawable(view.context)
        return TabViewHolder(
            view,
            onClick = onClick,
            onLongClick = onLongClick,
            onCloseClick = onCloseClick
        )
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.exitButton.tag = position

        val tab = (getItem(position) as? TabListItem.TabItem)?.tab ?: return

        holder.txtTitle.text = tab.title
        updateViewHolderAppearance(holder, tab.isSelected)
        updateViewHolderFavicon(holder, tab.icon, tab.isSelected)
        updateViewHolderBackground(holder, tab.isSelected)
    }

    private fun updateViewHolderFavicon(
        viewHolder: TabViewHolder,
        favicon: Bitmap?,
        isForeground: Boolean
    ) {
        favicon?.let {
            if (isForeground) {
                viewHolder.favicon.setImageBitmap(it)
            } else {
                viewHolder.favicon.setImageBitmap(it.desaturate())
            }
        } ?: viewHolder.favicon.setImageResource(R.drawable.ic_webpage)
    }

    private fun updateViewHolderBackground(viewHolder: TabViewHolder, isForeground: Boolean) {
        val verticalBackground = viewHolder.layout.background as BackgroundDrawable
        verticalBackground.isCrossFadeEnabled = false
        if (isForeground) {
            verticalBackground.startTransition(200)
        } else {
            verticalBackground.reverseTransition(200)
        }
    }

    private fun updateViewHolderAppearance(
        viewHolder: TabViewHolder,
        isForeground: Boolean
    ) {
        if (isForeground) {
            TextViewCompat.setTextAppearance(
                viewHolder.txtTitle,
                R.style.TextAppearance_OasisBrowser_TabTitle_Selected
            )
            viewHolder.txtTitle.setTextColor(
                ThemeUtils.getColor(viewHolder.itemView.context, R.attr.colorOnSurface)
            )
        } else {
            TextViewCompat.setTextAppearance(
                viewHolder.txtTitle,
                R.style.TextAppearance_OasisBrowser_TabTitle
            )
            viewHolder.txtTitle.setTextColor(
                ThemeUtils.getColor(viewHolder.itemView.context, R.attr.colorOnSurface)
            )
        }
    }
}
