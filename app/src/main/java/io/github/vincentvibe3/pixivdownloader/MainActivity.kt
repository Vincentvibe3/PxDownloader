package io.github.vincentvibe3.pixivdownloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import io.github.vincentvibe3.pixivdownloader.ui.theme.PixivDownloaderTheme
import io.github.vincentvibe3.pixivdownloader.utils.DownloadWorker
import io.github.vincentvibe3.pixivdownloader.utils.PixivMetadata
import io.github.vincentvibe3.pixivdownloader.utils.checkCookies
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    val model:AppViewModel by viewModels()

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = getString(R.string.notif_name)
        val descriptionText = getString(R.string.notif_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(NotificationManagerCompat.from(applicationContext)) {
            cancelAll()
        }
        createNotificationChannel()
        setContent {
            PixivDownloaderTheme {
                // A surface container using the 'background' color from the theme
                val loggedIn = model.loginStatus.observeAsState()
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home" ){
                        composable("Settings"){
                            Settings(model, loggedIn = loggedIn, loggedInState = loggedIn, navController)
                        }
                        composable("home"){
                            Home(loggedIn, navController = navController)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkCookies(model)
        if (PixivMetadata.PendingRequests.isNotEmpty()){
            for (req in PixivMetadata.PendingRequests){
                val context = this.applicationContext
                val workData = Data.Builder()
                    .putString("id", req.key)
                    .build()
                val downloadWorkRequest: WorkRequest =
                    OneTimeWorkRequestBuilder<DownloadWorker>()
                        .setInputData(workData)
                        .addTag("video")
                        .addTag(req.key)
                        .build()
                WorkManager
                    .getInstance(context)
                    .enqueue(downloadWorkRequest)




            }
        }
    }



}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Home(loggedIn:State<Boolean?>, navController:NavController) {
    PixivDownloaderTheme {
        val context = LocalContext.current
        val workQuery = WorkQuery.Builder
            .fromTags(listOf("video"))
            .addStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING))
            .build()
        val workManager = WorkManager
            .getInstance(context)
        val pendingWork = workManager.getWorkInfosLiveData(workQuery).observeAsState()
        val focusManager = LocalFocusManager.current
        val downloadSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden, confirmStateChange = {
            if (it == ModalBottomSheetValue.Hidden){
                focusManager.clearFocus()
            }
            true
        })
        val coroutineScope = rememberCoroutineScope()
        ModalBottomSheetLayout(
            sheetState = downloadSheetState,
            sheetShape = MaterialTheme.shapes.large,
            sheetBackgroundColor = MaterialTheme.colors.background,
            scrimColor = Color(0x90000000),
            sheetContent = {
                DownloadDialog(loggedIn,downloadSheetState)

        }) {

            Scaffold(
                bottomBar = {
                    BottomAppBar(cutoutShape = CircleShape, backgroundColor = MaterialTheme.colors.primary) {
                        Row(modifier = Modifier.fillMaxWidth() ,horizontalArrangement = Arrangement.Start){
                            IconButton(onClick = {
                                navController.navigate("settings")
                            }) {
                                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Back")
                            }

                        }

                    }
                },
                topBar = {
                    TopAppBar(backgroundColor = MaterialTheme.colors.background, elevation = 0.dp) {
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                            color = MaterialTheme.colors.onBackground )
                    }
                },
                floatingActionButton = {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FloatingActionButton(onClick = {
                            val intent = Intent(context, WebViewDownload::class.java)
                            ContextCompat.startActivity(context, intent, Bundle.EMPTY)

                        }, content = {
                            Icon(
                                imageVector = Icons.Outlined.Explore,
                                contentDescription = "Browse"
                            )
                        })
                        Spacer(Modifier.width(10.dp))
                        FloatingActionButton(onClick = {
                            coroutineScope.launch {
                                downloadSheetState.show()

                            }

                        }, content = {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Download"
                            )
                        })
                    }
            },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true
            ){
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(24.dp),
                    verticalArrangement = if (pendingWork.value.orEmpty().isEmpty()){
                            Arrangement.Center
                        } else {
                            Arrangement.Top
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (pendingWork.value.orEmpty().isEmpty()){
                        item {
                            Text(text = "No Pending tasks")
                        }
                    }
                    items(pendingWork.value.orEmpty()){ work ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(
                                        text = work.tags.first { !getInputId(it).isNullOrEmpty() },
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(text = if(work.state.name=="ENQUEUED"){
                                        "Pending"
                                    } else {
                                        "Processing"
                                    })
                                }
                                IconButton(onClick = {
                                    workManager.cancelWorkById(work.id)
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Cancel,
                                        contentDescription = "Cancel",
                                        tint=MaterialTheme.colors.onBackground
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}