package com.example.dailyquiz.ui.theme.screen.history_screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyquiz.R
import com.example.dailyquiz.ScoreManager
import com.example.dailyquiz.model.AnswerDetail
import com.example.dailyquiz.model.QuizHistory
import com.example.dailyquiz.data.remote.decodeHtml
import com.example.dailyquiz.ui.theme.screen.quiz_screen.WantedStars
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    scoreManager: ScoreManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var histories by remember { mutableStateOf(scoreManager.getQuizHistories()) }
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedHistory by remember { mutableStateOf<QuizHistory?>(null) }
    var selectedDeleteHistory by remember { mutableStateOf<QuizHistory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "")
            }
            Text(
                text = "История",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
            IconButton(
                onClick = { showClearDialog = true },
                enabled = histories.isNotEmpty()
            ) {
                Icon(Icons.Default.Delete, contentDescription = "")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (histories.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "Вы еще не проходили ни одной викторины",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                    }
                }

                Image(painter = painterResource(R.drawable.vector), contentDescription = "")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(histories, key = { it.id }) { history ->
                    HistoryListItem(
                        history = history,
                        onClick = { selectedHistory = history },
                        onHistorySelectedDelete = { selectedDeleteHistory = history }
                    )
                }
            }
        }
    }


    selectedHistory?.let { history ->
        HistoryDetailDialog(
            history = history,
            onDismiss = { selectedHistory = null }
        )
    }

    selectedDeleteHistory?.let {
        DeleteHistoryDialog(
            onClick = {
                selectedDeleteHistory?.id?.let { id ->
                    scoreManager.deleteQuizHistoryById(id)
                    histories = scoreManager.getQuizHistories()
                    Toast.makeText(context, "Попытка удалена", Toast.LENGTH_SHORT).show()
                }
                selectedDeleteHistory = null
            },
            onDismiss = { selectedDeleteHistory = null }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Удалить всю историю") },
            text = { Text("Вы уверены, что хотите удалить всю историю викторин?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scoreManager.clearQuizHistories()
                        histories = emptyList()
                        showClearDialog = false
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun HistoryListItem(
    history: QuizHistory,
    onClick: () -> Unit,
    onHistorySelectedDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onHistorySelectedDelete() }
                    )
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(history.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Категория: ${history.quizTitle ?: history.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WantedStars(
                    paddingValues = PaddingValues(0.dp),
                    count = history.score
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${history.score}/${history.totalQuestions}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${history.percentage}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        history.percentage >= 80 -> MaterialTheme.colorScheme.primary
                        history.percentage >= 60 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Composable
fun DeleteHistoryDialog(
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Удалить выбранную историю") },
        text = { Text("Вы уверены, что хотите удалить выбранную историю?") },
        confirmButton = {
            TextButton(
                content = { Text("Удалить") },
                onClick = { onClick() }
            )
        },
        dismissButton = {
            TextButton(content = { Text("Отмена") }, onClick = { onDismiss() })
        }
    )
}

@Composable
fun HistoryDetailDialog(
    history: QuizHistory,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Подробный тест")
                Text(
                    text = dateFormat.format(history.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Категория: ${history.quizTitle ?: history.source}")
                    Text(
                        text = "${history.score}/${history.totalQuestions} (${history.percentage}%)",
                        fontWeight = FontWeight.Bold,
                        color = when {
                            history.percentage >= 80 -> MaterialTheme.colorScheme.primary
                            history.percentage >= 60 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Подробности ответа:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                history.answers.forEachIndexed { index, answer ->
                    AnswerDetailItem(
                        questionNumber = index + 1,
                        answer = answer
                    )
                    if (index < history.answers.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun AnswerDetailItem(
    questionNumber: Int,
    answer: AnswerDetail
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$questionNumber. ${decodeHtml(answer.question)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ваш ответ: ${answer.userAnswer}",
            style = MaterialTheme.typography.bodySmall,
            color = if (answer.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        if (!answer.isCorrect) {
            Text(
                text = "Правильный ответ: ${answer.correctAnswer}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = if (answer.isCorrect) "✅ Правильно" else "❌ Неправильно",
                style = MaterialTheme.typography.bodySmall,
                color = if (answer.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
} 