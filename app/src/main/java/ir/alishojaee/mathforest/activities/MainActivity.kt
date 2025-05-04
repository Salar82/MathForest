package ir.alishojaee.mathforest.activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ir.alishojaee.mathforest.R
import ir.alishojaee.mathforest.data.Settings
import ir.alishojaee.mathforest.databinding.ActivityMainBinding
import ir.alishojaee.mathforest.databinding.DialogSettingsBinding
import ir.alishojaee.mathforest.enums.GameDifficulty


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var dialogBinding: DialogSettingsBinding
    lateinit var settingsSharedPreferences: SharedPreferences
    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initViews()
        initClickListeners()
    }

    private fun initData() {
        settingsSharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        settingsSharedPreferences.run {
            settings = Settings(
                getInt("count", 10),
                getInt("time", 1),
                when (getString("difficulty", "EASY")) {
                    "EASY" -> GameDifficulty.EASY
                    "REGULAR" -> GameDifficulty.REGULAR
                    "HARD" -> GameDifficulty.HARD
                    else -> GameDifficulty.EASY
                },
                getBoolean("isMusic", true),
                getBoolean("isSound", true),
            )
        }
    }

    private fun initViews() {
        // Clouds animation
        ObjectAnimator.ofFloat(binding.leftCloud, "translationX", 0f, 75f).run {
            duration = 10_000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
        ObjectAnimator.ofFloat(binding.rightCloud, "translationX", 0f, -100f).run {
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
            binding.leftCloud, "translationY", 0f, topOffset - 40f
        )
        val rightCloudAnimation = ObjectAnimator.ofFloat(
            binding.rightCloud, "translationY", 0f, topOffset - 40f
        )
        val sunAnimation = ObjectAnimator.ofFloat(
            binding.lottieSun, "translationY", 0f, topOffset
        )
        val quizLayoutAnimation = ObjectAnimator.ofFloat(
            binding.layoutQuiz, "translationY", 0f, topOffset - 40f
        )
        val tigerAnimation = ObjectAnimator.ofFloat(
            binding.lottieTiger, "translationY", 0f, bottomOffset + 20f
        )
        val tigerGoLeftAnimation = ObjectAnimator.ofFloat(
            binding.lottieTiger, "translationX", 0f, -60f
        )
        val grassAnimation = ObjectAnimator.ofFloat(
            binding.imgGrass, "translationY", 0f, bottomOffset
        )
        val flowersAnimation = ObjectAnimator.ofFloat(
            binding.imgFlowers, "translationY", 0f, bottomOffset
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
            var isSound = settings.isSound
            var isMusic = settings.isMusic
            dialogBinding = DialogSettingsBinding.inflate(layoutInflater)
            val dialog = Dialog(this).apply {
                setContentView(dialogBinding.root)
            }


            dialogBinding.etCount.setText(settings.count.toString())
            dialogBinding.etTime.setText(settings.time.toString())
            dialogBinding.btnVolume.setImageResource(
                when (settings.isSound) {
                    true -> R.drawable.img_volume
                    false -> R.drawable.img_volume_off
                }
            )
            dialogBinding.btnMusic.setImageResource(
                when (settings.isMusic) {
                    true -> R.drawable.img_music
                    false -> R.drawable.img_music_off
                }
            )

            dialogBinding.btnVolume.setOnClickListener {
                dialogBinding.btnVolume.setImageResource(
                    when (isSound) {
                        true -> R.drawable.img_volume_off
                        false -> R.drawable.img_volume
                    }
                )
                isSound = !isSound
            }
            dialogBinding.btnMusic.setOnClickListener {
                dialogBinding.btnMusic.setImageResource(
                    when (isMusic) {
                        true -> R.drawable.img_music_off
                        false -> R.drawable.img_music
                    }
                )
                isMusic = !isMusic
            }
            when (settings.difficulty) {
                GameDifficulty.EASY -> dialogBinding.chipEasy.isChecked = true
                GameDifficulty.REGULAR -> dialogBinding.chipRegular.isChecked = true
                GameDifficulty.HARD -> dialogBinding.chipHard.isChecked = true
            }


            // Save settings
            dialogBinding.btnSave.setOnClickListener {
                settings.count = dialogBinding.etCount.text.toString().toInt()
                settings.time = dialogBinding.etTime.text.toString().toInt()
                settings.difficulty = when (dialogBinding.chipGroup.checkedChipId) {
                    R.id.chip_easy -> GameDifficulty.EASY
                    R.id.chip_regular -> GameDifficulty.REGULAR
                    R.id.chip_hard -> GameDifficulty.HARD
                    else -> throw Error()
                }
                settings.isSound = isSound
                settings.isMusic = isMusic

                settingsSharedPreferences.edit().run {
                    putInt("count", settings.count)
                    putInt("time", settings.time)
                    putString(
                        "difficulty", when (settings.difficulty) {
                            GameDifficulty.EASY -> "EASY"
                            GameDifficulty.REGULAR -> "REGULAR"
                            GameDifficulty.HARD -> "HARD"
                        }
                    )
                    putBoolean("isMusic", settings.isMusic)
                    putBoolean("isSound", settings.isMusic)
                    apply()
                }
                dialog.dismiss()
            }

            // Cancel button
            dialogBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}