package com.razumly.mvp.userAuth

import androidx.activity.ComponentActivity
import com.razumly.mvp.core.data.dataTypes.LoginState
import com.razumly.mvp.core.data.oauth2Login
import kotlinx.coroutines.launch

fun DefaultAuthComponent.oauth2Login(activity: ComponentActivity) {
    scope.launch {
        _loginState.value = LoginState.Loading

        userRepository.oauth2Login(activity).onSuccess {
            _loginState.value = LoginState.Success
        }.onFailure {
            _loginState.value = LoginState.Error("Failed To Login")
        }
    }
}