package com.example.whatsappclone.data



sealed class Screens(var rout : String) {
    object BottomNavigationScreen : Screens("bottom_navigation_screen")
    object SignupScreen : Screens("signup_screen")
    object LoginScreen : Screens("login_screen")
    object ChattingList : Screens("chatting_list")
    object StatusList : Screens("status_list")
    object StatusScreen : Screens("status_screen/{statusId}"){
        fun createStatusRout(statusId : String) = "status_screen/$statusId"
    }
    object ChattingScreen : Screens("chatting_screen/{chatId}"){
        fun createChattingRout(chatId : String) = "chatting_screen/$chatId"
    }
    object ProfileScreen : Screens("profile_screen")
}