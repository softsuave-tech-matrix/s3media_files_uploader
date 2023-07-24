# softsuave-s3_bucket_files_upload
This library is for uploading any type of file to Amazon S3 bucket for Android applications. 
It just requires one line of code for integrating s3media_files_uploader in any Android project.

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
    //S3 bucket media file uploader
    implementation 'com.github.softsuave-tech-matrix:s3media_files_uploader:1.0.1'
    implementation 'com.amazonaws:aws-android-sdk-s3:2.26.0'
}
```
 # Usage

For all kinds of media file upload (image/png/jpg/video/mp4/mp3/pdf/doc/apk...etc)
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
## Contributing

Contributions are always welcome! For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

## Author

- [@softsuave-tech-matrix](https://github.com/softsuave-tech-matrix)

## Feedback

If you have any feedback, please reach out to us at techmatrix@softsuave.com
