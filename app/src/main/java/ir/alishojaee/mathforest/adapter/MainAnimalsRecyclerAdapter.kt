package ir.alishojaee.mathforest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import ir.alishojaee.mathforest.R
import ir.alishojaee.mathforest.data.MainAnimal


interface OnClickListener {
    fun onItemClick(resID: Int)
}

class MainAnimalsRecyclerAdapter(
    private var lottieIds: List<MainAnimal>,
    private var onClickListener: OnClickListener
) : RecyclerView.Adapter<MainAnimalsRecyclerAdapter.OptionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.layout_recycler_main_animal, parent, false
        )
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.lottie.run {
            setAnimation(lottieIds[position].lottieRawRes)
            playAnimation()
            setOnClickListener {
                onClickListener.onItemClick(lottieIds[position].lottieRawRes)
            }
        }
        holder.tvName.text = lottieIds[position].name
    }

    override fun getItemCount(): Int {
        return lottieIds.size
    }

    class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lottie: LottieAnimationView = itemView.findViewById(R.id.lottie_card_animal)
        val tvName: TextView = itemView.findViewById(R.id.tv_animal_name)
    }
}