package com.example.whatsappclone.presentation.screenns

import android.annotation.SuppressLint
import android.graphics.Paint.Align
import android.util.Log
import android.widget.ProgressBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.whatsappclone.data.Screens
import com.example.whatsappclone.domain.CommonDivider
import com.example.whatsappclone.domain.NavigateTo
import com.example.whatsappclone.domain.commonImage
import com.example.whatsappclone.domain.progressBar
import com.example.whatsappclone.presentation.viewModel.ChattingViewModel


@Composable
fun ProfileScreen(navController: NavController, viewModel: ChattingViewModel) {
    Column(
        modifier = Modifier
            .padding(start = 20.dp, top = 30.dp, end = 20.dp, bottom = 20.dp)
            .fillMaxSize()
            .padding(bottom = 20.dp)
    ) {

        val inProcess = viewModel.inProgress.value
        val userData by viewModel.userData.collectAsState()

        if (inProcess || userData == null) {
            progressBar()
        } else {
            var name by rememberSaveable {
                mutableStateOf(userData?.name ?: "")
            }
            var phone by rememberSaveable {
                mutableStateOf(userData?.phone ?: "")
            }

            ProfileDesign(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                viewModel = viewModel,
                name = name,
                number = phone,
                onNameChange = {
                    name = it
                    viewModel.updateName(it)
                },
                onNumberChange = {
                    phone = it
                    viewModel.updatePhone(it)
                },
                onBack = {
                    NavigateTo(navController = navController, rout = Screens.ChattingList.rout)
                },
                onSave = {
                    viewModel.createOrUpdateProfile(name = name, phone = phone)
                },
                onLogOut = {
                    viewModel.logOut()
                NavigateTo(navController, rout = Screens.SignupScreen.rout)
                }
            )
        }

        Spacer(modifier = Modifier.weight(0.8f))
        BottomNavigationScreen(
            navController = navController,
            selectedItem = BottomNavigationScreen.ProfileScreen,
            modifier = Modifier.padding(80.dp)
        )
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDesign(

    modifier: Modifier,
    viewModel: ChattingViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogOut: () -> Unit,
    name : String,
    number: String,
    onNameChange : (String) ->Unit,
    onNumberChange : (String) ->Unit
) {
    val imageUrl = viewModel.userData.value?.imageUrl
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back",
                modifier = Modifier.clickable {
                    onBack.invoke()
                })
            Text(text = "Save", modifier = Modifier.clickable {
                onSave.invoke()
            })
        }
            CommonDivider()
            ProfileImage(imageUrl = imageUrl, viewModel = viewModel)
            CommonDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Name",modifier = Modifier.width(100.dp))
                TextField(value = name, onValueChange =onNameChange,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedTextColor = Color.Black,
                        containerColor = Color.Transparent
                    ))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Number",modifier = Modifier.width(100.dp))
                TextField(value = number, onValueChange =onNumberChange,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedTextColor = Color.Black,
                        containerColor = Color.Transparent
                    ))
            }
            
            CommonDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Text(text = "Logout", modifier = Modifier.clickable { onLogOut.invoke() })
            }
        }
    }


@Composable
fun ProfileImage(
    imageUrl: String?,
    viewModel: ChattingViewModel
) {
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.uploadProfileImage(uri)
            }
        }

    Box(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    Log.d("ProfileImage", "Loading image with URL: $imageUrl")
                    commonImage(data = imageUrl)
                } else {
                    Log.d("ProfileImage", "Image URL is null or empty")
                }
            }
            Text(text = "Change Profile Picture")
        }
        if (viewModel.inProgress.value) {
            progressBar()
        }
    }
}