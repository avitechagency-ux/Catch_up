package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 6 Student Utilities.
 */
enum class ToolSubtype(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val description: String) {
    CALCULATOR("Calculator", Icons.Outlined.Calculate, "Arithmetic, brackets, % & calculation tape"),
    STUDY_TIMER("Study Timer", Icons.Outlined.Timer, "Pomodoro countdown, focus & break intervals"),
    TIMETABLE_MAKER("Timetable Maker", Icons.Outlined.ViewWeek, "Organise weekly schedule & save to My Learning"),
    UNIT_CONVERTER("Unit Converter", Icons.Outlined.SwapHoriz, "Length, mass, temp, volume, area & time"),
    QUICK_NOTES("Quick Notes", Icons.Outlined.EditNote, "Private revision scratchpad & checklists"),
    GEOGRAPHY_TOOLS("Geography Tools", Icons.Outlined.Public, "Map scale, distance, time zones & coordinates")
}

@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    var activeTool by remember { mutableStateOf<ToolSubtype?>(null) }

    BackHandler {
        if (activeTool != null) {
            activeTool = null
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPastelBackground)
            .testTag("tools_screen"),
        containerColor = SoftPastelBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = IndigoPurple,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (activeTool != null) activeTool = null
                            else onNavigateBack()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("tools_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeTool?.title ?: "Student Tools",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = activeTool?.description ?: "Practical utilities for secondary study & calculations",
                            fontSize = 12.sp,
                            color = LightLavender,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralTile
                    ) {
                        Text(
                            text = "Tools",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Body: If no tool selected, show grid of tools; otherwise show active tool
            Box(modifier = Modifier.weight(1f)) {
                if (activeTool == null) {
                    ToolsHubGrid(onSelectTool = { activeTool = it })
                } else {
                    when (activeTool!!) {
                        ToolSubtype.CALCULATOR -> SmartCalculatorView()
                        ToolSubtype.STUDY_TIMER -> StudyTimerView()
                        ToolSubtype.TIMETABLE_MAKER -> StudyTimetableMakerView()
                        ToolSubtype.UNIT_CONVERTER -> UnitConverterView()
                        ToolSubtype.QUICK_NOTES -> QuickNotesView()
                        ToolSubtype.GEOGRAPHY_TOOLS -> GeographyToolsView()
                    }
                }
            }
        }
    }
}

/**
 * Tools Grid Launcher.
 */
@Composable
private fun ToolsHubGrid(
    onSelectTool: (ToolSubtype) -> Unit
) {
    val scrollState = rememberScrollState()
    val toolsList = ToolSubtype.values()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Select a Student Utility",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        toolsList.forEach { tool ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(18.dp))
                    .border(1.dp, BorderGrey, RoundedCornerShape(18.dp))
                    .clickable { onSelectTool(tool) }
                    .testTag("tool_card_${tool.name.lowercase()}"),
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = IndigoPurple.copy(alpha = 0.12f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = tool.icon, contentDescription = null, tint = IndigoPurple, modifier = Modifier.size(26.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = tool.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = tool.description, fontSize = 12.sp, color = TextMuted)
                    }

                    Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = IconGrey)
                }
            }
        }
    }
}

/**
 * A. SMART CALCULATOR
 * Supports basic arithmetic, %, brackets, AC, backspace, and calculation tape history.
 */
