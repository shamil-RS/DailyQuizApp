package com.example.dailyquiz.ui.theme.screen.quiz_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyquiz.R
import com.example.dailyquiz.ScoreManager
import com.example.dailyquiz.data.remote.decodeHtml
import com.example.dailyquiz.model.AnswerDetail
import com.example.dailyquiz.model.Quiz
import com.example.dailyquiz.ui.theme.DarkPurple
import com.example.dailyquiz.ui.theme.Gray95
import com.example.dailyquiz.ui.theme.PurpleBlue40
import com.example.dailyquiz.ui.theme.RaisinBlack
import com.example.dailyquiz.ui.theme.SecondColor
import com.example.dailyquiz.ui.theme.screen.history_screen.HistoryScreen
import com.example.dailyquiz.ui.theme.screen.online_quiz_screen.MainScreenViewModel
import com.example.dailyquiz.ui.theme.screen.online_quiz_screen.OnlineQuizScreen
import kotlinx.coroutines.delay

enum class QuizSource { NONE, HARDCODE, API }
enum class Screen { MAIN, QUIZ, HISTORY, ONLINE_QUIZ }

@Composable
fun QuizScreen(viewModel: MainScreenViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
    var quizSource by remember { mutableStateOf(QuizSource.NONE) }
    var isLoading by remember { mutableStateOf(false) }
    var quizList by remember { mutableStateOf<List<Quiz>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var isDone by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(if (isDone) 0 else 0) }
    var showScore by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var totalTime by remember { mutableIntStateOf(30) }
    var timeLeft by remember { mutableIntStateOf(totalTime) }
    var timerRunning by remember { mutableStateOf(false) }
    var userAnswers by remember { mutableStateOf<List<AnswerDetail>>(emptyList()) }
    var currentQuizTitle by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scoreManager = remember { ScoreManager(context) }

    // Запуск таймера при начале викторины
    LaunchedEffect(quizSource, showScore) {
        try {
            if ((quizSource == QuizSource.HARDCODE || quizSource == QuizSource.API) && !showScore) {
                timeLeft = totalTime
                timerRunning = true
            } else timerRunning = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Таймер обратного отсчёта
    LaunchedEffect(timerRunning, timeLeft) {
        if (timerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (timeLeft == 0) {
                timerRunning = false
            }
        }
    }

    // Главное меню: выбор источника вопросов
    if (currentScreen == Screen.MAIN) {
        val highScore = remember { scoreManager.getHighScore() }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(20.dp, 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var showResetDialog by remember { mutableStateOf(false) }
                var currentHighScore by remember { mutableIntStateOf(highScore) }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp, 50.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { currentScreen = Screen.HISTORY },
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("История", fontWeight = FontWeight.Bold, color = PurpleBlue40)
                        Icon(
                            Icons.Outlined.Timer,
                            contentDescription = "",
                            tint = PurpleBlue40
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Image(painter = painterResource(R.drawable.vector), contentDescription = "")
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆 Лучший результат",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$currentHighScore%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(
                            onClick = { showResetDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Диалог подтверждения сброса лучшего результата
                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Сбросить лучший результат") },
                        text = { Text("Вы уверены, что хотите сбросить лучший результат?") },
                        confirmButton = {
                            TextButton(
                                content = { Text("Сбросить") },
                                onClick = {
                                    scoreManager.clearHighScore()
                                    currentHighScore = 0
                                    showResetDialog = false
                                }
                            )
                        },
                        dismissButton = {
                            TextButton(
                                content = { Text("Отмена") },
                                onClick = { showResetDialog = false })
                        }
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Добро пожаловать \nв DailyQuiz!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 26.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { currentScreen = Screen.ONLINE_QUIZ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(PurpleBlue40)
                        ) {
                            Text(
                                "🌐 Начать викторину",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        return
    }

    // Экран онлайн викторины
    if (currentScreen == Screen.ONLINE_QUIZ) {
        OnlineQuizScreen(
            viewModel = viewModel,
            onBack = { currentScreen = Screen.MAIN },
            onStartQuiz = { quizzes, title, questionCount ->
                quizList = quizzes
                currentQuizTitle = title
                totalTime = 300  // 5 минут в секундах
                timeLeft = totalTime
                quizSource = QuizSource.API
                currentScreen = Screen.QUIZ
            }
        )
        return
    }

    // Экран истории викторин
    if (currentScreen == Screen.HISTORY) {
        HistoryScreen(
            scoreManager = scoreManager,
            onBack = { currentScreen = Screen.MAIN }
        )
        return
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Загрузка вопросов...")
            }
        }
    } else if (showScore) {
        ScoreScreen(
            score = score,
            totalQuestions = quizList.size,
            userAnswers = userAnswers,
            source = if (quizSource == QuizSource.HARDCODE) "Жёстко заданные" else "API",
            quizTitle = currentQuizTitle,
            scoreManager = scoreManager
        ) {
            currentQuestionIndex = 0
            score = 0
            showScore = false
            selectedAnswer = null
            userAnswers = emptyList()
            currentScreen = Screen.MAIN
        }
    } else {
        // Если время вышло, сразу показываем результат
        if (timeLeft == 0 && !showScore) {
            timerRunning = false
            LaunchedEffect(Unit) { showScore = true }
        }
        val currentQuiz = quizList[currentQuestionIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp, 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Отображение таймера
            val minutes = timeLeft / 60
            val seconds = timeLeft % 60

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentScreen = Screen.MAIN }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "")
                }
                Image(painter = painterResource(R.drawable.vector), contentDescription = "")
            }

            Text(
                text = String.format("Оставшееся время: %02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleMedium,
                color = if (timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Вопрос ${currentQuestionIndex + 1} из ${quizList.size}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBCB7FF),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = decodeHtml(currentQuiz.question),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )

                    currentQuiz.options.forEachIndexed { index, option ->
                        val borderColor = if (selectedAnswer == index) DarkPurple else Gray95

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Gray95)
                                .border(
                                    width = 2.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedAnswer = index },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAnswer == index,
                                onClick = { selectedAnswer = index },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = DarkPurple,
                                    unselectedColor = Color.Black
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = decodeHtml(option),
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Кнопка "Завершить викторину сразу" (всегда видна)
                        Button(
                            content = {
                                Text(
                                    "Завершить",
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(16.dp),
                            onClick = {
                                if (selectedAnswer != null) {
                                    val isCorrect = selectedAnswer == currentQuiz.correctAnswerIndex
                                    if (isCorrect) score++

                                    // Сохраняем детали ответа
                                    val answerDetail = AnswerDetail(
                                        question = currentQuiz.question,
                                        userAnswer = currentQuiz.options[selectedAnswer!!],
                                        options = currentQuiz.options,
                                        correctAnswer = currentQuiz.options[currentQuiz.correctAnswerIndex],
                                        isCorrect = isCorrect
                                    )
                                    userAnswers = userAnswers + answerDetail
                                }
                                showScore = true
                            },
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Кнопка "Следующий" (всегда активна)
                        if (currentQuestionIndex < quizList.size - 1) {
                            Button(
                                content = {
                                    Text(
                                        "Далее",
                                        fontSize = 16.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PurpleBlue40,
                                    disabledContainerColor = PurpleBlue40,
                                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                enabled = selectedAnswer != null,
                                onClick = {
                                    if (selectedAnswer != null) {
                                        val isCorrect =
                                            selectedAnswer == currentQuiz.correctAnswerIndex
                                        if (isCorrect) score++

                                        // Сохраняем детали ответа
                                        val answerDetail = AnswerDetail(
                                            question = currentQuiz.question,
                                            userAnswer = currentQuiz.options[selectedAnswer!!],
                                            options = currentQuiz.options,
                                            correctAnswer = currentQuiz.options[currentQuiz.correctAnswerIndex],
                                            isCorrect = isCorrect
                                        )
                                        userAnswers = userAnswers + answerDetail
                                    }
                                    currentQuestionIndex++
                                    selectedAnswer = null
                                },
                            )
                        }
                    }
                }
            }

            Text(
                "Вернуться к предыдущим вопросам нельзя",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScoreScreen(
    score: Int,
    totalQuestions: Int,
    userAnswers: List<AnswerDetail>,
    source: String,
    quizTitle: String?,
    scoreManager: ScoreManager,
    onRestart: () -> Unit
) {
    val percentage =
        if (totalQuestions > 0) (score.toFloat() / totalQuestions.toFloat() * 100).toInt() else 0
    val highScore = remember { scoreManager.getHighScore() }
    val isNewHighScore = remember { percentage > highScore }

    val scoreTitle = when (score) {
        5 -> "Идеально!"
        4 -> "Почти идеально!"
        3 -> "Хороший результат!"
        2 -> "Есть над чем поработать"
        1 -> "Сложный вопрос?"
        0 -> "Бывает и так!"
        else -> ""
    }
    val scoreText = when (score) {
        5 -> "вы ответили на всё правильно. Это блестящий результат!"
        4 -> "очень близко к совершенству. Ещё один шаг!"
        3 -> "вы на верном пути. Продолжайте тренироваться!"
        2 -> "не расстраивайтесь, попробуйте ещё раз!"
        1 -> "иногда просто не ваш день. Следующая попытка будет лучше!"
        0 -> "не отчаивайтесь. Начните заново и удивите себя!"
        else -> ""
    }

    // Сохраняем лучший результат и историю
    LaunchedEffect(percentage) {
        scoreManager.saveHighScore(percentage)
        scoreManager.saveQuizHistory(
            score = score,
            totalQuestions = totalQuestions,
            percentage = percentage,
            source = source,
            quizTitle = quizTitle,
            answers = userAnswers
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Результаты",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.padding(vertical = 24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WantedStars(
                    paddingValues = PaddingValues(0.dp),
                    count = score,
                )

                if (isNewHighScore) {
                    Text(
                        text = "🎉 Новый рекорд! 🎉",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = "$score из $totalQuestions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB800),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = scoreTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "$score/$totalQuestions — $scoreText",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Твои ответы",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            itemsIndexed(userAnswers) { index, answer ->
                QuestionWithOptionsItem(
                    answerDetail = answer,
                    questionNumber = index + 1,
                    totalQuestions = userAnswers.size
                )
            }
            item {
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Начать заново",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = DarkPurple
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionWithOptionsItem(
    answerDetail: AnswerDetail, questionNumber: Int,
    totalQuestions: Int
) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Вопрос $questionNumber из $totalQuestions",
                    color = Color(0xFFBABABA),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Green), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = decodeHtml(answerDetail.question),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            answerDetail.options.forEach { option ->
                val isCorrectAnswer = option == answerDetail.correctAnswer
                val isUserAnswer = option == answerDetail.userAnswer

                val icon = when {
                    isCorrectAnswer -> Icons.Default.Check
                    isUserAnswer && !isCorrectAnswer -> Icons.Default.Close
                    else -> null
                }

                val iconTint = when {
                    isCorrectAnswer -> Color(0xFF4CAF50)
                    isUserAnswer && !isCorrectAnswer -> Color(0xFFF44336)
                    else -> Color.Black
                }

                val textColor = when {
                    isCorrectAnswer -> Color(0xFF4CAF50)
                    isUserAnswer && !isCorrectAnswer -> Color(0xFFF44336)
                    else -> Color.Black
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(width = 2.dp, color = textColor, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "",
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = decodeHtml(option),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    }
}

@Composable
fun WantedStars(paddingValues: PaddingValues, count: Int) {
    val colors: List<Color>
    when (count) {
        1 -> colors = listOf(
            SecondColor,
            RaisinBlack,
            RaisinBlack,
            RaisinBlack,
            RaisinBlack,
        )

        2 -> colors = listOf(
            SecondColor,
            SecondColor,
            RaisinBlack,
            RaisinBlack,
            RaisinBlack,
        )

        3 -> colors = listOf(
            SecondColor,
            SecondColor,
            SecondColor,
            RaisinBlack,
            RaisinBlack,
        )

        4 -> colors = listOf(
            SecondColor,
            SecondColor,
            SecondColor,
            SecondColor,
            RaisinBlack,
        )

        5 -> colors = listOf(
            SecondColor,
            SecondColor,
            SecondColor,
            SecondColor,
            SecondColor,
        )

        else -> colors = listOf(
            RaisinBlack,
            RaisinBlack,
            RaisinBlack,
            RaisinBlack,
            RaisinBlack,
        )
    }
    LazyRow(
        modifier = Modifier.padding(paddingValues),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(1) {
            colors.forEach { colorTint ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "",
                    modifier = Modifier.padding(top = 5.dp, bottom = 0.dp),
                    tint = colorTint
                )
            }
        }
    }
}

@Preview
@Composable
fun ScoreScreenPreview() {
    val sampleAnswers = listOf(
        AnswerDetail(
            question = "Столица Франции?",
            options = listOf("Париж", "Берлин", "Мадрид", "Рим"),
            userAnswer = "Париж",
            correctAnswer = "Париж",
            isCorrect = true
        ),
        AnswerDetail(
            question = "Сколько планет в Солнечной системе?",
            options = listOf("7", "8", "9", "10"),
            userAnswer = "7",
            correctAnswer = "8",
            isCorrect = false
        ),
        AnswerDetail(
            question = "Какой язык программирования используется для Android?",
            options = listOf("Java", "Kotlin", "Swift", "Python"),
            userAnswer = "Kotlin",
            correctAnswer = "Kotlin",
            isCorrect = true
        )
    )

    val context = LocalContext.current
    val scoreManager = remember { ScoreManager(context) }

    MaterialTheme {
        ScoreScreen(
            score = 2,
            totalQuestions = 3,
            userAnswers = sampleAnswers,
            source = "Жёстко заданные",
            quizTitle = "Общий тест",
            scoreManager = scoreManager,
            onRestart = {}
        )
    }
}