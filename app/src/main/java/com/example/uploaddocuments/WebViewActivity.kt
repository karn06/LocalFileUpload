package com.example.uploaddocuments

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pdfview.subsamplincscaleimageview.SubsamplingScaleImageView
import kotlinx.android.synthetic.main.fragment_webview.*

class WebViewActivity:BaseActivity() {

    override fun getLayoutId()=  R.layout.fragment_webview

    private var isShowinChat: Boolean = false
    var title =""

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)


    }

    override fun init() {


/*        toolbar.title ="PDF"
        toolbar.navigationIcon= ContextCompat.getDrawable(this,R.drawable.ic_baseline_arrow_back_24)*/

        if (intent.extras != null) {
            wvDocs.fromFile(intent.extras!!.getString("fileUrl","")!!).show()

        }


       /* toolbar.setNavigationOnClickListener {
            onBackPressed()
        }*/

        wvDocs.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onImageLoaded() {
                if (progressBar2 != null) progressBar2.visibility = View.GONE
            }

            override fun onReady() {
            }

            override fun onTileLoadError(e: Exception?) {
            }

            override fun onPreviewReleased() {
            }

            override fun onImageLoadError(e: Exception?) {
            }

            override fun onPreviewLoadError(e: Exception?) {
            }
        })
    }





    override fun onStop() {
        super.onStop()
    }
}