package com.agobikk.cookeatenjoy.ui.screens.ingredient

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.agobikk.cookeatenjoy.R
import com.agobikk.cookeatenjoy.aplication.App
import com.agobikk.cookeatenjoy.data.local.entities.ExtendedIngredientEntity
import com.agobikk.cookeatenjoy.databinding.FragmentListIngredientBinding
import kotlinx.coroutines.*
import timber.log.Timber


class IngredientListFragment : Fragment(R.layout.fragment_list_ingredient) {
    private val viewBinding: FragmentListIngredientBinding by viewBinding()
    private lateinit var adapter: IngredientListAdapter
    private val viewModel: IngredientViewModel by viewModels()
    private val args: IngredientListFragmentArgs by navArgs()

    private var listIngredientsBD = emptyList<ExtendedIngredientEntity>()
    private fun getFoodId(): Long {
        return args.idFood
    }

    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable -> Timber.d("throwable:$throwable") }
    private val scopeIo =
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler + SupervisorJob())
    private val scopeMain =
        CoroutineScope(Dispatchers.Main + coroutineExceptionHandler + SupervisorJob())
    private var job: Job? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        init()


        job = scopeMain.launch() {

            listIngredientsBD = withContext(Dispatchers.IO) {

                val i = App.instance.databaseService.getFoodInformation()
                    .getIngredients(getFoodId()).extendedIngredientEntity
                i
            }
            Timber.d("list ingredients from BD:${listIngredientsBD}")
            adapter.submitList(listIngredientsBD)
        }
    }

    private fun init() = with(viewBinding) {
        adapter = IngredientListAdapter(object : OnIngredientClickListener {
            override fun onClick(extendedIngredient: ExtendedIngredientEntity) {
            }
        })
        viewBinding.ingredientsRecyclerview.adapter = adapter
    }

    override fun onDestroy() {
        scopeIo.cancel()
        scopeMain.cancel()
        super.onDestroy()
    }
}


