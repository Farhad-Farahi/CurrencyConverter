package com.fd.currencyconverter.presentation.currency_exchange

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fd.currencyconverter.databinding.FragmentCurrencyConverterBinding
import com.fd.currencyconverter.domain.common.CommissionPlan
import com.fd.currencyconverter.domain.common.Helper
import com.fd.currencyconverter.domain.common.NetworkResult
import com.fd.currencyconverter.domain.common.toBigDecimalWithRound
import com.fd.currencyconverter.domain.model.RateItem
import com.fd.currencyconverter.domain.model.Rates
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlin.reflect.full.memberProperties


@AndroidEntryPoint
class CurrencyConverterFragment : Fragment() {

    private var _binding: FragmentCurrencyConverterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CurrencyExchangeViewModel by viewModels()
    private lateinit var currencyBalanceAdapter: CurrencyBalanceAdapter
    private lateinit var exchangeEuroRatioMap: HashMap<String, Double>
    private lateinit var balanceMap: HashMap<String, Double>
    private lateinit var myLayoutManager: RecyclerView.LayoutManager

    private lateinit var commissionPlan: CommissionPlan
    private var convertTimeCounter: Int = 0

    private var spinnerBuySelectedItem: String? = null
    private var spinnerSellSelectedItem: String? = null
    private var convertRate: Double = 0.0

    private var freeConvertLeft = -1

    private var firstTime = true

