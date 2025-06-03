package com.example.arcadecrawler

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.Px
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog

@Composable
fun MainGame(gameViewModel: GameViewModel,onnavigateup:()->Unit,onnavigaterestart:()->Unit){

    Box(modifier= Modifier.fillMaxSize()){
        MainCanvas(gameViewModel=gameViewModel,
            onnavigateup={onnavigateup},
            modifier=Modifier
            .fillMaxSize()
            .align(Alignment.Center)
            .padding(bottom= blackwhitegridheight))

        BlackWhiteGrid(modifier=Modifier.align(Alignment.BottomCenter))
        Image(
            painter=painterResource(R.drawable.pause),
            contentDescription = null,
            modifier=Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(CircleShape)
                .clickable {
                    gameViewModel.PlayButtonClick()
                    gameViewModel.PauseGame()
                }
        )
        if(gameViewModel.paused && !gameViewModel.iswin && !gameViewModel.islost){
            PauseDialog(onnavigateup = {
                onnavigateup()},
                onreset = {
                    onnavigaterestart()
                },
                onresume = {
                    gameViewModel.ResumeGame()
            },
                gameViewModel = gameViewModel)
        }
        if(gameViewModel.iswin){
            WinDialog(
                onnavigateup={
                    onnavigateup()},
                ondismiss = {gameViewModel.iswin=false},
                onnavigaterestart={
                    onnavigaterestart()},
                cur_bullet_count = gameViewModel.total_bullet_count,
                gameViewModel = gameViewModel)
        }
        if(gameViewModel.islost){
            LoseDialog(
                onnavigateup={onnavigateup()},
                ondismiss = {gameViewModel.islost=false},
                onnavigaterestart={
                    onnavigaterestart()},
                cur_bullet_count = gameViewModel.total_bullet_count,
                gameViewModel=gameViewModel)
        }
    }
}
@Composable
fun MainCanvas(gameViewModel: GameViewModel,onnavigateup: () -> Unit,modifier:Modifier){
    val localdensity= LocalDensity.current
    val context= LocalContext.current

    val gunbitmap= ImageBitmap.imageResource(R.drawable.gun)
    val bulletfirebitmap=ImageBitmap.imageResource(R.drawable.bulletfire)
    val bulletbitap=ImageBitmap.imageResource(R.drawable.bullet)
    val mushroombitmap=ImageBitmap.imageResource(R.drawable.mushroom)
    val snakeheadbitmap=ImageBitmap.imageResource(R.drawable.snakehead)
    val snakenodebitmap=ImageBitmap.imageResource(R.drawable.snake)
    val spiderbitmap=ImageBitmap.imageResource(R.drawable.spider)


    var joystickcenter = with(localdensity){Offset(320.dp.toPx(),660.dp.toPx())}
    var thumboffset by remember{mutableStateOf(joystickcenter)}
    var isdragging by remember{ mutableStateOf(false)}
    var canvas_size by remember{mutableStateOf(IntSize.Zero)}
    var velocity_factor by remember{ mutableStateOf(gameViewModel.GetGunVelocityFactor())}

    var bulletfireoffset:Offset
    var originalguntopleft:Offset
    var newgunoffset:Offset
    var thumbmoveoffset=Offset.Zero

    val bulletlist=gameViewModel.bullet_list
    val mushroomlist=gameViewModel.mushroom_list
    val snakelist=gameViewModel.snake_list

    var mushroomdummy by remember{mutableStateOf(0f)}
    val bgcolor= colorResource(R.color.ivory)

    with(localdensity){
        gameViewModel.SetInnerRadius(24.dp.toPx())
        gameViewModel.SetOuterRadius(40.dp.toPx())
        bulletfireoffset=Offset(28.dp.toPx(),joystickcenter.y-bulletfirebitmap.height/2)
        originalguntopleft=Offset(180.dp.toPx(),joystickcenter.y-gunbitmap.height/2)
    }

    LaunchedEffect(originalguntopleft) {
        gameViewModel.UpdateGunPosition(originalguntopleft)
    }
    LaunchedEffect(mushroomdummy) {
        gameViewModel.AddMushrooms(
            leastx = 0f,
            maxx = (canvas_size.width - mushroombitmap.width).toFloat(),
            leasty = mushroombitmap.height.toFloat()*2f,
            maxy = (originalguntopleft.y - with(localdensity) { 70.dp.toPx() } - mushroombitmap.height),
            mushroomwidth = mushroombitmap.width.toFloat(),
            mushroomheight = mushroombitmap.height.toFloat()
        )
    }
    LaunchedEffect(gameViewModel.initial_snakes) {
        Log.d("initialsnakes","${gameViewModel.initial_snakes}")
        if(gameViewModel.snake_list.isEmpty()){
            gameViewModel.AddSnake(
                //start_position = Offset(mushroombitmap.width.toFloat(), mushroombitmap.height.toFloat()),
                start_position = Offset(0f,-snakenodebitmap.height.toFloat()*1.2f),
                nodewidth = snakenodebitmap.width.toFloat(),
                movement = Movement.RIGHT,
                nodeheight = snakenodebitmap.height.toFloat()
            )
            gameViewModel.gunbitmapwidth=gunbitmap.width.toFloat()
            gameViewModel.gunbitmapheight=gunbitmap.height.toFloat()
        }
    }
    LaunchedEffect(gameViewModel.should_spawn_spider) {
        val possibleoffsets= listOf(Offset(0f, joystickcenter.y - gameViewModel.joyStick.outerradius * 3f),Offset(canvas_size.width/2f, joystickcenter.y - gameViewModel.joyStick.outerradius * 3f),Offset((canvas_size.width-spiderbitmap.width).toFloat(), joystickcenter.y - gameViewModel.joyStick.outerradius * 3f))
        if (gameViewModel.should_spawn_spider && gameViewModel.spider_list.isEmpty()) {
            val random_start_position=possibleoffsets.random()

            gameViewModel.SetSpiderStartPosition(random_start_position)
            gameViewModel.AddSpider(movement = GetRandomMovementBasedOnIdx(idx=possibleoffsets.indexOf(random_start_position)),
                bitmap_width = spiderbitmap.width.toFloat(),
                bitmap_height = spiderbitmap.height.toFloat())
        }
    }

    if(isdragging){
        thumbmoveoffset=thumboffset-joystickcenter
        newgunoffset=gameViewModel.gun_position+ thumbmoveoffset * velocity_factor
        newgunoffset=Offset(newgunoffset.x.coerceIn(0f,canvas_size.width.toFloat()-gunbitmap.width.toFloat()),newgunoffset.y.coerceIn(originalguntopleft.y- with(localdensity){40.dp.toPx()},canvas_size.height.toFloat()-gunbitmap.height.toFloat()))
        gameViewModel.UpdateGunPosition(newgunoffset)
    }
    gameViewModel.SetJoystickThumbPosition(thumboffset)

    SetBrightness(context=context, newbrightness = gameViewModel.cur_brightness)
    Canvas(modifier=Modifier
        .then(modifier)
        .onSizeChanged { size:IntSize->
            canvas_size=size
        }
        .pointerInput("Joystick"){
            detectDragGestures(
                onDrag={ change, dragAmount ->
                        thumboffset= if(isdragging || (change.position-joystickcenter).getDistance()<gameViewModel.joyStick.outerradius)
                            {
                                GetEquivalentThumbPosition(curtouched = change.position,center=joystickcenter, radius = gameViewModel.joyStick.outerradius)
                            }
                        else
                            {thumboffset}
                        gameViewModel.SetJoystickThumbPosition(thumboffset)
                        isdragging=true
                    },
                onDragEnd = {
                    thumboffset=joystickcenter
                    isdragging=false
                }
            )
        }
        .pointerInput("LaunchBullet"){
            detectTapGestures(
                onTap = {change ->
                    val tmp=Offset(bulletfireoffset.x+bulletfirebitmap.width/2f,bulletfireoffset.y+bulletfirebitmap.height/2f)
                    if((change-tmp).getDistance()<=bulletfirebitmap.width){
                        //fire bullet

                    }
                }
            )
        })
    {

        drawRect(
            color=bgcolor,
            topLeft = Offset.Zero,
            size = canvas_size.toSize()
            )
        drawImage(
            image = gunbitmap,
            topLeft = gameViewModel.gun_position,
        )
        drawCircle(
            color=Color.LightGray,
            radius = gameViewModel.joyStick.outerradius,
            center=joystickcenter
        )
        drawCircle(
            color=Color.DarkGray,
            radius = gameViewModel.joyStick.innerradius,
            center=gameViewModel.GetJoystickThumbPosition()
        )
        drawImage(
            image=bulletfirebitmap,
            topLeft = bulletfireoffset
        )
        bulletlist.forEach { bullet->
            drawImage(
                image=bulletbitap,
                topLeft = bullet.bullet_position.value
            )
        }
        gameViewModel.MoveBullets()
        mushroomlist.forEach { mushroom->
            drawImage(
                image = mushroombitmap,
                topLeft = mushroom.mushroom_position,
                alpha = mushroom.health/5f
            )
        }

        snakelist.forEach { snake ->
            snake.node_lst.forEach { node->
                if(node.hierarchy==SnakeHierarchy.HEAD){
                    rotate(
                        degrees = if (node.movement == Movement.RIGHT) 180f else 0f,
                        pivot = Offset(node.node_position.value.x + snake.bitmap_width/2f,node.node_position.value.y + snake.bitmap_height/2f),
                    ) {
                        drawImage(image = snakeheadbitmap, topLeft = node.node_position.value)
                    }
                }
                else{
                    drawImage(image = snakenodebitmap, topLeft = node.node_position.value)
                }
            }
        }
        gameViewModel.MoveSnakes(
            leastx =0f,
            maxx = canvas_size.width.toFloat(),
            maxy=canvas_size.height.toFloat(),
            step_down_height = snakeheadbitmap.width.toFloat()*1.2f
        )
        gameViewModel.spider_list.forEach{ spider->
            drawImage(
                image=spiderbitmap,
                topLeft = spider.spider_position.value
            )
        }
        gameViewModel.MoveSpiders(leastx = 0f, maxx = canvas_size.width.toFloat(), leasty = 0f, maxy = canvas_size.height.toFloat())
    }
    Box(modifier=Modifier
        .offset {
            IntOffset(
                bulletfireoffset.x.toInt(),
                bulletfireoffset.y.toInt()
            )
        }
        .size(
            with(localdensity){bulletfirebitmap.width.toDp()},
            with(localdensity){bulletfirebitmap.height.toDp()}
        )
        .clip(CircleShape)
        .background(Color.Transparent) // makes it hit-testable
        .clickable {
            Log.d("detectingtap", "attack button clicked")
            gameViewModel.AddBullet(
                start_position = gameViewModel.gun_position+Offset((gunbitmap.width-bulletbitap.width)/2f,-bulletbitap.height.toFloat()),
                width = gunbitmap.width.toFloat(),
                height = gunbitmap.height.toFloat()
            )
        })
}
@Composable
fun BlackWhiteGrid(cols:Int=20,modifier:Modifier){
    val cellColors = remember(cols) {
        List(cols * 10) { i ->
            if ((i / cols + i % cols) % 2 == 0) Color.White else Color.Black
        }
    }
    Canvas(
        modifier=Modifier
            .fillMaxWidth()
            .height(blackwhitegridheight)
            .then(modifier)
    ) {
        val cellsize=size.width/cols
        for(i in 0 until (size.height/cellsize).toInt()){
           for(j in 0 until cols){
                drawRect(
                    color = cellColors[i * cols + j],
                    size = Size(cellsize,cellsize),
                    topLeft = Offset(x=j*cellsize,y=i*cellsize))
            }
        }
    }
}
@Composable
fun PauseDialog(onnavigateup: () -> Unit,onreset:() ->Unit,onresume:()->Unit,gameViewModel: GameViewModel){
    var bgsliderpos by remember { mutableStateOf(gameViewModel.cur_volume) }
    var brightnesssliderpos by remember { mutableStateOf(gameViewModel.cur_brightness) }
    val context=LocalContext.current
    Dialog(onDismissRequest = {
        val prefs= context.getSharedPreferences(
            shared_pref_filename, Context.MODE_PRIVATE)
        val edit=prefs.edit()
        edit.putFloat("bgvolume",bgsliderpos)
        edit.putFloat("screenbrightness",brightnesssliderpos)
        edit.apply()
        onresume()}){
        Box(modifier=Modifier
            .size(300.dp,220.dp)
            .border(width=4.dp,color=Color.Black,shape=RoundedCornerShape(32.dp))
            .background(color= colorResource(R.color.blueish),shape=RoundedCornerShape(32.dp))) {

            Text(
                text = "PAUSED",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.arcade)),
                textAlign = TextAlign.Center,
                color= colorResource(R.color.dark_gold),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally,modifier=Modifier.align(Alignment.Center)) {
                Row(verticalAlignment = Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().padding(start=4.dp,end=4.dp)) {
                    Text(
                        text = "Music Sound",
                        textAlign = TextAlign.Center,
                        color= colorResource(R.color.dark_gray),
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
                        textAlign = TextAlign.Center,
                        color= colorResource(R.color.dark_gray),
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = brightnesssliderpos,
                        onValueChange = {
                            brightnesssliderpos = it
                            SetBrightness(context=context, newbrightness = it)
                            gameViewModel.SetBrightness(newbrightness = it)
                                        },
                        valueRange = 0.1f..1f,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier=Modifier.align(Alignment.BottomCenter)
            ) {
                Image(
                    painter=painterResource(R.drawable.home),
                    contentDescription = null,
                    modifier=Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            val prefs= context.getSharedPreferences(
                                shared_pref_filename, Context.MODE_PRIVATE)
                            val edit=prefs.edit()
                            edit.putFloat("bgvolume",bgsliderpos)
                            edit.putFloat("screenbrightness",brightnesssliderpos)
                            edit.apply()
                            onnavigateup()
                        }
                )
                Spacer(modifier=Modifier.width(24.dp))
                Image(
                    painter=painterResource(R.drawable.restart),
                    contentDescription = null,
                    modifier=Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            val prefs= context.getSharedPreferences(
                                shared_pref_filename, Context.MODE_PRIVATE)
                            val edit=prefs.edit()
                            edit.putFloat("bgvolume",bgsliderpos)
                            edit.putFloat("screenbrightness",brightnesssliderpos)
                            edit.apply()
                            onreset()
                        }
                )
                Spacer(modifier=Modifier.width(24.dp))
                Image(
                    painter=painterResource(R.drawable.miniplay),
                    contentDescription = null,
                    modifier=Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                        .clickable {
                            gameViewModel.PlayButtonClick()
                            val prefs= context.getSharedPreferences(
                                shared_pref_filename, Context.MODE_PRIVATE)
                            val edit=prefs.edit()
                            edit.putFloat("bgvolume",bgsliderpos)
                            edit.putFloat("screenbrightness",brightnesssliderpos)
                            edit.apply()
                            onresume()
                        }
                )
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
fun GetEquivalentThumbPosition(curtouched:Offset,center:Offset,radius:Float):Offset{
    val distance=(curtouched-center).getDistance()
    if((curtouched-center).getDistance()<radius){
        return curtouched
    }
    return Offset((radius * curtouched.x + (distance-radius)*center.x)/distance,(radius * curtouched.y + (distance-radius)*center.y)/distance)
}
fun GetRandomMovementBasedOnIdx(idx:Int):Movement{//modifiy for larger lists
    if(idx==0){
        return listOf(Movement.RIGHT,Movement.DIAGONAL_TOP_RIGHT,Movement.DIAGONAL_BOTTOM_RIGHT).random()
    }
    else if(idx==1){
        return all_movements.random()
    }
    return listOf(Movement.LEFT,Movement.DIAGONAL_TOP_LEFT,Movement.DIAGONAL_BOTTOM_LEFT).random()
}