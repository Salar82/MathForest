package ir.alishojaee.mathforest.utils

import ir.alishojaee.mathforest.data.MathQuestion
import ir.alishojaee.mathforest.enum.GameDifficulty
import kotlin.random.Random

class Quiz {
    companion object {
        fun evalMathExpression(expr: String): Double {
            val tokens = expr.split(" ").filter { it.isNotBlank() }

            val stack = mutableListOf<Double>()
            val opStack = mutableListOf<String>()

            var i = 0
            while (i < tokens.size) {
                val token = tokens[i]
                when {
                    token == "×" || token == "÷" -> {
                        val prev = stack.removeAt(stack.size - 1)
                        val next = tokens[i + 1].toDouble()
                        val res = if (token == "×") prev * next else prev / next
                        stack.add(res)
                        i += 2
                    }
                    token == "+" || token == "-" -> {
                        opStack.add(token)
                        i++
                    }
                    else -> {
                        stack.add(token.toDouble())
                        i++
                    }
                }
            }

            // حالا فقط جمع و تفریق داریم
            var result = stack[0]
            var opIndex = 0
            for (j in 1 until stack.size) {
                val op = opStack.getOrNull(opIndex++)
                val num = stack[j]
                when (op) {
                    "+" -> result += num
                    "-" -> result -= num
                }
            }
            return result
        }
        fun generateQuestion(difficulty: GameDifficulty): MathQuestion {
            val random = Random(System.currentTimeMillis())
            fun nextEasyNum() = random.nextInt(1, 21) * 5
            fun nextRegularNum() = random.nextInt(1, 100)
            fun nextHardNum() = random.nextInt(1, 100)
            fun nextSingleDigit() = random.nextInt(1, 10)
            val operatorsEasy = listOf("+", "-")
            val operatorsRegular = listOf("+", "-")
            val operatorsHard = listOf("+", "-", "×", "÷")
            val exprParts = mutableListOf<String>()

            when (difficulty) {
                GameDifficulty.EASY -> {
                    var current = nextEasyNum()
                    exprParts.add(current.toString())
                    val opCount = random.nextInt(1, 3)
                    repeat(opCount) {
                        val op = operatorsEasy.random(random)
                        if (op == "+") {
                            val maxAdd = 100 - current
                            val maxAddDiv = maxAdd / 5
                            if (maxAddDiv < 1) return@repeat
                            val n = random.nextInt(1, maxAddDiv + 1) * 5
                            exprParts.add("+")
                            exprParts.add(n.toString())
                            current += n
                        } else if (op == "-") {
                            val maxSubDiv = (current - 5) / 5
                            if (maxSubDiv < 1) return@repeat
                            val n = random.nextInt(1, maxSubDiv + 1) * 5
                            exprParts.add("-")
                            exprParts.add(n.toString())
                            current -= n
                        }
                    }
                }

                GameDifficulty.REGULAR -> {
                    var expr = ""
                    var tries = 0
                    do {
                        val parts = mutableListOf<String>()
                        var current = nextRegularNum()
                        parts.add(current.toString())
                        val opCount = random.nextInt(1, 3)
                        repeat(opCount) {
                            val op = operatorsRegular.random(random)
                            if (op == "+") {
                                val maxAdd = 100 - current
                                if (maxAdd < 1) return@repeat
                                val n = random.nextInt(1, maxAdd + 1)
                                parts.add("+")
                                parts.add(n.toString())
                                current += n
                            } else if (op == "-") {
                                if (current < 2) return@repeat
                                val n = random.nextInt(1, current)
                                parts.add("-")
                                parts.add(n.toString())
                                current -= n
                            }
                        }
                        expr = parts.joinToString(" ")
                        val result = evalMathExpression(expr)
                        if (result > 0 && result <= 100) {
                            exprParts.clear()
                            exprParts.addAll(parts)
                            break
                        }
                        tries++
                    } while (tries < 20)
                }

                GameDifficulty.HARD -> {
                    var expr = ""
                    var tries = 0
                    do {
                        val nums = mutableListOf<Int>()
                        val ops = mutableListOf<String>()
                        nums.add(nextHardNum())
                        val opCount = random.nextInt(2, 4)
                        for (i in 0 until opCount) {
                            val prevNum = nums.last()
                            val op = operatorsHard.random(random)
                            when (op) {
                                "+" -> {
                                    val maxAdd = 99 - prevNum
                                    if (maxAdd < 1) continue
                                    val n = random.nextInt(1, maxAdd + 1)
                                    nums.add(n)
                                    ops.add("+")
                                }
                                "-" -> {
                                    if (prevNum < 2) continue
                                    val n = random.nextInt(1, prevNum)
                                    nums.add(n)
                                    ops.add("-")
                                }
                                "×" -> {
                                    val n = nextSingleDigit()
                                    nums.add(n)
                                    ops.add("×")
                                }
                                "÷" -> {
                                    val possibleDiv = (1..9).filter { it != 0 && prevNum % it == 0 }
                                    if (possibleDiv.isEmpty()) continue
                                    val n = possibleDiv.random(random)
                                    nums.add(n)
                                    ops.add("÷")
                                }
                            }
                        }
                        val parts = mutableListOf(nums[0].toString())
                        for (i in ops.indices) {
                            parts.add(ops[i])
                            parts.add(nums[i + 1].toString())
                        }
                        expr = parts.joinToString(" ")
                        val result = evalMathExpression(expr)
                        if (result > 0 && result <= 100 && result == result.toInt().toDouble()) {
                            exprParts.clear()
                            exprParts.addAll(parts)
                            break
                        }
                        tries++
                    } while (tries < 20)
                }
            }
            val expr = exprParts.joinToString(" ")
            val answer = evalMathExpression(expr)
            return MathQuestion("$expr = ...", answer.toInt())
        }

        fun generateOptions(difficulty: GameDifficulty, correctAnswer: Int): List<Int> {
            val options = mutableSetOf<Int>()
            val random = Random(System.currentTimeMillis())

            when (difficulty) {
                GameDifficulty.EASY -> {
                    val possibleOptions = (1..20).map { it * 5 }.filter { it != correctAnswer }
                    val selected = possibleOptions.shuffled(random).take(5).toMutableList()
                    while (selected.size < 5) {
                        val fake = correctAnswer + (random.nextInt(1, 5) * 5)
                        if (fake != correctAnswer && fake in 5..100 && fake !in selected) selected.add(fake)
                    }
                    selected.add(correctAnswer)
                    return selected.shuffled(random)
                }
                else -> {
                    val range = when (difficulty) {
                        GameDifficulty.REGULAR -> 30
                        GameDifficulty.HARD -> 50
                        else -> 20
                    }
                    options.add(correctAnswer)
                    var tryCount = 0
                    while (options.size < 6 && tryCount < 100) {
                        val delta = random.nextInt(1, range + 1)
                        val sign = if (random.nextBoolean()) 1 else -1
                        val wrongAnswer = correctAnswer + delta * sign
                        if (wrongAnswer != correctAnswer && wrongAnswer > 0) {
                            options.add(wrongAnswer)
                        }
                        tryCount++
                    }
                    var filler = correctAnswer + range + 1
                    while (options.size < 6) {
                        options.add(filler++)
                    }
                    return options.shuffled(random)
                }
            }
        }
    }
}