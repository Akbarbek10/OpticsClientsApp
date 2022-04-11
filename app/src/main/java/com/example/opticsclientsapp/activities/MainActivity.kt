package com.example.opticsclientsapp.activities


import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.databinding.ActivityMainBinding
import com.example.opticsclientsapp.fragments.main.AllClientsFragment
import com.example.opticsclientsapp.fragments.main.NotificationsFragment
import com.example.opticsclientsapp.fragments.main.SettingsFragment
import com.example.opticsclientsapp.object_class.LocaleHelper
import com.example.opticsclientsapp.service.NavDrawerController
import com.example.opticsclientsapp.shared_preference.Pref
import com.google.android.material.navigation.NavigationView
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NavDrawerController {
    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: Pref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = Pref()
        pref.init(this)

        loadTheme()
        loadLanguage()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref.sortPosition = 0

        openFragment(savedInstanceState)

    }

    private fun openFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, AllClientsFragment())
                .commit()

            val notificationCall = intent.getStringExtra("key_notification_call")
            if (notificationCall != null && notificationCall == "from_notification") {
                moveToNewFragment(NotificationsFragment())
            }
        }


    }


    private fun loadTheme() {
        checkDeviceTheme()
        if (pref.nightMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun checkDeviceTheme() {
        if (pref.isOpenedFirstTime) {
            pref.nightMode =
                when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> true
                    Configuration.UI_MODE_NIGHT_NO -> false
                    else -> false
                }
            pref.isOpenedFirstTime = false
        }

    }

    private fun loadLanguage() {
        LocaleHelper.setLocale(baseContext, pref.chosenLang!!)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                val handler = Handler()
                handler.postDelayed({
                    moveToNewFragment(SettingsFragment())
                }, 300)

            }

            R.id.nav_exit -> {
                logout()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun moveToNewFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
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

    private fun logout() {
        Toast.makeText(binding.root.context, "logout()", Toast.LENGTH_SHORT).show()
    }

    override fun initDrawer(toolbar: Toolbar) {
        binding.navView.setNavigationItemSelectedListener(this)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toggle.drawerArrowDrawable.color = resources.getColor(R.color.icons_color)
    }

    override fun lockDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun unlockDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loadLanguage()
        setContentView(binding.root)
    }

    companion object {
        const val MY_DATE_FORMAT = "dd.MM.yyyy"
    }

}




