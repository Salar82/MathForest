package ir.alishojaee.mathforest.utils

import android.content.Context
import android.widget.Toast
import java.util.Random

fun Context.showToast(message: String, isLong: Boolean = false) {
    Toast.makeText(this, message, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun <T> randomChoice(items: List<T>): T {
    if (items.isEmpty()) throw IllegalArgumentException("List cannot be empty")
    return items[Random().nextInt(items.size)]
}
