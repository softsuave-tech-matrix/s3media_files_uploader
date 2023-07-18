# softsuave-s3_bucket_files_upload
This is the library for uploading any type of file to amazon S3 bucket for android applications.
And just onle line code by implementing s3media_files_uploader in any android project.

# Manifest

Go to your manifest and paste it.
```groovy
<service
     android:name="com.amazonaws.mobileconnectors.s3.transferutility.TransferService"
     android:enabled="true" />
```

# Gradle

Step 1. Add the JitPack repository to your build file
```groovy
allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }
```
Step 2. Add the dependency
```groovy
	dependencies {
	        implementation 'com.github.softsuave-tech-matrix:s3media_files_uploader:1.0.0'
	}
```
 # Usage
For all kind of media file upload (image/png/jpg/video/mp4/mp3/pdf/doc/apk...etc)
```groovy
public class MainActivity extends AppCompatActivity {

    S3MediaUploader s3MediaUploader;

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
            Log.e(TAG, "FilePath:         $filePath")
            filePath?.let {
                S3MediaUploader().uploadMediaFile(this,
                    it,
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
                            val credentials =
                                BasicAWSCredentials(
                                    AWSAccessKeyId,
                                    SecretAccessKey
                                )
                            val url = getUrl(bucketName, fileName, credentials)

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
}
```
