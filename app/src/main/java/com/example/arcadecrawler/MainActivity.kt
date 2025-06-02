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
                    StartGame()
                }
            }
        }
    }
}
@Composable
fun StartGame(navController: NavHostController = rememberNavController()){
    val gameViewModel:GameViewModel = viewModel()
    NavHost(navController = navController, startDestination = Screens.HOME.name, modifier = Modifier.fillMaxSize().statusBarsPadding()){
        composable(Screens.HOME.name){
            StartScreen(onsettingclick = {navController.navigate(Screens.SETTINGS.name)},
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
@Composable
fun StartScreen(onsettingclick:() ->Unit,onplayclick:() ->Unit,gameViewModel: GameViewModel){
    var about_dialog_visible by remember { mutableStateOf(false) }
    var snake_dialog_visible by remember{mutableStateOf(false)}
    val context= LocalContext.current
    SetPreviousSpeeds(gameViewModel=gameViewModel,context=context)
    SetPreviousBrightness(context=context,gameViewModel=gameViewModel)
    SetBrightness(context=context, newbrightness = 0.7f)
    if(gameViewModel.bgplayer==null) {
        gameViewModel.SetMediaPlayer(context = context)
    }
    Log.d("arcadething","yess")
    if(gameViewModel.IsBgPlayerInitialized() && !gameViewModel.bgplayer!!.isPlaying) {
        gameViewModel.StartMusic()
    }
    SetPreviousBgVolume(gameViewModel=gameViewModel,context=context)
    Box(modifier=Modifier.fillMaxSize()){
        Image(
            painter = painterResource(R.drawable.homescreen),
            contentDescription = null,
            modifier=Modifier.fillMaxSize().align(Alignment.Center),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text="ARCADE",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color=colorResource(R.color.little_dark_purple),
            fontFamily = FontFamily(Font(R.font.arcade)),
            modifier=Modifier
                .align(Alignment.TopCenter)
                .padding(top=32.dp)
        )
        Text(
            text="CRAWLER",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color=colorResource(R.color.little_dark_purple),
            fontFamily = FontFamily(Font(R.font.arcade)),
            modifier=Modifier
                .align(Alignment.TopCenter)
                .padding(top=92.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier=Modifier.align(Alignment.BottomCenter).padding(bottom=32.dp).fillMaxWidth()){
            Box(modifier=Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.settings),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                        .size(110.dp)
                        .clip(CircleShape)
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            onsettingclick()
                        }
                )
                Image(
                    painter = painterResource(R.drawable.play),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            snake_dialog_visible=true
                        }
                        .align(Alignment.Center)
                        //.padding(8.dp)
                )
                Image(
                    painter = painterResource(R.drawable.info),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp)
                        .size(110.dp)
                        .clip(CircleShape)
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            about_dialog_visible=true
                        }
                )
            }
        }
        if(about_dialog_visible){
            AboutDialog(ondismiss = {about_dialog_visible=false},
                gameViewModel=gameViewModel)
        }
        if(snake_dialog_visible){
            SnakeDialog(ondismiss = {snake_dialog_visible=false},onplayclick={onplayclick()},gameViewModel=gameViewModel)
        }
    }
}
@Composable
fun AboutDialog(ondismiss:() ->Unit,gameViewModel: GameViewModel){
    Dialog(onDismissRequest = {ondismiss()}) {
            Box(modifier=Modifier.size(350.dp,400.dp).background(color= colorResource(R.color.blueish), shape = RoundedCornerShape(33.dp))) {
                Image(
                    painter = painterResource(R.drawable.close),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            ondismiss()
                        }
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "ABOUT",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = colorResource(R.color.little_dark_purple),
                        fontFamily = FontFamily(Font(R.font.arcade))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = stringResource(R.string.about_message),
                            textAlign = TextAlign.Center,
                            color= colorResource(R.color.dark_gold),
                            fontFamily = FontFamily(Font(R.font.arcadebody))
                        )
                    }
                }
            }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeDialog(ondismiss: () -> Unit,onplayclick: () -> Unit,gameViewModel: GameViewModel){
    val items = listOf(1,2,3,4,5,6,7,8,9,10)
    var expanded by remember { mutableStateOf(false) }
    val sharedprefs= LocalContext.current.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    var selectedItem by remember { mutableStateOf(sharedprefs.getInt("selecteditem",1).toString()) }

    Dialog(onDismissRequest = {ondismiss()}) {
        Box (
            modifier=Modifier
                .size(300.dp,250.dp)
                .background(shape = RoundedCornerShape(16.dp),color= colorResource(R.color.blueish))){
            Column (verticalArrangement = Arrangement.SpaceBetween,horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.align(Alignment.Center).fillMaxSize().padding(8.dp)){
                Box(modifier=Modifier.fillMaxWidth()) {
                    Text(
                        text = "Snakes",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily(Font(R.font.arcade)),
                        modifier=Modifier.align(Alignment.Center),
                        color= colorResource(R.color.little_dark_purple)
                    )
                    Image(
                        painter=painterResource(R.drawable.close),
                        contentDescription = null,
                        modifier=Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                ondismiss()
                            }
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedItem,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Number of Snakes") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier=Modifier
                            .heightIn(max = 150.dp)
                    ) {
                        items.forEach { element ->
                            DropdownMenuItem(
                                text = { Text(element.toString()) },
                                onClick = {
                                    gameViewModel.PlayButtonClick()
                                    selectedItem = element.toString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        gameViewModel.PlayButtonClick()
                        val editor=sharedprefs.edit()
                        editor.putInt("selecteditem",selectedItem.toInt())
                        editor.apply()
                        gameViewModel.SetSnakes(selectedItem.toInt()-1)
                        gameViewModel.ResetGame()
                        ondismiss()
                        onplayclick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.green))
                ){
                    Text(
                        text="PLAY",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color= colorResource(R.color.ivory),
                        fontFamily = FontFamily(Font(R.font.arcadebody))
                    )
                }
            }
        }
    }
}
fun SetBrightness(context: Context,newbrightness:Float){
    val activity = context as Activity
    val layoutParams = activity.window.attributes
    layoutParams.screenBrightness=newbrightness
    activity.window.attributes=layoutParams
}
fun SetPreviousBrightness(context: Context,gameViewModel: GameViewModel){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val brightness=prefs.getFloat("screenbrightness",0.7f)
    gameViewModel.cur_brightness=brightness
}