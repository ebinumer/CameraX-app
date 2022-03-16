package com.ebinumer.cameraxandcrop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ebinumer.cameraxandcrop.databinding.ActivityGalleryBinding
import java.io.File


class GalleryActivity : AppCompatActivity(),itemClickedInterface {
    private lateinit var binding: ActivityGalleryBinding
    lateinit var files : Array<File>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setImages()

    }
    fun setImages(){
        //        all images
        val directory = File(externalMediaDirs[0].absolutePath)
        files = directory.listFiles() as Array<File>

        // array is reversed to ensure last taken photo appears first.
        val adapter = GalleryAdapter(files.reversedArray(),this)
        binding.viewPager.adapter = adapter
    }

    override fun clicked(position: Int) {
        Log.e("Clicked ="," $position")


        files[position].delete()

        this.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(files[position])
            )
        )
        setImages()
    }
}