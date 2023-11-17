package com.revolage.quzbus.ui.camera

import android.view.animation.AccelerateDecelerateInterpolator
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions.Companion.cameraAnimatorOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener

class CameraController(private val mapView: MapView) {

    fun cameraPosition(point: Point?) {
        val cameraPosition = CameraOptions.Builder()
            .center(point)
            .zoom(8.0)
            .build()

        mapView.getMapboxMap().setCamera(cameraPosition)
        mapViewAnimation()
    }

    fun listenCameraChange(onZoomChanged: (Double) -> Unit) {
        val listener = OnCameraChangeListener { _: CameraChangedEventData ->
            val zoom = mapView.getMapboxMap().cameraState.zoom
            onZoomChanged(zoom)
        }
        mapView.getMapboxMap().addOnCameraChangeListener(listener)
    }

    private fun mapViewAnimation() {
        mapView.camera.apply {
            val zoom = createZoomAnimator(
                cameraAnimatorOptions(12.0) {
                    startValue(3.0)
                }
            ) {
                duration = 3000
                interpolator = AccelerateDecelerateInterpolator()
            }
            playAnimatorsSequentially(zoom)
        }
    }
}
