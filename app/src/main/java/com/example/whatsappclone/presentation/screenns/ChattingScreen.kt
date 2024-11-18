package com.example.whatsappclone.presentation.screenns


import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappclone.data.Message
import com.example.whatsappclone.domain.commonImage
import com.example.whatsappclone.presentation.viewModel.ChattingViewModel


@Composable
fun ChattingScreen(navController: NavController, viewModel: ChattingViewModel, chatId: String) {
    val chatMessages = viewModel.chatMessages.value

    var reply by rememberSaveable { mutableStateOf("") }
    val sendMessage = {
        if (chatId.isNotEmpty()) {
            viewModel.onMessageReply(chatId, reply)
            reply = ""
        }
    }

    // Launching side effects to fetch the messages
    LaunchedEffect(chatId) {
        viewModel.populateMessages(chatId)
    }

    BackHandler {
        viewModel.depopulateMessages()
        navController.popBackStack()
    }

    // Observing user data to identify the current user
    val myUser = viewModel.userData.collectAsState()
    // Find the other user in the chat
    val chatList = viewModel.chats.value
    val currentChat = chatList.firstOrNull { it.chatId == chatId }
    val chatUser =
        if (myUser.value?.userId == currentChat?.user1?.userId) currentChat?.user2 else currentChat?.user1



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, top = 20.dp, end = 10.dp, bottom = 20.dp)
    ) {
        chatHeader(imageUrl = chatUser?.imageUrl ?: "", name = chatUser?.name ?: "") {
            navController.popBackStack()
            viewModel.depopulateMessages()
        }
        messageBox(
            modifier = Modifier.weight(1f),
            chatMessages = chatMessages,
            currentUserId = myUser?.value?.userId ?: ""
        )
        ChattingBox(
            modifier = Modifier.imePadding().padding(bottom = 15.dp),
            reply = reply,
            onReplyChange = { reply = it },
            onReplySend = sendMessage
        )
    }
}

@Composable
fun chatHeader(name: String, imageUrl: String, onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .clickable { onBackClicked.invoke() }
        )
        commonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 6.dp)
        )
    }
}

@Composable
fun ChattingBox(
    modifier: Modifier,
    reply: String,
    onReplyChange: (String) -> Unit,
    onReplySend: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = reply,
            onValueChange = onReplyChange,
            maxLines = 3,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Button(onClick = { onReplySend() }) {
            Text(text = "Send")
        }
    }
}

@Composable
fun messageBox(modifier: Modifier, chatMessages: List<Message>, currentUserId: String) {
    LazyColumn(modifier.fillMaxSize()) {
        items(chatMessages) { msg ->
            val alignment = if (msg.sendBy == currentUserId) Alignment.End else Alignment.Start
            val color = if (msg.sendBy == currentUserId) Color(0xFF68C400) else Color(0xFFC0C0C0)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "No message content",
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(color)
                        .padding(12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}