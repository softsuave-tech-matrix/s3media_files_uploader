
# Softsuave - S3 Bucket File Upload library
[![](https://jitpack.io/v/softsuave-tech-matrix/s3media_files_uploader.svg)](https://jitpack.io/#softsuave-tech-matrix/s3media_files_uploader)

# Introduction
This library simplifies the process of uploading various file types to an Amazon S3 bucket within Android applications. With just one line of code, developers can seamlessly integrate the s3media_files_uploader into their projects. This streamlined integration enhances the efficiency of file uploads, making the development process smoother and more accessible for Android developers.
## Features

- Securely upload any type of file to an S3 bucket.
- Instantly retrieve the URL for the uploaded file.
- Simplified implementation with minimal code required.
- supports all kind of file extensions(image/png/jpg/video/mp4/mp3/pdf/doc/apk...etc)
- Enhanced readability for easier integration.
- Plug-and-play functionality for seamless usage.


## Quick Setup

This section explains how to setup *s3media_files_uploader* as a library for your Android applications.

#### 1. Include library

###### a. Android Studio (AS)

* Modify the *settings.gradle.kts* file in the *project* module of your Android project by adding the following *compile* line in the *repositories* body:

```gradle
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}
```

* Modify the *build.gradle* file in the *app* module of your Android project by adding the following *compile* line in the *dependencies* body:
``` gradle
dependencies {
   //S3 bucket media files uploader and AWS android sdk
    implementation ("com.github.softsuave-tech-matrix:s3media_files_uploader:1.0.2")
    implementation ("com.amazonaws:aws-android-sdk-s3:2.26.0")
}
``` 
* Resync your project to apply changes.


#### 2. Android Manifest

Modify the *Android Manifest* of your application by adding a couple of **required permissions**:
``` xml
<manifest>
	<!-- 	Include following permission to be able to download remote resources 
			like files and data -->
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<!-- 	Include following permission to be able to download remote resources 
			like files and data -->
	<uses-permission android:name="android.permission.INTERNET" />
	<!-- 	Include following permission to be able to import local files 
			on SD card -->
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
	...
</manifest>
```

## Quick example of use

This quick use case gives you a taste on how to use *s3media_files_uploader* once you have added it to your project.
```kotlin
public class MainActivity extends AppCompatActivity {

    S3MediaUploader s3MediaUploader

    override fun onCreate(savedInstanceState: Bundle?) {
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

    fun uploadFileToS3(imageUri: Uri?) {
        val filePath = getFilePathFromURI(imageUri)
        val AWSAccessKeyId: String = "AWSAccessKeyId"
        val SecretAccessKey: String = "SecretAccessKey"
        val sessionToken: String = "sessionToken"
        val bucketName: String = "imagetestbucket1234567"
        val regions: Regions = Regions.US_EAST_1

        if (filePath != null) {
            Log.e(TAG, "FilePath: $filePath")
            filePath?.let { validFilePath ->
                S3MediaUploader().uploadMediaFile(
                    this,
                    validFilePath,
                    AWSAccessKeyId,
                    SecretAccessKey,
                    "sessionToken", // not compulsory
                    bucketName,
                    regions,
                    object : S3MediaUploader.UploadListener {
                        override fun onUploadFailed() {
                            Log.d(TAG, "onUploadFailed: #####")
                        }

                        override fun onUploadCompleted(fileName: String?) {
                            Log.d(TAG, "onUploadCompleted: $fileName ")
                            val credentials = BasicAWSCredentials(AWSAccessKeyId, SecretAccessKey)
                            val url = getUrl(bucketName, fileName, credentials)
                            Log.d(TAG, "getUrl: $url ")
                        }

                        override fun onProgressUpdate(id: Int, current: Int) {
                            super.onProgressUpdate(id, current)
                            Log.d(TAG, "Progress: $current")
                        }
                    }
		)
            }
        } else {
            Toast.makeText(this, "Null Path", Toast.LENGTH_SHORT).show()
        }
    }
}
```

## Contribution Guidelines
We welcome contributions to enhance this project! Before making significant changes, please open an issue to discuss your ideas.

When contributing, please ensure the following:

Open an issue to discuss major changes beforehand.
Update tests to reflect any modifications.
Follow coding conventions and project standards.
Your contributions help improve the project for everyone.

Thank you for your support!
## Authors

- [@softsuave-tech-matrix](https://github.com/softsuave-tech-matrix)


## Feedback

If you have any feedback, please reach out to us at techmatrix@softsuave.com

