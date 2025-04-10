package com.example.a3dmodelsample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // UI components
    private lateinit var carliftSceneView: SceneView
    private lateinit var btnDown: Button
    private lateinit var btnIdleFinal: Button
    private lateinit var btnIdleMid: Button
    private lateinit var btnIdUp: Button
    private lateinit var btnSpeedUp: Button
    private lateinit var btnSpeedDown: Button
    // Animation indexes (hardcoded)
    private val ANIM_DOWN = 0
    private val ANIM_IDLE_FINAL = 1
    private val ANIM_IDLE_MID = 2
    private val ANIM_UP = 3

    private var carLiftAnimationSpeed = 1
    private var carLiftModelNode: ModelNode? = null
    private var carLiftAnimationIndex = 0

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
        carliftSceneView = findViewById(R.id.carliftSceneView)
        btnDown = findViewById(R.id.btnDown)
        btnIdleFinal = findViewById(R.id.idleFinal)
        btnIdleMid = findViewById(R.id.idleMid)
        btnIdUp = findViewById(R.id.idUp)
        btnSpeedUp = findViewById(R.id.speedUp)
        btnSpeedDown = findViewById(R.id.speedDown)

        // Assign button listeners
        btnDown.setOnClickListener {
            playAnimation(ANIM_DOWN)
            carLiftAnimationIndex = ANIM_DOWN
        }
        btnIdleFinal.setOnClickListener {
            playAnimation(ANIM_IDLE_FINAL)
            carLiftAnimationIndex = ANIM_IDLE_FINAL
        }
        btnIdleMid.setOnClickListener {
            playAnimation(ANIM_IDLE_MID)
            carLiftAnimationIndex = ANIM_IDLE_MID
        }
        btnIdUp.setOnClickListener {
            playAnimation(ANIM_UP)
            carLiftAnimationIndex = ANIM_UP
        }

        btnSpeedUp.setOnClickListener {
            carLiftAnimationSpeed *= 2
            playAnimation(carLiftAnimationIndex)
        }
        btnSpeedDown.setOnClickListener {
            carLiftAnimationSpeed /= 2

            playAnimation(carLiftAnimationIndex)
        }

        initModelViewer()
    }

    // Initialize ModelViewer and environment
    @SuppressLint("ClickableViewAccessibility")
    private fun initModelViewer() {
        lifecycleScope.launch {
            loadEnvironment()
            loadGLBFile("car_lift")

        }
    }

    // Play selected animation
    private fun playAnimation(index: Int, isLoop: Boolean = true) {
        carLiftModelNode?.playAnimation(index, carLiftAnimationSpeed.toFloat(), isLoop)

        val duration = carLiftModelNode?.animator?.getAnimationDuration(index) ?: 0f
        val name = carLiftModelNode?.animator?.getAnimationName(index)

        if (duration == 0f) {
            Toast.makeText(this, "Animation '$name' has zero duration.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Playing '$name' ($duration seconds)", Toast.LENGTH_SHORT).show()
        }
    }

    // Stop rendering animation
    private fun stopAnimationLoop() {/* choreographer.removeFrameCallback(frameCallback)*/
    }

    // Load GLB model from assets
    private fun loadGLBFile(name: String) {
        val modelFile = "models/car_lift.glb"
        val modelInstance = carliftSceneView.modelLoader.createModelInstance(modelFile)
        carLiftModelNode = ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = 2.0f,
        )
        carliftSceneView.addChildNode(carLiftModelNode!!)
    }

    // Load environment lighting and skybox
    private suspend fun loadEnvironment() {
        val hdrFile = "environments/studio_small_09_2k.hdr"
        carliftSceneView.environmentLoader.loadHDREnvironment(hdrFile).apply {
            carliftSceneView.indirectLight = this?.indirectLight
            carliftSceneView.skybox = this?.skybox
        }
        carliftSceneView.cameraNode.apply {
            position = Position(z = 1.0f)
        }
    }

}
