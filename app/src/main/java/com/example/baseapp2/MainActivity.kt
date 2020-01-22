package com.example.baseapp2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.max


class MainActivity : AppCompatActivity() {

    lateinit var scene: Scene
    lateinit var transformableNode: TransformableNode
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            placeModel()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            placeModel()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("You must turn storage permission on to use this feature")
                    .setTitle("Attention!")
                val dialog = builder.create()
                dialog.show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Go to the settings and switch storage permission on to use this feature")
                    .setTitle("Error!")
                val dialog = builder
                    .setNegativeButton("Cancel") { dialog, which ->  dialog.dismiss() }
                    .setPositiveButton("Ok") { dialog, which -> passUserToAppSettings() }
                    .create()
                dialog.show()
            }
        }
    }

    private fun passUserToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    private fun placeModel() {
        if (Utils.copyAssetFolder(this.assets, "subs", "/data/data/com.example.baseapp2/files")) {
            currentTransformationSystem =
                renderObject("file:///data/data/com.example.baseapp2/files/subs.gltf")
        }
    }

    private fun addNodeToScene(model: ModelRenderable?) {
        transformableNode = DragTransformableNode(currentTransformationSystem).apply {
            renderable = model
            localPosition = Vector3(0f, -0.4f, -1f)
            scaleController.isEnabled = false
            setParent(scene)
        }

        val scaleFactor = getLocalScaleToFitSceen((transformableNode.collisionShape as Box).size)
        transformableNode.worldScale = Vector3(scaleFactor, scaleFactor, scaleFactor)
        scene.addChild(transformableNode)

     }

    private fun getLocalScaleToFitSceen(size: Vector3): Float {
        val maxExtent = max(size.x, max(size.y, size.z))
        val targetSize = 1f
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
