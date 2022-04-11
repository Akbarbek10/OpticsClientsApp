package com.example.opticsclientsapp.fragments.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity
import com.example.opticsclientsapp.databinding.DialogDeleteImgProfileBinding
import com.example.opticsclientsapp.databinding.FragmentAddEditPageBinding
import com.example.opticsclientsapp.date_picker.DatePickerFragment
import com.example.opticsclientsapp.db.DbHelper
import com.example.opticsclientsapp.fragments.views.ProfileImageFragment
import com.example.opticsclientsapp.models.Client
import com.example.opticsclientsapp.object_class.MyNotificationManager
import com.example.opticsclientsapp.service.NavDrawerController
import com.example.opticsclientsapp.shared_preference.Pref
import com.github.dhaval2404.imagepicker.ImagePicker
import com.szagurskii.patternedtextwatcher.PatternedTextWatcher
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddEditPageFragment : Fragment() {
    private var _binding: FragmentAddEditPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var pref: Pref
    private lateinit var dbHelper: DbHelper
    private lateinit var imgUri: String

    private lateinit var lockMode: NavDrawerController
    private lateinit var mActivity: MainActivity

    private var isApplied = false
    private var isAddPage = true
    private var client = Client()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddEditPageBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        dbHelper = DbHelper(binding.root.context)
        binding.etPhoneNum.addTextChangedListener(PatternedTextWatcher("+### ## ###-##-##"))

        val bundle = this.arguments
        if (bundle != null) {
            client = bundle.getSerializable("_client") as Client
            isAddPage = bundle.getBoolean("_whichPage", true)
        }
        imgUri = client.imgProfile
        initViews()
        initAddOrEditPage()

        binding.ivClearDate.visibility = if (client.dateOfBirth.isEmpty()) View.GONE
        else View.VISIBLE


        binding.apply {
            toolbar.root.setNavigationOnClickListener {
                mActivity.onBackPressed()
            }

            rgGenderGroup.setOnCheckedChangeListener { group, checkedId ->
                if (imgUri.isEmpty()) setImgByGender(group.checkedRadioButtonId)
            }

            etDateOfBirth.setOnClickListener {
                showDateDialog(binding.etDateOfBirth.id)
            }

            etPurchaseDate.setOnClickListener {
                showDateDialog(binding.etPurchaseDate.id)
            }

            ivClearDate.setOnClickListener {
                binding.etDateOfBirth.text = ""
                binding.ivClearDate.visibility = View.GONE
            }

            ivProfileImg.setOnClickListener {
                if (imgUri.isNotEmpty())
                    openImgProfile()
            }

            btnChangeImg.setOnClickListener {
                ImagePicker.with(this@AddEditPageFragment)
                    .crop()
                    .compress(400)
                    .saveDir(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)
                    .maxResultSize(1080, 1080)
                    .start()
            }

            btnDeleteImg.setOnClickListener {
                if (imgUri.isNotEmpty()) {
                    if (!isAddPage) {
                        showDeleteImgDialog()
                    } else {
                        if (deleteImgFile(imgUri))
                            setImgByGender(binding.rgGenderGroup.checkedRadioButtonId)
                    }

                }
            }
        }


        return binding.root
    }

    private fun showDeleteImgDialog() {
        val dialog = AlertDialog.Builder(binding.root.context).create()
        val view = DialogDeleteImgProfileBinding.inflate(layoutInflater, null, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setView(view.root)

        view.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        view.btnConfirm.setOnClickListener {
            dbHelper.updateClientImgProfile(client.id, "")
            if (deleteImgFile(imgUri))
                setImgByGender(binding.rgGenderGroup.checkedRadioButtonId)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openImgProfile() {
        val bundle = Bundle()
        bundle.putString("_img_uri", imgUri)
        val fragment = ProfileImageFragment()
        fragment.arguments = bundle

        fragmentManager?.beginTransaction()
            ?.addToBackStack("AddEditPageFragment()")
            ?.setCustomAnimations(
                R.anim.enter_from_bottom,
                R.anim.no_anim,
                R.anim.no_anim,
                R.anim.exit_to_bottom
            )
            ?.add(R.id.main_content, fragment)
            ?.commit()
    }

    private fun initAddOrEditPage() {
        if (isAddPage) {
            binding.tvTitle.text = getString(R.string.add_title)
            binding.etPurchaseDate.text =
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Calendar.getInstance().time)
        } else {
            binding.tvTitle.text = getString(R.string.edit_title)
            fillForm()
        }
    }

    private fun editClientInfo() {
        if (checkCredentials()) {
            val client = collectAllData()
            client.id = this.client.id
            dbHelper.updateClient(client)
            if(pref.notificationEnabled){
                createCallsNotification(client)
                if(client.dateOfBirth.isNotEmpty())
                    createBirthdayNotification(client)
            }

            Toast.makeText(binding.root.context, getString(R.string.toast_edited), Toast.LENGTH_SHORT).show()
            mActivity.onBackPressed()
        }
    }

    private fun addClientToDb() {
        if (checkCredentials()) {
            val client = collectAllData()
            client.id = dbHelper.addClient(client)
            if(pref.notificationEnabled){
                createCallsNotification(client)
                if(client.dateOfBirth.isNotEmpty())
                    createBirthdayNotification(client)
            }
            Toast.makeText(requireContext(), getString(R.string.toast_added), Toast.LENGTH_SHORT).show()
            mActivity.onBackPressed()
        }
    }

    private fun collectAllData(): Client {
        val fullName = binding.etFullname.text.toString()
        val phoneNum = binding.etPhoneNum.text.toString()
        val imgProfile = imgUri
        val productName = binding.etProductName.text.toString()
        val dioptre = binding.etDioptre.text.toString()
        val wearingTime = binding.etWearingTime.text.toString().toInt()
        val notes = binding.etNotes.text.toString()
        val gender = when (binding.rgGenderGroup.checkedRadioButtonId) {
            R.id.rb_male -> 0
            R.id.rb_female -> 1
            else -> 0
        }

        val dateOfBirthText = binding.etDateOfBirth.text.toString()
        val dateOfBirth = if (dateOfBirthText.isNotEmpty()) {
            val dateOfBirthFormat =
                DateFormat.getDateInstance(DateFormat.MEDIUM).parse(dateOfBirthText)
            SimpleDateFormat(MainActivity.MY_DATE_FORMAT).format(dateOfBirthFormat).toString()
        } else ""

        val dateOfPurchaseFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            .parse(binding.etPurchaseDate.text.toString())
        val dateOfPurchase =
            SimpleDateFormat(MainActivity.MY_DATE_FORMAT).format(dateOfPurchaseFormat)

        return Client(
            fullname = fullName,
            phoneNum = phoneNum,
            dateOfBirth = dateOfBirth,
            imgProfile = imgProfile,
            gender = gender,
            productName = productName,
            dioptre = dioptre,
            dateOfPurchase = dateOfPurchase,
            wearingTime = wearingTime,
            notes = notes
        )
    }

    private fun checkCredentials(): Boolean {
        val fullName = binding.etFullname.text.toString()
        val phoneNum = binding.etPhoneNum.text.toString().replace(Regex("[-+ ]"), "")
        val productName = binding.etProductName.text.toString()
        val dioptre = binding.etDioptre.text.toString()
        val wearingTime = binding.etWearingTime.text.toString()

        if (fullName.isEmpty() || !TextUtils.isDigitsOnly(phoneNum) || phoneNum.length < 12 || productName.isEmpty() || productName.isEmpty() || dioptre.isEmpty() || wearingTime.isEmpty()) {
            if (fullName.isEmpty()) binding.etFullname.error =
                getString(R.string.error_enter_fullname)
            if (phoneNum.length < 12 || !TextUtils.isDigitsOnly(phoneNum)) binding.etPhoneNum.error =
                getString(R.string.error_enter_phone_number)
            if (productName.isEmpty()) binding.etProductName.error =
                getString(R.string.error_enter_product_name)
            if (dioptre.isEmpty()) binding.etDioptre.error = getString(R.string.error_enter_diopter)
            if (wearingTime.isEmpty()) binding.etWearingTime.error =
                getString(R.string.error_enter_expiration_time)
            return false
        }
        val filteredWearingTime = wearingTime.replace(Regex("[0123456789]"), "")
        if (filteredWearingTime.isNotEmpty()) {
            binding.etWearingTime.error = getString(R.string.error_enter_days_number)
            return false
        }

        return true
    }

    private fun fillForm() {
        binding.etFullname.setText(client.fullname)
        val phoneNum = client.phoneNum.replace(Regex("[-+ ]"), "")
        binding.etPhoneNum.setText(phoneNum)
        if (client.dateOfBirth.isNotEmpty()) {
            val date =
                SimpleDateFormat(MainActivity.MY_DATE_FORMAT).parse(client.dateOfBirth)
            binding.etDateOfBirth.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
        }
        if (client.imgProfile.isNotEmpty() and File(client.imgProfile).exists()) {
            val imgUri = Uri.parse(client.imgProfile)
            binding.ivProfileImg.setImageURI(imgUri)

        } else {
            when (client.gender) {
                0 -> binding.ivProfileImg.setImageResource(R.drawable.ic_man_profile)
                else -> binding.ivProfileImg.setImageResource(R.drawable.ic_woman_profile)
            }
        }

        when (client.gender) {
            0 -> binding.rbMale.isChecked = true
            else -> binding.rbFemale.isChecked = true
        }
        binding.etProductName.setText(client.productName)
        binding.etDioptre.setText(client.dioptre)
        binding.etWearingTime.setText(client.wearingTime.toString())
        binding.etNotes.setText(client.notes)
        val date = SimpleDateFormat(MainActivity.MY_DATE_FORMAT).parse(client.dateOfPurchase)
        binding.etPurchaseDate.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
    }

    private fun createCallsNotification(client: Client) {
        MyNotificationManager.createCallsNotification(binding.root.context, client)
    }

    private fun createBirthdayNotification(client: Client) {
        MyNotificationManager.createBirthdaysNotification(binding.root.context, client)
    }

    private fun setImgByGender(groupId: Int) {
        val image = when (groupId) {
            R.id.rb_female -> R.drawable.ic_woman_profile
            else -> R.drawable.ic_man_profile
        }
        binding.ivProfileImg.setImageResource(image)
    }

    private fun deleteImgFile(mImgUri: String): Boolean {
        val mFile = File(mImgUri)
        if (mFile.exists()) {
            if (mFile.delete()) {
                imgUri = ""
                return true
            } else
                Toast.makeText(binding.root.context, "ERROR HAS OCCURRED", Toast.LENGTH_SHORT)
                    .show()
        } else {
            Toast.makeText(binding.root.context, "NOT FOUND", Toast.LENGTH_SHORT).show()

        }
        return false
    }

    private fun initViews() {
        mActivity = ((activity as MainActivity?)!!)
        lockMode = activity as NavDrawerController
        pref = Pref()
        pref.init(binding.root.context)
        lockMode.lockDrawer()

        mActivity?.setSupportActionBar(binding.toolbar.root)
        mActivity?.supportActionBar?.title = ""
        mActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity.supportActionBar?.setDisplayShowHomeEnabled(true)
        mActivity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_btn)
    }

    private fun showDateDialog(textViewId: Int) {
        val tvDate = view?.findViewById<TextView>(textViewId)!!
        val calendar = Calendar.getInstance()

        if (tvDate.text.toString().isEmpty()) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.YEAR, 1990)
        } else {
            val date = DateFormat.getDateInstance(DateFormat.MEDIUM).parse(tvDate.text.toString())
            calendar.time = date
        }

        val datePickerFragment = DatePickerFragment(calendar)
        val supportFragmentManager = mActivity.supportFragmentManager

        supportFragmentManager.setFragmentResultListener(
            "REQUEST_KEY",
            viewLifecycleOwner
        ) { resultKey, bundle ->
            if (resultKey == "REQUEST_KEY") {
                val selectedDate = bundle.getString("SELECTED_DATE")
                tvDate.text = selectedDate

                if (tvDate.id.toString() == binding.etDateOfBirth.id.toString()) {
                    binding.ivClearDate.visibility = View.VISIBLE
                }
            }
        }

        datePickerFragment.show(supportFragmentManager, "DatePickerFragment")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri = data?.data!!
                binding.ivProfileImg.setImageURI(uri)

                setNewImage(uri)
            }

            ImagePicker.RESULT_ERROR -> Toast.makeText(binding.root.context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setNewImage(uri: Uri) {
        val newImgUri = uri.toString().replace("file://", "")

        if (isAddPage) {
            if (imgUri.isNotEmpty())
                deleteImgFile(imgUri)
        } else {
            if (imgUri.isNotEmpty())
                deleteImgFile(imgUri)
            dbHelper.updateClientImgProfile(client.id, newImgUri)
        }

        imgUri = newImgUri
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu_check, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_check -> {
                if (isAddPage) addClientToDb()
                else editClientInfo()
                isApplied = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        mActivity.hideKeyboard()
        if (!isApplied and imgUri.isNotEmpty() and isAddPage)
            deleteImgFile(imgUri)

        super.onDestroyView()
        lockMode.unlockDrawer()
        _binding = null
    }

    override fun onResume() {
        val locale = resources.configuration.locale
        Locale.setDefault(locale)
        super.onResume()
    }

    fun Activity.hideKeyboard() {
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
    }

}