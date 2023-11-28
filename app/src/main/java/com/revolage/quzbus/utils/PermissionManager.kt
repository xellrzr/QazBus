package com.revolage.quzbus.utils

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.revolage.quzbus.utils.Constants.Companion.PERMISSION_ID
import javax.inject.Inject

class PermissionManager @Inject constructor(
    private val activity: FragmentActivity
) {

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity,
                ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }
}