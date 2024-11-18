package com.example.whatsappclone.presentation.screenns

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappclone.R
import com.example.whatsappclone.data.Screens
import com.example.whatsappclone.domain.NavigateTo
import com.example.whatsappclone.presentation.viewModel.ChattingViewModel


enum class BottomNavigationScreen(val icon : Int,val destination : Screens){
    ChattingList(R.drawable.chate,Screens.ChattingList),
    StatusList(R.drawable.ic_status,Screens.StatusList),
    ProfileScreen(R.drawable.ic_user,Screens.ProfileScreen)
}
@Composable
fun BottomNavigationScreen (
    navController: NavController,
    selectedItem : BottomNavigationScreen,
    modifier: Modifier=Modifier
    ) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(top = 4.dp)
        .background(Color.Black)){
        for(item in BottomNavigationScreen.values()){
            Image(painter = painterResource(id = item.icon), contentDescription = null,
                modifier = Modifier.size(40.dp)
                    .padding(4.dp)
                    .weight(1f)
                    .clickable {
                        NavigateTo(navController,item.destination.rout)
                    },
                colorFilter = if(item == selectedItem)
                    ColorFilter.tint(Color.White)
            else ColorFilter.tint(Color.Gray))
        }

    }
}