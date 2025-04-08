package com.midterm22nh12.appbangiayonline.viewmodel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.model.CartItem

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<CartItem>,
    private val onQuantityChangeListener: OnQuantityChangeListener,
    private val onItemCheckListener: OnItemCheckListener
) : BaseAdapter() {

    interface OnQuantityChangeListener {
        fun onQuantityChanged(position: Int, newQuantity: Int)
    }

    interface OnItemCheckListener {
        fun onItemChecked(position: Int, isChecked: Boolean)
    }

    override fun getCount(): Int = cartItems.size

    override fun getItem(position: Int): Any = cartItems[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val cartItem = cartItems[position]

        // Set item data
        viewHolder.tvProductName.text = cartItem.name
        viewHolder.tvColor.text = "Màu : ${cartItem.color}"
        viewHolder.tvSize.text = "Size : ${cartItem.size}"
        viewHolder.tvQuantity.text = cartItem.quantity.toString()
        viewHolder.imgProduct.setImageResource(cartItem.imageResource)
        viewHolder.checkItem.isChecked = cartItem.isSelected

        // Set listeners
        viewHolder.checkItem.setOnClickListener {
            cartItem.isSelected = viewHolder.checkItem.isChecked
            onItemCheckListener.onItemChecked(position, cartItem.isSelected)
        }

        viewHolder.btnMinus.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--
                viewHolder.tvQuantity.text = cartItem.quantity.toString()
                onQuantityChangeListener.onQuantityChanged(position, cartItem.quantity)
            }
        }

        viewHolder.btnPlus.setOnClickListener {
            cartItem.quantity++
            viewHolder.tvQuantity.text = cartItem.quantity.toString()
            onQuantityChangeListener.onQuantityChanged(position, cartItem.quantity)
        }

        return view
    }

    fun updateItemSelection(position: Int, isSelected: Boolean) {
        if (position < cartItems.size) {
            cartItems[position].isSelected = isSelected
            notifyDataSetChanged()
        }
    }

    fun updateAllSelection(isSelected: Boolean) {
        for (item in cartItems) {
            item.isSelected = isSelected
        }
        notifyDataSetChanged()
    }

    fun getSelectedItemCount(): Int {
        return cartItems.count { it.isSelected }
    }

    fun removeSelectedItems() {
        cartItems.removeAll { it.isSelected }
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val checkItem: CheckBox = view.findViewById(R.id.checkItem)
        val imgProduct: ImageView = view.findViewById(R.id.imgProduct)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvColor: TextView = view.findViewById(R.id.tvColor)
        val tvSize: TextView = view.findViewById(R.id.tvSize)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnMinus: ImageButton = view.findViewById(R.id.btnMinus)
        val btnPlus: ImageButton = view.findViewById(R.id.btnPlus)
    }
}