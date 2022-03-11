package com.fd.currencyconverter.presentation.currency_exchange


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fd.currencyconverter.databinding.ItemBalanceBinding
import com.fd.currencyconverter.domain.common.toBigDecimalWithRound
import com.fd.currencyconverter.domain.model.RateItem

class CurrencyBalanceAdapter : RecyclerView.Adapter<CurrencyBalanceAdapter.BalanceViewHolder>() {

    inner class BalanceViewHolder(val binding: ItemBalanceBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<RateItem>() {
        override fun areItemsTheSame(oldItem: RateItem, newItem: RateItem): Boolean {
            return oldItem.currencyName == newItem.currencyName
        }

        override fun areContentsTheSame(oldItem: RateItem, newItem: RateItem): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceViewHolder {
        return BalanceViewHolder(
            ItemBalanceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BalanceViewHolder, position: Int) {
        val currency = differ.currentList[position]
        val value = currency.currencyValue
        val valueBigDecimal = value.toBigDecimalWithRound(4)
        val result = " ${currency.currencyName} $valueBigDecimal "
        holder.binding.tvBalance.text = result
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}