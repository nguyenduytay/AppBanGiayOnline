package com.midterm22nh12.appbangiayonline.view.User

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankHomeUserBinding
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankShopUserBinding

class BlankFragmentShopUser : Fragment() {

    private lateinit var bindingFragmentShopUser: FragmentBlankShopUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingFragmentShopUser = FragmentBlankShopUserBinding.inflate(inflater,container,false)
        bindingFragmentShopUser.viewFlipperShopUser.displayedChild=0
        //sự kiện chuyển trang
        turnPageShoppingCartUser()

        return bindingFragmentShopUser.root
    }
    //sự kiện hiển thị khuyển mãi
    private fun turnPageShoppingCartUser()
    {
        bindingFragmentShopUser.includeShoppingCartUser.llPromotionShoppingCartUser.setOnClickListener{
            bindingFragmentShopUser.viewFlipperShopUser.displayedChild=1
        }
        bindingFragmentShopUser.includePromotionUser.ivBackPromotionUser.setOnClickListener{
            bindingFragmentShopUser.viewFlipperShopUser.displayedChild=0
        }
        bindingFragmentShopUser.includeShoppingCartUser.ivBackShoppingCartUser.setOnClickListener {
            (activity as? MainActivityUser)?.returnToPreviousOverlay()
        }
    }

}