package com.example.baseapp2

import com.example.baseapp2.controller.DragRotationController
import com.example.baseapp2.controller.ScaleController
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem

class DragTransformableNode(transformationSystem: TransformationSystem) :
    TransformableNode(transformationSystem) {

    private val dragRotationController = DragRotationController(this, transformationSystem.dragRecognizer)

    private val myScaleController = ScaleController(this, transformationSystem.pinchRecognizer)

    init {
        translationController.isEnabled = true
        rotationController.isEnabled=true
        scaleController.isEnabled=true
        removeTransformationController(translationController)
        removeTransformationController(rotationController)
        addTransformationController(dragRotationController)
        addTransformationController(myScaleController)

    }
}