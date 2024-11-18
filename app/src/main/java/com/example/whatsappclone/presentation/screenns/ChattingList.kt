package com.example.whatsappclone.presentation.screenns


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappclone.data.Screens
import com.example.whatsappclone.domain.NavigateTo
import com.example.whatsappclone.domain.commonRow
import com.example.whatsappclone.domain.progressBar
import com.example.whatsappclone.domain.titleText
import com.example.whatsappclone.presentation.viewModel.ChattingViewModel

@Composable
fun ChattingList(navController: NavController, viewModel: ChattingViewModel) {
    val inProgress = viewModel.chatInProcess.value
    val chats = viewModel.chats.value
    val isSuccess : Boolean = true
    val showDialog = remember { mutableStateOf(false) }
    val userData = viewModel.userData.collectAsState()

    if (inProgress) {
        progressBar()
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog.value = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    titleText(text = "Chats")
                    if (chats.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Chats Available")
                        }
                    }
                    else{
                        LazyColumn (modifier = Modifier.weight(1f)){
                            items(chats){ chat ->
                                val chatUser = if(chat.user1.userId == userData.value?.userId){
                                    chat.user2
                                }else{
                                    chat.user1
                                }
                                commonRow(imageUrl = chatUser.imageUrl, name = chatUser.name) {
                                    chat.chatId?.let {
                                        if(isSuccess) {
                                            Log.d("ChattingList", "Navigating with chatId: $it")
                                            NavigateTo(
                                                navController,
                                                Screens.ChattingScreen.createChattingRout(it)
                                            )
                                        } else {
                                            Log.d("TAG", "Chat Id is incorrect $it")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    BottomNavigationScreen(
                        navController = navController,
                        selectedItem = BottomNavigationScreen.ChattingList,
                        modifier = Modifier.padding(bottom = 100.dp)
                    )
                }
            }
        )

        if (showDialog.value) {
            addChatDialog(
                showDialog = showDialog,
                onAddChat = { chatNumber ->
                    viewModel.addChat(chatNumber)
                    showDialog.value = false
                }
            )
        }
    }
}

@Composable
fun addChatDialog(
    showDialog: MutableState<Boolean>,
    onAddChat: (String) -> Unit
) {
    val addChatNumber = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
            addChatNumber.value = ""
        },
        confirmButton = {
            Button(onClick = {
                onAddChat(addChatNumber.value)
                addChatNumber.value = ""
            }) {
                Text(text = "Add Chat")
            }
        },
        title = { Text(text = "Add Chat") },
        text = {
            OutlinedTextField(
                value = addChatNumber.value,
                onValueChange = { addChatNumber.value = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    )
}
