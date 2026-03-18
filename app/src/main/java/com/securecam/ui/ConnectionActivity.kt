package com.securecam.ui
import android.content.Intent; import android.os.Bundle; import android.view.View
import androidx.appcompat.app.AppCompatActivity; import com.securecam.camera.CameraActivity; import com.securecam.databinding.ActivityConnectionBinding
import com.securecam.utils.AppPreferences; import com.securecam.viewer.ViewerActivity; import java.util.UUID
class ConnectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectionBinding
    private var mode = MODE_CAMERA; private var connectionType = TYPE_WEBRTC
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); binding = ActivityConnectionBinding.inflate(layoutInflater); setContentView(binding.root)
        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_CAMERA
        val pre = intent.getStringExtra(EXTRA_ROOM_CODE) ?: ""
        if (pre.isNotEmpty()) binding.etRoomCode.setText(pre)
        binding.tvTitle.text = if(mode==MODE_CAMERA) "📷 Camera Mode" else "👁️ Viewer Mode"
        binding.tvSubtitle.text = if(mode==MODE_CAMERA) "This phone will stream video" else "This phone will receive video"
        binding.btnGenerate.setOnClickListener { binding.etRoomCode.setText(UUID.randomUUID().toString().take(8).uppercase()) }
        binding.rgConnectionType.setOnCheckedChangeListener { _, id ->
            connectionType = if(id==binding.rbWebRTC.id) TYPE_WEBRTC else TYPE_WEBSOCKET
            val ws = connectionType==TYPE_WEBSOCKET
            binding.tvServerUrlLabel.visibility = if(ws) View.VISIBLE else View.GONE
            binding.etServerUrl.visibility = if(ws) View.VISIBLE else View.GONE
            binding.tvSignalingNote.visibility = if(ws) View.GONE else View.VISIBLE
        }
        binding.rbWebRTC.isChecked = true
        binding.btnStart.setOnClickListener {
            val room = binding.etRoomCode.text.toString().trim()
            if(room.isEmpty()) { binding.etRoomCode.error="Enter room code"; return@setOnClickListener }
            AppPreferences.lastRoomCode = room
            val intent = if(mode==MODE_CAMERA) Intent(this,CameraActivity::class.java) else Intent(this,ViewerActivity::class.java)
            intent.putExtra(EXTRA_MODE,mode).putExtra(EXTRA_ROOM_CODE,room)
                .putExtra(EXTRA_CONNECTION_TYPE,connectionType)
                .putExtra(EXTRA_SERVER_URL, binding.etServerUrl.text.toString().ifEmpty { DEFAULT_SIGNALING_URL })
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener { finish() }
    }
    companion object {
        const val MODE_CAMERA="camera"; const val MODE_VIEWER="viewer"
        const val TYPE_WEBRTC="webrtc"; const val TYPE_WEBSOCKET="websocket"
        const val EXTRA_MODE="extra_mode"; const val EXTRA_ROOM_CODE="extra_room_code"
        const val EXTRA_CONNECTION_TYPE="extra_connection_type"; const val EXTRA_SERVER_URL="extra_server_url"
        const val DEFAULT_SIGNALING_URL="wss://your-signaling-server.com"
    }
}