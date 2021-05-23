package com.example.uploaddocuments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

public abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        init()
    }

    abstract fun getLayoutId(): Int

    abstract fun init()

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }
}