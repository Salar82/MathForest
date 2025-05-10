package ir.alishojaee.mathforest.data

import ir.alishojaee.mathforest.enums.GameDifficulty
import ir.alishojaee.mathforest.enums.Operation

data class Settings(
    var count: Int,
    var time: Int,
    var operations: MutableList<Operation>,
    var difficulty: GameDifficulty,
    var isMusic: Boolean,
    var isSound: Boolean,
    var mainAnimalResId: Int
)