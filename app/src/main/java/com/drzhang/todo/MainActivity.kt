package com.drzhang.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
            MyToDoTheme {
                MainScreen(viewModel)
            }
        }
    }
}