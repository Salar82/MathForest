package ir.alishojaee.mathforest.utils

import ir.alishojaee.mathforest.data.MathQuestion
import ir.alishojaee.mathforest.enums.GameDifficulty
import ir.alishojaee.mathforest.enums.Operation
import kotlin.math.max
import kotlin.random.Random

data class LevelConfig(
    val minNumber: Int,
    val maxNumber: Int
)

val levelConfigs = mapOf(
    GameDifficulty.EASY to LevelConfig(1, 20),
    GameDifficulty.MEDIUM to LevelConfig(1, 50),
    GameDifficulty.HARD to LevelConfig(1, 100)
)

class Quiz {
    companion object {
        fun generateValidMathQuestion(
            operators: List<Operation>,
            difficulty: GameDifficulty,
            maxAttempts: Int = 200
        ): MathQuestion {
            require(operators.isNotEmpty()) { "لیست اپراتورها نباید خالی باشد!" }
            val config = levelConfigs[difficulty] ?: LevelConfig(1, 100)
            val random = Random(System.currentTimeMillis())
            repeat(maxAttempts) {
                val currentExpression = mutableListOf<String>()
                val num1 = random.nextInt(config.minNumber, config.maxNumber + 1)
                currentExpression.add(num1.toString())

                var generationSuccessful = true
                val shuffledOps = operators.shuffled(random)

                for (op in shuffledOps) {
                    val currentEval = try {
                        evalMathExpression(currentExpression.joinToString(" "))
                    } catch (_: Exception) {
                        generationSuccessful = false
                        break
                    }
                    if (currentEval < config.minNumber || currentEval > config.maxNumber || currentEval.toInt().toDouble() != currentEval) {
                        generationSuccessful = false
                        break
                    }
                    val currentValueInt = currentEval.toInt()

                    var num2: Int? = null
                    repeat(10) {
                        when (op) {
                            Operation.SUM -> {
                                val maxAdd = config.maxNumber - currentValueInt
                                if (maxAdd >= config.minNumber) {
                                    val potentialN = random.nextInt(config.minNumber, max(config.minNumber + 1, maxAdd + 1))
                                    val tempExpr = currentExpression + listOf("+", potentialN.toString())
                                    if (isValidIntermediate(tempExpr.joinToString(" "), config)) {
                                        num2 = potentialN
                                        return@repeat
                                    }
                                }
                            }
                            Operation.INTERACT -> {
                                val maxSubtract = currentValueInt - config.minNumber
                                if (maxSubtract >= config.minNumber) {
                                    val potentialN = random.nextInt(config.minNumber, maxSubtract + 1)
                                    val tempExpr = currentExpression + listOf("-", potentialN.toString())
                                    if (isValidIntermediate(tempExpr.joinToString(" "), config)) {
                                        num2 = potentialN
                                        return@repeat
                                    }
                                }
                            }
                            Operation.MULTIPLY -> {
                                val possibleMultipliers = (2..9).filter { currentValueInt * it <= config.maxNumber }
                                if (possibleMultipliers.isNotEmpty()) {
                                    val potentialN = possibleMultipliers.random(random)
                                    val tempExpr = currentExpression + listOf("×", potentialN.toString())
                                    if (isValidIntermediate(tempExpr.joinToString(" "), config)) {
                                        num2 = potentialN
                                        return@repeat
                                    }
                                }
                            }
                            Operation.DIVISION -> {
                                val possibleDivisors = (2..9).filter { currentValueInt % it == 0 && currentValueInt / it >= config.minNumber }
                                if (possibleDivisors.isNotEmpty()) {
                                    val potentialN = possibleDivisors.random(random)
                                    val tempExpr = currentExpression + listOf("÷", potentialN.toString())
                                    if (isValidIntermediate(tempExpr.joinToString(" "), config)) {
                                        num2 = potentialN
                                        return@repeat
                                    }
                                }
                            }
                        }
                    }

                    if (num2 != null) {
                        currentExpression.add(when (op) {
                            Operation.SUM -> "+"
                            Operation.INTERACT -> "-"
                            Operation.MULTIPLY -> "×"
                            Operation.DIVISION -> "÷"
                        })
                        currentExpression.add(num2.toString())
                    } else {
                        generationSuccessful = false
                        break
                    }
                }

                if (generationSuccessful) {
                    val finalExpression = currentExpression.joinToString(" ")
                    try {
                        val finalAnswer = evalMathExpression(finalExpression)
                        if (finalAnswer.toInt().toDouble() == finalAnswer && finalAnswer >= config.minNumber.toDouble() && finalAnswer <= config.maxNumber.toDouble()) {
                            return MathQuestion("$finalExpression = ...", finalAnswer.toInt())
                        }
                    } catch (_: Exception) {
                    }
                }
            }

            return MathQuestion("خطا!", 0)
        }

        fun isValidIntermediate(expr: String, config: LevelConfig): Boolean {
            return try {
                val result = evalMathExpression(expr)
                result >= config.minNumber.toDouble() &&
                        result <= config.maxNumber.toDouble() &&
                        result.toInt().toDouble() == result
            } catch (_: Exception) {
                false
            }
        }

        fun evalMathExpression(expr: String): Double {
            val standardExpr = expr.replace("×", "*").replace("÷", "/")
            val tokens = standardExpr.split(' ').filter { it.isNotEmpty() }

            val values = mutableListOf<Double>()
            val ops = mutableListOf<Char>()

            fun precedence(op: Char): Int {
                return when (op) {
                    '+', '-' -> 1
                    '*', '/' -> 2
                    else -> 0
                }
            }

            fun applyOperation() {
                if (ops.isEmpty() || values.size < 2) throw IllegalArgumentException("Invalid expression near $ops $values")
                val op = ops.removeAt(ops.size - 1)
                val val2 = values.removeAt(values.size - 1)
                val val1 = values.removeAt(values.size - 1)
                when (op) {            '+' -> values.add(val1 + val2)
                    '-' -> values.add(val1 - val2)
                    '*' -> values.add(val1 * val2)
                    '/' -> {
                        if (val2 == 0.0) throw ArithmeticException("Division by zero")
                        values.add(val1 / val2)
                    }
                    else -> throw IllegalArgumentException("Unknown operator $op")
                }
            }

            for (token in tokens) {
                val num = token.toDoubleOrNull()
                if (num != null) {
                    values.add(num)
                } else if (token.length == 1 && token[0] in "+-*/") {
                    val currentOp = token[0]
                    while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(currentOp)) {
                        applyOperation()
                    }
                    ops.add(currentOp)
                } else {
                    throw IllegalArgumentException("Invalid token: $token")
                }
            }

            while (ops.isNotEmpty()) {
                applyOperation()
            }

            if (values.size != 1) throw IllegalArgumentException("Invalid final expression state")

            val result = values[0]
            if (result.toInt().toDouble() != result) throw ArithmeticException("Final result is not an integer")

            return result
        }

        fun generateOptions(
            operators: List<Operation>,
            correctAnswer: Int,
            level: GameDifficulty
        ): List<Int> {
            val config = levelConfigs[level] ?: LevelConfig(1, 100)
            val options = mutableSetOf<Int>()
            val random = Random(System.currentTimeMillis())

            options.add(correctAnswer)

            var tryCount = 0
            val maxTries = 200

            val range = when {
                operators.contains(Operation.MULTIPLY) || operators.contains(Operation.DIVISION) -> config.maxNumber / 2
                else -> config.maxNumber / 3
            }

            while (options.size < 6 && tryCount < maxTries) {
                val delta = random.nextInt(1, range + 1)
                val sign = if (random.nextBoolean()) 1 else -1
                val wrongAnswer = correctAnswer + delta * sign

                if (wrongAnswer != correctAnswer && wrongAnswer in config.minNumber..config.maxNumber) {
                    options.add(wrongAnswer)
                }
                tryCount++
            }

            var filler = config.minNumber
            while (options.size < 6) {
                if (filler != correctAnswer) options.add(filler)
                filler++
                if (filler > config.maxNumber) filler = config.minNumber
            }

            return options.shuffled(random)
        }
    }
}