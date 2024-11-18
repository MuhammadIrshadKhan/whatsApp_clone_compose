package com.example.whatsappclone.presentation.screenns

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappclone.domain.commonImage
import com.example.whatsappclone.presentation.viewModel.ChattingViewModel


enum class State {
    INITIAL,
    ACTIVE,
    COMPLETED
}

@Composable
fun StatusScreen(navController: NavController, viewModel: ChattingViewModel, userId: String?) {
    val statuses = viewModel.statusList.value.filter {
        it.user.userId == userId
    }

    if (statuses.isNotEmpty()) {
        val currentStatus = remember {
            mutableStateOf(0)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Text(text = "Status is being displayed") // Temporary check
            commonImage(
                data = statuses[currentStatus.value].imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                statuses.forEachIndexed { index, status ->
                    customProgressIndicator(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .padding(1.dp),
                        state = when {
                            currentStatus.value < index -> State.INITIAL
                            currentStatus.value == index -> State.ACTIVE
                            else -> State.COMPLETED
                        }
                    ) {
                        if (currentStatus.value < statuses.size - 1) {
                            currentStatus.value++
                        } else {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    } else {
        // Handle case when there are no statuses
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No status available", color = Color.White)
        }
    }
}

@Composable
fun customProgressIndicator(modifier: Modifier, state: State, onCompleted: () -> Unit) {
    var progress = if (state == State.INITIAL) 0f else 1f
    if (state == State.ACTIVE) {
        val toggleState = remember {
            mutableStateOf(false)
        }
        LaunchedEffect(toggleState) {
            toggleState.value = true
        }

        val p: Float by animateFloatAsState(
            if (toggleState.value) 1f else 0f,
            animationSpec = tween(5000),
            finishedListener = { onCompleted.invoke() })
        progress = p
    }
    LinearProgressIndicator(modifier = modifier, color = Color.Red, progress = progress)
}