package ir.alishojaee.mathforest.data

import ir.alishojaee.mathforest.R

data class MainAnimal(
    val lottieRawRes: Int,
    val name: String
)

val mainAnimals = listOf(
    MainAnimal(R.raw.lottie_main_tiger, "ببری"),
    MainAnimal(R.raw.lottie_main_cat, "مشکی"),
    MainAnimal(R.raw.lottie_main_cat2, "پیشی تنبل"),
    MainAnimal(R.raw.lottie_main_dog, "دم طلا"),
    MainAnimal(R.raw.lottie_main_dog2, "ملوس"),
    MainAnimal(R.raw.lottie_main_bird, "جیکی"),
    MainAnimal(R.raw.lottie_main_panda, "پاندا کیتی"),
    MainAnimal(R.raw.lottie_main_snake, "ماری"),
    MainAnimal(R.raw.lottie_main_giraffe, "خال خالی"),
    MainAnimal(R.raw.lottie_main_bear, "خرس کوچولو"),
    MainAnimal(R.raw.lottie_main_bear2, "شنگول"),
)
