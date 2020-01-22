package com.example.baseapp2.controller

import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.MathHelper
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.*

class ScaleController(
    transformableNode: BaseTransformableNode,
    gestureRecognizer: PinchGestureRecognizer
) :
    BaseTransformationController<PinchGesture>(transformableNode, gestureRecognizer) {

    var minScale = DEFAULT_MIN_SCALE
    var maxScale = DEFAULT_MAX_SCALE
    var sensitivity = DEFAULT_SENSITIVITY
    var elasticity = DEFAULT_ELASTICITY

    private var currentScaleRatio: Float = 0.toFloat()

    private val scaleDelta: Float
        get() {
            val scaleDelta = maxScale - minScale

            if (scaleDelta <= 0.0f) {
                throw IllegalStateException("maxScale must be greater than minScale.")
            }

            return scaleDelta
        }

    private val clampedScaleRatio: Float
        get() = Math.min(1.0f, Math.max(0.0f, currentScaleRatio))

    private val finalScale: Float
        get() {
            val elasticScaleRatio = clampedScaleRatio + elasticDelta
            return minScale + elasticScaleRatio * scaleDelta
        }

    private val elasticDelta: Float
        get() {
            val overRatio: Float
            if (currentScaleRatio > 1.0f) {
                overRatio = currentScaleRatio - 1.0f
            } else if (currentScaleRatio < 0.0f) {
                overRatio = currentScaleRatio
            } else {
                return 0.0f
            }

            return (1.0f - 1.0f / (Math.abs(overRatio) * elasticity + 1.0f)) * Math.signum(overRatio)
        }

    override fun onActivated(node: Node?) {
        super.onActivated(node)
        val scale = transformableNode.localScale
        currentScaleRatio = (scale.x - minScale) / scaleDelta
    }

    override fun onUpdated(node: Node?, frameTime: FrameTime?) {
        if (isTransforming) {
            return
        }

        val t = MathHelper.clamp(frameTime!!.deltaSeconds * LERP_SPEED, 0f, 1f)
        currentScaleRatio = MathHelper.lerp(currentScaleRatio, clampedScaleRatio, t)
        val finalScaleValue = finalScale
        val finalScale = Vector3(finalScaleValue, finalScaleValue, finalScaleValue)
        transformableNode.localScale = finalScale
    }

    public override fun canStartTransformation(gesture: PinchGesture): Boolean {
        return transformableNode.isSelected
    }

    fun getScaleRatio(): Float {
        return currentScaleRatio
    }

    fun updateScaleRatio(ratio: Float) {
        currentScaleRatio = ratio
    }

    public override fun onContinueTransformation(gesture: PinchGesture) {
        currentScaleRatio += gesture.gapDeltaInches() * sensitivity

        val finalScaleValue = finalScale
        val finalScale = Vector3(finalScaleValue, finalScaleValue, finalScaleValue)
        transformableNode.localScale = finalScale

        if (currentScaleRatio < -ELASTIC_RATIO_LIMIT || currentScaleRatio > 1.0f + ELASTIC_RATIO_LIMIT) {
            gesture.wasCancelled()
        }
    }

    public override fun onEndTransformation(gesture: PinchGesture) {}

    companion object {
        const val DEFAULT_MIN_SCALE = 0.2f
        const val DEFAULT_MAX_SCALE = 1.75f
        const val DEFAULT_SENSITIVITY = 0.75f
        const val DEFAULT_ELASTICITY = 0.15f

        private const val ELASTIC_RATIO_LIMIT = 0.8f
        private const val LERP_SPEED = 8.0f
    }


}