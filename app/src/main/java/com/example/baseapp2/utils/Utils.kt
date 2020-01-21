package com.example.baseapp2.utils

import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object Utils {

    fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    fun copyAsset(
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

    fun copyAssetFolder(
        assetManager: AssetManager,
        fromAssetPath: String, toPath: String
    ): Boolean {
        return try {
            val files = assetManager.list(fromAssetPath)
            File(toPath).mkdirs()
            var res = true
            for (file in files!!) {
                if (file.contains(".")) copyAsset(
                    assetManager,
                    "$fromAssetPath/$file",
                    "$toPath/$file"
                )
                else res and copyAssetFolder(assetManager, "$fromAssetPath/$file", "$toPath/$file")
            }
            res
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}