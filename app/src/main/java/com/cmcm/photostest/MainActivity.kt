package com.cmcm.photostest

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var mBtnGenerate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initListener()
        checkPermission()
    }

    private fun checkPermission(){
        var result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (result != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1001)
        }

    }

    private fun initListener() {
        mBtnGenerate.setOnClickListener {
            Toast.makeText(this, "generate it", Toast.LENGTH_SHORT).show()
            startGenerate()
        }
    }

    private fun initView() {
        mBtnGenerate = findViewById(R.id.button)
        Log.e("Main", Environment.getExternalStorageDirectory().toString())


    }

    private fun startGenerate(){
        runBlocking {
            for (index in 1..1000){
                launch {
                    Log.d("MainActivity", index.toString())
                    val bitmap: Bitmap? = generate(index)
                    if (bitmap != null) {
                        saveBitmapToLocal(bitmap)
                    }
                    Log.d("MainActivity", index.toString() + " ok")
                }
            }
        }

    }

    private fun saveBitmapToLocal(bitmap: Bitmap){
        var shareFile: File? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            shareFile = File(
                Environment.getExternalStorageDirectory(), "/DCIM/AIHomePhotos/" +
                        UUID.randomUUID().toString() + ".jpg"
            )
        }
        var fileOutputStream: FileOutputStream? = null
        if (shareFile != null) {
            if (!shareFile.exists()) {
                shareFile.createNewFile()
            }
            fileOutputStream = FileOutputStream(shareFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            bitmap.recycle()

            MediaScannerConnection.scanFile(
                this@MainActivity, arrayOf(shareFile.getAbsolutePath()),
                null
            ) { path, uri -> Log.d("MainActivity", "path:$path") }
        }
    }

    private fun generate(index: Int) : Bitmap? {
        val generateView: View = findViewById(R.id.generate_view)
        var textView: TextView = findViewById(R.id.textView)
        textView.text = "Num:" + index
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            (1 shl 30) - 1,
            View.MeasureSpec.AT_MOST
        )
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            (1 shl 30) - 1,
            View.MeasureSpec.AT_MOST
        )
        generateView.measure(widthMeasureSpec, heightMeasureSpec)
        generateView.layout(0, 0, generateView.measuredWidth, generateView.measuredHeight)
        val bitmap: Bitmap? = getGenerateImage(generateView)
        return bitmap;
    }

    private fun getGenerateImage(view: View): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


}
