package com.example.opticsclientsapp.fragments.views

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity
import com.example.opticsclientsapp.databinding.FragmentProfileImageBinding


class ProfileImageFragment : Fragment() {
    private var _binding: FragmentProfileImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var mActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileImageBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        initView()

        loadImgProfile()



        binding.toolbar.root.setNavigationOnClickListener {
            mActivity.onBackPressed()
        }


        return binding.root
    }

    private fun loadImgProfile() {
        val bundle = this.arguments
        if (bundle != null) {
            val mImgUri = bundle.getString("_img_uri", "")
            val imgUri = Uri.parse(mImgUri)
            binding.ivProfileImg.setImageURI(imgUri)
        }
    }

    private fun initView() {
        mActivity = ((activity as MainActivity?)!!)
        mActivity.setSupportActionBar(binding.toolbar.root)
        mActivity.supportActionBar?.title = ""
        mActivity.supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity.supportActionBar?.setDisplayShowHomeEnabled(true)
        mActivity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_btn_light)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item_menu_check = menu.findItem(R.id.menu_check)
        item_menu_check?.isVisible = false

        super.onPrepareOptionsMenu(menu)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}