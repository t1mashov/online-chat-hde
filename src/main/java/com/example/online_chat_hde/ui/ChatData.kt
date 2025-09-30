package com.example.online_chat_hde.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.online_chat_hde.models.Staff
import com.example.online_chat_hde.models.UserData
import com.example.online_chat_hde.viewmodels.ChatViewModel
import com.example.online_chat_hde.viewmodels.UiMessage

@Stable
interface ChatData {
    @Composable fun messages(): SnapshotStateList<UiMessage>
    @Composable fun loadingMessages(): SnapshotStateList<UiMessage>

    @Composable fun isGlobalLoading(): MutableState<Boolean>
    @Composable fun isConnected(): MutableState<Boolean>

    @Composable fun userData(): MutableState<UserData?>
    @Composable fun staff(): MutableState<Staff?>

}

val LocalChatData = staticCompositionLocalOf<ChatData> {
    error("ChatData not provided")
}


//internal class ViewModelChatData (
//    private val vm: ChatViewModel
//): ChatData {
//    @Composable
//    override fun messages(): SnapshotStateList<UiMessage> = vm.messages
//
//    @Composable
//    override fun loadingMessages(): SnapshotStateList<UiMessage> = vm.loadingMessages
//
//    @Composable
//    override fun isGlobalLoading(): MutableState<Boolean> = vm.isGlobalLoading
//
//    @Composable
//    override fun isConnected(): MutableState<Boolean> = vm.isConnected
//
//    @Composable
//    override fun userData(): MutableState<UserData?> = vm.userData
//
//    @Composable
//    override fun staff(): MutableState<Staff?> = vm.staff
//}



//@Composable
//fun ProvideChatData(vm: ChatViewModel, content: @Composable () -> Unit) {
//    val data = remember(vm) { ViewModelChatData(vm) }
//    CompositionLocalProvider(LocalChatData provides data, content = content)
//}