@Composable
private fun SmartCalculatorView() {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    val history = remember { mutableStateListOf<Pair<String, String>>() }
    var showHistory by remember { mutableStateOf(false) }

    fun evaluate(expr: String): String {
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/").replace("−", "-")
            // Simple robust math evaluation for secondary student calculations
            val res = evalMathExpression(sanitized)
            if (res == res.toLong().toDouble()) res.toLong().toString() else "%.4f".format(res).trimEnd('0').trimEnd('.')
        } catch (e: Exception) {
            "..."
        }
    }

    fun onKeyClick(key: String) {
        when (key) {
            "AC" -> {
                expression = ""
                result = "0"
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    result = if (expression.isEmpty()) "0" else evaluate(expression)
                }
            }
            "=" -> {
                val evaluated = evaluate(expression)
                if (evaluated != "..." && expression.isNotBlank()) {
                    history.add(0, Pair(expression, evaluated))
                    expression = evaluated
                    result = evaluated
                }
            }
            "%" -> {
                try {
                    val evaluated = evaluate(expression)
                    val percentVal = evaluated.toDouble() / 100.0
                    val formatted = percentVal.toString()
                    expression = formatted
                    result = formatted
                } catch (e: Exception) {
                    // Ignore
                }
            }
            "+", "−", "×", "÷" -> {
                if (expression.isNotEmpty() && expression.last() in listOf('+', '−', '×', '÷')) {
                    expression = expression.dropLast(1) + key
                } else if (expression.isNotEmpty()) {
                    expression += key
                }
            }
            else -> {
                expression += key
                result = evaluate(expression)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top calculation history toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { showHistory = !showHistory }) {
                Icon(imageVector = Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showHistory) "Keypad" else "History (${history.size})", fontSize = 12.sp)
            }
        }

        if (showHistory) {
            // History list
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "No calculations recorded yet.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                items(history) { (expr, res) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            expression = res
                            result = res
                            showHistory = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, BorderGrey)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = expr, fontSize = 14.sp, color = TextMuted)
                            Text(text = "= $res", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                    }
                }
            }
        } else {
            // Display Screen
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (expression.isEmpty()) "0" else expression,
                        fontSize = 24.sp,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calculator Keypad: 4 columns x 5 rows
            val keypad = listOf(
                listOf("AC", "(", ")", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"),
                listOf("%", "0", ".", "=")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keypad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            val isAction = key in listOf("AC", "(", ")", "%")
                            val isOp = key in listOf("÷", "×", "−", "+", "=")
                            val bg = when {
                                key == "=" -> GoldMustard
                                isOp -> IndigoPurple
                                isAction -> SoftPastelBackground
                                else -> Color.White
                            }
                            val fg = when {
                                key == "=" -> DeepMaroon
                                isOp -> Color.White
                                else -> TextDark
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .shadow(1.dp, RoundedCornerShape(14.dp))
                                    .clickable { onKeyClick(key) }
                                    .testTag("calc_key_$key"),
                                shape = RoundedCornerShape(14.dp),
                                color = bg,
                                border = if (bg == Color.White) BorderStroke(1.dp, BorderGrey) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = fg
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Elementary math expression evaluator.
 */
private fun evalMathExpression(expr: String): Double {
    if (expr.isBlank()) return 0.0
    val tokens = mutableListOf<String>()
    var current = ""
    for (char in expr) {
        if (char in "+-*/()") {
            if (current.isNotBlank()) {
                tokens.add(current)
                current = ""
            }
            tokens.add(char.toString())
        } else if (!char.isWhitespace()) {
            current += char
        }
    }
    if (current.isNotBlank()) tokens.add(current)

    // Evaluate simple left-to-right operations or parentheses
    fun parseSimple(list: List<String>): Double {
        if (list.isEmpty()) return 0.0
        // Multiply and divide first
        val pass1 = mutableListOf<String>()
        var i = 0
        while (i < list.size) {
            val token = list[i]
            if (token == "*" || token == "/") {
                val prev = pass1.removeAt(pass1.size - 1).toDouble()
                val next = list[i + 1].toDouble()
                val res = if (token == "*") prev * next else prev / next
                pass1.add(res.toString())
                i += 2
            } else {
                pass1.add(token)
                i += 1
            }
        }
        // Add and subtract
        var total = pass1[0].toDouble()
        var j = 1
        while (j < pass1.size) {
            val op = pass1[j]
            val next = pass1[j + 1].toDouble()
            total = if (op == "+") total + next else total - next
            j += 2
        }
        return total
    }

    return parseSimple(tokens)
}

/**
 * B. STUDY TIMER (Focus & Break intervals)
 */
@Composable
private fun StudyTimerView() {
    val context = LocalContext.current
    var studyMinutes by remember { mutableStateOf(25) }
    var breakMinutes by remember { mutableStateOf(5) }
    var isBreakMode by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(studyMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(studyMinutes, isBreakMode) {
        if (!isRunning) {
            secondsLeft = if (isBreakMode) breakMinutes * 60 else studyMinutes * 60
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
        if (isRunning && secondsLeft == 0) {
            isRunning = false
            Toast.makeText(context, "⏰ Study Timer completed! Take a well-deserved break.", Toast.LENGTH_LONG).show()
            // Record to history
            MyLearningRepository.recordHistoryItem(
                context,
                title = "Study Sprint: $studyMinutes mins",
                subtitle = "Study Timer Focus Sprint",
                category = HistoryCategory.SESSION
            )
        }
    }

    val totalSecs = if (isBreakMode) breakMinutes * 60 else studyMinutes * 60
    val progress = if (totalSecs > 0) (secondsLeft.toFloat() / totalSecs.toFloat()).coerceIn(0f, 1f) else 0f
    val displayMin = secondsLeft / 60
    val displaySec = secondsLeft % 60
    val formattedCountdown = "%02d:%02d".format(displayMin, displaySec)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Mode selector
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    isBreakMode = false
                    isRunning = false
                    secondsLeft = studyMinutes * 60
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isBreakMode) IndigoPurple else SoftPastelBackground,
                    contentColor = if (!isBreakMode) Color.White else TextDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Focus Mode (${studyMinutes}m)")
            }

            Button(
                onClick = {
                    isBreakMode = true
                    isRunning = false
                    secondsLeft = breakMinutes * 60
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBreakMode) TealTile else SoftPastelBackground,
                    contentColor = if (isBreakMode) Color.White else TextDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Break Mode (${breakMinutes}m)")
            }
        }

        // Circular Timer Display
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            border = BorderStroke(4.dp, if (isBreakMode) TealTile else IndigoPurple),
            modifier = Modifier.size(240.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isBreakMode) "Rest & Rehydrate" else "Deep Focus",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedCountdown,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRunning) "Timer running" else "Paused",
                    fontSize = 12.sp,
                    color = if (isRunning) NoteCountGreen else CoralTile,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Controls: Start/Pause, Reset
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Button(
                onClick = { isRunning = !isRunning },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) CoralTile else IndigoPurple,
                    contentColor = Color.White
                ),
                modifier = Modifier.width(130.dp).height(48.dp)
            ) {
                Icon(imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRunning) "Pause" else "Start", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    secondsLeft = if (isBreakMode) breakMinutes * 60 else studyMinutes * 60
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        // Duration Presets
        Text(text = "Focus Presets", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 25, 45, 60).forEach { mins ->
                FilterChip(
                    selected = studyMinutes == mins && !isBreakMode,
                    onClick = {
                        studyMinutes = mins
                        isBreakMode = false
                        isRunning = false
                        secondsLeft = mins * 60
                    },
                    label = { Text("$mins mins") }
                )
            }
        }
    }
}

