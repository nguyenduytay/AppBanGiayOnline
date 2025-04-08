package com.midterm22nh12.appbangiayonline.model

data class CartItem(
    val id: Int,
    val name: String,
    val color: String,
    val size: String,
    val imageResource: Int,
    var quantity: Int,
    var isSelected: Boolean = false

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartItem

        if (id != other.id) return false
        if (imageResource != other.imageResource) return false
        if (quantity != other.quantity) return false
        if (isSelected != other.isSelected) return false
        if (name != other.name) return false
        if (color != other.color) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + imageResource
        result = 31 * result + quantity
        result = 31 * result + isSelected.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }

    /**
     * Returns a string representation of the object.
     */
    override fun toString(): String {
        return super.toString()
    }
}