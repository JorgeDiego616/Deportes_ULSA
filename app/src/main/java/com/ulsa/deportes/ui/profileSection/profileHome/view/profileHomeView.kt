package com.ulsa.deportes.ui.profileSection.profileHome.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.ui.profileSection.profileHome.viewmodel.profileHomeViewModel
import com.ulsa.deportes.ui.qrSection.qrHome.data.QrPreferences
import com.ulsa.deportes.ui.qrSection.qrHome.model.CreateQrResponse

@Composable
fun profileHomeView(
    onLogout: () -> Unit = {},
    onNavigateToQr: () -> Unit = {},
    onViewQr: (CreateQrResponse) -> Unit = {},
    homeViewModel: profileHomeViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val qrPreferences = remember { QrPreferences(context) }
    val savedQr by remember { 
        androidx.compose.runtime.mutableStateOf(qrPreferences.getQr()) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Personal Information")

        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar botón "Ver QR" si hay QR guardado
        val currentQr = savedQr
        if (currentQr != null) {
            Button(
                onClick = { onViewQr(currentQr) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF1A73E8),
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Icon(imageVector = Icons.Filled.Visibility, contentDescription = "Ver QR")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver mi QR", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onNavigateToQr,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Filled.QrCode, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear código QR", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}