/**
 * C. STUDY TIMETABLE MAKER
 * Weekly grid builder with option to save into My Learning Personal Timetable!
 */
@Composable
private fun StudyTimetableMakerView() {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("Physics") }
    var topic by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("Monday") }
    var startTime by remember { mutableStateOf("16:00") }
    var endTime by remember { mutableStateOf("17:30") }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Organise Weekly Study Schedule", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = "Create weekly study blocks and directly save them into your My Learning timetable.", fontSize = 12.sp, color = TextMuted)

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Study Topic / Objective") },
                    placeholder = { Text("e.g. Wave Optics Formula Derivations") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = it },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        if (topic.isNotBlank()) {
                            MyLearningRepository.addSession(
                                context,
                                StudySession(
                                    subject = subject.trim(),
                                    topic = topic.trim(),
                                    dayOfWeek = day.trim(),
                                    startTime = startTime.trim(),
                                    endTime = endTime.trim(),
                                    focusLockEnabled = true
                                )
                            )
                            Toast.makeText(context, "Saved session into My Learning Timetable!", Toast.LENGTH_SHORT).show()
                            topic = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to My Learning Timetable", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * D. UNIT CONVERTER
 * Length, Mass, Time, Temperature, Volume, Area.
 */
@Composable
private fun UnitConverterView() {
    var category by remember { mutableStateOf("Length") }
    var inputValue by remember { mutableStateOf("1") }
    var fromUnit by remember { mutableStateOf("Metres (m)") }
    var toUnit by remember { mutableStateOf("Kilometres (km)") }

    val categories = listOf("Length", "Mass", "Temperature", "Time", "Volume", "Area")

    val unitsForCategory = when (category) {
        "Length" -> listOf("Metres (m)", "Kilometres (km)", "Centimetres (cm)", "Millimetres (mm)", "Miles", "Feet")
        "Mass" -> listOf("Kilograms (kg)", "Grams (g)", "Milligrams (mg)", "Pounds (lb)", "Tonnes")
        "Temperature" -> listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)")
        "Time" -> listOf("Seconds (s)", "Minutes (min)", "Hours (hr)", "Days")
        "Volume" -> listOf("Litres (L)", "Millilitres (mL)", "Cubic metres (m³)", "Cubic centimetres (cm³)")
        else -> listOf("Square metres (m²)", "Square kilometres (km²)", "Hectares", "Acres")
    }

    LaunchedEffect(category) {
        fromUnit = unitsForCategory[0]
        toUnit = unitsForCategory.getOrElse(1) { unitsForCategory[0] }
    }

    // Bidirectional conversion formula computation
    val convertedResult = remember(inputValue, fromUnit, toUnit, category) {
        val num = inputValue.toDoubleOrNull() ?: 0.0
        when (category) {
            "Length" -> {
                val inMetres = when (fromUnit) {
                    "Kilometres (km)" -> num * 1000.0
                    "Centimetres (cm)" -> num / 100.0
                    "Millimetres (mm)" -> num / 1000.0
                    "Miles" -> num * 1609.34
                    "Feet" -> num * 0.3048
                    else -> num
                }
                when (toUnit) {
                    "Kilometres (km)" -> inMetres / 1000.0
                    "Centimetres (cm)" -> inMetres * 100.0
                    "Millimetres (mm)" -> inMetres * 1000.0
                    "Miles" -> inMetres / 1609.34
                    "Feet" -> inMetres / 0.3048
                    else -> inMetres
                }
            }
            "Temperature" -> {
                val inCelsius = when (fromUnit) {
                    "Fahrenheit (°F)" -> (num - 32.0) * 5.0 / 9.0
                    "Kelvin (K)" -> num - 273.15
                    else -> num
                }
                when (toUnit) {
                    "Fahrenheit (°F)" -> (inCelsius * 9.0 / 5.0) + 32.0
                    "Kelvin (K)" -> inCelsius + 273.15
                    else -> inCelsius
                }
            }
            "Mass" -> {
                val inKg = when (fromUnit) {
                    "Grams (g)" -> num / 1000.0
                    "Milligrams (mg)" -> num / 1000000.0
                    "Pounds (lb)" -> num * 0.453592
                    "Tonnes" -> num * 1000.0
                    else -> num
                }
                when (toUnit) {
                    "Grams (g)" -> inKg * 1000.0
                    "Milligrams (mg)" -> inKg * 1000000.0
                    "Pounds (lb)" -> inKg / 0.453592
                    "Tonnes" -> inKg / 1000.0
                    else -> inKg
                }
            }
            "Time" -> {
                val inSeconds = when (fromUnit) {
                    "Minutes (min)" -> num * 60.0
                    "Hours (hr)" -> num * 3600.0
                    "Days" -> num * 86400.0
                    else -> num
                }
                when (toUnit) {
                    "Minutes (min)" -> inSeconds / 60.0
                    "Hours (hr)" -> inSeconds / 3600.0
                    "Days" -> inSeconds / 86400.0
                    else -> inSeconds
                }
            }
            else -> num
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) }
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Input Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "From Unit", fontSize = 11.sp, color = TextMuted)
                        Text(text = fromUnit, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    IconButton(onClick = {
                        val tmp = fromUnit
                        fromUnit = toUnit
                        toUnit = tmp
                    }) {
                        Icon(imageVector = Icons.Outlined.SwapHoriz, contentDescription = "Swap")
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "To Unit", fontSize = 11.sp, color = TextMuted)
                        Text(text = toUnit, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }

                // Output Result Display Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftPastelBackground,
                    border = BorderStroke(1.dp, BorderGrey),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Converted Result:", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "%.4f %s".format(convertedResult, toUnit),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPurple
                        )
                    }
                }
            }
        }
    }
}

