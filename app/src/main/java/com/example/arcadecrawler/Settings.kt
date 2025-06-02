package com.example.arcadecrawler

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun ArcadeSettings(onnavigateup:()->Unit,gameViewModel: GameViewModel){
     val context= LocalContext.current
    val sharedprefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val editor=sharedprefs.edit()

    var gunselectedoption by remember{mutableStateOf(sharedprefs.getInt("gunselectedoption",speed_options.indexOf(gameViewModel.gunvelocityfactor)))}
    var bulletselectedoption by remember{mutableStateOf(sharedprefs.getInt("bulletselectedoption", speed_options.indexOf(gameViewModel.bulletvelocity)))}
    var snakeselectedoption by remember{mutableStateOf(sharedprefs.getInt("snakeselectedoption", speed_options.indexOf(gameViewModel.snakevelocityfactor)))}

    var sliderpos by remember { mutableStateOf(gameViewModel.cur_volume) }
    Column(horizontalAlignment = Alignment.CenterHorizontally ,modifier= Modifier.fillMaxSize().background(color= colorResource(R.color.ivory)).padding(top=32.dp)){
        Box(modifier=Modifier.fillMaxWidth()){
            Image(
                painter= painterResource(R.drawable.back),
                contentDescription = null,
                modifier=Modifier
                    .align(Alignment.TopStart)
                    //.padding(8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        gameViewModel.PlayButtonClick()
                        editor.putFloat("bgvolume",sliderpos)
                        editor.apply()
                        onnavigateup()
                    }

            )
            Text(
                text="SETTINGS",
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color= colorResource(R.color.little_dark_purple),
                fontFamily = FontFamily(Font(R.font.arcade)),
                modifier=Modifier
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier=Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically){
            Text(
                text="Gun Speed: ",
                fontSize = 16.sp,
                fontWeight= FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.dark_gold),
                modifier=Modifier.weight(1f)
            )
            speed_options.forEachIndexed{idx, speed->
                val isSelected = (idx == gunselectedoption)
                Text(
                    text = speed.name,
                    fontWeight= FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if(isSelected) colorResource(R.color.mint) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            gunselectedoption = idx
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    color = colorResource(R.color.coral)
                )
            }
        }
        Spacer(modifier=Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically){
            Text(
                text="Bullet Speed: ",
                fontSize = 16.sp,
                fontWeight= FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.dark_gold),
                modifier=Modifier.weight(1f)
            )
            speed_options.forEachIndexed{idx, speed->
                val isSelected = (idx == bulletselectedoption)
                Text(
                    text = speed.name,
                    fontWeight= FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if(isSelected) colorResource(R.color.mint) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            bulletselectedoption = idx
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    color = colorResource(R.color.coral)
                )
            }
        }
        Spacer(modifier=Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically){
            Text(
                text="Snake Speed: ",
                fontSize = 16.sp,
                fontWeight= FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.dark_gold),
                modifier=Modifier.weight(1f)
            )
            speed_options.forEachIndexed{idx, speed->
                val isSelected = (idx == snakeselectedoption)
                Text(
                    text = speed.name,
                    fontWeight= FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if(isSelected) colorResource(R.color.mint) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            snakeselectedoption = idx
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    color = colorResource(R.color.coral)
                )
            }
        }
        Spacer(modifier=Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().padding(4.dp)) {
            Text(
                text = "Music Sound",
                textAlign = TextAlign.Center,
                color= colorResource(R.color.dark_gray),
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = sliderpos,
                onValueChange = {
                    sliderpos = it
                    gameViewModel.SetBgVolume(it)
                },
                valueRange = 0f..1f,
                modifier = Modifier
                    .weight(1f)
            )
        }
        Spacer(modifier=Modifier.height(16.dp))
        Image(
                painter= painterResource(R.drawable.tick),
                contentDescription = null,
                modifier=Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .clickable{
                        gameViewModel.PlayButtonClick()
                        gameViewModel.SetSnakeVelocityFactor(speed_options[snakeselectedoption])
                        gameViewModel.SetBulletVelocityFactor(speed_options[bulletselectedoption])
                        gameViewModel.SetGunVelocityFactor(speed_options[gunselectedoption])

                        editor.putInt("snakeselectedoption",snakeselectedoption)
                        editor.putInt("bulletselectedoption",bulletselectedoption)
                        editor.putInt("gunselectedoption",gunselectedoption)
                        editor.putFloat("bgvolume",sliderpos)
                        editor.apply()
                        onnavigateup()
                    }
            )
    }

}
fun SetPreviousSpeeds(gameViewModel: GameViewModel,context: Context){
    val sharedprefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    var gunselectedoption =sharedprefs.getInt("gunselectedoption",speed_options.indexOf(gameViewModel.gunvelocityfactor))
    var bulletselectedoption =sharedprefs.getInt("bulletselectedoption", speed_options.indexOf(gameViewModel.bulletvelocity))
    var snakeselectedoption  = sharedprefs.getInt("snakeselectedoption", speed_options.indexOf(gameViewModel.snakevelocityfactor))

    gameViewModel.SetSnakeVelocityFactor(speed_options[snakeselectedoption])
    gameViewModel.SetGunVelocityFactor(speed_options[gunselectedoption])
    gameViewModel.SetBulletVelocityFactor(speed_options[bulletselectedoption])
}
fun SetPreviousBgVolume(gameViewModel: GameViewModel,context: Context){
    val prefs=context.getSharedPreferences(shared_pref_filename,Context.MODE_PRIVATE)
    val vol=prefs.getFloat("bgvolume",1f)
    gameViewModel.SetBgVolume(vol)
}