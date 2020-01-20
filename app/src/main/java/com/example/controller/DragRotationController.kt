package com.example.controller

import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.BaseTransformationController
import com.google.ar.sceneform.ux.DragGesture
import com.google.ar.sceneform.ux.DragGestureRecognizer

class DragRotationController(transformableNode: BaseTransformableNode, gestureRecognizer: DragGestureRecognizer) :
    BaseTransformationController<DragGesture>(transformableNode, gestureRecognizer) {

    // Rate that the node rotates in degrees per degree of twisting.
    var rotationRateDegrees = 0.5f

    override fun onEndTransformation(gesture: DragGesture?) {

    }

    override fun onContinueTransformation(gesture: DragGesture?) {
        var localRotation = transformableNode.localRotation

        val rotationAmountX = gesture?.delta!!.x * rotationRateDegrees
        val rotationDeltaX = Quaternion(Vector3.up(), rotationAmountX)
        localRotation = Quaternion.multiply(localRotation, rotationDeltaX)

        transformableNode.localRotation = localRotation
    }

    override fun canStartTransformation(gesture: DragGesture?): Boolean {
        return transformableNode.isSelected
    }
}