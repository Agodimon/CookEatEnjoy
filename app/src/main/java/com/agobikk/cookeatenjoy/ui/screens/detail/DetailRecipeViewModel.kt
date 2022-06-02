package com.agobikk.cookeatenjoy.ui.screens.detail

import androidx.lifecycle.*
import com.agobikk.cookeatenjoy.application.di.AssistedSavedStateViewModelFactory
import com.agobikk.cookeatenjoy.data.Repository
import com.agobikk.cookeatenjoy.data.local.entities.FavoriteRecipeEntity
import com.agobikk.cookeatenjoy.data.local.entities.FoodInformationEntity
import com.agobikk.cookeatenjoy.models.FoodInformation
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber

class DetailRecipeViewModel @AssistedInject constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    @AssistedFactory
    interface Factory : AssistedSavedStateViewModelFactory<DetailRecipeViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): DetailRecipeViewModel
    }

    lateinit var deferred: Deferred<FoodInformationEntity>
    lateinit var deferredOne: Deferred<LiveData<List<FoodInformationEntity>>>

    init {

        Timber.d("-------------some.value:${savedStateHandle.get<Long>("detail_some")}")

    }

    private val _recipeDetail = MutableLiveData<Response<FoodInformation>?>()
    val recipeDetail: LiveData<Response<FoodInformation>?> = _recipeDetail


    private fun getFoodInformation(id: Long) {
        viewModelScope.launch {
            _recipeDetail.postValue(repository.remote.getFoodInformation(id = id))
        }
    }

    suspend fun foodInformationEntity(id: Long): FoodInformationEntity {
        deferredOne = viewModelScope.async {
            repository.getFoodInfo(id).asLiveData()
        }
        return deferred.await()
    }

    suspend fun getFoodInformationConvertToFoodInformationEntity(id: Long): FoodInformationEntity {
        deferred = viewModelScope.async {
            foodInformationEntity(id)
        }
        return deferred.await()
    }

    fun onViewCreated(id: Long) {
        getFoodInformation(id = id)
    }

    fun insert(foodInformationEntity: FoodInformationEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertFoodInfo(foodInformationEntity)
        }

    fun insertFavoriteRecipe(favoriteRecipeEntity: FavoriteRecipeEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertFavoriteRecipe(favoriteRecipeEntity)
        }

    fun deleteFavoriteRecipe(favoriteRecipeEntity: FavoriteRecipeEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteFavoriteRecipe(favoriteRecipeEntity)
        }
}
