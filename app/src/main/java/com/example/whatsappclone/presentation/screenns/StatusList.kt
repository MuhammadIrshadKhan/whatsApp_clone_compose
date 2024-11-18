package com.example.whatsappclone.presentation.screenns

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappclone.data.Screens
import com.example.whatsappclone.domain.CommonDivider
import com.example.whatsappclone.domain.NavigateTo
import com.example.whatsappclone.domain.commonRow
import com.example.whatsappclone.domain.progressBar
import com.example.whatsappclone.domain.titleText
import com.example.whatsappclone.presentation.viewModel.ChattingViewModel


@Composable
fun StatusList(navController: NavController, viewModel: ChattingViewModel) {
    val inProcess = viewModel.inStatusProgress.value
    val statuses = viewModel.statusList.value
    val userData = viewModel.userData.collectAsState()

    Log.d("StatusList", "inProcess: $inProcess, statuses: ${statuses.size}")

    if (inProcess) {
        progressBar()
    } else {
        val statuses = viewModel.statusList.value
        val userData = viewModel.userData.collectAsState()

        // Separate user's status and contacts' statuses
        val myStatus = statuses.filter { it.user.userId == userData.value?.userId }
        val otherStatus = statuses.filter { it.user.userId != userData.value?.userId }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { viewModel.upLoadStatus(it) }
        }

        Scaffold(
            floatingActionButton = {
                fabClick { launcher.launch("image/*") }
            },
            modifier = Modifier.padding(bottom = 40.dp),
            bottomBar = {
                BottomNavigationScreen(
                    navController = navController,
                    selectedItem = BottomNavigationScreen.StatusList
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    titleText(text = "Status")

                    // Display user's status
                    if (myStatus.isNotEmpty()) {
                        commonRow(
                            imageUrl = myStatus[0].user.imageUrl,
                            name = myStatus[0].user.name
                        ) {
                            NavigateTo(
                                navController = navController,
                                Screens.StatusScreen.createStatusRout(myStatus[0].user.userId!!)
                            )
                        }
                        CommonDivider()
                    }

                    // Display contacts' statuses
                    if (otherStatus.isNotEmpty()) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            val uniqueUsers = otherStatus.map { it.user }.toSet().toList()
                            items(uniqueUsers) { user ->
                                commonRow(
                                    imageUrl = user.imageUrl,
                                    name = user.name
                                ) {
                                    NavigateTo(
                                        navController = navController,
                                        Screens.StatusScreen.createStatusRout(user.userId!!)
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No statuses available")
                        }
                    }
                }
            }
        )
    }
}


@Composable
fun fabClick(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },  // Make sure onClick is called
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Add Status",
            tint = Color.White
        )
    }
}