package ir.alishojaee.mathforest.data

import ir.alishojaee.mathforest.enum.GameDifficulty

data class Settings(
    var count: Int,
    var time: Int,
    var difficulty: GameDifficulty,
    var isMusic: Boolean,
    var isSound: Boolean
)