package com.example.dailyquiz.ui.theme.screen.online_quiz_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dailyquiz.R
import com.example.dailyquiz.model.OnlineQuiz
import com.example.dailyquiz.model.Quiz
import com.example.dailyquiz.model.TriviaCategory
import kotlinx.coroutines.launch

@Composable
fun OnlineQuizScreen(
    onBack: () -> Unit,
    onStartQuiz: (List<Quiz>, String, Int) -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val onlineQuizzes = remember {
        listOf(
            OnlineQuiz("0", "Общие вопросы", "Вопросы из разных категорий", null),
            OnlineQuiz("1", "Музыка", "Вопросы по музыке", TriviaCategory.Music.id),
            OnlineQuiz("2", "Спорт", "Вопросы по спорту", TriviaCategory.Sports.id),
            OnlineQuiz("3", "География", "Вопросы по географии", TriviaCategory.Geography.id),
            OnlineQuiz("4", "История", "Вопросы по истории", TriviaCategory.History.id)
        )
    }

    var selectedQuiz by remember { mutableStateOf<OnlineQuiz?>(null) }
    var quizStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.handleIntent(MainIntent.LoadMain())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp, vertical = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "")
            }
            Image(painter = painterResource(R.drawable.vector), contentDescription = "")
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(onlineQuizzes) { quiz ->
                OnlineQuizCard(
                    quiz = quiz,
                    questionCount = if (quiz.categoryId == null) state.questions.size else null,
                    onStart = {
                        selectedQuiz = quiz
                        quizStarted = false
                        coroutineScope.launch {
                            viewModel.handleIntent(
                                MainIntent.LoadMain(
                                    amount = 5,
                                    category = quiz.categoryId,
                                    difficulty = null,
                                    type = "multiple"
                                )
                            )
                        }
                    }
                )
            }
        }

        if (state.isLoading) {
            Box(
                content = { CircularProgressIndicator() },
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            )
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (state.isOffline) "$error (Данные из кеша)" else error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    LaunchedEffect(state.questions, state.error, state.isLoading, selectedQuiz, quizStarted) {
        if (
            selectedQuiz != null &&
            state.questions.isNotEmpty() &&
            !state.isLoading &&
            !quizStarted
        ) {
            val quizzes = state.questions.mapIndexed { index, q ->
                val options = (q.incorrectAnswers + q.correctAnswer).shuffled()
                val correctIndex = options.indexOf(q.correctAnswer)
                Quiz(
                    id = index,
                    question = q.question,
                    options = options,
                    correctAnswerIndex = correctIndex
                )
            }

            if (state.error == null) {
                // Есть интернет, запускаем с выбранным названием
                val title = selectedQuiz?.title ?: "Викторина"
                onStartQuiz(quizzes, title, quizzes.size)
            } else {
                // Нет интернета, запускаем с названием из кеша (offline)
                val firstCategoryId = state.questions.firstOrNull()?.category?.toIntOrNull()
                val title =
                    onlineQuizzes.find { it.categoryId == firstCategoryId }?.title ?: "Викторина"
                onStartQuiz(quizzes, title, quizzes.size)
            }
            quizStarted = true
        }
    }
}

@Composable
fun OnlineQuizCard(
    quiz: OnlineQuiz,
    questionCount: Int? = null,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = quiz.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📝 ${questionCount ?: "0"} вопросов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "⏱️ 5 минут",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Начать викторину",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B0063)
                )
            }
        }
    }
}
