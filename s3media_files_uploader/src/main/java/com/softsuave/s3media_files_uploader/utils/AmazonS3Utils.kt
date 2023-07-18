package com.softsuave.s3media_files_uploader.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.amazonaws.HttpMethod
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class AmazonS3Utils {
    var sCredProvider: CognitoCachingCredentialsProvider? = null
    var sTransferUtility: TransferUtility? = null
    var sS3Client: AmazonS3Client? = null

    protected fun getCredProvider(
        context: Context?,
        poolId: String?,
        region: Regions?
    ): CognitoCachingCredentialsProvider? {
        if (sCredProvider == null) {
            sCredProvider =
                CognitoCachingCredentialsProvider(
                    context,
                    poolId,  // Identity Pool ID
                    region // Region
                )
        }
        return sCredProvider
    }

    fun getTransferUtility(context: Context, poolId: String?, regions: Regions?): TransferUtility? {
        if (sTransferUtility == null) {
            sTransferUtility =
                TransferUtility(
                    getS3Client(
                        context.applicationContext,
                        poolId,
                        regions
                    ),
                    context.applicationContext
                )
        }
        return sTransferUtility
    }

    fun getS3Client(context: Context?, poolId: String?, regions: Regions?): AmazonS3Client? {
        if (sS3Client == null) {
            sS3Client = AmazonS3Client(
                getCredProvider(
                    context,
                    poolId,
                    regions
                )
            )
            sS3Client?.setRegion(
                Region.getRegion(
                    regions
                )
            )
        }
        return sS3Client
    }

    /**
     * Copies the data from the passed in Uri, to a new file for use with the
     */
    @SuppressLint("Recycle")
    @Throws(IOException::class)
    fun copyContentUriToFile(context: Context, uri: Uri?): File {
        val `is` = context.contentResolver.openInputStream(uri!!)
        val copiedData = File(
            context.getDir("SampleImagesDir", Context.MODE_PRIVATE), UUID
                .randomUUID().toString()
        )
        copiedData.createNewFile()
        val fos = FileOutputStream(copiedData)
        val buf = ByteArray(2046)
        var read = -1
        while (`is`!!.read(buf).also { read = it } != -1) {
            fos.write(buf, 0, read)
        }
        fos.flush()
        fos.close()
        return copiedData
    }

    /**
     * Converts number of bytes into proper scale.
     * @param bytes number of bytes to be converted.
     * @return A string that represents the bytes in a proper scale.
     */

    fun getBytesString(bytes: Long): String {
        val quantifiers = arrayOf(
            "KB", "MB", "GB", "TB"
        )
        var speedNum = bytes.toDouble()
        var i = 0
        while (true) {
            if (i >= quantifiers.size) {
                return ""
            }
            speedNum /= 1024.0
            if (speedNum < 512) {
                return String.format("%.2f", speedNum) + " " + quantifiers[i]
            }
            i++
        }
    }

    fun getFilePathFromURI(selectedImageUri: Uri?, context: Context): String? {
        var filePath = ""
        val wholeID = DocumentsContract.getDocumentId(selectedImageUri)

        // Split at colon, use second item in the array
        val id = wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val column = arrayOf(MediaStore.Images.Media.DATA)

        // where id is equal to
        val sel = MediaStore.Images.Media._ID + "=?"
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column, sel, arrayOf(id), null
        )!!
        val columnIndex = cursor.getColumnIndex(column[0])
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex)
        }
        cursor.close()
        return filePath
    }

    fun getUrl(bucket: String?, objectKey: String?, credentials: BasicAWSCredentials): String? {
        val newObjectKey =
            if (objectKey?.contains("https://") == true) objectKey.replace("https://", "")
                .substringAfter("/")
            else {
                objectKey
            }
        if (credentials != null) {

            val sS3Client = AmazonS3Client(credentials, Region.getRegion(Regions.US_EAST_1))
            val generateResignedUrlRequest = GeneratePresignedUrlRequest(bucket, newObjectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(Date(Date().time + 1000  *60 * 15))
            val urls = sS3Client.generatePresignedUrl(generateResignedUrlRequest)
            return urls.toString()
        }
        return newObjectKey
    }
}