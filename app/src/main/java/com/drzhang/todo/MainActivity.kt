package com.drzhang.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.drzhang.todo.data.TaskRepository
import com.drzhang.todo.mode.TaskViewModel
import com.drzhang.todo.mode.TaskViewModelFactory
import com.drzhang.todo.ui.pages.MainScreen
import com.drzhang.todo.ui.theme.MyToDoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by lazy {
        val app = application as TodoApp
        val dao = app.database.taskDao()
        val repository = TaskRepository(dao)

        ViewModelProvider(this, TaskViewModelFactory(repository))[TaskViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(isSystemInDarkTheme()) }
            MyToDoTheme(darkTheme = darkTheme) {
                MainScreen(
                    viewModel = viewModel,
                    isDarkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme }
                )
            }
        }
    }
}
