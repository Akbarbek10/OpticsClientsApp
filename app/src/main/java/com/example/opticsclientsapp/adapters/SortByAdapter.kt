package com.example.opticsclientsapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.databinding.ItemCustomSpinnerBinding
import com.example.opticsclientsapp.shared_preference.Pref


class SortByAdapter(context: Context, sortList: List<String>) :
    ArrayAdapter<String>(context, 0, sortList) {
    private lateinit var pref: Pref
    private lateinit var binding: ItemCustomSpinnerBinding

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        pref = Pref()
        pref.init(context)

        var row = convertView
        binding = if (row == null) {
            ItemCustomSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        } else {
            ItemCustomSpinnerBinding.bind(row)
        }

        onBindView()

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        binding = if (row == null) {
            ItemCustomSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        } else {
            ItemCustomSpinnerBinding.bind(row)
        }

        onBindDropDownView(getItem(position)!!, position)
        return binding.root
    }

    private fun onBindView() {
        binding.tvSpinner.text = ""
        binding.spinnerBg.setBackgroundColor(Color.TRANSPARENT)
    }


    private fun onBindDropDownView(sortText: String, position: Int) {
        if (pref.sortPosition == position){
            binding.tvSpinner.setBackgroundResource(R.drawable.frame_spinner_chosen_item_style)
            binding.tvSpinner.setTextColor(binding.root.context.resources.getColor(R.color.spinner_checked_text_color))
        }else{
            binding.tvSpinner.setBackgroundColor(Color.TRANSPARENT)
            binding.tvSpinner.setTextColor(binding.root.context.resources.getColor(R.color.spinner_text_color))
        }

        binding.tvSpinner.text = sortText

    }

}

