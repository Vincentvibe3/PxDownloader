package io.github.vincentvibe3.pixivdownloader

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.outlinedButtonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
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
fun DownloadDialog(
    loggedIn: State<Boolean?>,
    modalState: ModalBottomSheetState,
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val fieldText = remember { mutableStateOf("") }
    BackHandler(enabled = modalState.isVisible) {
        focusManager.clearFocus()
        coroutineScope.launch {
            modalState.hide()
        }
    }
    if(!modalState.isVisible){
        focusManager.clearFocus()
        fieldText.value = ""
    }
    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = 0.dp) {
        IconButton(onClick = {
            focusManager.clearFocus()
            fieldText.value = ""
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
            value = fieldText.value,
            label = {
                Text(text = "Pixiv Artwork URL or Id")
            },
            singleLine = true,
            isError = getInputId(fieldText.value) == null && fieldText.value.isNotBlank(),
            onValueChange = { fieldText.value = it },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )
        )
        OutlinedButton(
            onClick = {
                      fieldText.value = "${fieldText.value}${checkClipboard(context)?:""}"
            },
            enabled = !checkClipboard(context).isNullOrEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedButtonColors(backgroundColor = MaterialTheme.colors.background)
        ) {
            Text("Paste from clipboard")
        }
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
                "You are not logged in to Pixiv,\nLog in to access NSFW content.",
                style=MaterialTheme.typography.caption)
        }
        Spacer(modifier = Modifier.height(90.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    fieldText.value = ""
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
                enabled = !getInputId(fieldText.value).isNullOrBlank(),
                onClick = {
                    focusManager.clearFocus()
                    val url = fieldText.value
                    val input = getInputId(url)
                    if (input!=null){
                        val intent = Intent(context, Browse::class.java)
                        intent.putExtra("id", input)
                        ContextCompat.startActivity(context, intent, Bundle.EMPTY)
                        fieldText.value = ""
                    } else {
                        val toast = Toast.makeText(context, "Invalid link", Toast.LENGTH_SHORT)
                        toast.show()
                    }
                    coroutineScope.launch {
                        modalState.hide()
                    }
                },
            ) {
                Text(text = "Download")
            }
        }
    }
}

fun getInputId(input:String):String?{
    val pattern = "(?<=https:\\/\\/www\\.pixiv\\.net\\/en\\/artworks\\/)([0-9]+)$".toRegex()
    val patternNosecure = "(?<=http:\\/\\/www\\.pixiv\\.net\\/en\\/artworks\\/)([0-9]+)$".toRegex()
    val pattern2 = "^([0-9]+)$".toRegex()
    return if (pattern.containsMatchIn(input)) {
        input.removePrefix("https://www.pixiv.net/en/artworks/")
    }else if (patternNosecure.containsMatchIn(input)){
        input.removePrefix("http://www.pixiv.net/en/artworks/")
    } else if (pattern2.matches(input)) {
        input
    } else {
        null
    }
}

fun checkClipboard(context: Context):String?{
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    if(clipboard.hasPrimaryClip()){
        if (clipboard.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN)){
            return clipboard.primaryClip!!.getItemAt(0).text.toString()
        }
    }
    return null
}