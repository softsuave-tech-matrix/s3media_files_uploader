package com.softsuave.s3media_files_uploader

import android.content.Context
import android.text.TextUtils
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File

class S3MediaUploader :TransferListener {

    private var mObserverMap: MutableMap<Int, String?>? = null
    private var mTransferUtility: TransferUtility? = null
    private var urls: MutableList<String> = ArrayList()
    private var imageUploadListener: UploadListener? = null
    private var sS3Client: AmazonS3Client? = null
    private var sTransferUtility: TransferUtility? = null
    private var credentials: BasicSessionCredentials? = null


    fun uploadMediaFile(
        context: Context,
        filePath: String, // AmazonS3Utils().getFilePathFromURI
        AWSAccessKeyId: String,
        SecretAccessKey: String,
        sessionToken: String,
        bucketName:String,
        regions: Regions,
        aListener: UploadListener?
    ) {
        imageUploadListener = aListener
        val filename = filePath.substring(
            filePath.lastIndexOf('/') + 1
        )
        urls.add(filePath)
        credentials = BasicSessionCredentials(AWSAccessKeyId, SecretAccessKey, sessionToken)
        sS3Client = AmazonS3Client(credentials, Region.getRegion(regions))
        mTransferUtility = getTransferUtility(context, bucketName)
        mObserverMap = HashMap()
        urls.forEach {
            if (!TextUtils.isEmpty(it)) {
                mTransferUtility?.let { it1 ->
                    upload(
                        it1,
                        mObserverMap as HashMap<Int, String?>,
                        it,
                        filename,
                        bucketName
                    )
                }
            }
        }
    }

    private fun getTransferUtility(context: Context, bucketName: String): TransferUtility? {
        if (sTransferUtility == null) {
            sTransferUtility = TransferUtility.builder()
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .context(context.applicationContext)
                .s3Client(sS3Client)
                .defaultBucket(bucketName)
                .build()
        }
        return sTransferUtility
    }

    private fun upload(
        aUtility: TransferUtility,
        aObserverMap: MutableMap<Int, String?>,
        aSourceFile: String,
        s3fileName: String?,
        bucketName: String
    ) {
        val observer = aUtility.upload(
            bucketName,
            s3fileName,
            File(aSourceFile)
        )
        observer.setTransferListener(this)
        aObserverMap[observer.id] = s3fileName
    }

    override fun onStateChanged(id: Int, aTransferState: TransferState) {
        val s3FileName = mObserverMap!![id]
        if (aTransferState == TransferState.COMPLETED) {
            mObserverMap!!.remove(id)
            if (mObserverMap!!.isEmpty()) {
                finish(s3FileName)
            }
        } else if (aTransferState == TransferState.FAILED) {
            imageUploadListener!!.onUploadFailed()
        }
    }

    private fun finish(s3fileName: String?) {
        if (imageUploadListener != null) {
            imageUploadListener!!.onUploadCompleted(s3fileName)
        }
    }

    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
        val percentDone = (bytesCurrent.toFloat() / bytesTotal.toFloat() * 100).toInt()
        if (bytesCurrent != 0L) {
            imageUploadListener?.onProgressUpdate(id, percentDone)
        }

    }

    override fun onError(i: Int, e: Exception) {
        if (mObserverMap!!.isEmpty()) {
            return
        }
        mObserverMap!!.clear()
        stopAllTransfers()
        if (imageUploadListener != null) {
            imageUploadListener?.onUploadFailed()
        }
    }

    private fun stopAllTransfers() {
        if (mTransferUtility != null) {
            mTransferUtility!!.cancelAllWithType(TransferType.UPLOAD)
        }
    }

    interface UploadListener {
        fun onUploadFailed()
        fun onUploadCompleted(fileName: String?)
        fun onProgressUpdate(id: Int, current: Int) {}
    }
}