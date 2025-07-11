package com.example.arcadecrawler

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog

@Composable
fun PauseDialog(onnavigateup: () -> Unit,onreset:() ->Unit,onresume:()->Unit,gameViewModel: GameViewModel){
    var bgsliderpos by remember { mutableStateOf(gameViewModel.cur_volume) }
    var brightnesssliderpos by remember { mutableStateOf(gameViewModel.cur_brightness) }
    var gyrosliderpos by remember { mutableStateOf(gameViewModel.gyro_sensitivity) }
    val context=LocalContext.current
    Dialog(onDismissRequest = {
        val prefs= context.getSharedPreferences(
            shared_pref_filename, Context.MODE_PRIVATE)
        val edit=prefs.edit()
        edit.putFloat("bgvolume",bgsliderpos)
        edit.putFloat("screenbrightness",brightnesssliderpos)
        edit.putFloat("gyrosensitvity",gyrosliderpos)
        edit.apply()
        onresume()}){
        Box(modifier=Modifier
            //.size(300.dp,220.dp)
            .border(width=4.dp,color=Color.Black,shape=RoundedCornerShape(32.dp))
            .background(color= colorResource(R.color.blueish),shape=RoundedCornerShape(32.dp))) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,modifier=Modifier.align(Alignment.Center).padding(8.dp)) {
                Text(
                    text = "PAUSED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.arcade)),
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.dark_gold),
                    modifier = Modifier
                        .padding(8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp)
                ) {
                    Text(
                        text = "Music Sound",
                        fontFamily = FontFamily(Font(R.font.arcadebody)),
                        textAlign = TextAlign.Center,
                        color = colorResource(R.color.dark_gray),
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = bgsliderpos,
                        onValueChange = {
                            bgsliderpos = it
                            gameViewModel.SetBgVolume(it)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Screen Brightness",
                        fontFamily = FontFamily(Font(R.font.arcadebody)),
                        textAlign = TextAlign.Center,
                        color = colorResource(R.color.dark_gray),
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = brightnesssliderpos,
                        onValueChange = {
                            brightnesssliderpos = it
                            SetBrightness(context = context, newbrightness = it)
                            gameViewModel.SetBrightness(newbrightness = it)
                        },
                        valueRange = 0.1f..1f,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
                if(gameViewModel.isgyro){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp)
                    ) {
                        Text(
                            text = "Gyro Sensitivity",
                            fontFamily = FontFamily(Font(R.font.arcadebody)),
                            textAlign = TextAlign.Center,
                            color = colorResource(R.color.dark_gray),
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = gyrosliderpos,
                            onValueChange = {
                                gyrosliderpos = it
                                gameViewModel.SetGyroSensitivity(it)
                            },
                            valueRange = 10f..50f,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.home),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                val prefs = context.getSharedPreferences(
                                    shared_pref_filename, Context.MODE_PRIVATE
                                )
                                val edit = prefs.edit()
                                edit.putFloat("bgvolume", bgsliderpos)
                                edit.putFloat("gyrosensitvity",gyrosliderpos)
                                edit.putFloat("screenbrightness", brightnesssliderpos)
                                edit.apply()
                                onnavigateup()
                            }
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Image(
                        painter = painterResource(R.drawable.restart),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()

                                val prefs = context.getSharedPreferences(
                                    shared_pref_filename, Context.MODE_PRIVATE
                                )
                                val edit = prefs.edit()
                                edit.putFloat("bgvolume", bgsliderpos)
                                edit.putFloat("gyrosensitvity",gyrosliderpos)
                                edit.putFloat("screenbrightness", brightnesssliderpos)
                                edit.apply()

                                onreset()
                            }
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Image(
                        painter = painterResource(R.drawable.miniplay),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()

                                val prefs = context.getSharedPreferences(
                                    shared_pref_filename, Context.MODE_PRIVATE
                                )
                                val edit = prefs.edit()
                                edit.putFloat("bgvolume", bgsliderpos)
                                edit.putFloat("gyrosensitvity",gyrosliderpos)
                                edit.putFloat("screenbrightness", brightnesssliderpos)
                                edit.apply()

                                onresume()
                            }
                    )
                }
            }
        }
    }
}
@Composable
fun WinDialog(ondismiss:() ->Unit,onnavigateup: () -> Unit,onnavigaterestart: () -> Unit,cur_bullet_count:Int,gameViewModel: GameViewModel){
    Dialog(onDismissRequest = {}){
        Box(modifier=Modifier
            .size(300.dp,240.dp)
            .border(width=4.dp,color= colorResource(R.color.dark_gray), shape = RoundedCornerShape(24.dp))
            .background(color= colorResource(R.color.mint), shape = RoundedCornerShape(24.dp)))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.SpaceBetween,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(16.dp)
            )
            {
                Text(
                    text="YOU WON",
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily(Font(R.font.circlefont)),
                    color= colorResource(R.color.little_dark_gold),
                    fontWeight = FontWeight.Bold
                )

                Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text="Bullets Shot",
                            textAlign = TextAlign.Center,
                            color = colorResource(R.color.dark_gold),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text="$cur_bullet_count",
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center,
                            color= colorResource(R.color.dark_green),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text="Bullets",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center){
                    Image(
                        painter= painterResource(R.drawable.home),
                        contentDescription = null,
                        modifier=Modifier
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                ondismiss()
                                onnavigateup()
                            }
                    )
                    Spacer(modifier=Modifier.width(16.dp))
                    Image(
                        painter= painterResource(R.drawable.restart),
                        contentDescription = null,
                        modifier=Modifier
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                ondismiss()
                                onnavigaterestart()
                            }
                    )
                }
            }
        }
    }
}
@Composable
fun LoseDialog(onnavigateup: () -> Unit, ondismiss:() ->Unit,onnavigaterestart: () -> Unit,cur_bullet_count:Int,gameViewModel: GameViewModel){
    Dialog(onDismissRequest = {}){
        Box(modifier=Modifier
            .size(300.dp,240.dp)
            .border(width=4.dp,color= colorResource(R.color.dark_gray), shape = RoundedCornerShape(24.dp))
            .background(color= colorResource(R.color.mint), shape = RoundedCornerShape(24.dp)))
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.SpaceBetween,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(16.dp)
            )
            {
                Text(
                    text="YOU LOST",
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily(Font(R.font.circlefont)),
                    color= colorResource(R.color.error_red),
                    fontWeight = FontWeight.Bold
                )

                Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text="Bullet Shot",
                            textAlign = TextAlign.Center,
                            color = colorResource(R.color.dark_gold),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text="$cur_bullet_count",
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center,
                            color= colorResource(R.color.dark_green),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text="Bullets",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center){
                    Image(
                        painter= painterResource(R.drawable.home),
                        contentDescription = null,
                        modifier=Modifier
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                ondismiss()
                                onnavigateup()
                            }
                    )
                    Spacer(modifier=Modifier.width(16.dp))
                    Image(
                        painter= painterResource(R.drawable.restart),
                        contentDescription = null,
                        modifier=Modifier
                            .clip(CircleShape)
                            .clickable {
                                gameViewModel.PlayButtonClick()
                                ondismiss()
                                onnavigaterestart()
                            }
                    )
                }
            }
        }
    }
}