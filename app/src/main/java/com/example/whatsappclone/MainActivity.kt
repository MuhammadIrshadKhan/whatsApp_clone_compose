package com.example.whatsappclone

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.whatsappclone.data.Screens
import com.example.whatsappclone.presentation.screenns.BottomNavigationScreen
import com.example.whatsappclone.presentation.screenns.ChattingList
import com.example.whatsappclone.presentation.screenns.ChattingScreen
import com.example.whatsappclone.presentation.screenns.LoginScreen
import com.example.whatsappclone.presentation.screenns.ProfileScreen
import com.example.whatsappclone.presentation.screenns.SignupScreen
import com.example.whatsappclone.presentation.screenns.StatusList
import com.example.whatsappclone.presentation.screenns.StatusScreen
import com.example.whatsappclone.presentation.viewModel.ChattingViewModel
import com.example.whatsappclone.ui.theme.WhatsAppCloneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhatsAppCloneTheme {
                Scaffold() {
                    ChatAppNavigation()
                }
            }
        }
    }
}

@Composable
fun ChatAppNavigation() {
    val navController = rememberNavController()
    val viewModel = hiltViewModel<ChattingViewModel>()
    NavHost(navController = navController, startDestination = Screens.SignupScreen.rout) {
        composable(Screens.SignupScreen.rout) {
            SignupScreen(navController, viewModel)
        }
        composable(Screens.LoginScreen.rout) {
            LoginScreen(navController, viewModel)
        }
        composable(Screens.ChattingList.rout) {
            ChattingList(navController, viewModel)
        }

        composable(Screens.ChattingScreen.rout) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            chatId?.let {
                ChattingScreen(navController, viewModel, chatId)
            }
        }
        composable(Screens.ProfileScreen.rout) {
            ProfileScreen(navController, viewModel)
        }
        composable(Screens.StatusScreen.rout) {
            val userId = it.arguments?.getString("userId")
            userId?.let {
                StatusScreen(navController, viewModel,userId =it)
            }
        }
        composable(Screens.StatusList.rout) {
            StatusList(navController, viewModel)
        }
    }
}

