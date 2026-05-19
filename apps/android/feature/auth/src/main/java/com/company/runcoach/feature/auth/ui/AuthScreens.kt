package com.company.runcoach.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.company.runcoach.feature.auth.ui.model.SignInUiState
import com.company.runcoach.feature.auth.ui.model.SignUpUiState

@Composable
fun SplashScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
            Text("Restoring session...", modifier = Modifier.padding(top = 12.dp))
        } else if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun SignInScreen(
    state: SignInUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateSignUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign In", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            label = { Text("Email") },
            isError = state.emailError != null,
            supportingText = { state.emailError?.let { Text(it) } }
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.passwordError != null,
            supportingText = { state.passwordError?.let { Text(it) } }
        )
        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text(if (state.isLoading) "Signing in..." else "Sign In")
        }
        TextButton(onClick = onNavigateSignUp, modifier = Modifier.align(Alignment.End)) {
            Text("Create account")
        }
    }
}

@Composable
fun SignUpScreen(
    state: SignUpUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            label = { Text("Email") },
            isError = state.emailError != null,
            supportingText = { state.emailError?.let { Text(it) } }
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.passwordError != null,
            supportingText = { state.passwordError?.let { Text(it) } }
        )
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.confirmPasswordError != null,
            supportingText = { state.confirmPasswordError?.let { Text(it) } }
        )
        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text(if (state.isLoading) "Creating account..." else "Sign Up")
        }
        TextButton(onClick = onNavigateSignIn, modifier = Modifier.align(Alignment.End)) {
            Text("Already have an account?")
        }
    }
}
