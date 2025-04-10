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

    // UI components
    private lateinit var carLiftSurfaceView: SurfaceView
    private lateinit var btnDown: Button
    private lateinit var btnIdleFinal: Button
    private lateinit var btnIdleMid: Button
    private lateinit var btnIdUp: Button
    private lateinit var btnSpeedUp: Button
    private lateinit var btnSpeedDown: Button

    // Filament ModelViewer & Choreographer
    private lateinit var choreographer: Choreographer
    private lateinit var carLiftModelViewer: ModelViewer

    // Animation control variables
    private var carLiftAnimationIndex = 0
    private var carLiftAnimationSpeed = 1
    private var carLiftAnimationStartTime = 0L
    private var loopAnimation = false

    // Animation indexes (hardcoded)
    private val ANIM_DOWN = 0
    private val ANIM_IDLE_FINAL = 1
    private val ANIM_IDLE_MID = 2
    private val ANIM_UP = 3

    // Frame callback for animation update
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(currentTime: Long) {
            val elapsedSeconds = (currentTime - carLiftAnimationStartTime).toDouble() / 1_000_000_000
            choreographer.postFrameCallback(this)

            carLiftModelViewer.animator?.apply {
                if (animationCount > 0) {
                    val duration = getAnimationDuration(carLiftAnimationIndex)
                    if (duration != 0f) {
                        val timeInAnim = if (loopAnimation) {
                            (elapsedSeconds * carLiftAnimationSpeed % duration)
                        } else {
                            (elapsedSeconds * carLiftAnimationSpeed).coerceAtMost(duration.toDouble())
                        }

                        applyAnimation(carLiftAnimationIndex, timeInAnim.toFloat())
                    }
                }
                updateBoneMatrices()
            }

            carLiftModelViewer.render(currentTime)
        }
    }

    // Entry point
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupSystemInsets()
        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnimationLoop()
    }

    // Setup system insets (padding for status bar, etc.)
    private fun setupSystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Initialize all UI and model-related views
    private fun initViews() {
        // Bind views
        carLiftSurfaceView = findViewById(R.id.carLiftSurfaceView)
        btnDown = findViewById(R.id.btnDown)
        btnIdleFinal = findViewById(R.id.idleFinal)
        btnIdleMid = findViewById(R.id.idleMid)
        btnIdUp = findViewById(R.id.idUp)
        btnSpeedUp = findViewById(R.id.speedUp)
        btnSpeedDown = findViewById(R.id.speedDown)

        // Assign button listeners
        btnDown.setOnClickListener { playAnimation(ANIM_DOWN) }
        btnIdleFinal.setOnClickListener { playAnimation(ANIM_IDLE_FINAL) }
        btnIdleMid.setOnClickListener { playAnimation(ANIM_IDLE_MID) }
        btnIdUp.setOnClickListener { playAnimation(ANIM_UP) }

        btnSpeedUp.setOnClickListener { carLiftAnimationSpeed *= 2 }
        btnSpeedDown.setOnClickListener { carLiftAnimationSpeed /= 2 }

        initModelViewer()
    }

    // Initialize ModelViewer and environment
    @SuppressLint("ClickableViewAccessibility")
    private fun initModelViewer() {
        choreographer = Choreographer.getInstance()
        carLiftModelViewer = ModelViewer(carLiftSurfaceView)
        carLiftSurfaceView.setOnTouchListener(carLiftModelViewer)

        loadGLBFile("car_lift")
        loadEnvironment("venetian_crossroads_2k")

        choreographer.postFrameCallback(frameCallback)
    }

    // Play selected animation
    private fun playAnimation(index: Int, isLoop: Boolean = true) {
        carLiftAnimationIndex = index
        loopAnimation = isLoop
        carLiftAnimationStartTime = System.nanoTime()

        val duration = carLiftModelViewer.animator?.getAnimationDuration(index) ?: 0f
        val name = carLiftModelViewer.animator?.getAnimationName(index)

        if (duration == 0f) {
            Toast.makeText(this, "Animation '$name' has zero duration.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Playing '$name' ($duration seconds)", Toast.LENGTH_SHORT).show()
        }
    }

    // Stop rendering animation
    private fun stopAnimationLoop() {
        choreographer.removeFrameCallback(frameCallback)
    }

    // Load GLB model from assets
    private fun loadGLBFile(name: String) {
        val buffer = readAsset("models/$name.glb")
        carLiftModelViewer.loadModelGlb(buffer)
        carLiftModelViewer.transformToUnitCube()
        carLiftSurfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    // Load environment lighting and skybox
    private fun loadEnvironment(ibl: String) {
        var buffer = readAsset("envs/$ibl/${ibl}_ibl.ktx")
        KTX1Loader.createIndirectLight(carLiftModelViewer.engine, buffer).apply {
            intensity = 50_000f
            carLiftModelViewer.scene.indirectLight = this
        }

        buffer = readAsset("envs/$ibl/${ibl}_skybox.ktx")
        KTX1Loader.createSkybox(carLiftModelViewer.engine, buffer).apply {
            carLiftModelViewer.scene.skybox = this
        }
    }

    // Helper: Read asset file into ByteBuffer
    private fun readAsset(assetName: String): ByteBuffer {
        val input = assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }
}
