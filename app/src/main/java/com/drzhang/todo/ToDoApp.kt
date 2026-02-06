package com.drzhang.todo

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.room.Room
import com.drzhang.todo.data.AppDatabase

class TodoApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "todo.db"
        )
            // 初期开发阶段建议打开，方便调试
            // 正式版可以删掉
            .fallbackToDestructiveMigration()
            .build()

        backupDatabaseOnStart()
    }

    private fun backupDatabaseOnStart() {
        runCatching {
            // 确保数据库文件已经被创建
            database.openHelper.writableDatabase

            val sourceFile = getDatabasePath(DATABASE_NAME)
            if (!sourceFile.exists()) {
                Log.w(TAG, "Database file not found, skip backup.")
                return
            }

            contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, BACKUP_FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$BACKUP_DIR")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }
            )?.let { uri ->
                contentResolver.openOutputStream(uri, "w")?.use { output ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentResolver.update(
                        uri,
                        ContentValues().apply {
                            put(MediaStore.MediaColumns.IS_PENDING, 0)
                        },
                        null,
                        null
                    )
                }
            }
        }.onFailure {
            Log.e(TAG, "Failed to backup database on app start", it)
        }
    }

    companion object {
        private const val TAG = "TodoApp"
        private const val DATABASE_NAME = "todo.db"
        private const val BACKUP_DIR = "TodoMe"
        private const val BACKUP_FILE_NAME = "todo.db"
    }
}
