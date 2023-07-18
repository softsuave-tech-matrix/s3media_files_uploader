package com.softsuave.s3bucketmediaupload.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.softsuave.s3bucketmediaupload.R
import com.softsuave.s3bucketmediaupload.S3MediaUploader
import com.softsuave.s3media_files_uploader.utils.AmazonS3Utils
import java.io.*

class MainActivity : AppCompatActivity(), S3MediaUploader.UploadListener {
    var bt_upload: Button? = null
    var bt_select: Button? = null
    var image: ImageView? = null
    var s3uploaderObj: S3MediaUploader? = null
    var urlFromS3: String? = null
    var imageUri: Uri? = null
    var filePath: String? = null
    var docFile: File? = null
    private val TAG = MainActivity::class.java.canonicalName
    private val SELECT_PICTURE = 1
    var count = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt_upload = findViewById(R.id.bt_upload)
        bt_select = findViewById(R.id.bt_select)
        image = findViewById(R.id.image)
        bt_select!!.setOnClickListener { isStoragePermissionGranted }
        bt_upload!!.setOnClickListener {
            uploadFileToS3(imageUri)
        }
    }
    val isStoragePermissionGranted: Unit
        get() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    chooseImage()
                } else {
                    Log.v(TAG, "Permission is revoked")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                }
            } else {
                Log.v(TAG, "Permission is granted")
                chooseImage()
            }
        }

    private fun chooseImage() {
        val intent = Intent()
        intent.setType("*/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooseImage()
            Log.e(TAG, "Permission: " + permissions[0] + "was " + grantResults[0])
        } else {
            Log.e(TAG, "Please click again and select allow to choose profile picture")
        }
    }

    fun OnPictureSelect(data: Intent) {
        imageUri = data.getData()
        filePath =  getPath(data.data) // AmazonS3Utils().getFilePathFromURI
        var imageStream: InputStream? = null
        try {
            imageStream = imageUri?.let { getContentResolver().openInputStream(it) }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (imageStream != null) {
            count++
            image!!.setImageBitmap(BitmapFactory.decodeStream(imageStream))
        }
    }


    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null) ?: return null
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }

    fun uploadFileToS3(imageUri: Uri?) {
       // val path = getFilePathFromURI(imageUri)
        val AWSAccessKeyId: String = "AWSAccessKeyId"
        val SecretAccessKey: String = "SecretAccessKey"
        val sessionToken: String = ""
        val bucketName: String = "imagetestbucket1234567"
        val regions: Regions = Regions.US_EAST_1

        if (filePath != null) {
            Log.e(TAG, "FilePath:         $filePath")
            filePath?.let {
                S3MediaUploader().uploadMediaFile(this,
                    it,
                    AWSAccessKeyId,
                    SecretAccessKey,
                    "",
                    bucketName,
                    regions,
                    object : S3MediaUploader.UploadListener {
                        override fun onUploadFailed() {
                            Log.d(TAG, "onUploadFailed: #####")
                        }

                        override fun onUploadCompleted(fileName: String?) {
                            Log.d(TAG, "onUploadCompleted: $fileName ")
                            val credentials =
                                BasicAWSCredentials(
                                    AWSAccessKeyId,
                                    SecretAccessKey
                                )
                            val url = AmazonS3Utils().getUrl(bucketName, fileName, credentials)

                            Log.d(TAG, "getUrl: $url ")
                        }

                        override fun onProgressUpdate(id: Int, current: Int) {
                            super.onProgressUpdate(id, current)
                            Log.d(TAG, "Progress: $current")
                        }

                    })
            }

        } else {
            Toast.makeText(this, "Null Path", Toast.LENGTH_SHORT).show()
        }
    }


    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    OnPictureSelect(data)
                }
            }
        }
    }

    override fun onProgressUpdate(id: Int, current: Int) {
        super.onProgressUpdate(id, current)
    }

    override fun onUploadFailed() {
        TODO("Not yet implemented")
    }

    override fun onUploadCompleted(fileName: String?) {
        TODO("Not yet implemented")
    }
}