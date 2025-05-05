package ir.alishojaee.mathforest.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView
import ir.alishojaee.mathforest.R


interface WinnerLoserListener {
    fun onWin(
        cardOption: MaterialCardView,
        tvOptions: TextView,
        lottieParticle: LottieAnimationView
    )

    fun onLose(cardOption: MaterialCardView, tvOptions: TextView)
}

class QuizOptionsRecyclerAdapter(
    private var options: List<Int>,
    var answer: Int = -1,
    val winnerLoserListener: WinnerLoserListener
) : RecyclerView.Adapter<QuizOptionsRecyclerAdapter.OptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_recycler_quiz_options, parent, false
        )
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.cardOption.setOnClickListener {
            if (holder.tvOption.text.equals(answer.toString()))
                winnerLoserListener.onWin(
                    holder.cardOption,
                    holder.tvOption,
                    holder.lottieParticle
                )
            else
                winnerLoserListener.onLose(holder.cardOption, holder.tvOption)
        }
        holder.tvOption.text = options[position].toString()
    }

    override fun getItemCount(): Int {
        return options.size  // always 6
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Int>) {
        options = newItems
        this.notifyDataSetChanged()
    }

    class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardOption: MaterialCardView = itemView.findViewById(R.id.card_option)
        val tvOption: TextView = itemView.findViewById(R.id.tv_option)
        val lottieParticle: LottieAnimationView = itemView.findViewById(R.id.lottie_particle)
    }
}