package com.example.arcadecrawler

import android.graphics.Point
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

enum class Screens{
    HOME,
    GAME,
    SETTINGS
}
enum class Speed{
    SLOW,
    MEDIUM,
    FAST
}
enum class Movement{
    LEFT,
    RIGHT,
    UP,
    DOWN,
    DIAGONAL_TOP_RIGHT,
    DIAGONAL_TOP_LEFT,
    DIAGONAL_BOTTOM_RIGHT,
    DIAGONAL_BOTTOM_LEFT
}
enum class SnakeHierarchy{
    HEAD,
    NODE
}


val blackwhitegridheight=100.dp
val speed_options = listOf(Speed.SLOW,Speed.MEDIUM,Speed.FAST)
val shared_pref_filename="ArcadeCrawler"
val all_movements=Movement.values().toList()

data class Joystick(var thumbpositon:Offset=Offset.Zero,var outerradius:Float=0f,var innerradius:Float=0f)
data class Bullet(val id:Int,var bullet_position:MutableState<Offset> = mutableStateOf(Offset.Zero),var bitmap_width:Float,var bitmap_height:Float)
data class Mushroom(val id:Int,var mushroom_position:Offset,var health:Int=5,var bitmap_width: Float,var bitmap_height: Float)
data class Snake(val id:Int,var node_lst:SnapshotStateList<SnakeNode>,var head_position:MutableState<Offset>,val bitmap_width: Float,val bitmap_height: Float)
data class SnakeNode(
    var hierarchy: SnakeHierarchy,
    var movement: Movement,
    var node_position: MutableState<Offset>,
    var previous: SnakeNode?=null
)
data class Spider(
    var id:Int,
    var spider_position:MutableState<Offset>,
    var movement: MutableState<Movement>,
    val bitmap_width: Float,
    val bitmap_height: Float
)

