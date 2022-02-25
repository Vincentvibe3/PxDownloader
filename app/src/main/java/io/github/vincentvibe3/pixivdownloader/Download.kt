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
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.volley.toolbox.Volley
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.Download
import io.github.vincentvibe3.pixivdownloader.utils.PixivMetadata
import io.github.vincentvibe3.pixivdownloader.utils.checkCookies
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun DlDialogPreview(){
    val downloadSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded, confirmStateChange = {
        true
    })
    val loggedIn = remember {mutableStateOf(false)}
    DownloadDialog(loggedIn = loggedIn, modalState = downloadSheetState)
}

@ExperimentalMaterialApi
@Composable
fun DownloadDialog(loggedIn:State<Boolean?>, modalState:ModalBottomSheetState) {
    val focusManager = LocalFocusManager.current
    val textState = remember { mutableStateOf(TextFieldValue()) }
    val coroutineScope = rememberCoroutineScope()
    val requestQueue = Volley.newRequestQueue(LocalContext.current)
    val context = LocalContext.current
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
                .fillMaxWidth(),
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
            modifier = Modifier
                .padding(10.dp)
                .alpha(
                    if (loggedIn.value == true) {
                        0F
                    } else {
                        1F
                    }
                ),
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
                    val url = textState.value.text
                    coroutineScope.launch {
                        "93767828"
//                        if (url!=""){
                            Download.download("93767828", requestQueue, context)
//                        }
                    }
                    textState.value = TextFieldValue()
                },
            ) {
                Text(text = "Download")
            }
        }
    }
}