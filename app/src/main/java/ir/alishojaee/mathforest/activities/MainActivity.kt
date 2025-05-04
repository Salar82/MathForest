package ir.alishojaee.mathforest.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ir.alishojaee.mathforest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()

    }

    fun initViews() {
        // Right cloud animation
        ObjectAnimator.ofFloat(binding.leftCloud, "translationX", 0f, 75f).apply {
            duration = 10_000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }.start()
        ObjectAnimator.ofFloat(binding.rigthCloud, "translationX", 0f, -100f).apply {
            duration = 8_000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }.start()
    }
}