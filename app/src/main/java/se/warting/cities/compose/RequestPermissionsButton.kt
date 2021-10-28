package se.warting.cities.compose

import android.Manifest
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.marcelpibi.permissionktx.compose.rememberLauncherForPermissionsResult
import se.warting.cities.R

@Composable
fun RequestPermissionsButton() {
    val permissionLauncher =
        rememberLauncherForPermissionsResult(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // we don't need to do anything here since we have a flow that is checking for permissions status in MainViewModel
        }

    Button(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        onClick = {
            permissionLauncher.launch(null)
            // TODO: Use permissionLauncher.safeLaunch and show rationale if required
        }) {
        Text(text = stringResource(R.string.sort_by_gps))
    }
}