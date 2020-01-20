package com.example.baseapp2

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    lateinit var scene: Scene
    lateinit var modelNode: Node
    lateinit var transformableNode: DragTransformableNode
    private var transformationSystem: TransformationSystem? = null


    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1001
    private val dest = "/data/data/com.example.baseapp2/files"
    var gltdModelName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scene = sceneView.scene // get current scene

      //  transformationSystem = makeTransformationSystem()

        scene.setOnTouchListener { hitTestResult, motionEvent ->

            false
        }

        placeModel.setOnClickListener {
            checkPermission()
        }

        showModel.setOnClickListener {
            renderObject("file:///data/data/com.example.baseapp2/files/kito_sb_2309.gltf")
        }

    }



    protected fun makeTransformationSystem(): TransformationSystem? {
        val selectionVisualizer = FootprintSelectionVisualizer()
        val transformationSystem =
            TransformationSystem(resources.displayMetrics, selectionVisualizer)
        ModelRenderable.builder()
            .setSource(this, com.google.ar.sceneform.ux.R.raw.sceneform_footprint)
            .build()
            .thenAccept { renderable: ModelRenderable? ->
                // If the selection visualizer already has a footprint renderable, then it was set to
                // something custom. Don't override the custom visual.
                if (selectionVisualizer.footprintRenderable == null) {
                    selectionVisualizer.setFootprintRenderable(renderable)
                }
            }
            .exceptionally { throwable: Throwable? ->
                val toast = Toast.makeText(
                    this, "Unable to load footprint renderable", Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            }
        return transformationSystem
    }

    private fun renderObject(pathToModel: String) {
        val selectionVisualizer = FootprintSelectionVisualizer()

        val mtransformationSystem =
            TransformationSystem(resources.displayMetrics, selectionVisualizer)

        ModelRenderable.builder()
            .setSource(this, RenderableSource.builder().setSource(this, Uri.parse(pathToModel), RenderableSource.SourceType.GLTF2)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build())
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
       transformationSystem = mtransformationSystem
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
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
        copyAssetFolder(this.assets, "kito","/data/data/com.example.baseapp2/files")
    }

    private fun copyAssetFolder(
        assetManager: AssetManager,
        fromAssetPath: String, toPath: String
    ): Boolean {
        return try {
            val files = assetManager.list(fromAssetPath)
            File(toPath).mkdirs()
            var res = true
            for (file in files!!) {
                if (file.contains(".")) copyAsset(assetManager, "$fromAssetPath/$file", "$toPath/$file")
                else res and copyAssetFolder(assetManager,"$fromAssetPath/$file","$toPath/$file")
            }
            res
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun copyAsset(
        assetManager: AssetManager,
        fromAssetPath: String, toPath: String
    ): Boolean {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        return try {
            `in` = assetManager.open(fromAssetPath)
            File(toPath).createNewFile()
            out = FileOutputStream(toPath)
            copyFile(`in`, out)
            `in`.close()
            out.flush()
            out.close()
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    private fun addNodeToScene(model: ModelRenderable?) {
       /* model?.let {
            modelNode = Node().apply {
                setParent(scene)
                localPosition = Vector3(0f, -0.4f, -1f)
                localScale = Vector3(3f, 3f, 3f)
                name = "Model"
                renderable = it
            }
            scene.addChild(modelNode)
        }*/


        model?.let {
            transformableNode = DragTransformableNode(this.transformationSystem!!).apply {
                setParent(scene)
                localPosition = Vector3(0f, -0.4f, -1f)
                localScale = Vector3(0.2f, 0.2f, 0.2f)
                name = "Model"
                renderable = it
                this.transformationSystem.selectNode(this)
            }
            scene.addChild(transformableNode)
        }


       // modelNode.localScale = Vector3(0.5f, 0.5f, 0.5f)

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
