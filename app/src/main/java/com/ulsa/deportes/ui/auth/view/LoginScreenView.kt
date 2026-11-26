package com.ulsa.deportes.ui.auth.view

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulsa.deportes.R
import com.ulsa.deportes.ui.auth.viewmodel.LoginViewModel

private val SportWhite = Color(0xFFFFFFFF)
private val SportBlueStrong = Color(0xFF0D47A1)

/**
 * Pantalla de inicio de sesión integrada con soporte biométrico, teclado específico,
 * ocultación de teclado al tocar fuera, desplazamiento con IME padding y navegación a registro.
 */
@Composable
fun LoginScreenView(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SportWhite)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ULSA Logo
            Image(
                painter = painterResource(id = R.drawable.logo_ulsa),
                contentDescription = stringResource(R.string.login_logo_desc),
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.login_welcome),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = SportBlueStrong,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.login_subtitle),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SportBlueStrong.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearError()
                },
                label = { Text(stringResource(R.string.email_label), fontWeight = FontWeight.SemiBold) },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false
                ),
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
                label = { Text(stringResource(R.string.password_label), fontWeight = FontWeight.SemiBold) },
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SportBlueStrong)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) {
                                stringResource(R.string.login_hide_password)
                            } else {
                                stringResource(R.string.login_show_password)
                            },
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

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.login(email, password)
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportBlueStrong,
                    contentColor = SportWhite
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = SportWhite
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_enter),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Biometric Login Button
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    launchBiometricPrompt(context, onLoginSuccess)
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SportBlueStrong
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, SportBlueStrong)
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = "Biométrico", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ACCEDER CON BIOMETRÍA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading message
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.login_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = SportBlueStrong,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    color = SportBlueStrong.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.login_register_link),
                    color = SportBlueStrong,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                )
            }
        }
    }
}

private fun launchBiometricPrompt(context: Context, onSuccess: () -> Unit) {
    val activity = context.findFragmentActivity()
    if (activity != null) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Biométrica ULSA")
            .setSubtitle("Usa tu huella digital o reconocimiento facial para iniciar sesión")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    } else {
        Log.w("Biometric", "Context is not a FragmentActivity")
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}
