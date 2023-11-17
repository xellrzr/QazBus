package com.revolage.quzbus.ui.annotation

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.revolage.quzbus.R
import com.revolage.quzbus.domain.models.routes.Pallet
import com.revolage.quzbus.domain.models.routes.Route
import javax.inject.Inject

class AnnotationManager @Inject constructor(
    annotationApi: AnnotationPlugin,
    private val resources: Resources,
    private val context: Context,
    private val getIconId: () -> Int
) {
    private val lines = hashMapOf<Pallet, PolylineAnnotationManager>()
    private val points = hashMapOf<Pallet, PointAnnotationManager>()
    private val pointsBuses = hashMapOf<Pallet, PointAnnotationManager>()
    private val busStops = hashMapOf<Pallet, PointAnnotationManager>()

    init {
        setupAnnotationManagers(annotationApi)
    }

    fun clearMap(){
        lines.values.forEach { it.deleteAll() }
        points.values.forEach { it.deleteAll() }
        pointsBuses.values.forEach{ it.deleteAll() }
        busStops.values.forEach{ it.deleteAll() }
    }

    //Отрисовка остановок
    fun drawStops(stops: List<Point>, pallet: Pallet) {
        val pointAnnotationManager = busStops[pallet]
        pointAnnotationManager?.deleteAll()
        if (stops.isEmpty()) return

        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_bus_stop)
        val pointsOptions = mutableListOf<PointAnnotationOptions>()
        for (stop in stops) {
            val options = PointAnnotationOptions()
                .withPoint(stop)
                .withIconImage(bitmap)
                .withIconSize(0.8)
            pointsOptions.add(options)
        }
        pointAnnotationManager?.create(pointsOptions)
    }

    fun drawBuses(busRoute: Route, pallet: Pallet) {
        val pointAnnotationManager = pointsBuses[pallet]
        pointAnnotationManager?.deleteAll()

        val directionBuses = busRoute.busPoints.filter { it.direction == busRoute.selectedDirection }
        val pointsB = directionBuses.map { Point.fromLngLat(it.pointA.x, it.pointA.y) }
        val pointsA = directionBuses.map { Point.fromLngLat(it.pointB.x, it.pointB.y) }

        if (pointsA.isEmpty()) return

        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, getIconId())

        val pointsOptions = mutableListOf<PointAnnotationOptions>()
        for (index in pointsA.indices) {
            val pointStart = pointsA[index]
            val pointFinish = pointsB[index]

            val loc1 = android.location.Location("")
            loc1.latitude = pointStart.latitude()
            loc1.longitude = pointStart.longitude()

            val loc2 = android.location.Location("")
            loc2.latitude = pointFinish.latitude()
            loc2.longitude = pointFinish.longitude()

            val direction = loc1.bearingTo(loc2) + 90f

            val rotatedBitMap = rotateBitmap(bitmap, direction)

            val pointOption = PointAnnotationOptions()
                .withPoint(pointStart)
                .withIconImage(rotatedBitMap)
                .withIconSize(1.2)

            pointsOptions.add(pointOption)
        }
        pointAnnotationManager?.create(pointsOptions)
    }

    fun drawRoute(points: List<Point>, pallet: Pallet) {
        val color = colorFor(pallet)
        val polyLineAnnotationManager = lines[pallet]
        polyLineAnnotationManager?.deleteAll()

        val options = mutableListOf<PolylineAnnotationOptions>()
        for (point in points) {
            val option = PolylineAnnotationOptions()
                .withPoints(points)
                .withLineColor(ContextCompat.getColor(context, color))
                .withLineWidth(4.5)
            options.add(option)
        }
        polyLineAnnotationManager?.create(options)
    }

    //Отрисовка конечной точки маршрута
    fun drawFlags(point: Point?, pallet: Pallet) {
        val pointAnnotationManager = points[pallet]
        pointAnnotationManager?.deleteAll()
        if (point == null) return

        val bitmap: Bitmap = BitmapFactory.decodeResource(resources,R.drawable.finish)

        val options = mutableListOf<PointAnnotationOptions>()
        val option = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(bitmap)
            .withIconSize(2.0)

        options.add(option)

        pointAnnotationManager?.create(options)
    }

    private fun setupAnnotationManagers(annotationApi: AnnotationPlugin) {
        Pallet.values().forEach { pallet ->
            lines[pallet] = annotationApi.createPolylineAnnotationManager()
            points[pallet] = annotationApi.createPointAnnotationManager()
            pointsBuses[pallet] = annotationApi.createPointAnnotationManager()
            busStops[pallet] = annotationApi.createPointAnnotationManager()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    //Выбор цвета маршрута
    private fun colorFor(pallet: Pallet): Int {
        return when(pallet) {
            Pallet.RED -> R.color.red
            Pallet.GREEN -> R.color.green
            Pallet.BLUE -> R.color.blue
            Pallet.YELLOW -> R.color.yellow
            Pallet.PURPLE -> R.color.cyan
            Pallet.MAGENTA -> R.color.magenta
        }
    }
}