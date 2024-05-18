package com.example.mathgame

import kotlin.random.Random

class EquationGenerator {
    private val lastFiveEquations = mutableListOf<String>()

    fun generateEquation(score: Int): Equation {
        var correctAnswer: Int
        var equation: String
        var operands: MutableList<Int>
        var operators: MutableList<String>

        do {
            do {
                val operandCount = when {
                    score < 5 -> Random.nextInt(2, 5)
                    score < 10 -> Random.nextInt(2, 6)
                    score < 15 -> Random.nextInt(2, 7)
                    score < 20 -> Random.nextInt(3, 8)
                    else -> Random.nextInt(4, 9)
                }

                operands = MutableList(operandCount) { Random.nextInt(1, 4) }
                operators = MutableList(operandCount - 1) { if (Random.nextBoolean()) "+" else "-" }

                correctAnswer = operands[0]
                for (i in 1 until operands.size) {
                    if (operators[i - 1] == "+") {
                        correctAnswer += operands[i]
                    } else {
                        correctAnswer -= operands[i]
                    }
                }

                equation = buildEquation(operands, operators) + " = ?"
            } while (correctAnswer !in 1..3)
        } while (equation in lastFiveEquations)

        lastFiveEquations.add(equation)
        if (lastFiveEquations.size > 5) {
            lastFiveEquations.removeAt(0)
        }

        val answers = listOf("1", "2", "3")

        return Equation(equation, answers, correctAnswer.toString())
    }

    fun generateInvertedEquation(score: Int): Equation {
        var correctAnswer: Int
        var equation: String
        var operands: MutableList<Int>
        var operators: MutableList<String>

        do {
            do {
                val operandCount = when {
                    score < 5 -> Random.nextInt(2, 3)
                    score < 10 -> Random.nextInt(2, 4)
                    score < 15 -> Random.nextInt(2, 5)
                    score < 20 -> Random.nextInt(3, 6)
                    else -> Random.nextInt(4, 7)
                }

                operands = MutableList(operandCount) { Random.nextInt(1, 4) }
                operators = MutableList(operandCount - 1) { if (Random.nextBoolean()) "+" else "-" }

                correctAnswer = operands[0]
                for (i in 1 until operands.size) {
                    if (operators[i - 1] == "+") {
                        correctAnswer += operands[i]
                    } else {
                        correctAnswer -= operands[i]
                    }
                }

                equation = buildEquation(operands, operators) + " = $correctAnswer"
            } while (correctAnswer !in 1..3)
        } while (equation in lastFiveEquations)

        lastFiveEquations.add(equation)
        if (lastFiveEquations.size > 5) {
            lastFiveEquations.removeAt(0)
        }

        val operatorIndex = Random.nextInt(operators.size)
        equation = equation.replaceFirst(operators[operatorIndex], "?")

        val answers = listOf("+", "-", "")

        return Equation(equation, answers, operators[operatorIndex])
    }

    private fun buildEquation(operands: List<Int>, operators: List<String>): String {
        val equation = StringBuilder()
        for (i in operands.indices) {
            equation.append(operands[i])
            if (i < operators.size) {
                equation.append(" ${operators[i]} ")
            }
        }

        return equation.toString()
    }
}