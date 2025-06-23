// DatePickerUtils.kt
package com.example.utils

import android.app.DatePickerDialog
import android.content.Context
import java.time.LocalDate
import java.util.*

fun showDatePicker(context: Context, onDateSelected: (LocalDate?) -> Unit) {
    val today = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            onDateSelected(selectedDate)
        },
        today.get(Calendar.YEAR),
        today.get(Calendar.MONTH),
        today.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear") { _, _ ->
        onDateSelected(null)
    }
    datePickerDialog.show()
}
