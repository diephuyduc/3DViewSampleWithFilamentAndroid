▶️ [Click to watch the demo video](https://github.com/user-attachments/assets/f1ed116b-1da1-4177-aed8-a8bf52f356ab)
### 5. Play Animation

Control animations with buttons:

```kotlin
private fun playAnimation(index: Int, isLoop: Boolean = true) {
    carLiftAnimationIndex = index
    loopAnimation = isLoop
    carLiftAnimationStartTime = System.nanoTime()

    val duration = carLiftModelViewer.animator?.getAnimationDuration(index) ?: 0f
    Toast.makeText(this, "Playing animation ($duration seconds)", Toast.LENGTH_SHORT).show()
}

### 6. Update Animation (Frame Callback)

```kotlin
private val frameCallback = object : Choreographer.FrameCallback {
    override fun doFrame(currentTime: Long) {
        val elapsedSeconds = (currentTime - carLiftAnimationStartTime).toDouble() / 1_000_000_000
        choreographer.postFrameCallback(this)

        carLiftModelViewer.animator?.apply {
            if (animationCount > 0) {
                val duration = getAnimationDuration(carLiftAnimationIndex)
                val timeInAnim = (elapsedSeconds * carLiftAnimationSpeed % duration)
                applyAnimation(carLiftAnimationIndex, timeInAnim.toFloat())
            }
        }

        carLiftModelViewer.render(currentTime)
    }
}

