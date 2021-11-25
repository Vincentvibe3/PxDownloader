package io.github.vincentvibe3.pixivdownloader

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.outlinedButtonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.checkCookies
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun DownloadDialog(modalState:ModalBottomSheetState) {
    val focusManager = LocalFocusManager.current
    val textState = remember { mutableStateOf(TextFieldValue()) }
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = modalState.isVisible) {
        focusManager.clearFocus()
        textState.value = TextFieldValue()
        coroutineScope.launch {
            modalState.hide()
        }
    }
    if(!modalState.isVisible){
        focusManager.clearFocus()
        textState.value = TextFieldValue()
    }
    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = 0.dp) {
        IconButton(onClick = {
            focusManager.clearFocus()
            textState.value = TextFieldValue()
            coroutineScope.launch {
                modalState.hide()
            }

        }) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = "Back")
        }
        Text(text = "New Download", style = MaterialTheme.typography.h6)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Bottom
    ) {

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    println(it.isFocused)
                },
            value = textState.value,
            label = {
                Text(text = "Pixiv Artwork URL")
            },
            singleLine = true,
            onValueChange = { textState.value = it },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )
        )
        Row(
            modifier = Modifier.padding(10.dp)
                .alpha(if(checkCookies()){
                    0F
                } else {
                    1F
                }),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Outlined.Warning, contentDescription = "Back")
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "You are not logged in to Pixiv,\nNSFW content will not be loaded.",
                style=MaterialTheme.typography.caption)
        }
        Spacer(modifier = Modifier.height(90.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    textState.value = TextFieldValue()
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        modalState.hide()
                    }

                },
                colors = outlinedButtonColors(backgroundColor = MaterialTheme.colors.background)
            ) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    textState.value = TextFieldValue()
                },
            ) {
                Text(text = "Download")
            }
        }
    }
}