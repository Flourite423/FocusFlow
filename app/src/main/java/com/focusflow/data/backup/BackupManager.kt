package com.focusflow.data.backup

import android.content.Context
import android.net.Uri
import com.focusflow.data.db.FocusFlowDatabase

// TODO: Implement full backup/restore with JSON serialization
class BackupManager(private val db: FocusFlowDatabase) {
    suspend fun exportToJson(context: Context): Uri = Uri.EMPTY
    suspend fun importFromJson(context: Context, uri: Uri): Boolean = false
}
