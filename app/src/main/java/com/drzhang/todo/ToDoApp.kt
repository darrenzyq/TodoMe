package com.drzhang.todo

import android.app.Application
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
    }
}
