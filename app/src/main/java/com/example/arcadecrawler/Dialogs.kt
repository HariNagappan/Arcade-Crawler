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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    var brightnesssliderpos by remember { mutableStateOf(gameViewModel.gameplay_brightness) }
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
                            gameViewModel.SetGameplayBrightness(newbrightness = it)
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
                .size(300.dp,190.dp)
                .background(shape = RoundedCornerShape(16.dp),color= colorResource(R.color.blueish))){
            Column (
                verticalArrangement = Arrangement.spacedBy(16.dp),horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.align(Alignment.Center).fillMaxSize().padding(
                    dimensionResource(R.dimen.med_padding)
                )){
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
                            .size(32.dp)
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