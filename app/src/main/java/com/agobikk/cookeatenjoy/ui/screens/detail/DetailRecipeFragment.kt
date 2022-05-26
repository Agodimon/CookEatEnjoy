package com.agobikk.cookeatenjoy.ui.screens.detail

import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.agobikk.cookeatenjoy.R
import com.agobikk.cookeatenjoy.aplication.App
import com.agobikk.cookeatenjoy.data.converters.ExtendedIngredientImpl
import com.agobikk.cookeatenjoy.data.local.Database
import com.agobikk.cookeatenjoy.data.local.entities.FoodInformationEntity
import com.agobikk.cookeatenjoy.databinding.FragmentDetailRecipeBinding
import com.agobikk.cookeatenjoy.models.FoodInformation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class DetailRecipeFragment : Fragment(R.layout.fragment_detail_recipe) {
    private val viewBinding: FragmentDetailRecipeBinding by viewBinding()
    private val args: DetailRecipeFragmentArgs by navArgs()
    private var isFavorite = false
    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable -> Timber.d("throwable:$throwable") }
    private val scope =
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler + SupervisorJob())
    private var job: Job? = null


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: DetailRecipeViewModel

    @Inject
    lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.instance.appComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[DetailRecipeViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModelFactory.create(DetailRecipeViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        val foodId = getFoodId()
        viewModel.onViewCreated(id = foodId)
        setScrollListener()
        subscribeUi()
        navigate(foodId)
        iconSelected ()

    }

    private fun getFoodId(): Long {
        return args.idFood
    }

    private fun setScrollListener() = with(viewBinding) {
        val appBarLayout = recipeDetailAppBarLayout
        val offsetChangedListener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            Timber.i("setScrollListener: " + verticalOffset + " " + "\n" + "range:  " + appBarLayout.totalScrollRange)
            val seekPosition = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
            recipeDetailMotionLayout.progress = seekPosition
        }
        appBarLayout.addOnOffsetChangedListener(offsetChangedListener)
    }

    private fun iconSelected () = with(viewBinding) {
        includeLayoutDetailIcon.recipeDetailFavoriteIcon.setOnClickListener{
            isFavorite = if (!isFavorite) {
                includeLayoutDetailIcon.recipeDetailFavoriteIcon.setImageResource(R.drawable.ic_baseline_favorite)
                true
            } else {
                includeLayoutDetailIcon.recipeDetailFavoriteIcon.setImageResource(R.drawable.ic_baseline_favorite_border)
                false
            }
        }

    }

    private fun subscribeUi() {
        viewModel.recipeDetail.observe(viewLifecycleOwner) {
            it?.body()?.let { foodInformation ->
                Timber.i("subscribeUi: $it")
                setDetails(foodInformation)
            }
            viewModel.recipeDetail.observe(viewLifecycleOwner) { list ->

                val body = list?.body()
                Timber.d("ExtendedIngredient--->>>>>>:${list?.body()?.extendedIngredient}")
                val converter = ExtendedIngredientImpl()
                val ingredients =
                    list?.body()?.extendedIngredient?.map { converter.convert(it) } ?: emptyList()
                val foodInformation = FoodInformationEntity(
                    id = body?.id ?: 1,
                    image = body?.image ?: "image_food_url",
                    instructions = body?.instructions ?: "instructions",
                    title = body?.title ?: "title",
                    sourceName = body?.sourceName ?: "sourceName",
                    extendedIngredientEntity = ingredients
                )

                job?.cancel()
                job = scope.launch {
                    database
                        .getFoodInformation()
                        .insertFoodInfo(foodInformation)
                }
            }
        }
    }

    private fun setDetails(detailRecipe: FoodInformation) = with(viewBinding) {
        context?.let {
            Glide.with(it)
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image)
                )
                .load(detailRecipe.image)
                .into(recipeDetailImage)
        }
        recipeDetailTitle.text = detailRecipe.title
        sourceNameRecipe.text = detailRecipe.sourceName
        wordProcessing(detailRecipe)
    }

    private fun wordProcessing(detailRecipe: FoodInformation) = with(viewBinding) {
        includeLayoutCardInstruction.cookingInstructions.text =
            detailRecipe
                .instructions
                .parseAsHtml(HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                includeLayoutCardInstruction.cookingInstructions.justificationMode =
                    JUSTIFICATION_MODE_INTER_WORD
            }
        }
    }

    private fun navigate(value: Long) {
        with(viewBinding) {
            includeLayoutDetailIcon.recipeDetailCloseIcon.setOnClickListener {
                findNavController()
                    .navigateUp()
            }
            ingredientImage.setOnClickListener {
                val direction =
                    DetailRecipeFragmentDirections.actionDetailRecipeFragmentToIngredientFragment(
                        value
                    )

                findNavController()
                    .navigate(direction)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()

    }
}