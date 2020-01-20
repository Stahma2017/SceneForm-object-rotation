package com.example.baseapp2

import com.example.controller.DragRotationController
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem

class DragTransformableNode(transformationSystem: TransformationSystem) :
    TransformableNode(transformationSystem) {

    private val dragRotationController = DragRotationController(
        this,
        transformationSystem.dragRecognizer
    )

    init {
        translationController.isEnabled = false
        removeTransformationController(translationController)
        removeTransformationController(rotationController)
        addTransformationController(dragRotationController)
    }
}