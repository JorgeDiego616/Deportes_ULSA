package com.ulsa.deportes.ui.auth.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.R
import com.ulsa.deportes.ui.auth.viewmodel.RegisterViewModel

private val SportWhite = Color(0xFFFFFFFF)
private val SportBlueStrong = Color(0xFF0D47A1)

@Composable
fun RegisterScreenView(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var matricula by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) onRegisterSuccess()
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
                contentDescription = stringResource(R.string.register_logo_desc),
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.register_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = SportBlueStrong,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.register_subtitle),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SportBlueStrong.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Matrícula Field
            OutlinedTextField(
                value = matricula,
                onValueChange = {
                    matricula = it
                    viewModel.clearFieldError("matricula")
                },
                label = { Text(stringResource(R.string.register_matricula_label), fontWeight = FontWeight.SemiBold) },
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SportBlueStrong)
                },
                shape = RoundedCornerShape(12.dp),
                isError = uiState.fieldErrors.containsKey("matricula"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportBlueStrong,
                    unfocusedBorderColor = SportBlueStrong.copy(alpha = 0.5f),
                    focusedLabelColor = SportBlueStrong,
                    cursorColor = SportBlueStrong
                )
            )

            if (uiState.fieldErrors["matricula"] != null) {
                ErrorText(uiState.fieldErrors["matricula"]!!)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre Field
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    viewModel.clearFieldError("nombre")
                },
                label = { Text(stringResource(R.string.register_name_label), fontWeight = FontWeight.SemiBold) },
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SportBlueStrong)
                },
                shape = RoundedCornerShape(12.dp),
                isError = uiState.fieldErrors.containsKey("nombre"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportBlueStrong,
                    unfocusedBorderColor = SportBlueStrong.copy(alpha = 0.5f),
                    focusedLabelColor = SportBlueStrong,
                    cursorColor = SportBlueStrong
                )
            )

            if (uiState.fieldErrors["nombre"] != null) {
                ErrorText(uiState.fieldErrors["nombre"]!!)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearFieldError("email")
                },
                label = { Text(stringResource(R.string.register_email_label), fontWeight = FontWeight.SemiBold) },
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = SportBlueStrong)
                },
                shape = RoundedCornerShape(12.dp),
                isError = uiState.fieldErrors.containsKey("email"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportBlueStrong,
                    unfocusedBorderColor = SportBlueStrong.copy(alpha = 0.5f),
                    focusedLabelColor = SportBlueStrong,
                    cursorColor = SportBlueStrong
                )
            )

            if (uiState.fieldErrors["email"] != null) {
                ErrorText(uiState.fieldErrors["email"]!!)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearFieldError("password")
                },
                label = { Text(stringResource(R.string.register_password_label), fontWeight = FontWeight.SemiBold) },
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
                            contentDescription = if (passwordVisible) {
                                stringResource(R.string.register_hide_password)
                            } else {
                                stringResource(R.string.register_show_password)
                            },
                            tint = SportBlueStrong
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = uiState.fieldErrors.containsKey("password"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SportBlueStrong,
                    unfocusedBorderColor = SportBlueStrong.copy(alpha = 0.5f),
                    focusedLabelColor = SportBlueStrong,
                    cursorColor = SportBlueStrong
                )
            )

            if (uiState.fieldErrors["password"] != null) {
                ErrorText(uiState.fieldErrors["password"]!!)
            }

            // General Error Message
            if (uiState.generalError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.generalError!!,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            Button(
                onClick = { viewModel.register(matricula, nombre, email, password) },
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
                        text = stringResource(R.string.register_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Loading message
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.register_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = SportBlueStrong,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to Login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.register_have_account),
                    color = SportBlueStrong.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.register_login_link),
                    color = SportBlueStrong,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                        .clickable { onBackToLogin() }
                )
            }
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        color = Color.Red,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 4.dp)
    )
}