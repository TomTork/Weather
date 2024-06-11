package ru.anotherworld.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.anotherworld.weather.helper.Helper
import ru.anotherworld.weather.server.Cities
import ru.anotherworld.weather.server.MyClient

@Composable
fun WeatherApp(args: Cities) {
    val client = MyClient()
    val helper = Helper()
    val robotoFamily = FontFamily(
        Font(R.font.roboto_regular, FontWeight.W400),
        Font(R.font.roboto_medium, FontWeight.W500)
    )
    var temperature by remember { mutableIntStateOf(0) }
    var updateData by remember { mutableStateOf(true) }

    //0 - start loading, 1 - end loading, 2 - error in fetch data
    val loading = remember { mutableStateOf<UByte>(0U) }
    LaunchedEffect(key1 = loading.value) { //Получаем данные с сервера
        if (loading.value == 0.toUByte()){
            val weather = client.getWeather(args.latitude, args.longitude)
            if(weather != null) {
                temperature = Math.round(weather.temp)
                loading.value = 1.toUByte()
            }
            else loading.value = 2.toUByte()
            updateData = false
        }
    }
    Box(modifier = Modifier
        .fillMaxSize(1f)
        .padding(start = 16.dp, end = 16.dp)
        .background(color = colorResource(id = R.color.primary_background))) {
        when(loading.value){
            0.toUByte() -> { //Анимация (окно) загрузки
                helper.Loading(modifier = Modifier.align(Alignment.Center))
            }
            2.toUByte() -> { //Окно ошибки
                helper.ErrorInFetchData(modifier = Modifier.align(Alignment.Center),
                    robotoFamily = robotoFamily, loading = loading)
            }
            else -> {
                Column(modifier = Modifier
                    .fillMaxWidth(1f)
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    ) {
                    Text(text = "$temperature°C",
                        fontFamily = robotoFamily,
                        fontSize = 57.sp,
                        lineHeight = 64.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.W400,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text(text = args.city,
                        fontFamily = robotoFamily,
                        fontSize = 32.sp,
                        lineHeight = 40.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.W400,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                ElevatedButton(onClick = { loading.value = 0.toUByte() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = colorResource(
                        id = R.color.color_button
                    ))
                ) {
                    Text(text = stringResource(id = R.string.update),
                        fontSize = 14.sp, fontFamily = robotoFamily, fontWeight = FontWeight.W500,
                        lineHeight = 20.sp,
                        maxLines = 1, color = colorResource(id = R.color.white)
                    )
                }
            }
        }
    }
}