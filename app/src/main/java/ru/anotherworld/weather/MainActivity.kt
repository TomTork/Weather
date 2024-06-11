package ru.anotherworld.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ru.anotherworld.weather.helper.Helper
import ru.anotherworld.weather.server.Cities
import ru.anotherworld.weather.server.MyClient
import ru.anotherworld.weather.ui.theme.WeatherTheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.anotherworld.weather.model.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherTheme {
                val viewModel = viewModel<MainViewModel>()
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home"){
                    composable("home"){
                        Content(navController, viewModel)
                    }
                    composable("city/{json}"){ backStackEntry ->
                        val args = backStackEntry.arguments?.getString("json")
                            ?.let { Json.decodeFromString<Cities>(it) }
                        WeatherApp(args = args!!)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Content(navController: NavController, viewModel: MainViewModel){
    val robotoFamily = FontFamily(
        Font(R.font.roboto_regular, FontWeight.W400),
        Font(R.font.roboto_medium, FontWeight.W500)
    )
    val client = MyClient()
    val helper = Helper()
    val isKeyboardOpen by viewModel.keyboardAsState()
    val focusManager = LocalFocusManager.current

    //0 - start loading, 1 - end loading, 2 - error in fetch data
    var limitIndex by remember { mutableIntStateOf(20) } //Идекс, после прохождения которого, список будет расширяться
    val loading = remember { mutableStateOf<UByte>(0U) }

    val state = rememberLazyListState()
    LaunchedEffect(key1 = loading.value, key2 = viewModel.flag) {
        if (viewModel.flag || loading.value == 0.toUByte()){
            val result = client.getCities()
            if(result != null){
                viewModel.addAllCities(helper.sortByName(result))
                loading.value = 1.toUByte()
                viewModel.flag = false
            }
            else loading.value = 2.toUByte()
        }
    }
    Box(modifier = Modifier
        .fillMaxSize(1f)
        .background(color = colorResource(id = R.color.primary_background))) {
        when (loading.value) {
            0.toUByte() -> {
                helper.Loading(modifier = Modifier.align(Alignment.Center))
                loading.value = 0.toUByte()
            }
            2.toUByte() -> {
                helper.ErrorInFetchData(modifier = Modifier.align(Alignment.Center),
                    robotoFamily = robotoFamily, loading = loading)
            }
            1.toUByte() -> {
                if (!isKeyboardOpen){
                    focusManager.clearFocus()
                }
                lateinit var valueInStickyHeader: String
                LazyColumn(modifier = Modifier
                    .fillMaxSize(1f)
                    .padding(start = 16.dp, end = 16.dp)
                    .background(color = Color.Transparent),
                    state = state) {
                    item {
                        Row(modifier = Modifier
                            .fillMaxWidth(1f)
                            .padding(top = 40.dp)) {
                            OutlinedTextField(
                                value = viewModel.search, onValueChange = { viewModel.search = it },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .weight(1f),
                                textStyle = TextStyle(
                                    fontFamily = robotoFamily, fontWeight = FontWeight.W400,
                                    fontSize = 16.sp),
                                colors = TextFieldDefaults.colors(
                                    cursorColor = colorResource(id = R.color.color_button),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedTextColor = Color.Black,
                                    focusedTextColor = Color.Black,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Black
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    autoCorrect = true,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(onSearch = {
                                    focusManager.clearFocus()
                                }),
                                placeholder = {
                                    Text(text = stringResource(R.string.enter_city),
                                        fontFamily = robotoFamily, fontWeight = FontWeight.W400,
                                        fontSize = 16.sp)
                                },
                                maxLines = 1)
                        }
                    }
                    stickyHeader(content = {
                        valueInStickyHeader = if(viewModel.search != "")
                                (viewModel.getFirstCityInSearch(viewModel.search)?.city?.get(0) ?: "").toString()
                        else viewModel.getCityByIndex(state.firstVisibleItemIndex).city[0].toString()
                        Text(text = valueInStickyHeader,
                            fontFamily = robotoFamily, fontWeight = FontWeight.W500,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                                .offset(y = (64).dp)
                                .height(40.dp)
                                .background(color = Color.Transparent))
                    })
                    itemsIndexed(items = helper.
                        setLimitIndex(viewModel.cities.value, viewModel.search, limitIndex))
                    { index, city ->
                        //Обработка расширения основного списка
                        if (viewModel.search == ""){
                            if(index + 2 >= limitIndex && limitIndex + 20 <= viewModel.getSizeCities()) {
                                limitIndex += 20
                            }
                            else if(limitIndex + 20 <= viewModel.getSizeCities()) limitIndex = viewModel.getSizeCities() - 1
                        }
                        else limitIndex = 20
                        Button(
                            onClick = {
                                //Переход ко второму окну
                                navController.navigate("city/${Json.encodeToString(city)}")
                                loading.value = 0.toUByte(); viewModel.flag = true
                                      },
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                                .fillMaxWidth(1f)
                                .height(40.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(1f)) {
                                Text(
                                    text = city.city,
                                    fontFamily = robotoFamily, fontWeight = FontWeight.W400,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .background(color = Color.Transparent)
                                        .padding(top = 4.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}