package com.example.whatsappclone.presentation.viewModel


import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappclone.data.CHAT
import com.example.whatsappclone.data.ChatData
import com.example.whatsappclone.data.ChatUser
import com.example.whatsappclone.data.DataModel
import com.example.whatsappclone.data.Events
import com.example.whatsappclone.data.MESSAGE
import com.example.whatsappclone.data.Message
import com.example.whatsappclone.data.STATUS
import com.example.whatsappclone.data.Status
import com.example.whatsappclone.data.USER_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.core.UserData
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChattingViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    var inProgress = mutableStateOf(false)
    var event = mutableStateOf<Events<String>?>(null)
    var signIn = mutableStateOf(false)
    private val _userData = MutableStateFlow<DataModel?>(null)
    val userData: StateFlow<DataModel?> = _userData.asStateFlow()
    val chatInProcess = mutableStateOf(false)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressMessage = mutableStateOf(false)
    var currentMessagesListener: ListenerRegistration? = null

    val statusList = mutableStateOf<List<Status>>(listOf())
    val inStatusProgress = mutableStateOf(false)


    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun signUp(name: String, phone: String, email: String, password: String) {
        inProgress.value = true
        if (name.isEmpty() or phone.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(message = "Please fill all fields first")
            return
        } else {
            inProgress.value = true
            db.collection(USER_NODE).whereEqualTo("phone", phone).get().addOnSuccessListener {
                if (it.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                signIn.value = true
                                inProgress.value = false
                                createOrUpdateProfile(name, phone)
                                Log.d("TAG", "User signed up successfully")
                            } else {
                                it.exception.let {
                                    Log.d("TAG", "Can't create user ${it?.message}")
                                }
                                handleException(it.exception, message = "Sign-up failed")
                                inProgress.value = false
                            }
                        }
                } else {
                    handleException(message = "Phone Number already exists")
                    inProgress.value = false
                }
            }

        }
    }

    fun logIn(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            inProgress.value = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        inProgress.value = false
                        signIn.value = true
                        auth.currentUser?.uid?.let {
                            getUserData(it)
                        }
                    }
                    handleException(it.exception, message = "Login failed! please try again")
                }
        }
        handleException(message = "Please fill the fields first")
        return
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) { imageUrl ->
            Log.d("ChattingViewModel", "Image uploaded successfully: $imageUrl")
            createOrUpdateProfile(image = imageUrl.toString())
        }
    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProgress.value = false
        }.addOnFailureListener {
            handleException(it)
        }
    }

    fun createOrUpdateProfile(
        name: String? = null,
        phone: String? = null,
        image: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = DataModel(
            userId = uid,
            name = name ?: _userData.value?.name,
            phone = phone ?: _userData.value?.phone,
            imageUrl = image ?: _userData.value?.imageUrl
        )

        uid?.let {
            inProgress.value = true
            db.collection(USER_NODE).document(uid).set(userData).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("ChattingViewModel", "User profile updated: $userData")
                    getUserData(uid)
                } else {
                    handleException(it.exception, "Profile update failed")
                }
            }
        }
    }

    fun populateMessages(chatId: String) {
        inProgressMessage.value = true
        currentMessagesListener = db.collection(CHAT).document(chatId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timeStamp }
                }
                inProgressMessage.value = false
            }
    }

    fun depopulateMessages() {
        chatMessages.value = listOf()
        currentMessagesListener = null
    }

    fun populateChats() {
        chatInProcess.value = true
        Log.d("ChattingViewModel", "Fetching chats for user: ${userData.value?.userId}")
        db.collection(CHAT).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId),
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
                Log.e("ChattingViewModel", "Error fetching chats: ${error.message}")
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                Log.d("ChattingViewModel", "Chats retrieved: ${chats.value}")
                chatInProcess.value = false
            } else {
                Log.d("ChattingViewModel", "No chats found for user: ${userData.value?.userId}")
            }
        }
    }


    private fun getUserData(uid: String) {
        inProgress.value = true
        Log.d("ChattingViewModel", "Fetching user data for uid: $uid")
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Can't retrieve data")
                Log.e("ChattingViewModel", "Error fetching user data: ${error.message}")
            }
            if (value != null) {
                val user = value.toObject<DataModel>()
                _userData.value = user
                Log.d("ChattingViewModel", "User data retrieved: $user")
                inProgress.value = false
                if (user != null) {
                    populateChats()
                    populateStatuses()
                }
            } else {
                Log.d("ChattingViewModel", "User data is null for uid: $uid")
            }
        }
    }


    fun handleException(exception: Exception? = null, message: String = "") {
        Log.e("ChattingApp", "Chatting App Exception: ", exception)
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val displayMessage = if (message.isNullOrEmpty()) errorMessage else message
        event.value = Events(displayMessage)
        inProgress.value = false
    }

    fun updateName(newName: String) {
        Log.d("ChattingViewModel", "Updating user name to: $newName")
        _userData.value = _userData.value?.copy(name = newName)
    }

    fun updatePhone(newPhone: String) {
        Log.d("ChattingViewModel", "Updating user phone to: $newPhone")
        _userData.value = _userData.value?.copy(phone = newPhone)
    }

    fun onMessageReply(chatId: String, message: String) {
        val userId = userData.value?.userId
        Log.d("ChattingViewModel", "Attempting to send message in chatId: $chatId by user: $userId")
        try {
            if (userId != null) {
                val time = Calendar.getInstance().time.toString()
                val msg = Message(
                    userId,
                    message,
                    time
                )
                db.collection(CHAT)
                    .document(chatId)
                    .collection(MESSAGE)
                    .document()
                    .set(msg)
                    .addOnSuccessListener {
                        Log.d("ChattingViewModel", "Message sent successfully: $msg")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChattingViewModel", "Error sending message", e)
                    }
            } else {
                Log.e("ChattingViewModel", "User ID is null, cannot send message")
            }
        } catch (e: Exception) {
            Log.e("ChattingViewModel", "Error sending message", e)
        }
    }

    fun logOut() {
        auth.signOut()
        signIn.value = false
        _userData.value = null
        event.value = Events("Logged Out")
        depopulateMessages()
        currentMessagesListener = null
        Log.d("ChattingViewModel", "User logged out")
    }

    fun addChat(number: String) {
        Log.d("ChattingViewModel", "Attempting to add chat with number: $number")
        if (number.isEmpty() or !number.isDigitsOnly()) {
            handleException(message = "Number must contains digits only")
        } else {
            db.collection(CHAT).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.phone)
                    ),
                    Filter.and(
                        Filter.equalTo("user2.number", userData.value?.phone),
                        Filter.equalTo("user1.number", number)
                    )
                )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection(USER_NODE).whereEqualTo("phone", number).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                handleException(message = "Number not found")
                            } else {
                                val chatPartner = it.toObjects<DataModel>()[0]
                                val id = db.collection(CHAT).document().id
                                val chat = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.phone,
                                        userData.value?.imageUrl
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.phone,
                                        chatPartner.imageUrl
                                    )
                                )
                                db.collection(CHAT).document(id).set(chat)
                            }
                        }
                        .addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(message = "Phone Number already exists")
                }
            }
        }
    }

    fun upLoadStatus(uri: Uri) {
        uploadImage(uri) {
            createStatus(it.toString())
        }
    }

    fun createStatus(imageUrl: String) {
        val newStatus = Status(
            ChatUser(
                _userData.value?.userId,
                _userData.value?.name,
                _userData.value?.phone,
                _userData.value?.imageUrl,
            ),
            imageUrl,
            System.currentTimeMillis()
        )
        db.collection(STATUS).document().set(newStatus)
    }

    fun populateStatuses() {
        inStatusProgress.value = true
        val timeDelta = 24L * 60 * 60 * 1000
        val cutOff = System.currentTimeMillis() - timeDelta
        Log.d("ChattingViewModel", "CutOff time: $cutOff")

        db.collection(CHAT).where(
            Filter.or(
                Filter.equalTo("user1.userId", _userData.value?.userId),
                Filter.equalTo("user2.userId", _userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
                Log.e("ChattingViewModel", "Error fetching chats: ${error.message}")
                inStatusProgress.value = false
            }
            if (value != null) {
                val currentConnections = arrayListOf(_userData.value?.userId)
                val chats = value.toObjects<ChatData>()
                chats.forEach { chat ->
                    if (chat.user1.userId == _userData.value?.userId) {
                        currentConnections.add(chat.user2.userId)
                    } else currentConnections.add(chat.user1.userId)
                }
                Log.d("ChattingViewModel", "Current connections: $currentConnections")

                db.collection(STATUS).whereGreaterThan("timeStamp", cutOff)
                    .whereIn("user.userId", currentConnections)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            handleException(error)
                            inStatusProgress.value = false
                            Log.e("ChattingViewModel", "Error fetching statuses: ${error.message}")
                        }
                        if (value != null) {
                            Log.d("ChattingViewModel", "Statuses fetched: ${value.documents.size}")
                            statusList.value = value.toObjects()
                            Log.d("ChattingViewModel","these are the statuses ${statusList.value}")
                            inStatusProgress.value = false
                        } else {
                            Log.d("ChattingViewModel", "No statuses found.")
                        }
                    }
            } else {
                Log.d("ChattingViewModel", "No chats found for user: ${_userData.value?.userId}")
            }
        }
    }
}

