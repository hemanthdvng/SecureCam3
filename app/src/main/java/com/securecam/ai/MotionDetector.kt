package com.securecam.ai
import android.graphics.Bitmap; import android.graphics.Color; import android.util.Log
import kotlinx.coroutines.*; import kotlin.math.abs
class MotionDetector(private val sensitivity: Int=30, private val listener: MotionListener) {
    private var previousFrame: IntArray? = null; private var isProcessing = false
    private val diffThreshold get() = (255*(1.0-sensitivity/100.0)*0.5+10).toInt()
    private val motionThreshold get() = 0.02+(100-sensitivity)/100.0*0.08
    private var lastMotionTime = 0L; private val cooldown = 2000L
    private val W=160; private val H=90
    private val scope = CoroutineScope(Dispatchers.Default+SupervisorJob())
    fun processFrame(bitmap: Bitmap) {
        if(isProcessing) return; isProcessing=true
        scope.launch {
            val sc = Bitmap.createScaledBitmap(bitmap,W,H,false)
            val px = IntArray(W*H); sc.getPixels(px,0,W,0,0,W,H); sc.recycle()
            val prev = previousFrame
            if(prev!=null && prev.size==px.size) {
                var changed=0
                for(i in prev.indices){val d=(abs(Color.red(prev[i])-Color.red(px[i]))+abs(Color.green(prev[i])-Color.green(px[i]))+abs(Color.blue(prev[i])-Color.blue(px[i])))/3;if(d>diffThreshold)changed++}
                val score=changed.toFloat()/prev.size
                withContext(Dispatchers.Main){
                    if(score>motionThreshold){val now=System.currentTimeMillis();if(now-lastMotionTime>cooldown){lastMotionTime=now;listener.onMotionDetected(score, emptyList())}}
                    else listener.onMotionScore(score)
                }
            }
            previousFrame=px; isProcessing=false
        }
    }
    fun reset(){previousFrame=null}; fun release(){scope.cancel();reset()}
    data class MotionRegion(val col:Int,val row:Int,val score:Float)
    interface MotionListener{fun onMotionDetected(score:Float,regions:List<MotionRegion>);fun onMotionScore(score:Float)}
}