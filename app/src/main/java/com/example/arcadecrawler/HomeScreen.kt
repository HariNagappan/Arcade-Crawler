package com.example.arcadecrawler

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onsettingclick:() ->Unit,onplayclick:() ->Unit,gameViewModel: GameViewModel){
    var about_dialog_visible by remember { mutableStateOf(false) }
    var snake_dialog_visible by remember{mutableStateOf(false)}
    val context= LocalContext.current
    SetPreviousSpeeds(gameViewModel=gameViewModel,context=context)
    SetPreviousHomeScreenBrightness(context=context,gameViewModel=gameViewModel)
    SetPreviousGameplayBrightness(context=context,gameViewModel=gameViewModel)
    SetBrightness(context=context, newbrightness = gameViewModel.homescreen_brightness)
    SetPreviousGunMovement(context=context,gameViewModel=gameViewModel)
    SetPreviousGyroSensitivity(context=context,gameViewModel=gameViewModel)
    if(gameViewModel.bgplayer==null) {
        gameViewModel.SetMusicPlayers(context = context)
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
