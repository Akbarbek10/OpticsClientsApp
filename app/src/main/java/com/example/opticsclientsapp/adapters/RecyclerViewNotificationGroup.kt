package com.example.opticsclientsapp.adapters

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity
import com.example.opticsclientsapp.databinding.ItemDateBinding
import com.example.opticsclientsapp.db.DbHelper
import com.example.opticsclientsapp.fragments.main.AllClientsFragment
import com.example.opticsclientsapp.fragments.views.ProfileImageFragment
import com.example.opticsclientsapp.object_class.MyDropAnimation
import com.example.opticsclientsapp.models.Client
import com.example.opticsclientsapp.models.MyDate


class RecyclerViewNotificationGroup(
    private val context: Context,
    private val listener: OnItemClickNotification
) :
    RecyclerView.Adapter<RecyclerViewNotificationGroup.ViewHolder>() {

    private val itemCallback = object : DiffUtil.ItemCallback<MyDate>() {
        override fun areItemsTheSame(oldItem: MyDate, newItem: MyDate): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: MyDate, newItem: MyDate): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, itemCallback)

    inner class ViewHolder(private val binding: ItemDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val dbHelper = DbHelper(binding.root.context)
        private lateinit var adapter: RecyclerViewNotificationClients

        fun onBind(myDate: MyDate, mPosition: Int) {
            val clientList = myDate.clientList
            binding.tvDate.text = myDate.date
            (binding.rvClientList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

            adapter = RecyclerViewNotificationClients(object : OnItemClickNotificationClient {
                override fun onItemClick(client: Client, view: View, position: Int) {
                    val delay = if (!client.isExpanded) MyDropAnimation.expand(view)
                                else MyDropAnimation.collapse(view)


                    if (!client.isNotificationSeen) {
                        dbHelper.makeNotificationChecked(client.id)
                        client.isNotificationSeen = true

                        val handler = Handler()
                        handler.postDelayed({
//                            listener.onNotificationItemClick(mPosition)
                            adapter.notifyItemChanged(position)
                        }, delay+200)
                    }


                    client.isExpanded = !client.isExpanded
                }

                override fun openProfileImg(imgUri: String) {
                    if (imgUri.isNotEmpty()) {
                        openProfileImgFragment(imgUri)
                    }

                }

                override fun callBtnClick(client: Client) {
                    makePhoneCall(client.phoneNum)
                }
            })

            adapter.differ.submitList(clientList)
            adapter.notifyDataSetChanged()
            binding.rvClientList.adapter = adapter
        }

    }

    private fun makePhoneCall(number: String) {
        val phoneNum = number.replace(Regex("""[${' '}-]"""), "")
        if (ContextCompat.checkSelfPermission(context as MainActivity, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, arrayOf(android.Manifest.permission.CALL_PHONE), AllClientsFragment.REQUEST_CALL)
        }else {
            val dial = "tel:$phoneNum"
            context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
        }
    }


    private fun openProfileImgFragment(imgUri: String) {
        val bundle = Bundle()
        bundle.putString("_img_uri", imgUri)
        val fragment = ProfileImageFragment()
        fragment.arguments = bundle

        (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            .addToBackStack("AllClientsFragment()")
            .setCustomAnimations(
                R.anim.enter_from_bottom,
                R.anim.no_anim,
                R.anim.no_anim,
                R.anim.exit_to_bottom
            )
            .add(R.id.main_content, fragment)
            .commit()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(differ.currentList[position], position)
    }

    override fun getItemCount(): Int = differ.currentList.size

}

interface OnItemClickNotification {
    fun onNotificationItemClick(mPosition: Int)
}








