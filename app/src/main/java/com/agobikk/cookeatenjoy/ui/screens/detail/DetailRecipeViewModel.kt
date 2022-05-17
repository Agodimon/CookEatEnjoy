package com.agobikk.cookeatenjoy.ui.screens.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.navArgument
import com.agobikk.cookeatenjoy.data.remote.RemoteRepository
import com.agobikk.cookeatenjoy.model.ExtendedIngredient
import com.agobikk.cookeatenjoy.model.FoodInformation
import kotlinx.coroutines.launch
import retrofit2.Response

class DetailRecipeViewModel : ViewModel() {
    private val repository = RemoteRepository()

    private val _recipeDetail = MutableLiveData<Response<FoodInformation>?>()
    val recipeDetail: LiveData<Response<FoodInformation>?> = _recipeDetail

    private fun getFoodInformation(id:Long) {
        viewModelScope.launch {
            _recipeDetail.postValue(repository.getFoodInformation(id = id))
        }
    }

    fun onViewCreated(id:Long) {
        getFoodInformation(id = id)

    }

}