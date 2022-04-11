package com.example.opticsclientsapp.date_picker

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.opticsclientsapp.R
import java.text.DateFormat
import java.util.*

class DatePickerFragment(private val calendar: Calendar) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireActivity(),
            R.style.MyDatePickerDialogTheme,
            this,
            year,
            month,
            day
        )
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val selectedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.time)

        val bundle = Bundle()
        bundle.putString("SELECTED_DATE", selectedDate)

        setFragmentResult("REQUEST_KEY", bundle)
    }
}