    private val commission = 0.007

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencyConverterBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeCurrencyExchangesRate()
        observeFreeConvertLeft()
        observeBalanceList()
        observeCommissionPlan()
        observeConvertTimeCounter()
        setupBalanceRecyclerView()
        onClickEvent()
        onTextChangedEvent()


    }

    private fun observeCommissionPlan() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.commissionPlan.collectLatest { plan ->
                    commissionPlan = plan
                }
            }
        }
    }

    private fun observeConvertTimeCounter() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.convertTimeCount.collectLatest { convertTimeCount ->
                    convertTimeCounter = convertTimeCount
                }
            }
        }
    }

    private fun onTextChangedEvent() {
        binding.etSell.addTextChangedListener {
            onTextChanged()
        }
    }

    private fun onClickEvent() {
        binding.btnConvert.setOnClickListener {
            val sellValue = binding.etSell.text.toString()
            val buyValue = binding.tvReceiveValue.text.toString()

            if (sellValue.isNotEmpty() && buyValue.isNotEmpty()) {
                val sellValueDouble = sellValue.toDouble()
                val buyValueDouble = buyValue.toDouble()

                val sellCurrency = binding.spinnerSell.selectedItem
                val buyCurrency = binding.spinnerBuy.selectedItem

                val oldSellCurrencyValue = balanceMap[sellCurrency.toString()]
                val oldBuyCurrencyValue = balanceMap[buyCurrency.toString()]

                if (oldSellCurrencyValue != null && oldBuyCurrencyValue != null) {
                    if (sellValueDouble <= oldSellCurrencyValue.toDouble()) {

                        var commissionValue = 0.0
                        val information: String

                        var above200Euro = false
                        val sellRate: Double? = exchangeEuroRatioMap[sellCurrency]
                        if (sellRate != null) {
                            above200Euro = sellValueDouble * sellRate > 200
                        }
                        if (above200Euro) {
                            information =
                                "you have converted $sellValueDouble $sellCurrency to $buyValueDouble ${buyCurrency}. " +
                                        "\n Commission Fee = Above 200 Euro is free." +
                                        "\n Free convert left = $freeConvertLeft"
                        } else if (freeConvertLeft > 0 && commissionPlan == CommissionPlan.First5) {
                            viewModel.decreaseFreeConvertLeft()
                            information =
                                "you have converted $sellValueDouble $sellCurrency to $buyValueDouble ${buyCurrency}. " +
                                        "\n Commission Fee = Free." +
                                        "\n Free convert left = $freeConvertLeft"
                        } else if (freeConvertLeft > 0 && commissionPlan == CommissionPlan.Every5 && convertTimeCounter % 5 == 0) {
                            viewModel.decreaseFreeConvertLeft()
                            information =
                                "you have converted $sellValueDouble $sellCurrency to $buyValueDouble ${buyCurrency}. " +
                                        "\n Commission Fee = Free." +
                                        "\n Free convert left = $freeConvertLeft"
                        } else {
                            commissionValue = sellValueDouble * commission
                            information =
                                "you have converted $sellValueDouble $sellCurrency to $buyValueDouble ${buyCurrency}. " +
                                        "\n Commission Fee = $commissionValue $buyCurrency"
                        }

                        Helper.createDialog(
                            binding.root.context,
                            "Currency Converted ",
                            information,
                            "Done"
                        ).show()


                        val newSellCurrencyValue = oldSellCurrencyValue - sellValueDouble
                        //we should get commission value from our new currency so when user wants to withdraw whole value of
                        //specific currency he can write it all  the calculation and statements handling will be better and more clear
                        val newBuyCurrencyValue =
                            oldBuyCurrencyValue + (buyValueDouble - commissionValue)

                        val listToUpdate = ArrayList<RateItem>()
                        listToUpdate.add(
                            RateItem(
                                currencyName = sellCurrency.toString(),
                                currencyValue = newSellCurrencyValue
                            )
                        )
                        listToUpdate.add(
                            RateItem(
                                currencyName = buyCurrency.toString(),
                                currencyValue = newBuyCurrencyValue
                            )
                        )
                        viewModel.updateCurrencyBalanceList(listToUpdate)
                        viewModel.increaseConvertTimeCount()


                    } else {
                        Toast.makeText(
                            activity,
                            "currency amount is not enough",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {

                    Toast.makeText(activity, "an unknown error Occurred", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activity, "Convert Field is empty", Toast.LENGTH_SHORT).show()
            }

            //scroll to position 0 after updating list
            lifecycleScope.launch(Dispatchers.IO) {
                delay(100)
                activity?.runOnUiThread {
                    myLayoutManager.scrollToPosition(0)
                }
            }
        }

        binding.tvChangeCommissionPlan.setOnClickListener {

            val options = arrayOf(CommissionPlan.First5.toString(), CommissionPlan.Every5.toString())

            val dialog2 = AlertDialog.Builder(binding.root.context)
                .setTitle("Choose Commission plan")
                .setSingleChoiceItems(options, 0) { _, i ->
                    when(i){
                        0 ->{
                            viewModel.changeCommissionPlan(CommissionPlan.First5)
                        }
                        1 ->{
                            viewModel.changeCommissionPlan(CommissionPlan.Every5)
                            viewModel.refreshConvertItemCount()
                        }
                    }
                }.setPositiveButton("Ok") { _, _ -> }.create()

            dialog2.show()
        }
    }

    private fun onTextChanged() {
        val sellValue = binding.etSell.text.toString()
        if (sellValue.isNotEmpty()) {
            val sellValueDouble = sellValue.toDouble()

            if (convertRate != 0.0) {

                val buyValue = sellValueDouble * convertRate
                val buyValueDecimal = buyValue.toBigDecimalWithRound(4)
                binding.tvReceiveValue.text = "$buyValueDecimal"
            }
        } else {
            binding.tvReceiveValue.text = "0.0"
        }
    }


    private fun setupSpinner() {
        val currencyNames: List<String> = Rates::class.memberProperties.map {
            it.name
        }
        val adapter = ArrayAdapter(
            binding.root.context,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, currencyNames
        )

        binding.spinnerBuy.adapter = adapter
        binding.spinnerSell.adapter = adapter

        binding.spinnerBuy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerBuySelectedItem = adapterView?.selectedItem.toString()
                if (!spinnerSellSelectedItem.isNullOrEmpty()) {
                    convertRate =
                        calculateConvertRate(spinnerSellSelectedItem, spinnerBuySelectedItem)
                }
                onTextChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.spinnerSell.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerSellSelectedItem = adapterView?.selectedItem.toString()
                if (!spinnerBuySelectedItem.isNullOrEmpty()) {
                    convertRate =
                        calculateConvertRate(spinnerSellSelectedItem, spinnerBuySelectedItem)
                }
                onTextChanged()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }


    private fun calculateConvertRate(
        spinnerSellSelectedItem: String?,
        spinnerBuySelectedItem: String?
    ): Double {


        val sellRate: Double? = exchangeEuroRatioMap[spinnerSellSelectedItem]
        val buyRate: Double? = exchangeEuroRatioMap[spinnerBuySelectedItem]


        Log.d(
            "calculate5436", "spinnerSellSelectedItem : $spinnerBuySelectedItem " +
                    "\n spinnerBuySelectedItem : $spinnerBuySelectedItem "
        )


        if (sellRate != null && sellRate != 0.0 && buyRate != null && buyRate != 0.0) {
            convertRate = 1 / (sellRate / buyRate)
        } else {
            Toast.makeText(activity, "something wrong", Toast.LENGTH_SHORT).show()
        }


        return convertRate
    }


    private fun setupBalanceRecyclerView() {
        myLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        currencyBalanceAdapter = CurrencyBalanceAdapter()
        binding.apply {
            rvBalance.layoutManager = myLayoutManager
            rvBalance.adapter = currencyBalanceAdapter
        }
    }

    private fun observeBalanceList() {

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currencyBalanceList.collectLatest { currencyBalanceList ->
                    val list: List<RateItem> = currencyBalanceList
                    currencyBalanceAdapter.differ.submitList(list)
                    balanceMap = HashMap()
                    for (item in list) {
                        balanceMap[item.currencyName] = item.currencyValue
                    }
                }
            }
        }
    }

    private fun observeFreeConvertLeft() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.freeConvertLeft.collectLatest { convertLeft ->
                    freeConvertLeft = convertLeft
                }
            }
        }
    }

    private fun observeCurrencyExchangesRate() {
        exchangeEuroRatioMap = HashMap()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exchanges.collectLatest { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            hideProgressBar()
                            response.data?.let { exchangeModelResponse ->
                                val rates = exchangeModelResponse.rates
                                for (currency in Rates::class.memberProperties) {
                                    val value: Double = currency.get(rates) as Double
                                    val name: String = currency.name
                                    exchangeEuroRatioMap[name] = value
                                }
                                if (firstTime) {
                                    firstTime = false
                                    setupSpinner()
                                }

                            }
                        }
                        is NetworkResult.Error -> {
                            hideProgressBar()
                            response.message?.let { _ ->
                                val information = "Please check your internet connection " +
                                        "\n or API_KEY limit ended "
                                Helper.createDialog(
                                    binding.root.context,
                                    "Connection Error",
                                    information,
                                    "OK"
                                ).show()

                            }
                        }
                        is NetworkResult.Loading -> {
                            showProgressBar()
                        }
                    }
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}