package com.dicoding.asclepius.view.main

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.view.result.ResultActivity
import com.dicoding.asclepius.view.save.SaveActivity
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var result: String? = null
    private var prediction: String? = null
    private var score: String? = null
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val resultIntent = Intent(this, ResultActivity::class.java)

        binding.galleryButton.setOnClickListener { openGallery() }
        binding.analyzeButton.setOnClickListener {
            analyzeAndMoveToResult(resultIntent)
        }
        binding.buttonSave.setOnClickListener {
            startActivity(Intent(this, SaveActivity::class.java))
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            displayImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun openGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun displayImage() {
        currentImageUri?.let {
            cropImage(it)
            Log.d("Image URI", "displayImage: $it")
        }
    }

    private fun analyzeAndMoveToResult(intent: Intent) {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        showToast(error)
                    }
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    results?.let { it ->
                        if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                            val sortedCategories =
                                it[0].categories.sortedByDescending { it?.score }
                            result =
                                sortedCategories.joinToString("\n") {
                                    "${it.label} " + NumberFormat.getPercentInstance()
                                        .format(it.score).trim()
                                }
                            prediction = sortedCategories[0].label
                            score =
                                NumberFormat.getPercentInstance().format(sortedCategories[0].score)
                        } else {
                            showToast()
                        }
                    }
                }
            }
        )
        currentImageUri?.let { this.imageClassifierHelper.classifyStaticImage(it) }
        intent.putExtra(ResultActivity.EXTRA_RESULT, result)
        intent.putExtra(ResultActivity.EXTRA_PREDICT, prediction)
        intent.putExtra(ResultActivity.EXTRA_SCORE, score)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
        startActivity(intent)
    }

    private fun showToast(message: String = "No results found") {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private var cropImage = registerForActivityResult(
        CropImageContract()
    ) { result: CropImageView.CropResult ->
        if (result.isSuccessful) {
            val crop =
                BitmapFactory.decodeFile(result.getUriFilePath(applicationContext, true))
            binding.previewImageView.setImageBitmap(crop)
            currentImageUri = result.uriContent
        }
    }

    private fun cropImage(uri: Uri) {
        cropImage.launch(
            CropImageContractOptions(
                uri = uri, cropImageOptions = CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON
                )
            )
        )
    }
}

