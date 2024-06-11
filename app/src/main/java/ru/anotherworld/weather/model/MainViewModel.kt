package ru.anotherworld.weather.model

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.anotherworld.weather.server.Cities

class MainViewModel: ViewModel(){
    private val _cities = MutableStateFlow(ArrayList<Cities>())
    val cities: StateFlow<List<Cities>> = _cities.asStateFlow() //Основной список, в котором хранятся города
    fun addAllCities(data: List<Cities>){
        if(_cities.value.isEmpty()) _cities.value.addAll(data)
    }
    fun getCities(): List<Cities> = _cities.value
    fun getSizeCities(): Int = _cities.value.size
    fun getFirstCityInSearch(search: String): Cities? {
        return try {
            _cities.value.first { it.city.contains(search) }
        } catch (e: NoSuchElementException){
            null
        }
    }
    fun getCityByIndex(i: Int): Cities = _cities.value[i]

    var flag: Boolean by mutableStateOf(false)
    var search by mutableStateOf("") //Переменная для поиска

    @Composable
    fun keyboardAsState(): State<Boolean> { //Проверка, открыта ли клавиатура
        val view = LocalView.current
        var isImeVisible by remember { mutableStateOf(false) }

        DisposableEffect(LocalWindowInfo.current) {
            val listener = ViewTreeObserver.OnPreDrawListener {
                isImeVisible = ViewCompat.getRootWindowInsets(view)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                true
            }
            view.viewTreeObserver.addOnPreDrawListener(listener)
            onDispose {
                view.viewTreeObserver.removeOnPreDrawListener(listener)
            }
        }
        return rememberUpdatedState(isImeVisible)
    }
}