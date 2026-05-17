package com.aegis.pdf.ui.components  
  
import androidx.activity.compose.rememberLauncherForActivityResult  
import androidx.activity.result.contract.ActivityResultContracts  
import androidx.compose.material3.AlertDialog  
import androidx.compose.material3.Button  
import androidx.compose.material3.Text  
import androidx.compose.runtime.Composable  
import androidx.compose.runtime.LaunchedEffect  
import androidx.compose.runtime.collectAsState  
import androidx.compose.runtime.getValue  
import com.aegis.pdf.core.permission.PermissionState  
import com.aegis.pdf.core.permission.PermissionViewModel  
  
@Composable  
fun HandleStoragePermissions(  
    viewModel: PermissionViewModel,  
    onPermissionGranted: () -> Unit  
) {  
    val state by viewModel.permissionState.collectAsState()  
      
    // System Permission Request Dialog Launcher Bridge  
    val launcher = rememberLauncherForActivityResult(  
        contract = ActivityResultContracts.RequestMultiplePermissions()  
    ) { resultResultMap ->  
        val allGranted = resultResultMap.values.all { it }  
        viewModel.onPermissionsResult(allGranted)  
    }  
  
    // Screen load hote hi automatic permission check run karo  
    LaunchedEffect(Unit) {  
        viewModel.checkStoragePermissions()  
    }  
  
    when (val currentState = state) {  
        is PermissionState.Granted -> {  
            LaunchedEffect(Unit) { onPermissionGranted() }  
        }  
        is PermissionState.Denied -> {  
            LaunchedEffect(currentState.requiredPermissions) {  
                launcher.launch(currentState.requiredPermissions.toTypedArray())  
            }  
        }  
        is PermissionState.PermanentlyDenied -> {  
            // Premium Corporate Custom Alert Dialog UX Box  
            AlertDialog(  
                onDismissRequest = { },  
                title = { Text("Storage Permission Required") },  
                text = { Text("Aegis requires storage access to find, convert, and save PDFs on your device. Please enable it from app settings.") },  
                confirmButton = {  
                    Button(onClick = { viewModel.checkStoragePermissions() }) {  
                        Text("Check Again")  
                    }  
                }  
            )  
        }  
        else -> {}  
    }  
}