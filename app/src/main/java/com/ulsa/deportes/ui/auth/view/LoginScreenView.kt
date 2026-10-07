package com.ulsa.deportes.ui.auth.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.R
import com.ulsa.deportes.ui.auth.viewmodel.LoginViewModel

// Consistent Sporty Color Palette
private val SportWhite = Color(0xFFFFFFFF)
private val SportBlueStrong = Color(0xFF0D47A1)

/**
 * Pantalla de inicio de sesión rediseñada con fondo blanco y estética deportiva ULSA.
 */
@Composable
fun LoginScreenView(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SportWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ULSA Logo
            Image(
                painter = painterResource(id = R.drawable.logo_ulsa),
                contentDescription = "ULSA Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BIENVENIDO",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = SportBlueStrong,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Inicia sesión para continuar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SportBlueStrong.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearError()
                },
                label = { Text("Correo Electrónico", fontWeight = FontWeight.SemiBold) },
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = SportBlueStrong)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportBlueStrong,
                    unfocusedBorderColor = SportBlueStrong.copy(alpha = 0.5f),
                    focusedLabelColor = SportBlueStrong,
                    cursorColor = SportBlueStrong
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearError()
                },
                label = { Text("Contraseña", fontWeight = FontWeight.SemiBold) },
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SportBlueStrong)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                            tint = SportBlueStrong
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportBlueStrong,
                    unfocusedBorderColor = SportBlueStrong.copy(alpha = 0.5f),
                    focusedLabelColor = SportBlueStrong,
                    cursorColor = SportBlueStrong
                )
            )

            // Error Message
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.error!!,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = { viewModel.login(email, password)
                    Log.d("apiView", "Debugear")},
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportBlueStrong,
                    contentColor = SportWhite
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = SportWhite
                    )
                } else {
                    Text(
                        text = "ENTRAR",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Loading message
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Conectando al servidor deportivo...",
                    style = MaterialTheme.typography.bodySmall,
                    color = SportBlueStrong,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
