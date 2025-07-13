package com.example.arcadecrawler

import android.app.Activity
import android.content.Context
import android.icu.number.NumberFormatter.UnitWidth
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DisplayMode.Companion.Picker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arcadecrawler.ui.theme.ArcadeCrawlerTheme

class MainActivity : ComponentActivity() {
    private val gameViewModel:GameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // If system bars are visible, reapply immersive mode
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                window.decorView.apply {
                    systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                            )
                }
            }
        }
        setContent {
            ArcadeCrawlerTheme {
                Box(modifier=Modifier.fillMaxSize()){
                    StartGame(gameViewModel=gameViewModel)
                }
            }
        }
    }
    override fun onPause(){
        super.onPause()
        gameViewModel.PauseMusic()
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.ResumeMusic()
    }
}
@Composable
fun StartGame(gameViewModel: GameViewModel, navController: NavHostController = rememberNavController()){

    NavHost(navController = navController, startDestination = Screens.HOME.name, modifier = Modifier.fillMaxSize().statusBarsPadding()){
        composable(Screens.HOME.name){
            HomeScreen(onsettingclick = {navController.navigate(Screens.SETTINGS.name)},
                onplayclick = {navController.navigate(Screens.GAME.name)},
                gameViewModel=gameViewModel)
            val context= LocalContext.current
            val sharedprefs=context.getSharedPreferences(shared_pref_filename, Context.MODE_PRIVATE)
            //val editor=sharedprefs.edit()

        }
        composable(Screens.SETTINGS.name) {
            ArcadeSettings(onnavigateup = {navController.navigateUp()}, gameViewModel = gameViewModel)
        }
        composable(Screens.GAME.name) {
            MainGame(gameViewModel=gameViewModel,
                onnavigateup = {
                    gameViewModel.ResetGame()
                    navController.navigateUp()},
                onnavigaterestart = {
                    navController.navigateUp()
                    gameViewModel.ResetGame()
                    navController.navigate(Screens.GAME.name)
                })
        }
    }
}

fun SetBrightness(context: Context,newbrightness:Float){
    val activity = context as Activity
    val layoutParams = activity.window.attributes
    layoutParams.screenBrightness=newbrightness
    activity.window.attributes=layoutParams
}
fun SetPreviousGameplayBrightness(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val brightness=prefs.getFloat("gameplaybrightness",0.7f)
    gameViewModel.gameplay_brightness=brightness
}
fun SetPreviousHomeScreenBrightness(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val brightness=prefs.getFloat("homescreenbrightness",0.7f)
    gameViewModel.homescreen_brightness=brightness
}
fun SetPreviousGunMovement(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val isgyro=prefs.getBoolean("isgyro",false)
    gameViewModel.isgyro=isgyro
}
fun SetPreviousGyroSensitivity(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val sensitvity=prefs.getFloat("gyrosensitvity",gameViewModel.gyro_sensitivity)
    gameViewModel.SetGyroSensitivity(sensitvity)
}