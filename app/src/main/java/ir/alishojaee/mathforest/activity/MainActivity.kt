package ir.alishojaee.mathforest.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView
import ir.alishojaee.mathforest.R
import ir.alishojaee.mathforest.adapter.QuizOptionsRecyclerAdapter
import ir.alishojaee.mathforest.adapter.WinnerLoserListener
import ir.alishojaee.mathforest.data.MathQuestion
import ir.alishojaee.mathforest.data.Settings
import ir.alishojaee.mathforest.databinding.ActivityMainBinding
import ir.alishojaee.mathforest.databinding.DialogSettingsBinding
import ir.alishojaee.mathforest.databinding.LayoutQuizBinding
import ir.alishojaee.mathforest.enum.GameDifficulty
import ir.alishojaee.mathforest.utils.Quiz
import ir.alishojaee.mathforest.utils.showToast


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogBinding: DialogSettingsBinding
    private lateinit var quizBinding: LayoutQuizBinding
    private lateinit var quizAdapter: QuizOptionsRecyclerAdapter
    private lateinit var settingsSharedPreferences: SharedPreferences
    private lateinit var settings: Settings
    private lateinit var mainMusic: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initViews()
        initClickListeners()
    }

    override fun onPause() {
        super.onPause()
        if (settings.isMusic)
            mainMusic.pause()
    }

    override fun onResume() {
        super.onResume()
        if (settings.isMusic)
            mainMusic.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (settings.isMusic)
            mainMusic.release()
    }

    private fun initData() {
        settingsSharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        settingsSharedPreferences.run {
            settings = Settings(
                getInt("count", 10),
                getInt("time", 60),
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
        mainMusic = MediaPlayer.create(this, R.raw.sunny).apply {
            isLooping = true
        }
        if (settings.isMusic)
            mainMusic.start()
    }

    private fun initClickListeners() {
        binding.btnPlay.setOnClickListener {
            binding.btnPlay.apply {
                animate()
                    .alpha(0f)
                    .setDuration(600)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            isVisible = false
                        }

                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    })
            }
            binding.btnSettings.apply {
                animate()
                    .alpha(0f)
                    .setDuration(600)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            isVisible = false
                        }

                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    })
            }
            toggleQuizLayout()
            startGame()
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

                if (!isMusic)
                    mainMusic.pause()
                else
                    mainMusic.start()

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

    private fun toggleQuizLayout(isNotShowQuiz: Boolean = true) {
        val cloudsQuizOffset = -250f
        val sunOffset = -210f
        val grassFlowersOffset = 300f
        val tigerVerticalFlowersOffset = 320f
        val tigerHorizontalFlowersOffset = -60f

        val (fromCloud, toCloud) = if (isNotShowQuiz) 0f to cloudsQuizOffset else cloudsQuizOffset to 0f
        val (fromSun, toSun) = if (isNotShowQuiz) 0f to sunOffset else sunOffset to 0f
        val (fromQuiz, toQuiz) = if (isNotShowQuiz) 0f to cloudsQuizOffset else cloudsQuizOffset to 0f
        val (fromTigerY, toTigerY) = if (isNotShowQuiz) 0f to tigerVerticalFlowersOffset else tigerVerticalFlowersOffset to 0f
        val (fromTigerX, toTigerX) = if (isNotShowQuiz) 0f to tigerHorizontalFlowersOffset else tigerHorizontalFlowersOffset to 0f
        val (fromGrass, toGrass) = if (isNotShowQuiz) 0f to grassFlowersOffset else grassFlowersOffset to 0f
        val (fromFlowers, toFlowers) = if (isNotShowQuiz) 0f to grassFlowersOffset else grassFlowersOffset to 0f

        val leftCloudAnimation =
            ObjectAnimator.ofFloat(binding.leftCloud, "translationY", fromCloud, toCloud)
        val rightCloudAnimation =
            ObjectAnimator.ofFloat(binding.rightCloud, "translationY", fromCloud, toCloud)
        val sunAnimation = ObjectAnimator.ofFloat(binding.lottieSun, "translationY", fromSun, toSun)
        val quizLayoutAnimation =
            ObjectAnimator.ofFloat(binding.layoutQuiz, "translationY", fromQuiz, toQuiz)
        val tigerAnimation =
            ObjectAnimator.ofFloat(binding.lottieTiger, "translationY", fromTigerY, toTigerY)
        val tigerGoLeftAnimation =
            ObjectAnimator.ofFloat(binding.lottieTiger, "translationX", fromTigerX, toTigerX)
        val grassAnimation =
            ObjectAnimator.ofFloat(binding.imgGrass, "translationY", fromGrass, toGrass)
        val flowersAnimation =
            ObjectAnimator.ofFloat(binding.imgFlowers, "translationY", fromFlowers, toFlowers)

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
            duration = 1000
        }

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                if (isNotShowQuiz) {
                    binding.layoutQuiz.apply {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate()
                            .alpha(1f)
                            .setDuration(600)
                            .setListener(null)
                    }
                } else {
                    binding.btnPlay.apply {
                        alpha = 0f
                        isVisible = true
                        animate()
                            .alpha(1f)
                            .setDuration(600)
                            .setListener(null)
                    }
                    binding.btnSettings.apply {
                        alpha = 0f
                        isVisible = true
                        animate()
                            .alpha(1f)
                            .setDuration(600)
                            .setListener(null)
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animatorSet.start()
    }

    private fun startGame() {
        var qCount = settings.count  // Wrong Answers
        var cCount = 0  // Correct Answers

        fun nextQuestion(adapter: QuizOptionsRecyclerAdapter) {
            if (qCount == 0)
                finishGame(settings.count - cCount, cCount)
            val question: MathQuestion = Quiz.generateQuestion(settings.difficulty)
            val options: List<Int> = Quiz.generateOptions(settings.difficulty, question.answer)

            quizBinding.tvQuestion.text = question.question
            adapter.answer = question.answer
            adapter.updateData(options)
            qCount--
        }

        quizBinding = LayoutQuizBinding.bind(binding.included.root)
        quizAdapter = QuizOptionsRecyclerAdapter(
            listOf(), winnerLoserListener = object : WinnerLoserListener {
                override fun onWin(
                    cardOption: MaterialCardView,
                    tvOption: TextView,
                    lottieParticle: LottieAnimationView
                ) {
                    cCount++
                    tvOption.setTextColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.white
                        )
                    )
                    cardOption.setCardBackgroundColor(
                        ContextCompat.getColor(this@MainActivity, R.color.jungle_green_primary)
                    )
                    lottieParticle.isVisible = true
                    lottieParticle.addAnimatorListener(
                        object : Animator.AnimatorListener {
                            override fun onAnimationCancel(p0: Animator) {}
                            override fun onAnimationEnd(p0: Animator) {
                                tvOption.setTextColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.cloud_test
                                    )
                                )
                                cardOption.setCardBackgroundColor(
                                    ContextCompat.getColor(this@MainActivity, R.color.white)
                                )
                                nextQuestion(quizAdapter)
                            }

                            override fun onAnimationRepeat(p0: Animator) {}
                            override fun onAnimationStart(p0: Animator) {}
                        }
                    )
                    lottieParticle.playAnimation()
                }

                override fun onLose(cardOption: MaterialCardView, tvOption: TextView) {
                    qCount--
                    nextQuestion(quizAdapter)
                    tvOption.setTextColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.cloud_test
                        )
                    )
                    cardOption.setCardBackgroundColor(
                        ContextCompat.getColor(this@MainActivity, R.color.white)
                    )
                }
            }
        )


        quizBinding.recyclerOptions.layoutManager = GridLayoutManager(
            this,
            2,
            GridLayoutManager.VERTICAL, false
        )
        quizBinding.recyclerOptions.adapter = quizAdapter
        nextQuestion(quizAdapter)
    }

    private fun finishGame(wCount: Int, cCount: Int) {
        showToast("You won! $wCount, $cCount")
    }
}


//var qCount = settings.count
//quizBinding.btnCancel.setOnClickListener {
//    binding.layoutQuiz.animate()
//        .alpha(0f)
//        .setDuration(600)
//        .setListener(object : Animator.AnimatorListener {
//            override fun onAnimationCancel(p0: Animator) {}
//            override fun onAnimationEnd(p0: Animator) {
//                binding.layoutQuiz.isVisible = false
//                toggleQuizLayout(false)
//            }
//
//            override fun onAnimationRepeat(p0: Animator) {}
//            override fun onAnimationStart(p0: Animator) {}
//
//        })
//}