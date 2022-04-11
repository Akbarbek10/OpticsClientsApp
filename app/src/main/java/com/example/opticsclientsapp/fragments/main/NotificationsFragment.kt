package com.example.opticsclientsapp.fragments.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity
import com.example.opticsclientsapp.adapters.OnItemClickNotification
import com.example.opticsclientsapp.adapters.RecyclerViewNotificationGroup
import com.example.opticsclientsapp.databinding.FragmentNotificationsBinding
import com.example.opticsclientsapp.db.DbHelper
import com.example.opticsclientsapp.models.Client
import com.example.opticsclientsapp.models.MyDate
import com.example.opticsclientsapp.service.NavDrawerController
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecyclerViewNotificationGroup
    private lateinit var allClientsFromDb: ArrayList<Client>

    private lateinit var dbHelper: DbHelper
    private lateinit var lockMode: NavDrawerController
    private lateinit var mActivity: MainActivity
    private lateinit var groupedList: ArrayList<MyDate>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(layoutInflater)
        initViews()

        binding.apply {
            toolbar.setNavigationOnClickListener {
                lockMode.unlockDrawer()
                mActivity?.onBackPressed()
            }

            menuReadAll.root.setOnClickListener {
                readAll()
            }
        }

        return binding.root
    }


    private fun initViews() {
        dbHelper = DbHelper(binding.root.context)
        allClientsFromDb = arrayListOf()

        mActivity = ((activity as MainActivity?)!!)
        lockMode = activity as NavDrawerController
        lockMode.lockDrawer()

        mActivity?.setSupportActionBar(binding.toolbar)
        mActivity?.supportActionBar?.title = ""
        mActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity.supportActionBar?.setDisplayShowHomeEnabled(true)
        mActivity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_btn)

        (binding.rvClientList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun generateList() {
        groupedList = generateGroupList()
        binding.tvNoNotifications.visibility =
            if (groupedList.isEmpty()) View.VISIBLE
            else View.GONE
        adapter =
            RecyclerViewNotificationGroup(binding.root.context, object : OnItemClickNotification {
                override fun onNotificationItemClick(mPosition: Int) {
                    adapter.notifyItemChanged(mPosition)
                }

            })
        adapter.differ.submitList(groupedList)
        binding.rvClientList.adapter = adapter
        binding.rvClientList.scheduleLayoutAnimation()


    }

    private fun generateGroupList(): ArrayList<MyDate> {
        val allAvailableDates = arrayListOf<String>()
        val mGroupedList = arrayListOf<MyDate>()

        allClientsFromDb.forEach { client ->
            val notificationDate = getExpirationDate(client)

            if (notificationDate != null && !allAvailableDates.contains(notificationDate)) {
                allAvailableDates.add(notificationDate)
            }
        }

        allAvailableDates.forEach { date ->
            val clientList = arrayListOf<Client>()

            allClientsFromDb.forEach { client ->
                val notificationDate = getExpirationDate(client)

                if (notificationDate != null && date == notificationDate)
                    clientList.add(client)
            }

            mGroupedList.add(MyDate(date, clientList))
        }

        mGroupedList.sortWith { first, second ->
            val date1 = SimpleDateFormat(MainActivity.MY_DATE_FORMAT).parse(first.date)
            val date2 = SimpleDateFormat(MainActivity.MY_DATE_FORMAT).parse(second.date)
            date2.compareTo(date1)
        }

        val calendar = Calendar.getInstance()
        val today = SimpleDateFormat(MainActivity.MY_DATE_FORMAT).format(calendar.time)
        val yesterday = SimpleDateFormat(MainActivity.MY_DATE_FORMAT).format(
            calendar.timeInMillis - TimeUnit.DAYS.toMillis(
                1
            )
        )
        mGroupedList.forEach { myDate ->
            if (myDate.date == today.toString()) {
                myDate.date = getString(R.string.tv_date_today)
            } else if (myDate.date == yesterday.toString()) {
                myDate.date = getString(R.string.tv_date_yesterday)
            }
        }

        return mGroupedList
    }

    private fun getExpirationDate(client: Client): String? {
        val dateOfPurchase =
            SimpleDateFormat(MainActivity.MY_DATE_FORMAT).parse(client.dateOfPurchase)
        val calendar = Calendar.getInstance()
        calendar.time = dateOfPurchase
        calendar.add(Calendar.DAY_OF_MONTH, client.wearingTime)

        val checkTime =
            TimeUnit.HOURS.toMillis(12) + calendar.timeInMillis < System.currentTimeMillis()
        val calendarDate =
            if (checkTime) SimpleDateFormat(MainActivity.MY_DATE_FORMAT).format(calendar.time)
            else null

        return calendarDate
    }

    private fun readAll() {
        groupedList.forEach { myDate ->
            myDate.clientList.forEach { client ->
                if (!client.isNotificationSeen) {
                    dbHelper.makeNotificationChecked(client.id)
                    client.isNotificationSeen = true
                }

            }
        }
        adapter.differ.submitList(groupedList)
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        allClientsFromDb = dbHelper.getAllClients()

        generateList()

        super.onResume()
    }

    override fun onDestroyView() {
        lockMode.unlockDrawer()
        super.onDestroyView()
        _binding = null
    }


}