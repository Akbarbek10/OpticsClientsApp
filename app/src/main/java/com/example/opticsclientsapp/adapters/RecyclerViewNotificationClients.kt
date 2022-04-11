package com.example.opticsclientsapp.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity.Companion.MY_DATE_FORMAT
import com.example.opticsclientsapp.databinding.ItemClientNotificationBinding
import com.example.opticsclientsapp.models.Client
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat


class RecyclerViewNotificationClients(private val listener: OnItemClickNotificationClient) :
    RecyclerView.Adapter<RecyclerViewNotificationClients.ViewHolder>() {

    private val itemCallback = object : DiffUtil.ItemCallback<Client>() {
        override fun areItemsTheSame(oldItem: Client, newItem: Client): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Client, newItem: Client): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, itemCallback)

    inner class ViewHolder(private val binding: ItemClientNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(client: Client, position: Int) {
            setData(client)
            setVisibility(client.isExpanded)
            isNotificationSeen(client.isNotificationSeen)

            binding.cvItemClient.setOnClickListener {
                listener.onItemClick(client, binding.clSecond, position)
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
            binding.tvDate.text =
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(dateOfPurchase)
            binding.tvDioptre.text = client.dioptre
            binding.tvWearingTime.text =
                "${client.wearingTime} " + context.getString(R.string.days_txt)
            binding.tvNotes.text =
                if (client.notes.isEmpty()) context.getString(R.string.empty_txt)
                else client.notes

            if (client.imgProfile.isNotEmpty() and File(client.imgProfile).exists()) {
                val imgUri = Uri.parse(client.imgProfile)
                binding.ivProfileImg.setImageURI(imgUri)
            } else {
                when (client.gender) {
                    0 -> binding.ivProfileImg.setImageResource(R.drawable.ic_man)
                    else -> binding.ivProfileImg.setImageResource(R.drawable.ic_woman)
                }
            }
        }

        private fun isNotificationSeen(isSeen: Boolean) {
            val fValue = if (isSeen) 0.6f else 1f

            binding.tvName.alpha = fValue
            binding.tvPhoneNum.alpha = fValue
            binding.ivProfileImg.alpha = fValue
            binding.btnCall.alpha = fValue

        }

        private fun setVisibility(isExpanded: Boolean) {
            binding.clSecond.visibility =
                if (isExpanded) View.VISIBLE
                else View.GONE
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemClientNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(differ.currentList[position], position)
    }

    override fun getItemCount(): Int = differ.currentList.size
}

interface OnItemClickNotificationClient {
    fun onItemClick(client: Client, view: View, position: Int)

    fun openProfileImg(imgUri: String)

    fun callBtnClick(client: Client)
}




