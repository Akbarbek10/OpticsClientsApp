package com.example.opticsclientsapp.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity.Companion.MY_DATE_FORMAT
import com.example.opticsclientsapp.databinding.ItemClientBinding
import com.example.opticsclientsapp.models.Client
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class RecyclerViewClientList(private val listener: OnItemClickClientList) :
    RecyclerView.Adapter<RecyclerViewClientList.ViewHolder>() {

    private val itemCallback = object : DiffUtil.ItemCallback<Client>() {
        override fun areItemsTheSame(oldItem: Client, newItem: Client): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Client, newItem: Client): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, itemCallback)

    inner class ViewHolder(private val binding: ItemClientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val currentDateInMillis = SimpleDateFormat("dd-MM-yyyy").parse(
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        ).time
        private val currentTimeInMillis = System.currentTimeMillis()

        fun onBind(client: Client, position: Int) {
            setData(client)
            checkBirthday(client.dateOfBirth)
            setVisibility(client.isExpanded)


            binding.cvItemClient.setOnClickListener {
                listener.onItemClick(client, position, binding.clSecond)
            }

            binding.btnDelete.setOnClickListener {
                listener.deleteBtnClick(client, position)
            }

            binding.btnEdit.setOnClickListener {
                listener.editBtnClick(client)
            }

            binding.ivProfileImg.setOnClickListener {
                listener.openProfileImg(client.imgProfile)
            }

            binding.btnCall.setOnClickListener {
                listener.callBtnClick(client)
            }

        }



        private fun setData(client: Client) {
            val context = binding.root.context
            val dateOfPurchase = SimpleDateFormat(MY_DATE_FORMAT).parse(client.dateOfPurchase)
            binding.tvName.text = client.fullname
            binding.tvProductName.text = client.productName
            binding.tvPhoneNum.text = client.phoneNum

            binding.tvDate.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(
                dateOfPurchase
            )


            if(client.imgProfile.isNotEmpty() and File(client.imgProfile).exists()) {
                val imgUri = Uri.parse(client.imgProfile)
                binding.ivProfileImg.setImageURI(imgUri)
            }else{
                when (client.gender) {
                    0 -> binding.ivProfileImg.setImageResource(R.drawable.ic_man)
                    else -> binding.ivProfileImg.setImageResource(R.drawable.ic_woman)
                }
            }

            val dateInMillis =
                dateOfPurchase.time + TimeUnit.DAYS.toMillis(client.wearingTime!!.toLong()) - currentDateInMillis

            var daysLeft = TimeUnit.MILLISECONDS.toDays(dateInMillis)
            if (daysLeft <= 0) daysLeft = 0

            val notifyAt = dateOfPurchase.time + TimeUnit.DAYS.toMillis(client.wearingTime!!.toLong()) + TimeUnit.HOURS.toMillis(
                12
            )
            if (currentTimeInMillis >= notifyAt){
                binding.icDaysLeftTxt.visibility = View.GONE
                binding.tvDaysLeftTxt.visibility = View.GONE
                binding.tvDaysLeft.text = context.getString(R.string.expired)
                binding.itemBg.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.notify_item_card_color
                    )
                )
            } else{
                binding.icDaysLeftTxt.visibility = View.VISIBLE
                binding.tvDaysLeftTxt.visibility = View.VISIBLE
                binding.tvDaysLeft.text = " $daysLeft"
                binding.itemBg.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.item_card_color
                    )
                )
            }

            binding.tvDioptre.text = client.dioptre
            binding.tvWearingTime.text = "${client.wearingTime} "+context.getString(R.string.days_txt)
            binding.tvNotes.text =
                if (client.notes.isEmpty()) context.getString(R.string.empty_txt)
                else client.notes
        }

        private fun setVisibility(isExpanded: Boolean) {
                binding.clSecond.visibility =
                    if (isExpanded) View.VISIBLE
                    else View.GONE
        }

        private fun checkBirthday(dateOfBirth: String) {
            if(dateOfBirth.isNotEmpty()){
                val today = SimpleDateFormat("dd.MM").format(Date(currentTimeInMillis))
                binding.ivBirthday.visibility = if (today.equals(dateOfBirth.substring(0,5))) View.VISIBLE
                else View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(differ.currentList[position], position)
    }

    override fun getItemCount(): Int = differ.currentList.size
}

interface OnItemClickClientList {
    fun deleteBtnClick(client: Client, position: Int)

    fun onItemClick(client: Client, position: Int, view:View)

    fun editBtnClick(client: Client)

    fun openProfileImg(imgUri: String)

    fun callBtnClick(client: Client)
}




