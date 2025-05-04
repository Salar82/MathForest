package ir.alishojaee.mathforest.activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.View
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
        initClickListeners()
    }

    private fun initViews() {
        // Clouds animation
        ObjectAnimator.ofFloat(binding.leftCloud, "translationX", 0f, 75f).apply {
            duration = 10_000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
        ObjectAnimator.ofFloat(binding.rightCloud, "translationX", 0f, -100f).apply {
            duration = 8_000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    private fun initClickListeners() {
        val topOffset = -210f
        val bottomOffset = 300f
        val leftCloudAnimation = ObjectAnimator.ofFloat(
            binding.leftCloud,
            "translationY",
            0f,
            topOffset - 40f
        )
        val rightCloudAnimation = ObjectAnimator.ofFloat(
            binding.rightCloud,
            "translationY",
            0f,
            topOffset - 40f
        )
        val sunAnimation = ObjectAnimator.ofFloat(
            binding.lottieSun,
            "translationY",
            0f, topOffset
        )
        val quizLayoutAnimation = ObjectAnimator.ofFloat(
            binding.layoutQuiz,
            "translationY",
            0f,
            topOffset - 40f
        )
        val tigerAnimation = ObjectAnimator.ofFloat(
            binding.lottieTiger,
            "translationY",
            0f,
            bottomOffset + 20f
        )
        val tigerGoLeftAnimation = ObjectAnimator.ofFloat(
            binding.lottieTiger,
            "translationX",
            0f,
            -60f
        )
        val grassAnimation = ObjectAnimator.ofFloat(
            binding.imgGrass,
            "translationY",
            0f,
            bottomOffset
        )
        val flowersAnimation = ObjectAnimator.ofFloat(
            binding.imgFlowers,
            "translationY",
            0f,
            bottomOffset
        )

        val animatorSet = AnimatorSet().apply {
            playTogether(
                leftCloudAnimation,
                rightCloudAnimation,
                quizLayoutAnimation,
                sunAnimation,
                tigerAnimation,
                tigerGoLeftAnimation,
                grassAnimation,
                flowersAnimation
            )
            duration = 3000
        }

        binding.btnPlay.setOnClickListener {
            binding.btnPlay.visibility = View.INVISIBLE
            binding.btnSettings.visibility = View.INVISIBLE
            animatorSet.start()
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(p0: Animator) {}

                override fun onAnimationEnd(p0: Animator) {
                    binding.layoutQuiz.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(p0: Animator) {}

                override fun onAnimationStart(p0: Animator) {}

            })
        }

        binding.btnSettings.setOnClickListener {
            Dialog(this).apply {

            }
        }
    }
}