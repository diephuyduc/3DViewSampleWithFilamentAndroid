▶️ [Click to watch the demo video](https://github.com/user-attachments/assets/f1ed116b-1da1-4177-aed8-a8bf52f356ab)

### **FILAMENT**
#### *Play Animation* 
```kotlin
    private fun playAnimation(index: Int, isLoop: Boolean = true) {
        carLiftAnimationIndex = index
        loopAnimation = isLoop
        carLiftAnimationStartTime = System.nanoTime()
    
        val duration = carLiftModelViewer.animator?.getAnimationDuration(index) ?: 0f
        Toast.makeText(this, "Playing animation ($duration seconds)", Toast.LENGTH_SHORT).show()
    }
   ```
   #### *Update Animation (Frame Callback)*
   ```kotlin
   private val frameCallback = object : Choreographer.FrameCallback {
    override fun doFrame(currentTime: Long) {
        val elapsedSeconds = (currentTime - carLiftAnimationStartTime).toDouble() / 1_000_000_000
        choreographer.postFrameCallback(this)

        carLiftModelViewer.animator?.apply {
            if (animationCount > 0) {
                val duration = getAnimationDuration(carLiftAnimationIndex)
                val timeInAnim = (elapsedSeconds * carLiftAnimationSpeed % duration)
                applyAnimation(carLiftAnimationIndex, timeInAnim.toFloat())  // Update index to change animation
            }
        }

        carLiftModelViewer.render(currentTime)
    }
}
```
### SceneView
#### *Play animation:*
```kotlin
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
```
