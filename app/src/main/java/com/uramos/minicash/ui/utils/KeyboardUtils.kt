package com.uramos.minicash.ui.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Cierra el teclado de verdad, también dentro de una hoja o un diálogo. Solo quitar el foco o llamar al controlador
 * no basta ahí: la ventana de la hoja vuelve a darle el foco al campo y el teclado reaparece, así que además se
 * le pide a la ventana de la hoja y al sistema que lo oculten.
 */
fun hideKeyboardFully(view: View, focusManager: FocusManager, keyboardController: SoftwareKeyboardController?) {
    focusManager.clearFocus(force = true)
    keyboardController?.hide()
    val dialogWindow = (view.parent as? DialogWindowProvider)?.window
    if (dialogWindow != null) {
        WindowCompat.getInsetsController(dialogWindow, view).hide(WindowInsetsCompat.Type.ime())
    }
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}
