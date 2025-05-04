package com.midterm22nh12.appbangiayonline.Adapter.User

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.Utils.NotificationType
import com.midterm22nh12.appbangiayonline.databinding.ItemNotificationUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewNotificationUser

class MyAdapterRecyclerViewNotificationUser(private val itemList: List<ItemRecyclerViewNotificationUser>) :
    RecyclerView.Adapter<MyAdapterRecyclerViewNotificationUser.MyViewHolder>() {
    private lateinit var bindingItemRecyclerViewNotificationUse: ItemNotificationUserBinding
    class MyViewHolder(val binding: ItemNotificationUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        bindingItemRecyclerViewNotificationUse = ItemNotificationUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(bindingItemRecyclerViewNotificationUse)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        bindingItemRecyclerViewNotificationUse.tvTitleNotificationUser.text = itemList[position].title
        bindingItemRecyclerViewNotificationUse.tvContentNotificationUser.text = itemList[position].content
        bindingItemRecyclerViewNotificationUse.ivIconNotificationUser.setImageResource(NotificationType.getByTitle(itemList[position].title).iconResId)
    }

    override fun getItemCount(): Int = itemList.size
}