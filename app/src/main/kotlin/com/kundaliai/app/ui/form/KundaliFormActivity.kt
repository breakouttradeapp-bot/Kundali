package com.kundaliai.app.ui.form

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class KundaliFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "Form Screen Working"
        textView.textSize = 22f
        textView.setPadding(40, 100, 40, 40)

        setContentView(textView)
    }
}
