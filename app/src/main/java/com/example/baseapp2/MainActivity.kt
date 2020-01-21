package com.example.baseapp2

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.baseapp2.utils.Utils
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.max


class MainActivity : AppCompatActivity() {

    lateinit var scene: Scene
    lateinit var transformableNode: DragTransformableNode
    lateinit var currentTransformationSystem: TransformationSystem

    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scene = sceneView.scene // get current scene

        scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
            if (this::currentTransformationSystem.isInitialized)
                currentTransformationSystem.onTouch(hitTestResult, motionEvent)
        }

        placeModel.setOnClickListener {
            checkPermission()
        }

        showModel.setOnClickListener {
            currentTransformationSystem =
                renderObject("file:///data/data/com.example.baseapp2/files/kito_sb_2309.gltf")
        }
    }

    private fun renderObject(pathToModel: String): TransformationSystem {
        val selectionVisualizer = FootprintSelectionVisualizer()
        ModelRenderable.builder()
            .setSource(
                this,
                RenderableSource.builder().setSource(
                    this,
                    Uri.parse(pathToModel),
                    RenderableSource.SourceType.GLTF2
                )
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()
            )
            .build()
            .thenAccept {
                if (selectionVisualizer.footprintRenderable == null) {
                    selectionVisualizer.footprintRenderable = it
                }
                addNodeToScene(it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("error!")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
        return TransformationSystem(resources.displayMetrics, selectionVisualizer)
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE
            )
            //     }
        } else {
            placeModel()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            placeModel()
        }
    }

    private fun placeModel() {
        Utils.copyAssetFolder(this.assets, "kito", "/data/data/com.example.baseapp2/files")
    }

    private fun addNodeToScene(model: ModelRenderable?) {
        model?.let {
            transformableNode = DragTransformableNode(currentTransformationSystem).apply {
                rotationController.isEnabled = true
                scaleController.isEnabled = true
                translationController.isEnabled = false
                setParent(scene)
                localPosition = Vector3(0f, -0.8f, -1.4f)
                name = "Model"
                renderable = it
            }

            transformableNode.localScale = Vector3(getLocalScaleToFitSceen((transformableNode.collisionShape as Box).size), getLocalScaleToFitSceen((transformableNode.collisionShape as Box).size), getLocalScaleToFitSceen((transformableNode.collisionShape as Box).size))
            scene.addChild(transformableNode)
        }

        val height = (transformableNode.renderable!!.collisionShape as Box).size.y
    }

    private fun getLocalScaleToFitSceen(size: Vector3): Float {
        val maxExtent = max(size.x, max(size.y, size.z))
        val targetSize = 0.3f
        return targetSize / maxExtent
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }
}
