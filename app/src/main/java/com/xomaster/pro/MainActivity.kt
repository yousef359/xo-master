package com.xomaster.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XOMasterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF070913)
                ) {
                    GameScreen()
                }
            }
        }
    }
}

@Composable
fun XOMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF070913),
            surface = Color(0xFF0F172A),
            primary = Color(0xFF38BDF8),
            secondary = Color(0xFFFF2247)
        ),
        content = content
    )
}

@Composable
fun GameScreen() {
    var boardState = remember { mutableStateOf(List(9) { "" }) }
    var currentPlayer = remember { mutableStateOf("X") }
    var scoresX = remember { mutableStateOf(0) }
    var scoresO = remember { mutableStateOf(0) }
    var scoresDraw = remember { mutableStateOf(0) }
    var gameActive = remember { mutableStateOf(true) }
    var statusMessage = remember { mutableStateOf("دور اللاعب X الآن") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF070913), Color(0xFF0F172A), Color(0xFF030712))
                )
            )
            .padding(16px_to_dp(16)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // شريط العنوان العلوي
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* فتح الإعدادات */ }) {
                    Text("⚙️", fontSize = 20.sp)
                }
                Text(
                    text = "إكس أو Master Pro",
                    color = Color(0xFFF8FAFC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = { /* خيارات إضافية */ }) {
                    Text("📶", fontSize = 20.sp)
                }
            }

            // لوحة النتائج (Scoreboard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreCard("اللاعب X", scoresX.value.toString(), Color(0xFF38BDF8), Modifier.weight(1f))
                ScoreCard("تعادل", scoresDraw.value.toString(), Color(0xFF94A3B8), Modifier.weight(1f))
                ScoreCard("الكمبيوتر", scoresO.value.toString(), Color(0xFFFF2247), Modifier.weight(1f))
            }

            // مؤشر الدور الحالي
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (currentPlayer.value == "X") Color(0x2238BDF8) else Color(0x2FFF2247))
                    .border(1.dp, if (currentPlayer.value == "X") Color(0xFF38BDF8) else Color(0xFFFF2247), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusMessage.value,
                    color = if (currentPlayer.value == "X") Color(0xFF38BDF8) else Color(0xFFFF2247),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // شبكة اللعبة (3x3 Board)
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                CellItem(
                                    value = boardState.value[index],
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (gameActive.value && boardState.value[index] == "") {
                                            val newBoard = boardState.value.toMutableList()
                                            newBoard[index] = currentPlayer.value
                                            boardState.value = newBoard

                                            // التحقق من الفوز أو التبديل
                                            if (checkWin(newBoard, currentPlayer.value)) {
                                                statusMessage.value = "اللاعب ${currentPlayer.value} فاز بالجولة! 🎉"
                                                if (currentPlayer.value == "X") scoresX.value++ else scoresO.value++
                                                gameActive.value = false
                                            } else if (!newBoard.contains("")) {
                                                statusMessage.value = "تعادل بين الطرفين 🤝"
                                                scoresDraw.value++
                                                gameActive.value = false
                                            } else {
                                               currentPlayer.value = if (currentPlayer.value == "X") "O" else "X"
                                               statusMessage.value = "دور اللاعب ${currentPlayer.value} الآن"
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // أزرار التحكم السفلية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scoresX.value = 0
                        scoresO.value = 0
                        scoresDraw.value = 0
                        resetBoard(boardState, currentPlayer, gameActive, statusMessage)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("إعادة النتائج", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { resetBoard(boardState, currentPlayer, gameActive, statusMessage) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("لعبة جديدة", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CellItem(value: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x0FFFFFFF))
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            fontSize = 36.sp,
            fontWeight = FontWeight.900,
            color = if (value == "X") Color(0xFF38BDF8) else Color(0xFFFF2247)
        )
    }
}

@Composable
fun ScoreCard(title: String, score: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x0FFFFFFF))
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(18.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = score, fontSize = 20.sp, fontWeight = FontWeight.9.toFontWeight(), color = Color.White)
    }
}

fun Int.toFontWeight(): FontWeight = FontWeight.Bold

fun checkWin(b: List<String>, p: String): Boolean {
    val winPos = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )
    return winPos.any { it.all { idx -> b[idx] == p } }
}

fun resetBoard(
    boardState: MutableState<List<String>>,
    currentPlayer: MutableState<String>,
    gameActive: MutableState<Boolean>,
    statusMessage: MutableState<String>
) {
    boardState.value = List(9) { "" }
    currentPlayer.value = "X"
    gameActive.value = true
    statusMessage.value = "دور اللاعب X الآن"
}

fun 16px_to_dp(v: Int) = v.dp
