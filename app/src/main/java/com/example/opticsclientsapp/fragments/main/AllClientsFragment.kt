package com.example.opticsclientsapp.fragments.main

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity
import com.example.opticsclientsapp.adapters.OnItemClickClientList
import com.example.opticsclientsapp.adapters.RecyclerViewClientList
import com.example.opticsclientsapp.adapters.SortByAdapter
import com.example.opticsclientsapp.databinding.DialogDeleteClientBinding
import com.example.opticsclientsapp.databinding.FragmentAllClientsBinding
import com.example.opticsclientsapp.db.DbHelper
import com.example.opticsclientsapp.fragments.views.ProfileImageFragment
import com.example.opticsclientsapp.object_class.MyDropAnimation
import com.example.opticsclientsapp.models.Client
import com.example.opticsclientsapp.object_class.MyNotificationManager
import com.example.opticsclientsapp.shared_preference.Pref
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class AllClientsFragment : Fragment() {
    private var _binding: FragmentAllClientsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecyclerViewClientList
    private lateinit var allClientsFromDb: ArrayList<Client>
    private lateinit var filteredList: ArrayList<Client>
    private lateinit var sortedList: ArrayList<Client>
    private lateinit var mActivity: MainActivity

    private lateinit var dbHelper: DbHelper
    private lateinit var pref: Pref

    private var isFromSearch = false

    companion object {
        const val REQUEST_CALL = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllClientsBinding.inflate(layoutInflater)
        initVariables()

        adapter = RecyclerViewClientList(object : OnItemClickClientList {
            override fun deleteBtnClick(client: Client, position: Int) {
                showDeleteDialog(client, position)
            }

            override fun onItemClick(client: Client, position: Int, view: View) {
                client.isExpanded = !client.isExpanded

                if (client.isExpanded) MyDropAnimation.expand(view)
                else MyDropAnimation.collapse(view)
            }

            override fun editBtnClick(client: Client) {
                val bundle = Bundle()
                bundle.putSerializable("_client", client)
                bundle.putBoolean("_whichPage", false)
                val fragment = AddEditPageFragment()
                fragment.arguments = bundle

                moveToNewFragment(fragment)
            }

            override fun openProfileImg(imgUri: String) {
                if (imgUri.isNotEmpty()) {
                    val bundle = Bundle()
                    bundle.putString("_img_uri", imgUri)
                    val fragment = ProfileImageFragment()
                    fragment.arguments = bundle

                    fragmentManager?.beginTransaction()
                        ?.addToBackStack("AllClientsFragment()")
                        ?.setCustomAnimations(
                            R.anim.enter_from_bottom,
                            R.anim.no_anim,
                            R.anim.no_anim,
                            R.anim.exit_to_bottom
                        )
                        ?.add(R.id.main_content, fragment)
                        ?.commit()
                }

            }

            override fun callBtnClick(client: Client) {
                makePhoneCall(client.phoneNum)
            }
        })
        binding.rvClientList.adapter = adapter

        binding.apply {
            menuNotification.root.setOnClickListener {
                moveToNewFragment(NotificationsFragment())
            }

            ivClearTxt.setOnClickListener {
                binding.motionLayout.transitionToStart()
                hideAndClearFocusEditText()
                binding.etSearch.setText("")
            }

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p1 == 0 && p0.toString().isEmpty()) {
                        binding.ivClearTxt.visibility = View.VISIBLE
                    }
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    if (p0.toString().isEmpty())
                        binding.ivClearTxt.visibility = View.GONE
                    generateList(p0.toString())

                }
            })

            rvClientList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0 && !binding.myFab.isHidden) {
                        binding.myFab.hide(true)
                        hideAndClearFocusEditText()
                    } else if (dy < 0 && binding.myFab.isHidden) {
                        binding.myFab.show(true)
                    }
                }
            })

            myFab.setOnClickListener {
                hideAndClearFocusEditText()
                moveToNewFragment(AddEditPageFragment())
            }

            etSearch.setOnTouchListener { view, motionEvent ->
                binding.motionLayout.transitionToEnd()
                return@setOnTouchListener false
            }


        }


        return binding.root
    }

    private fun initVariables() {
        pref = Pref()
        pref.init(binding.root.context)
        dbHelper = DbHelper(binding.root.context)
        mActivity = ((activity as MainActivity?)!!)
        allClientsFromDb = arrayListOf()
        mActivity.initDrawer(binding.toolbar)
    }

    private fun generateList(searchedText: String) {
        filteredList = if (searchedText.trim().isEmpty()) {
            isFromSearch = false
            allClientsFromDb
        } else {
            isFromSearch = true
            var tempList = arrayListOf<Client>()
            val textPhoneNum = searchedText.replace(" ", "")
            val textFullName = searchedText.toLowerCase()

            allClientsFromDb.forEach { client ->
                val fullName = client.fullname?.toLowerCase()!!
                val phoneNum = client.phoneNum?.replace(Regex("""[${' '}-]"""), "")!!
                if (fullName.contains(textFullName) or phoneNum.contains(textPhoneNum))
                    tempList.add(client)
            }
            tempList
        }

        sortList()
    }

    private fun sortList() {
        val sortByItemList =
            binding.root.context.resources.getStringArray(R.array.sort_list_options).toList()
        val myAdapter = SortByAdapter(binding.root.context, sortByItemList)
        binding.ivFilter.adapter = myAdapter

        binding.ivFilter.setSelection(pref.sortPosition)
        binding.ivFilter?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                pref.sortPosition = position
                sortedList = when (position) {
                    1 -> sortByDaysLeft(filteredList)
                    2 -> sortByFullName(filteredList)
                    else -> sortById(filteredList)
                }

                adapter.differ.submitList(sortedList)
                adapter.notifyDataSetChanged()
                binding.rvClientList.scheduleLayoutAnimation()

                checkList()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showDeleteDialog(client: Client, position: Int) {
        val dialog = AlertDialog.Builder(binding.root.context).create()
        val view = DialogDeleteClientBinding.inflate(layoutInflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(view.root)

        view.tvClientName.text = client.fullname
        view.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        view.btnConfirm.setOnClickListener {
            dbHelper.deleteClientById(client.id)
            allClientsFromDb.remove(client)
            sortedList.remove(client)

            deleteImgFile(client.imgProfile)
            deleteNotifications(client.id)

            adapter.differ.submitList(sortedList)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, sortedList.size)

            checkList()
            if (!client.isNotificationSeen)
                countNotification()

            dialog.dismiss()
        }


        dialog.show()
    }

    private fun sortByDaysLeft(mList: ArrayList<Client>): ArrayList<Client> {
        val currentTimeInMillis = System.currentTimeMillis()
        mList.forEach { client ->
            val dateOfPurchase =
                SimpleDateFormat(MainActivity.MY_DATE_FORMAT).parse(client.dateOfPurchase)
            val dateInMillis =
                dateOfPurchase.time + TimeUnit.DAYS.toMillis(client.wearingTime!!.toLong()) - currentTimeInMillis
            client.daysLeft = dateInMillis
        }
        mList.sortBy { client ->
            client.daysLeft

        }
        return mList
    }

    private fun sortById(mList: ArrayList<Client>): ArrayList<Client> {
        mList.sortBy { client ->
            client.id
        }
        return mList
    }

    private fun sortByFullName(mList: ArrayList<Client>): ArrayList<Client> {
        mList.sortBy { client ->
            client.fullname?.toLowerCase()
        }
        return mList
    }

    private fun checkList() {
        binding.tvNoData.text = if (sortedList.size < 1) {
            if (isFromSearch)
                getString(R.string.no_data_search)
            else
                getString(R.string.no_data)
        } else
            ""
    }

    private fun makePhoneCall(number: String) {
        val phoneNum = number.replace(Regex("""[${' '}-]"""), "")
        if (ContextCompat.checkSelfPermission(
                mActivity,
                android.Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(android.Manifest.permission.CALL_PHONE),
                REQUEST_CALL
            )
        } else {
            val dial = "tel:$phoneNum"
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
        }
    }

    private fun deleteNotifications(id: Int) {
        val context = binding.root.context

        MyNotificationManager.deleteCallNotification(context, id)
        MyNotificationManager.deleteBirthdayNotification(context, id)
    }

    private fun deleteImgFile(mImgUri: String) {
        val mFile = File(mImgUri)
        if (mFile.exists()) {
            mFile.delete()
        }
    }

    private fun hideAndClearFocusEditText() {
        mActivity.hideKeyboard()
        binding.etSearch.clearFocus()
    }

    private fun moveToNewFragment(fragment: Fragment) {
        mActivity.supportFragmentManager.beginTransaction()
            .addToBackStack("AllClientsFragment()")
            .setCustomAnimations(
                R.anim.f_open_from_right,
                R.anim.no_anim,
                R.anim.no_anim,
                R.anim.f_exit_to_right
            )
            .replace(R.id.main_content, fragment)
            .commit()
    }

    override fun onResume() {
        allClientsFromDb = dbHelper.getAllClients()

        generateList(binding.etSearch.text.toString())
        countNotification()

        super.onResume()
    }

    private fun countNotification() {
        val counterView = binding.menuNotification.root
        val counterFrame = counterView.findViewById<CardView>(R.id.counter_circle)
        val counterText = counterView.findViewById<TextView>(R.id.counter_circle_txt)

        val num = getUnreadNotificationNum()
        when {
            num < 1 -> counterFrame.visibility = View.GONE
            num > 100 -> counterText.text = "99+"
            else -> {
                counterFrame.visibility = View.VISIBLE
                counterText.text = num.toString()
            }
        }
    }

    private fun getUnreadNotificationNum(): Int {
        var counter = 0
        val allAvailableDates = arrayListOf<String>()

        allClientsFromDb.forEach { client ->
            val notificationDate = getExpirationDate(client)

            if (notificationDate != null && !allAvailableDates.contains(notificationDate)) {
                allAvailableDates.add(notificationDate)
            }
        }

        allAvailableDates.forEach { date ->
            allClientsFromDb.forEach { client ->
                val notificationDate = getExpirationDate(client)

                if (notificationDate != null && date == notificationDate && !client.isNotificationSeen)
                    counter++
            }
        }

        return counter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun Activity.hideKeyboard() {
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
    }

}
