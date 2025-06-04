package com.example.arcadecrawler

import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.annotation.Px
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.max
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class GameViewModel: ViewModel() {
    var gun_position by mutableStateOf(Offset.Zero)
        private set
    var joyStick by mutableStateOf(Joystick())
        private set

    var gunvelocityfactor by mutableStateOf(Speed.SLOW)
        private set
    var gunbitmapwidth=0f
    var gunbitmapheight=0f

    var bulletvelocity by mutableStateOf(Speed.MEDIUM)
    val bullet_list = mutableStateListOf<Bullet>()
    private var bullet_count = 0
        private set
    var total_bullet_count=0
        private set

    var initial_snakes by mutableStateOf(1)
        private set
    var snakevelocityfactor by mutableStateOf(Speed.SLOW)
    var snake_length by mutableStateOf(9)
        private set
    val snake_list = mutableStateListOf<Snake>()
    var cur_snake_id = 0
        private set
    var snakes_to_remove= mutableListOf<Snake>()

    var spider_count =0
    var should_spawn_spider by mutableStateOf(true)
    val spider_list = mutableStateListOf<Spider>()
    val spider_velocity by mutableStateOf(6f)
    var spider_spawn_delay = 5000f
    var spider_start_position=Offset.Zero
    var spiderbitmapwidth=0f
    var spiderbitmapheight=0f


    var org_mushroom_count by mutableStateOf(10)
    var cur_mushroom_id = 0
        private set
    val mushroom_list = mutableStateListOf<Mushroom>()

    var bgplayer:MediaPlayer?=null
    lateinit var buttonplayer:MediaPlayer
    var cur_volume by mutableStateOf(1f)

    var cur_brightness by mutableStateOf(0.7f)

    var iswin by mutableStateOf(false)
    var islost by mutableStateOf(false)
    var paused by mutableStateOf(false)
    private var private_initial_snakes=0
    private var mushroomwidth:Float=0f
    private var mushroomheight:Float=0f
    private var spiderdelaythread:Job?=null


    fun UpdateGunPosition(newOffset: Offset) {
        gun_position = newOffset
    }
    fun SetJoystickThumbPosition(newOffset: Offset) {
        joyStick = joyStick.copy(thumbpositon = newOffset)
    }
    fun SetInnerRadius(innerradius: Float) {
        joyStick.innerradius = innerradius
    }
    fun SetOuterRadius(outerradius: Float) {
        joyStick.outerradius = outerradius
    }
    fun GetJoystickThumbPosition(): Offset {
        return joyStick.thumbpositon
    }
    fun GetGunVelocityFactor(): Float {
        if (gunvelocityfactor == Speed.SLOW) {
            return 0.1f
        } else if (gunvelocityfactor == Speed.MEDIUM) {
            return 0.2f
        }
        return 0.3f
    }
    fun SetGunVelocityFactor(newfactor: Speed) {
        gunvelocityfactor = newfactor
    }

    fun SetSnakes(new_snakes: Int) {
        initial_snakes = new_snakes
        private_initial_snakes=initial_snakes
    }
    fun AddSnake(
        start_position: Offset,
        length: Int = snake_length,
        movement: Movement,
        nodewidth: Float,
        nodeheight: Float
    ) {
        val tmp = mutableStateListOf<SnakeNode>()
        for (i in 0 until length) {
            val hierarchy = if (i == 0) SnakeHierarchy.HEAD else SnakeHierarchy.NODE
            val pos = start_position + Offset(i * nodewidth, 0f)
            tmp.add(
                SnakeNode(
                    hierarchy = hierarchy,
                    movement = movement,
                    node_position = mutableStateOf(pos),
                    previous = if (i == 0) null else tmp[i - 1]
                )
            )
        }

        //Log.d("previous","tmp: $tmp")
        if(movement == Movement.RIGHT) {
            ReverseLL(lst=tmp)
        }
        else{
            tmp.reverse()
        }
        snake_list.add(
            Snake(
                id = cur_snake_id,
                node_lst = tmp,
                head_position = mutableStateOf(start_position),
                bitmap_width = nodewidth,
                bitmap_height = nodeheight
            )
        )
        IncrementSnakeId()
        Log.d("snakelist","$snake_list")
    }
    fun IncrementSnakeId(){
        cur_snake_id+=1
    }
    fun SetSnakeVelocityFactor(newfactor: Speed){
        snakevelocityfactor=newfactor
    }
    fun GetSnakeVelocity():Float{
        if(paused){
            return 0f
        }
        if(snakevelocityfactor==Speed.SLOW){
            return 6f
        }
        else if(snakevelocityfactor==Speed.MEDIUM){
            return 12f
        }
        return 18f
    }
    fun MoveSnakes(leastx: Float,maxx: Float,maxy: Float,step_down_height: Float){
        snakes_to_remove.clear()
        snake_list.forEach { snake ->
            MoveSnake(snake=snake,leastx=leastx,maxx=maxx,maxy=maxy,step_down_height=step_down_height)
        }
        if(!snakes_to_remove.isEmpty()){
            for(snake in snakes_to_remove){
                snake_list.remove(snake)
            }
        }
    }
    private fun MoveSnake(snake: Snake,leastx:Float,maxx: Float,maxy: Float,step_down_height:Float){
         snake.node_lst.forEach { snakeNode ->
             if(snakeNode.movement==Movement.RIGHT){
                 if(snakeNode.node_position.value.x+snake.bitmap_width>=maxx || mushroom_list.find {IsOverlapping(startA=snakeNode.node_position.value, widthA=snake.bitmap_width,heightA=snake.bitmap_height,startB=it.mushroom_position, widthB = it.bitmap_width, heightB = it.bitmap_height)}!=null){
                     if(snakeNode.hierarchy==SnakeHierarchy.HEAD){
                         snakeNode.node_position.value+=Offset(-GetSnakeVelocity()*2,step_down_height)
                     }
                     else{
                        snakeNode.node_position.value=Offset(snakeNode.previous!!.node_position.value.x+snake.bitmap_width-GetSnakeVelocity(),snakeNode.previous!!.node_position.value.y)
                     }
                     snakeNode.movement=Movement.LEFT
                 }
                 else{
                     snakeNode.node_position.value+=Offset(GetSnakeVelocity(),0f)
                 }
             }
             else if(snakeNode.movement==Movement.LEFT){
                 if(snakeNode.node_position.value.x<=leastx || mushroom_list.find {IsOverlapping(startA=snakeNode.node_position.value, widthA=snake.bitmap_width,heightA=snake.bitmap_height,startB=it.mushroom_position, widthB = it.bitmap_width, heightB = it.bitmap_height)}!=null){
                     if(snakeNode.hierarchy==SnakeHierarchy.HEAD){
                         snakeNode.node_position.value+=Offset(GetSnakeVelocity()*2,step_down_height)
                     }
                     else{
                         snakeNode.node_position.value=Offset(snakeNode.previous!!.node_position.value.x-snake.bitmap_width+GetSnakeVelocity(),snakeNode.previous!!.node_position.value.y)
                     }
                     snakeNode.movement=Movement.RIGHT
                 }
                 else{
                     snakeNode.node_position.value-=Offset(GetSnakeVelocity(),0f)
                 }
             }
             if(IsOverlapping(startA=snakeNode.node_position.value, widthA = snake.bitmap_width, heightA = snake.bitmap_height, startB = gun_position, widthB =gunbitmapwidth, heightB = gunbitmapheight)){
                 islost=true
                 paused=true
             }
             if(snakeNode.hierarchy==SnakeHierarchy.HEAD && snakeNode.node_position.value.y>=maxy){
                 snakes_to_remove.add(snake)
                 if(snake_list.isEmpty() && initial_snakes>0 && bullet_count>0){
                     if (initial_snakes > 0) {
                         bullet_count=0
                         DecrementInitialSnakes()
                     }
                 }
                 else if(snake_list.isEmpty() && initial_snakes==0 && bullet_count>0){
                     iswin=true
                     paused=true
                 }
             }
         }
    }
    fun SetSnakeSize(newsize:Int){
        snake_length=newsize
    }
    fun RemoveSnakeNode(snake: Snake,snakeNode: SnakeNode){
        var idx=snake.node_lst.indexOf(snakeNode)
        if(idx>=0){
            //Log.d("geterrordebug","yessss,$idx")
            var multiplication_factor = if (snakeNode.movement == Movement.RIGHT) -1f else 1f
            val tmp_list = mutableListOf<SnakeNode>()
            if (snake.node_lst.size == 1) {
                snake.node_lst.remove(snakeNode)
                RemoveSnake(snake = snake)
            } else {
                if (idx == 0) {//this is tail node since head is last element
                    //modifiy this if snake is initally moving left xxxx since we reversed the  list for left, both are same
                    //snake.node_lst.remove(snakeNode)

                } else if (idx == snake.node_lst.lastIndex) {//this is head node
                    snake.node_lst[snake.node_lst.lastIndex - 1].hierarchy = SnakeHierarchy.HEAD
                    snake.node_lst[snake.node_lst.lastIndex - 1].previous = null
                } else {
                    snake.node_lst[idx - 1].previous = null
                    snake.node_lst[idx - 1].hierarchy = SnakeHierarchy.HEAD
                    for (i in 0..idx - 1) {
                        tmp_list.add(snake.node_lst[i])
                    }
                }
            }
            AddMushroom(
                offset = snakeNode.node_position.value + Offset(
                    GetSnakeVelocity() * multiplication_factor,
                    0f
                ), mushroomwidth = this.mushroomwidth, mushroomheight = this.mushroomheight
            )

            snake.node_lst.remove(snakeNode)

            if (!tmp_list.isEmpty()) {
                snake.node_lst.removeIf { it in tmp_list }
                snake_list.add(
                    Snake(
                        id = cur_snake_id,
                        node_lst = tmp_list.toMutableStateList(),
                        head_position = tmp_list.find { it.hierarchy == SnakeHierarchy.HEAD }!!.node_position,
                        bitmap_width = snake.bitmap_width,
                        bitmap_height = snake.bitmap_height
                    )
                )
                IncrementSnakeId()
            }
        }
    }
    fun RemoveSnake(snake:Snake){
        snake_list.remove(snake)
    }
    fun ResetSnakes(){
        snake_list.clear()
        cur_snake_id=0
        initial_snakes =private_initial_snakes
    }

    fun SetSpiderStartPosition(offset: Offset){
        spider_start_position=offset
    }
    fun AddSpider(movement: Movement,bitmap_width:Float,bitmap_height: Float){
        //always do SetSpiderStartPosition before this
        spiderdelaythread= viewModelScope.launch {
            delay(spider_spawn_delay.toLong())
            ChangeSpawnDelay(start = 2000, end = 10000)
            spider_list.add(
                Spider(
                    id = spider_count,
                    spider_position = mutableStateOf(spider_start_position),
                    movement = mutableStateOf(movement),
                    bitmap_width = bitmap_width,
                    bitmap_height = bitmap_height
                )
            )
            spiderbitmapwidth = bitmap_width
            spiderbitmapheight = bitmap_height
            IncrementSpiderCount()
            spiderdelaythread=null
        }
        should_spawn_spider=false
    }
    fun MoveSpiders(leastx: Float,maxx: Float,leasty: Float, maxy: Float){
        spider_list.forEach(){spider->
            MoveSpider(spider=spider ,maxy = maxy)
        }
        RemoveRequiredSpiders(leastx=leastx,maxx=maxx,leasty=leasty)
    }
    private fun MoveSpider(spider: Spider,maxy: Float){
        spider.spider_position.value +=GetRequiredOffset(movement = spider.movement.value, speed = GetSpiderVelocity())
        if(spider.spider_position.value.y+spider.bitmap_height>=maxy){
            spider.movement.value=PickRandomMovement(lst = listOf(Movement.UP,Movement.DIAGONAL_TOP_LEFT,Movement.DIAGONAL_TOP_RIGHT))
        }
        if(IsOverlapping(startA = spider.spider_position.value, widthA = spider.bitmap_width, heightA = spider.bitmap_height, startB = gun_position, widthB = gunbitmapwidth, heightB = gunbitmapheight)){
            islost=true
            paused=true
        }
        val colliding_mushroom=mushroom_list.find { IsOverlapping(startA=spider.spider_position.value, widthA = spider.bitmap_width, heightA = spider.bitmap_height, startB = it.mushroom_position, widthB = it.bitmap_width, heightB = it.bitmap_height) }
        if(colliding_mushroom!=null){
            val delete_mushroom=listOf(false,true).random()
            if(delete_mushroom==false) {
                var newmovement = PickRandomMovement(all_movements)
                while (IsOverlapping(
                        startA = spider.spider_position.value + GetRequiredOffset(
                            movement = newmovement,
                            speed = spider_velocity
                        ),
                        widthA = spider.bitmap_width,
                        heightA = spider.bitmap_height,
                        startB = colliding_mushroom.mushroom_position,
                        widthB = colliding_mushroom.bitmap_width,
                        heightB = colliding_mushroom.bitmap_height
                    )
                ) {
                    newmovement = PickRandomMovement(all_movements)
                }
                spider.movement.value = newmovement
            }
            else{
                mushroom_list.remove(colliding_mushroom)
            }
        }
    }
    fun RemoveRequiredSpiders(leastx: Float,maxx: Float,leasty: Float){
        val spiders_to_remove = mutableListOf<Int>()
        spider_list.forEach{
            if(it.spider_position.value.x<=leastx ||  it.spider_position.value.x+spiderbitmapwidth >=maxx || it.spider_position.value.y<=leasty){
                spiders_to_remove.add(it.id)
            }
        }
        for(id in spiders_to_remove){
            RemoveSpider(id=id)
        }

    }
    fun RemoveSpider(id:Int){
        spider_list.removeIf { it.id==id }
        should_spawn_spider=true
    }
    fun IncrementSpiderCount(){
        spider_count+=1
    }
    fun GetSpiderVelocity():Float{
        if(paused){
            return 0f
        }
        return spider_velocity
    }
    fun ChangeSpawnDelay(start:Int,end:Int){
        spider_spawn_delay=Math.random().toFloat()*(end-start) + start
    }
    fun ResetSpiderStuff(){
        spiderdelaythread?.cancel()
        spider_list.clear()
        spider_count=0
        spider_spawn_delay=5000f
        should_spawn_spider=true
    }

    fun IncrementBulletCount(){
        bullet_count+=1
        total_bullet_count+=1
    }
    fun ResetBulletCount(){
        bullet_count=0
    }
    fun ResetTotalBullets(){
        total_bullet_count=0
    }
    fun AddBullet(start_position:Offset,width:Float,height:Float){
        bullet_list.add(Bullet(id = bullet_count, bullet_position = mutableStateOf(start_position), bitmap_width = width, bitmap_height = height))
        IncrementBulletCount()
        Log.d("snakelist","$snake_list")

    }
    fun MoveBullets(){
        bullet_list.forEach { bullet->
            bullet.bullet_position.value=bullet.bullet_position.value + Offset(0f,-GetBulletVelocity())
        }
        RemoveRequiredBullets()
    }
    fun RemoveRequiredBullets(){
        val bullets_to_remove= mutableListOf<Int>()
        val snake_nodes_to_remove= mutableListOf<Pair<Snake,SnakeNode>>()
        val spiders_to_remove=mutableListOf<Int>()
        bullet_list.removeIf{ it.bullet_position.value.y<=0f}
        for(i in 0 until mushroom_list.size){
            if(bullet_list.removeIf{IsOverlapping(
                startA=it.bullet_position.value,
                widthA=it.bitmap_width,
                heightA = it.bitmap_height,
                startB = mushroom_list[i].mushroom_position,
                widthB = mushroom_list[i].bitmap_width,
                heightB = mushroom_list[i].bitmap_height)})
            {
                mushroom_list[i].health-=1
            }
        }
        for(snake in snake_list){
            for(bullet in bullet_list){
                val coincide_snake=snake.node_lst.find { IsOverlapping(startA = bullet.bullet_position.value, widthA = bullet.bitmap_width, heightA = bullet.bitmap_height, startB = it.node_position.value, widthB =snake.bitmap_width, heightB = snake.bitmap_height)}
                if(coincide_snake!=null){
                    bullets_to_remove.add(bullet.id)
                    snake_nodes_to_remove.add(Pair(snake,coincide_snake))
                }
            }
        }
        for(spider in spider_list){
            val coincide_bullet=bullet_list.find { IsOverlapping(startA = it.bullet_position.value, widthA = it.bitmap_width, heightA=it.bitmap_height, startB = spider.spider_position.value, widthB = spider.bitmap_width, heightB = spider.bitmap_height) }
            if(coincide_bullet!=null){
                bullets_to_remove.add(coincide_bullet.id)
                spiders_to_remove.add(spider.id)
            }
        }
        bullet_list.removeIf{ it.id in bullets_to_remove}
        for(spider_remove in spiders_to_remove){
            RemoveSpider(id=spider_remove)
        }
        for(pair in snake_nodes_to_remove){
            RemoveSnakeNode(snake=pair.first, snakeNode = pair.second)
        }
        RemoveRequiredMushrooms()

        if(snake_list.isEmpty() && initial_snakes>0 && bullet_count>0){
            if (initial_snakes > 0) {
                ResetBulletCount()
                DecrementInitialSnakes()
            }
        }
        else if(snake_list.isEmpty() && initial_snakes==0 && bullet_count>0){
            iswin=true
            paused=true
        }
    }
    fun RemoveBullet(bullet_id:Int){
        bullet_list.removeIf{ it.id==bullet_id}
    }
    fun GetBulletVelocity():Float{
        if(paused){
            return 0f
        }
        if(bulletvelocity==Speed.SLOW){
            return 6f
        }
        else if(bulletvelocity==Speed.MEDIUM){
            return 12f
        }
        return 18f
    }
    fun SetBulletVelocityFactor(newvelocity:Speed){
        bulletvelocity=newvelocity
    }
    fun ResetBullets(){
        bullet_list.clear()
        ResetBulletCount()
    }
    fun DecrementInitialSnakes(){
        initial_snakes -=1
    }

    fun RemoveRequiredMushrooms(){
        mushroom_list.removeIf{it.health<=0}
    }
    fun IncrementMushroomId(){
        cur_mushroom_id+=1
    }
    fun AddMushroom(offset: Offset,mushroomwidth: Float,mushroomheight: Float){
        mushroom_list.add(Mushroom(id = cur_mushroom_id,
            mushroom_position = offset,
            bitmap_width = mushroomwidth,
            bitmap_height = mushroomheight))
        IncrementMushroomId()
    }
    fun AddMushrooms(leastx:Float,maxx:Float,leasty:Float,maxy:Float,mushroomwidth:Float,mushroomheight:Float){
        var randomx:Float
        var randomy:Float
        var j:Int
//        val max_count=1000
//        var curcount=0
        for(i in 0 until org_mushroom_count){
            randomx=Random.nextFloat() * (maxx-leastx) + leastx
            randomy=Random.nextFloat() * (maxy-leasty) + leasty
            j=0
//            curcount=0
            while(j < mushroom_list.size){
//                curcount+=1
//                if(curcount>=max_count){
//                    break
//                }
                if(IsOverlapping(
                        startA=Offset(randomx,randomy),
                        widthA=mushroomwidth,
                        heightA = mushroomheight,
                        startB=mushroom_list[j].mushroom_position,
                    xtra=Offset(64f,64f))){
                    randomx=Random.nextFloat() * (maxx-leastx) + leastx
                    randomy=Random.nextFloat() * (maxy-leasty) + leasty
                    j=0
                    continue
                }
//                else{
//                    if(randomy>=mushroom_list[j].mushroom_position.y && randomy<=mushroom_list[j].mushroom_position.y + mushroomheight){
//                        j=0
//                        continue
//                    }
//                }
                j+=1
            }
            //Log.d("mushroomlist","$randomx,$randomy")
            this.mushroomwidth=mushroomwidth
            this.mushroomheight=mushroomheight
            mushroom_list.add(Mushroom(id=cur_mushroom_id, mushroom_position = Offset(randomx,randomy), bitmap_width = mushroomwidth, bitmap_height = mushroomheight))
            IncrementMushroomId()
        }
    }
    fun ResetMushrooms(){
        org_mushroom_count=10
        mushroom_list.clear()
        cur_mushroom_id=0
    }
    fun IsOverlapping(startA:Offset,widthA:Float,heightA: Float,startB:Offset,widthB:Float=widthA,heightB: Float=heightA,xtra:Offset=Offset.Zero):Boolean{
        return !(startA.x>=startB.x+widthB+xtra.x || startA.x+widthA+xtra.x<=startB.x || startA.y>=startB.y+heightB+xtra.y || startA.y+heightA+xtra.y<=startB.y)
    }
    fun ReverseLL(lst:MutableList<SnakeNode>){
        lst[0].hierarchy=SnakeHierarchy.NODE
        lst.last().hierarchy=SnakeHierarchy.HEAD
        for(i in lst.lastIndex downTo 0){
            if(i==lst.lastIndex){
                lst[i].previous=null
            }
            else{
                lst[i].previous=lst[i+1]
            }
        }
        //lst.reverse()
    }

    fun SetMediaPlayer(context:Context){
        bgplayer=MediaPlayer.create(context,R.raw.bgmusic)
        buttonplayer=MediaPlayer.create(context,R.raw.buttonclick)
    }
    fun IsBgPlayerInitialized():Boolean{
        return bgplayer!=null
    }
    fun StartMusic(){
        if(bgplayer!=null && !bgplayer!!.isPlaying)
        {
            bgplayer!!.start()
            bgplayer!!.setVolume(cur_volume,cur_volume)
            bgplayer!!.isLooping=true
        }
    }
    fun SetBgVolume(newvolume:Float){
        cur_volume=newvolume
        bgplayer!!.setVolume(newvolume,newvolume)
    }
    fun PlayButtonClick(){
        if(buttonplayer.isPlaying){
            buttonplayer.stop()
            buttonplayer.prepare()
        }
        buttonplayer.start()
    }

    fun SetBrightness(newbrightness:Float){
        cur_brightness=newbrightness
    }

    fun PauseGame(){
        paused=true
        //PauseMusic()
    }
    fun ResumeGame(){
        paused=false
    }
    fun ResetGame(){
        ResetMushrooms()
        ResetBullets()
        ResetTotalBullets()
        ResetSnakes()
        ResetSpiderStuff()
        iswin=false
        islost=false
        paused=false
    }

    fun PickRandomMovement(lst:List<Movement> = listOf()):Movement{
        return lst.random()
    }

    fun GetRequiredOffset(movement: Movement,speed:Float):Offset{
        var newoff=Offset.Zero
        if(movement==Movement.UP){
            newoff= Offset(0f,-speed)
        }
        else if(movement==Movement.DIAGONAL_TOP_RIGHT){
            newoff=Offset(speed,-speed)
        }
        else if(movement==Movement.RIGHT){
            newoff=Offset(speed,0f)
        }
        else if(movement==Movement.DIAGONAL_BOTTOM_RIGHT){
            newoff=Offset(speed,speed)
        }
        else if(movement==Movement.DOWN){
            newoff=Offset(0f,speed)
        }
        else if(movement==Movement.DIAGONAL_BOTTOM_LEFT){
            newoff=Offset(-speed,-speed)
        }
        else if(movement==Movement.LEFT){
            newoff=Offset(-speed,0f)
        }
        else if(movement==Movement.DIAGONAL_TOP_LEFT){
            newoff=Offset(-speed,speed)
        }

        return newoff
    }
}