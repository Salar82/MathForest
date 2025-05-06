package ir.alishojaee.mathforest.activity

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView
import ir.alishojaee.mathforest.R
import ir.alishojaee.mathforest.adapter.MainAnimalsRecyclerAdapter
import ir.alishojaee.mathforest.adapter.OnClickListener
import ir.alishojaee.mathforest.adapter.QuizOptionsRecyclerAdapter
import ir.alishojaee.mathforest.adapter.WinnerLoserListener
import ir.alishojaee.mathforest.data.MathQuestion
import ir.alishojaee.mathforest.data.Settings
import ir.alishojaee.mathforest.data.mainAnimals
import ir.alishojaee.mathforest.databinding.ActivityMainBinding
import ir.alishojaee.mathforest.databinding.DialogChangeAnimalBinding
import ir.alishojaee.mathforest.databinding.DialogExitBinding
import ir.alishojaee.mathforest.databinding.DialogSettingsBinding
import ir.alishojaee.mathforest.databinding.DialogWinLoseBinding
import ir.alishojaee.mathforest.databinding.LayoutQuizBinding
import ir.alishojaee.mathforest.enums.GameDifficulty
import ir.alishojaee.mathforest.enums.Operation
import ir.alishojaee.mathforest.utils.Quiz
import ir.alishojaee.mathforest.utils.randomChoice


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogBinding: DialogSettingsBinding
    private lateinit var quizBinding: LayoutQuizBinding
    private lateinit var quizAdapter: QuizOptionsRecyclerAdapter
    private lateinit var settingsSharedPreferences: SharedPreferences
    private lateinit var settings: Settings
    private lateinit var mainMusic: MediaPlayer
    private lateinit var playMusic: MediaPlayer
    private lateinit var winMusic: MediaPlayer
    private var quizCounter: CountDownTimer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initViews()
        initListeners()
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
        quizCounter?.cancel()
    }

    private fun initData() {
        settingsSharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        settingsSharedPreferences.run {
            settings = Settings(
                getInt("count", 10),
                getInt("time", -1),
                getString("operations", "+,-")!!.split(",").mapNotNull { op ->
                    Operation.entries.find { it.value == op.trim() }
                } as MutableList<Operation>,
                GameDifficulty.valueOf(getString("difficulty", "EASY")!!),
                getBoolean("isMusic", true),
                getBoolean("isSound", true),
                getInt("mainAnimalResId", R.raw.lottie_main_tiger)
            )
        }
        mainMusic = MediaPlayer.create(this, R.raw.sunny).apply {
            isLooping = true
        }
        playMusic = MediaPlayer.create(this, R.raw.se_play)
        winMusic = MediaPlayer.create(this, R.raw.music_win)
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
        if (settings.isMusic)
            mainMusic.start()
        binding.lottieMainAnimal.setAnimation(settings.mainAnimalResId)
    }

    private fun initListeners() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val animalBinding = DialogExitBinding.inflate(layoutInflater)
                val dialog = Dialog(this@MainActivity).apply {
                    setContentView(animalBinding.root)
                }

                animalBinding.btnExit.setOnClickListener {
                    finish()
                }
                animalBinding.btnRateApp.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        })
    }

    private fun initClickListeners() {
        binding.btnPlay.setOnClickListener {
            binding.btnPlay.apply {
                if (settings.isSound) {
                    mainMusic.setVolume(0.4f, 0.4f)
                    playMusic.start()
                }
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
            showSettingsDialog()
        }

        binding.lottieMainAnimal.setOnClickListener {
            showAnimalDialog()
        }
    }

    private fun toggleQuizLayout(isNotShowQuiz: Boolean = true) {  // Fixme: rename variable
        if (!isNotShowQuiz) {
            binding.lottieMainAnimal.isClickable = true
            binding.layoutQuiz.run {
                animate()
                    .alpha(0f)
                    .setDuration(600L)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationCancel(p0: Animator) {}

                        override fun onAnimationEnd(p0: Animator) {
                            this@run.isVisible = false
                        }

                        override fun onAnimationRepeat(p0: Animator) {}
                        override fun onAnimationStart(p0: Animator) {}

                    }).start()
            }
        } else {
            binding.lottieMainAnimal.isClickable = false
        }


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
            ObjectAnimator.ofFloat(binding.lottieMainAnimal, "translationY", fromTigerY, toTigerY)
        val tigerGoLeftAnimation =
            ObjectAnimator.ofFloat(binding.lottieMainAnimal, "translationX", fromTigerX, toTigerX)
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
        var qRemainedCount = settings.count  // Wrong Answers
        var wAnswerCount = 0  // Wrong Answers

        @SuppressLint("SetTextI18n")
        fun nextQuestion(adapter: QuizOptionsRecyclerAdapter) {
            if (qRemainedCount == 0 || wAnswerCount == 3) {
                finishGame(wAnswerCount)
                wAnswerCount = 0
                return
            }
            val question: MathQuestion =
                Quiz.generateValidMathQuestion(settings.operations, settings.difficulty)
            val options: List<Int> =
                Quiz.generateOptions(settings.operations, question.answer, settings.difficulty)

            quizBinding.tvQuestion.text = question.question
            quizBinding.tvQNumber.text = "${settings.count - qRemainedCount-- + 1}"
            adapter.answer = question.answer
            adapter.updateData(options)
        }

        quizBinding = LayoutQuizBinding.bind(binding.included.root)
        quizAdapter = QuizOptionsRecyclerAdapter(
            listOf(), winnerLoserListener = object : WinnerLoserListener {
                override fun onWin(
                    cardOption: MaterialCardView,
                    tvOption: TextView,
                    lottieParticle: LottieAnimationView
                ) {
                    // Correct sound effect
                    if (settings.isSound) {
                        val wonSound = MediaPlayer.create(
                            this@MainActivity,
                            randomChoice(listOf(R.raw.afarin, R.raw.afarin1))
                        )
                        wonSound.start()
                    }

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
                    wAnswerCount++

                    tvOption.setTextColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.white
                        )
                    )
                    cardOption.setCardBackgroundColor(
                        ContextCompat.getColor(this@MainActivity, R.color.button_red)
                    )

                    if (settings.isSound) {
                        // Wrong sound effect
                        val loseSound =
                            MediaPlayer.create(this@MainActivity, R.raw.se_wrong_answer).apply {
                                // Next question
                                setOnCompletionListener {
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
                            }
                        loseSound.start()
                    } else {
                        // Next question
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
            }
        )

        quizBinding.btnBack.setOnClickListener {
            toggleQuizLayout(false)
        }

        quizBinding.recyclerOptions.layoutManager = GridLayoutManager(
            this,
            2,
            GridLayoutManager.VERTICAL, false
        )
        quizBinding.recyclerOptions.adapter = quizAdapter
        nextQuestion(quizAdapter)

        if (settings.time == -1) {
            quizBinding.progressBar.isVisible = false
        } else {
            quizBinding.progressBar.isVisible = true
            startProgressBarTimer(settings.time)
        }
    }

    private fun startProgressBarTimer(durationSeconds: Int) {
        val totalDurationMillis = durationSeconds * 1000L
        updateProgressBarColor(100)

        quizCounter = object : CountDownTimer(totalDurationMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val progressRatio = millisUntilFinished.toDouble() / totalDurationMillis.toDouble()
                val currentProgress = (progressRatio * 1000).toInt()
                quizBinding.progressBar.progress = currentProgress

                val currentPercentage = (progressRatio * 100).toInt()
                updateProgressBarColor(currentPercentage)
            }

            override fun onFinish() {
                finishGame(3)  // Lose
            }
        }

        quizCounter?.start()
    }

    private fun updateProgressBarColor(progressPercent: Int) {
        val colorResId = when {
            progressPercent > 75 -> R.color.light_green
            progressPercent > 50 -> R.color.jungle_accent_yellow
            progressPercent > 25 -> R.color.orange
            else -> R.color.red
        }
        val color = ContextCompat.getColor(this, colorResId)
        quizBinding.progressBar.progressTintList = ColorStateList.valueOf(color)

        val backgroundColor = ContextCompat.getColor(this, R.color.progress_background)
        quizBinding.progressBar.progressBackgroundTintList = ColorStateList.valueOf(backgroundColor)
    }

    private fun finishGame(wAnswerCount: Int) {
        mainMusic.setVolume(1f, 1f)
        quizCounter?.cancel()
        quizBinding.progressBar.progress = 1000
        updateProgressBarColor(1000)
        if (wAnswerCount < 3) {
            showWinLoseDialog(true)
        } else {
            showWinLoseDialog(false)
        }
    }

    private fun showWinLoseDialog(isWin: Boolean) {
        val loseMusic = MediaPlayer.create(this, R.raw.se_lose)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)

        val binding = DialogWinLoseBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        if (isWin) {
            binding.textMessage.text = "آفرین! تو بردی!"
            binding.lottieAnimationView.setAnimation(R.raw.lottie_win)
        } else {
            binding.textMessage.text = "باختی عزیزم! دوباره امتحان کن."
            binding.lottieAnimationView.setAnimation(R.raw.lottie_lose)
        }

        if (settings.isMusic) {
            mainMusic.pause()
            if (isWin)
                winMusic.start()
            else
                loseMusic.start()

        }
        binding.buttonOk.setOnClickListener {
            dialog.dismiss()
            if (settings.isMusic) {
                winMusic.stop()
                mainMusic.start()
            }
        }
        toggleQuizLayout(false)
        dialog.show()
    }

    private fun showSettingsDialog() {
        var isSound = settings.isSound
        var isMusic = settings.isMusic
        dialogBinding = DialogSettingsBinding.inflate(layoutInflater)
        val dialog = Dialog(this).apply {
            setContentView(dialogBinding.root)
        }

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

        settings.operations.forEach {
            when (it) {
                Operation.SUM -> dialogBinding.chOpSum.isChecked = true
                Operation.INTERACT -> dialogBinding.chOpInteract.isChecked = true
                Operation.MULTIPLY -> dialogBinding.chOpMultiply.isChecked = true
                Operation.DIVISION -> dialogBinding.chOpDivision.isChecked = true
            }
        }

        when (settings.time) {
            -1 -> dialogBinding.chTimeNone.isChecked = true
            15 -> dialogBinding.chTime15s.isChecked = true
            30 -> dialogBinding.chTime30s.isChecked = true
            60 -> dialogBinding.chTime1m.isChecked = true
            5 * 60 -> dialogBinding.chTime5m.isChecked = true
            10 * 60 -> dialogBinding.chTime10m.isChecked = true
        }

        when (settings.count) {
            5 -> dialogBinding.chCount5.isChecked = true
            10 -> dialogBinding.chCount10.isChecked = true
            20 -> dialogBinding.chCount20.isChecked = true
            50 -> dialogBinding.chCount50.isChecked = true
            100 -> dialogBinding.chCount100.isChecked = true
        }

        when (settings.difficulty) {
            GameDifficulty.EASY -> dialogBinding.chDifficultyEasy.isChecked = true
            GameDifficulty.MEDIUM -> dialogBinding.chDifficultyMedium.isChecked = true
            GameDifficulty.HARD -> dialogBinding.chDifficultyHard.isChecked = true
        }

        // Save settings
        dialogBinding.btnSave.setOnClickListener {
            // Validates
            settings.count = when (dialogBinding.chipGroupCount.checkedChipId) {
                R.id.ch_count_5 -> 5
                R.id.ch_count_10 -> 10
                R.id.ch_count_20 -> 20
                R.id.ch_count_50 -> 50
                R.id.ch_count_100 -> 100
                else -> 10
            }
            settings.time = when (dialogBinding.chipGroupTime.checkedChipId) {
                R.id.ch_time_none -> -1
                R.id.ch_time_15s -> 15
                R.id.ch_time_30s -> 30
                R.id.ch_time_1m -> 1 * 60
                R.id.ch_time_5m -> 5 * 60
                R.id.ch_time_10m -> 10 * 60
                else -> -1
            }
            settings.difficulty = when (dialogBinding.chipGroupDifficulty.checkedChipId) {
                R.id.ch_difficulty_easy -> GameDifficulty.EASY
                R.id.ch_difficulty_medium -> GameDifficulty.MEDIUM
                R.id.ch_difficulty_hard -> GameDifficulty.HARD
                else -> GameDifficulty.EASY
            }
            settings.isSound = isSound
            settings.isMusic = isMusic
            val operations = mutableListOf<String>()

            dialogBinding.chipGroupOperation.checkedChipIds.forEach {
                operations.add(
                    when (it) {
                        R.id.ch_op_sum -> Operation.SUM.value
                        R.id.ch_op_interact -> Operation.INTERACT.value
                        R.id.ch_op_multiply -> Operation.MULTIPLY.value
                        R.id.ch_op_division -> Operation.DIVISION.value
                        else -> Operation.SUM.value
                    }
                )
            }

            settings.operations = operations.mapNotNull { op ->
                Operation.entries.find { it.value == op.trim() }
            } as MutableList<Operation>

            if (!isMusic)
                mainMusic.pause()
            else
                mainMusic.start()

            settingsSharedPreferences.edit().run {
                putInt("count", settings.count)
                putInt("time", settings.time)
                putString("operations", operations.joinToString(","))
                putString("difficulty", settings.difficulty.name)
                putBoolean("isMusic", settings.isMusic)
                putBoolean("isSound", settings.isSound)
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

    private fun showAnimalDialog() {
        val animalBinding = DialogChangeAnimalBinding.inflate(layoutInflater)
        val dialog = Dialog(this).apply {
            setContentView(animalBinding.root)
        }

        animalBinding.rvAnimal.layoutManager = GridLayoutManager(
            this,
            2,
            GridLayoutManager.VERTICAL, false
        )
        animalBinding.rvAnimal.adapter = MainAnimalsRecyclerAdapter(
            mainAnimals, object : OnClickListener {
                override fun onItemClick(resID: Int) {
                    binding.lottieMainAnimal.setAnimation(resID)
                    settings.mainAnimalResId = resID
                    settingsSharedPreferences.edit().run {
                        putInt("mainAnimalResId", resID)
                        commit()
                    }
                    dialog.dismiss()
                }
            }
        )
        dialog.show()

    }
}
