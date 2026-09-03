package com.alzimerahmed.oasisbrowser.browser.tab

import com.alzimerahmed.oasisbrowser.R
import com.alzimerahmed.oasisbrowser.browser.tab.view.BackgroundDrawable
import com.alzimerahmed.oasisbrowser.extensions.desaturate
import com.alzimerahmed.oasisbrowser.extensions.inflater
import com.alzimerahmed.oasisbrowser.utils.ThemeUtils
import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * The adapter that renders tabs in a grouped drawer list form.
 *
 * @param onClick Invoked when a tab is clicked.
 * @param onLongClick Invoked when a tab is long pressed.
 * @param onCloseClick Invoked when a tab's close button is clicked.
 * @param onGroupHeaderClick Invoked when a group header is clicked (toggles collapse).
 * @param onGroupCloseClick Invoked when a group's close button is clicked.
 */
class GroupedTabRecyclerViewAdapter(
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit,
    private val onCloseClick: (Int) -> Unit,
    private val onGroupHeaderClick: (Int) -> Unit,
    private val onGroupCloseClick: (Int) -> Unit,
) : ListAdapter<TabListItem, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<TabListItem>() {
        override fun areItemsTheSame(oldItem: TabListItem, newItem: TabListItem): Boolean =
            when {
                oldItem is TabListItem.TabItem && newItem is TabListItem.TabItem ->
                    oldItem.tab.id == newItem.tab.id
                oldItem is TabListItem.GroupHeader && newItem is TabListItem.GroupHeader ->
                    oldItem.group.id == newItem.group.id
                else -> false
            }

        override fun areContentsTheSame(oldItem: TabListItem, newItem: TabListItem): Boolean =
            oldItem == newItem
    }
) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is TabListItem.GroupHeader -> VIEW_TYPE_GROUP
            is TabListItem.TabItem -> VIEW_TYPE_TAB
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == VIEW_TYPE_GROUP) {
            val view = viewGroup.context.inflater.inflate(R.layout.tab_group_header, viewGroup, false)
            TabGroupViewHolder(
                view,
                onHeaderClick = onGroupHeaderClick,
                onCloseClick = onGroupCloseClick
            )
        } else {
            val view = viewGroup.context.inflater.inflate(R.layout.tab_list_item, viewGroup, false)
            view.background = BackgroundDrawable(view.context)
            TabViewHolder(
                view,
                onClick = onClick,
                onLongClick = onLongClick,
                onCloseClick = onCloseClick
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TabListItem.GroupHeader -> bindGroupHeader(holder as TabGroupViewHolder, item)
            is TabListItem.TabItem -> bindTab(holder as TabViewHolder, item)
        }
    }

    private fun bindGroupHeader(holder: TabGroupViewHolder, item: TabListItem.GroupHeader) {
        holder.title.text = item.group.name
        holder.tabCount.text = item.tabCount.toString()
        holder.collapseIcon.setImageResource(R.drawable.ic_action_delete)
        holder.collapseIcon.rotation = if (item.group.isCollapsed) 90f else 0f
        holder.title.setTextColor(
            ThemeUtils.getColor(holder.itemView.context, R.attr.colorOnSurface)
        )
    }

    private fun bindTab(holder: TabViewHolder, item: TabListItem.TabItem) {
        holder.exitButton.tag = holder.adapterPosition

        val tab = item.tab

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

    companion object {
        private const val VIEW_TYPE_GROUP = 0
        private const val VIEW_TYPE_TAB = 1
    }
}
