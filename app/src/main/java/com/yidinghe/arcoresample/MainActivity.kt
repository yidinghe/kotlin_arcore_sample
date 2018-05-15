package com.yidinghe.arcoresample

import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ux.ArFragment

import kotlinx.android.synthetic.main.activity_main.*

private var fragment: ArFragment? = null

private val pointer = PointerDrawable()
private var isTracking = false
private var isHitting = false

/*
FYI, we can put the sceneform models to two locations:
1. src/main/assets/andy.sfb
2. src/main/res/raw/andy.sfb
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        fragment?.let {
            it.arSceneView.scene.setOnUpdateListener { frameTime ->
                it.onUpdate(frameTime)
                this.onUpdate()
            }
        }
    }

    private fun onUpdate() {
        val trackingChanged = updateTracking()
        val contentView = findViewById<View>(android.R.id.content)
        if (trackingChanged) {
            if (isTracking) {
                contentView.overlay.add(pointer)
            } else {
                contentView.overlay.remove(pointer)
            }
            contentView.invalidate()
        }

        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                pointer.setEnabled(isHitting)
                contentView.invalidate()
            }
        }
    }

    private fun updateHitTest(): Boolean {
        val frame = fragment!!.arSceneView.arFrame
        val pt = getScreenCenter()
        val wasHitting = isHitting
        isHitting = false
        val hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
        hits.forEach {
            val trackable = it.trackable
            if (trackable is Plane && trackable.isPoseInPolygon(it.hitPose)) {
                isHitting = true
            }
        }
        return wasHitting != isHitting
    }

    private fun updateTracking(): Boolean {
        val frame = fragment!!.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame.camera.trackingState == TrackingState.TRACKING
        return isTracking != wasTracking
    }

    private fun getScreenCenter(): Point {
        val view = findViewById<View>(android.R.id.content)
        return Point(view.width / 2, view.height / 2)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
