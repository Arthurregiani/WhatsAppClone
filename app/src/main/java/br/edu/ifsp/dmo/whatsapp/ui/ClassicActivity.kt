package br.edu.ifsp.dmo.whatsapp.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/** Apply system/keyboard insets once at the activity root, including Android 15 edge-to-edge. */
open class ClassicActivity : AppCompatActivity() {
    override fun setContentView(view: View?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.setContentView(view)
        if (view == null) return
        val left = view.paddingLeft
        val right = view.paddingRight
        val top = view.paddingTop
        val bottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { root, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime())
            root.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        ViewCompat.requestApplyInsets(view)
    }
}
