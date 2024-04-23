package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationDisplay()
        }
    }
}

@OptIn (ExperimentalPermissionsApi::class)
@Composable
fun LocationDisplay() {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val latitude = remember { mutableStateOf(0.0)}
    val longitude = remember { mutableStateOf(0.0)}
    val latitudeListened = remember { mutableStateOf(0.0)}
    val longitudeListened = remember { mutableStateOf(0.0)}

    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            longitudeListened.value = location!!.longitude
            latitudeListened.value = location!!.latitude
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    LaunchedEffect(locationPermissionState.permission) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,  // minimum time interval between location updates (milliseconds)
                    1f,    // minimum distance between location updates (meters)
                    locationListener
                )
            } catch (ex: SecurityException) {
                // Handle security exception
            }
        }
    }

    Column(){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {
                    Text(
                        text = "Location permission Granted",
                        style = TextStyle(fontSize = 20.sp)
                    )
                } else {
                    Button(
                        onClick = {
                            locationPermissionState.launchPermissionRequest()
                            val location = fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                longitude.value = location!!.longitude
                                latitude.value = location!!.latitude
                            }
                        },
                        modifier = Modifier.padding(end = 5.dp)
                    ) {
                        Text(
                            text = "Request location permission",
                            style = TextStyle(fontSize = 20.sp)
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                if (cameraPermissionState.status.isGranted) {
                    Text(
                        text = "Camera permission Granted",
                        style = TextStyle(fontSize = 20.sp)
                    )
                } else {
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        modifier = Modifier.padding(start = 5.dp)
                    ) {
                        Text(
                            text = "Request camera permission",
                            style = TextStyle(fontSize = 20.sp)
                        )
                    }
                }
            }
        }
        Text(
            text = "Long:${longitude.value}"
        )
        Text(
            text = "Lat:${latitude.value}"
        )
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED){
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            longitude.value = location!!.longitude
                            latitude.value = location!!.latitude
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure
                        }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Update Location",
                style = TextStyle(fontSize = 20.sp)
            )
        }
        Text(
            text = "Long Right Now:${longitudeListened.value}"
        )
        Text(
            text = "Lat Right Now:${latitudeListened.value}"
        )
    }
}
