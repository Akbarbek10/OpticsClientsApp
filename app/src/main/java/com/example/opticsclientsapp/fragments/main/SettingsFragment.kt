package com.example.opticsclientsapp.fragments.main


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity
import com.example.opticsclientsapp.adapters.OnLangItemClick
import com.example.opticsclientsapp.adapters.RecyclerViewLanguagesAdapter
import com.example.opticsclientsapp.databinding.DialogLangListBinding
import com.example.opticsclientsapp.databinding.FragmentSettingsBinding
import com.example.opticsclientsapp.db.DbHelper
import com.example.opticsclientsapp.models.Language
import com.example.opticsclientsapp.models.enum.AppLanguage
import com.example.opticsclientsapp.object_class.LocaleHelper
import com.example.opticsclientsapp.object_class.MyNotificationManager
import com.example.opticsclientsapp.service.NavDrawerController
import com.example.opticsclientsapp.shared_preference.Pref
import com.rm.rmswitch.RMSwitch
import java.util.*


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var pref: Pref
    private lateinit var lockMode: NavDrawerController
    private lateinit var mActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(layoutInflater)
        initViews()

        binding.apply {
            toolbar.setNavigationOnClickListener {
                lockMode.unlockDrawer()
                mActivity?.onBackPressed()
            }

            themeSwitch.isChecked = pref.nightMode
            notificationSwitch.isChecked = pref.notificationEnabled

            themeSwitch.addSwitchObserver { _, isChecked ->
                setSwitcherColor(binding.themeSwitch)
            }

            notificationSwitch.addSwitchObserver { _, isChecked ->
                setSwitcherColor(binding.notificationSwitch)
            }

            themeSwitch.setOnClickListener {
                val isChecked = !binding.themeSwitch.isChecked
                binding.themeSwitch.isChecked = isChecked

                pref.nightMode = isChecked

                val handler = Handler()
                handler.postDelayed({
                    if (isChecked)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }, 300)
            }

            notificationSwitch.setOnClickListener {
                val isChecked = !binding.notificationSwitch.isChecked
                binding.notificationSwitch.isChecked = isChecked
                pref.notificationEnabled = isChecked


                if (isChecked)
                    enableNotifications()
                else
                    disableNotifications()
            }

            btnLanguage.setOnClickListener {
                showChangeLangDialog()
            }
        }


        return binding.root
    }

    private fun setSwitcherColor(switchView: RMSwitch) {
        switchView.switchToggleCheckedColor =
            context?.resources?.getColor(R.color.switcher_toggle_checked_color)!!
        switchView.switchToggleNotCheckedColor =
            context?.resources?.getColor(R.color.switcher_toggle_not_checked_color)!!
        switchView.switchBkgCheckedColor =
            context?.resources?.getColor(R.color.switcher_bg_checked_color)!!
        switchView.switchBkgNotCheckedColor =
            context?.resources?.getColor(R.color.switcher_bg_not_checked_color)!!
    }

    private fun showChangeLangDialog() {
        val langList = arrayListOf(
            Language(AppLanguage.UZBEK.ordinal, R.drawable.ic_uzb, AppLanguage.UZBEK.language),
            Language(AppLanguage.RUSSIAN.ordinal, R.drawable.ic_rus, AppLanguage.RUSSIAN.language),
            Language(AppLanguage.ENGLISH.ordinal, R.drawable.ic_usa, AppLanguage.ENGLISH.language)
        )

        val dialog = AlertDialog.Builder(binding.root.context).create()
        val view = DialogLangListBinding.inflate(layoutInflater, null, false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(view.root)

        view.rvLang.adapter = RecyclerViewLanguagesAdapter(langList, object : OnLangItemClick {
            override fun onClick(lang: Language) {
                var tempLang = when (lang.id) {
                    0 -> AppLanguage.UZBEK.langAbbr
                    1 -> AppLanguage.RUSSIAN.langAbbr
                    else -> AppLanguage.ENGLISH.langAbbr
                }

                if (!tempLang.equals(pref.chosenLang)) {
                    LocaleHelper.setLocale(mActivity.baseContext, tempLang)
                    pref.chosenLang = tempLang
                    dialog.dismiss()
                    mActivity.recreate()
                }
                dialog.dismiss()
            }
        })
        dialog.show()
    }

    private fun enableNotifications() {
        val context = binding.root.context
        val dbHelper = DbHelper(context)
        val clientList = dbHelper.getAllClients()

        val currentDateInMillis = getCurrentDateInMillis()

        clientList.forEach { client ->
            val notificationDateInMillis = MyNotificationManager.getNotificationDateInMillis(client.dateOfPurchase, client.wearingTime)

            //reset call notifications
            if (notificationDateInMillis >= currentDateInMillis)
                MyNotificationManager.createCallsNotification(context, client)

            //reset birthday notifications
            if (client.dateOfBirth.isNotEmpty())
                MyNotificationManager.createBirthdaysNotification(context, client)

        }
    }

    private fun disableNotifications() {
        val context = binding.root.context
        val dbHelper = DbHelper(context)
        val clients = dbHelper.getAllClients()

        clients.forEach { client ->
            MyNotificationManager.deleteCallNotification(context,client.id)
            MyNotificationManager.deleteBirthdayNotification(context,client.id)
        }

    }

    private fun getCurrentDateInMillis(): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.timeInMillis
    }

    private fun initViews() {
        pref = Pref()
        pref.init(binding.root.context)
        mActivity = ((activity as MainActivity?)!!)
        lockMode = activity as NavDrawerController
        lockMode.lockDrawer()

        mActivity.setSupportActionBar(binding.toolbar)
        mActivity.supportActionBar?.title = ""
        mActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity.supportActionBar?.setDisplayShowHomeEnabled(true)
        mActivity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_btn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lockMode.unlockDrawer()
        _binding = null
    }


}