package com.example.a3dmodelsample

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Choreographer
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var btnDown: Button
    private lateinit var btnIdleFinal: Button
    private lateinit var btnIdleMid: Button
    private lateinit var btnIdUp: Button
    private lateinit var btnSpeedUp: Button
    private lateinit var btnSpeedDown: Button

    private lateinit var choreographer: Choreographer

    private lateinit var carLiftModelViewer: ModelViewer
    private lateinit var carLiftSurfaceView: SurfaceView

    private var carLiftAnimationCount = 0
    private var carLiftAnimationSpeed = 1
    private var carLiftAnimationIndex = 0
    private var carLiftAnimationStartTime = 0L
    private var loopAnimation = false

    private var ANIMTION_DOWN_INDEX = 0 // down
    private var ANIMTION_IDLE_FINAL_INDEX = 1 // idle final
    private var ANIMTION_IDLE_MID_INDEX = 2 // idle mid
    private var ANIMTION_UP_INDEX = 3 // up

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(currentTime: Long) {
            val seconds = (currentTime - carLiftAnimationStartTime).toDouble() / 1_000_000_000
            choreographer.postFrameCallback(this)

            carLiftModelViewer.animator?.apply {
                if (animationCount > 0) {
                    val duration = getAnimationDuration(carLiftAnimationIndex)
                    if (duration == 0f) {
                    } else {
                        // ➕ Loop logic
                        val loopedSeconds = if (loopAnimation) {
                            (seconds * carLiftAnimationSpeed % duration)
                        } else {
                            (seconds * carLiftAnimationSpeed).coerceAtMost(duration.toDouble())
                        }

                        applyAnimation(carLiftAnimationIndex, loopedSeconds.toFloat())
                    }
                }
                updateBoneMatrices()
            }

            carLiftModelViewer.render(currentTime)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        initViews()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        freezeCarlift()
    }

    private fun initViews() {
        carLiftSurfaceView = findViewById(R.id.carLiftSurfaceView)
        btnDown = findViewById(R.id.btnDown)
        btnIdleFinal = findViewById(R.id.idleFinal)
        btnIdleMid = findViewById(R.id.idleMid)
        btnIdUp = findViewById(R.id.idUp)
        btnSpeedUp = findViewById(R.id.speedUp)
        btnSpeedDown = findViewById(R.id.speedDown)
        btnDown.setOnClickListener {
            playAnimation(ANIMTION_DOWN_INDEX)
        }
        btnIdUp.setOnClickListener {
            playAnimation(ANIMTION_UP_INDEX)
        }
        btnIdleFinal.setOnClickListener {
            playAnimation(ANIMTION_IDLE_FINAL_INDEX)
        }
        btnIdleMid.setOnClickListener {
            playAnimation(ANIMTION_IDLE_MID_INDEX)
        }

        btnSpeedUp.setOnClickListener {
            carLiftAnimationSpeed *= 2
        }
        btnSpeedDown.setOnClickListener {
            carLiftAnimationSpeed /= 2
        }

        initCarliftView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initCarliftView() {
        choreographer = Choreographer.getInstance()
        carLiftModelViewer = ModelViewer(carLiftSurfaceView)
        carLiftSurfaceView.setOnTouchListener(carLiftModelViewer)
        loadGLBFile("car_lift")
        loadEnvironment("venetian_crossroads_2k")
        choreographer.postFrameCallback(frameCallback)

    }

    private fun playAnimation(index: Int, isLoop: Boolean = true) {
        carLiftAnimationIndex = index
        loopAnimation = isLoop
        carLiftAnimationStartTime = System.nanoTime()
        val duration = carLiftModelViewer.animator?.getAnimationDuration(carLiftAnimationIndex)
        if (duration == 0f) {
            Toast.makeText(
                this@MainActivity,
                "Can not visible animation with Animation name: ${
                    carLiftModelViewer.animator?.getAnimationName(carLiftAnimationIndex)
                } with duration $duration",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Animation name: ${
                    carLiftModelViewer.animator?.getAnimationName(carLiftAnimationIndex)
                } with duration $duration",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun freezeCarlift() {
        choreographer.removeFrameCallback(frameCallback)
    }

    private fun loadGLBFile(name: String) {
        val buffer = readAsset("models/${name}.glb")
        carLiftModelViewer.loadModelGlb(buffer)
        carLiftModelViewer.transformToUnitCube()
        carLiftSurfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = this.assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private fun loadEnvironment(ibl: String) {
        // Create the indirect light source and add it to the scene.
        var buffer = readAsset("envs/$ibl/${ibl}_ibl.ktx")
        KTX1Loader.createIndirectLight(carLiftModelViewer.engine, buffer).apply {
            intensity = 50_000f
            carLiftModelViewer.scene.indirectLight = this
        }

        // Create the sky box and add it to the scene.
        buffer = readAsset("envs/$ibl/${ibl}_skybox.ktx")
        KTX1Loader.createSkybox(carLiftModelViewer.engine, buffer).apply {
            carLiftModelViewer.scene.skybox = this
        }
    }
}