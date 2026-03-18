package com.securecam.ai
import android.graphics.Bitmap; import android.util.Log
import com.google.mlkit.vision.common.InputImage; import com.google.mlkit.vision.face.*
class FaceDetectionManager(private val listener: FaceDetectionListener) {
    private var detector: FaceDetector? = null; private var isProcessing=false; private var lastTime=0L; private val interval=300L
    private var lastFaceCount=0; private var lastAlertTime=0L; private val alertCooldown=10_000L
    fun initialize() {
        detector = FaceDetection.getClient(FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL).setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).setMinFaceSize(0.1f).enableTracking().build())
    }
    fun processFrame(bitmap: Bitmap) {
        val now=System.currentTimeMillis(); if(isProcessing||now-lastTime<interval) return; isProcessing=true; lastTime=now
        detector?.process(InputImage.fromBitmap(bitmap,0))?.addOnSuccessListener{faces->
            val results=faces.map{DetectedFace(it.trackingId?:-1,BoundingBox(it.boundingBox.left,it.boundingBox.top,it.boundingBox.right,it.boundingBox.bottom),it.smilingProbability?:-1f,it.leftEyeOpenProbability?:-1f,it.rightEyeOpenProbability?:-1f,it.headEulerAngleY,it.headEulerAngleZ)}
            if(results.isNotEmpty()){listener.onFacesDetected(results);if(lastFaceCount==0){val t=System.currentTimeMillis();if(t-lastAlertTime>alertCooldown){lastAlertTime=t;listener.onFaceAlert(results.size)}}}
            else listener.onNoFaceDetected()
            lastFaceCount=results.size; isProcessing=false
        }?.addOnFailureListener{isProcessing=false}
    }
    fun release(){detector?.close();detector=null}
    data class BoundingBox(val left:Int,val top:Int,val right:Int,val bottom:Int)
    data class DetectedFace(val trackingId:Int,val boundingBox:BoundingBox,val smilingProbability:Float,val leftEyeOpenProbability:Float,val rightEyeOpenProbability:Float,val headEulerAngleY:Float,val headEulerAngleZ:Float)
    interface FaceDetectionListener{fun onFacesDetected(faces:List<DetectedFace>);fun onNoFaceDetected();fun onFaceAlert(count:Int)}
}