package com.example.opticsclientsapp.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.opticsclientsapp.databinding.ItemLanguageBinding
import com.example.opticsclientsapp.models.Language
import com.example.opticsclientsapp.shared_preference.Pref

class RecyclerViewLanguagesAdapter(
    private val list: ArrayList<Language>,
    val listener: OnLangItemClick
) :
    RecyclerView.Adapter<RecyclerViewLanguagesAdapter.ViewHolder>() {

    private lateinit var pref: Pref

    inner class ViewHolder(private val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(lang: Language) {
            pref = Pref()
            pref.init(binding.root.context)

            binding.ivLang.setImageResource(lang.image)
            binding.tvLang.text = lang.language
            binding.isLangChecked.isChecked = pref.chosenLangId == lang.id

            binding.root.setOnClickListener {
                pref.chosenLangId = lang.id
                listener.onClick(lang)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

}

interface OnLangItemClick {
    fun onClick(lang: Language)
}