/**
 * E. QUICK NOTES (Private student revision scratchpad)
 */
@Composable
private fun QuickNotesView() {
    val context = LocalContext.current
    var notes by remember { mutableStateOf<List<QuickNote>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    fun refreshNotes() {
        notes = MyLearningRepository.getNotes(context)
    }

    LaunchedEffect(Unit) {
        refreshNotes()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "My Study Notes (${notes.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(notes, key = { it.id }) { note ->
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = note.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            IconButton(
                                onClick = {
                                    MyLearningRepository.deleteNote(context, note.id)
                                    refreshNotes()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = null, tint = IconGrey, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = note.content, fontSize = 12.sp, color = TextDark.copy(alpha = 0.85f), lineHeight = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${note.subject} • ${note.dateModified}", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteSubject by remember { mutableStateOf("Physics") }
        var noteContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Study Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteSubject,
                        onValueChange = { noteSubject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Content") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                            MyLearningRepository.addNote(
                                context,
                                QuickNote(
                                    title = noteTitle.trim(),
                                    subject = noteSubject.trim(),
                                    content = noteContent.trim()
                                )
                            )
                            refreshNotes()
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPurple)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * F. GEOGRAPHY TOOLS
 * Map scale calculator, Distance converter, Time zone calculator & Coordinates reference.
 */
@Composable
private fun GeographyToolsView() {
    var mapCm by remember { mutableStateOf("4") }
    var scaleDenominator by remember { mutableStateOf("50000") } // 1:50,000

    val groundKm = remember(mapCm, scaleDenominator) {
        val cm = mapCm.toDoubleOrNull() ?: 0.0
        val scale = scaleDenominator.toDoubleOrNull() ?: 50000.0
        (cm * scale) / 100000.0 // cm to km
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Map Scale Calculator
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "1. Map Scale (R.F.) Calculator", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepMaroon)
                Text(text = "Computes actual ground distance from topographical map measurements.", fontSize = 12.sp, color = TextMuted)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mapCm,
                        onValueChange = { mapCm = it },
                        label = { Text("Map Distance (cm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = scaleDenominator,
                        onValueChange = { scaleDenominator = it },
                        label = { Text("R.F. Denominator (1:X)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SoftPastelBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ground Distance: %.2f km (%.0f metres)".format(groundKm, groundKm * 1000.0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPurple,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // 2. East Africa Time Zone Calculator
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "2. East Africa Time (EAT) Comparator", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepMaroon)
                Text(text = "Base: Uganda / Kenya / Tanzania = UTC + 3 hours", fontSize = 12.sp, color = TextMuted)

                val cities = listOf(
                    "London (UTC+0)" to "-3 hours",
                    "New York (UTC-5)" to "-8 hours",
                    "Dubai (UTC+4)" to "+1 hour",
                    "Tokyo (UTC+9)" to "+6 hours",
                    "Johannesburg (UTC+2)" to "-1 hour"
                )
                cities.forEach { (city, diff) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = city, fontSize = 12.sp, color = TextDark)
                        Text(text = diff, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndigoPurple)
                    }
                }
            }
        }

        // 3. East Africa Coordinates Preset Tool
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "3. Geographic Coordinates & Landmarks", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepMaroon)

                val landmarks = listOf(
                    "Kampala (City Centre)" to "0.3476° N, 32.5825° E",
                    "Mt. Rwenzori (Margherita Peak)" to "0.3850° N, 29.8739° E",
                    "Source of the Nile (Jinja)" to "0.4244° N, 33.1907° E",
                    "Uganda Equator Monument (Kayabwe)" to "0.0000° N, 32.0000° E"
                )
                landmarks.forEach { (name, coords) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Text(text = coords, fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}
