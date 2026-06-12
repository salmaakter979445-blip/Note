package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: NoteViewModel by viewModels {
    NoteViewModelFactory((applicationContext as NoteApplication).repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        NavHost(
          navController = navController,
          startDestination = "splash",
          modifier = Modifier.fillMaxSize()
        ) {
          composable("splash") {
            SplashScreen(navController = navController)
          }
          composable("home") {
            HomeScreen(navController = navController, viewModel = viewModel)
          }
          composable(
            route = "add_edit/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
          ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            AddEditNoteScreen(
              noteId = noteId,
              navController = navController,
              viewModel = viewModel
            )
          }
          composable("about") {
            AboutScreen(navController = navController, viewModel = viewModel)
          }
        }
      }
    }
  }
}
