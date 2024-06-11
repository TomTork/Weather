package ru.anotherworld.weather.helper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.anotherworld.weather.R
import ru.anotherworld.weather.server.Cities

/*
Класс-помощник, содержит функцию сортировки, которая также убирает пустые города, хранящееся в json-файле,
а также содержит две вспомогательные composable-функции
 */
class Helper {
    fun sortByName(mas: List<Cities>):
            List<Cities> = mas.sortedBy { it.city }
                .filter { it.city != "" }

    //В случае поиска -> не оптимизируем общий список
    fun setLimitIndex(cities: List<Cities>, search: String, limitIndex: Int): List<Cities>{
        if (search != "") return cities.filter{ it.city.contains(search) }
        return cities.slice(0..limitIndex)
    }

    @Composable
    fun Loading(modifier: Modifier){ //Интерфейс стандартной загрузки
        val strokeWidth = 5.dp
        val color = colorResource(id = R.color.color_button)
        CircularProgressIndicator(
            modifier = modifier
                .size(48.dp)
                .drawBehind {
                    drawCircle(
                        color = color,
                        radius = size.width / 2 - strokeWidth.toPx() / 2,
                        style = Stroke(strokeWidth.toPx())
                    )
                },
            color = Color.LightGray,
            strokeWidth = strokeWidth
        )
    }

    @Composable
    fun ErrorInFetchData(modifier: Modifier, robotoFamily: FontFamily, loading: MutableState<UByte>){ //Интерфейс получения ошибки
        Column(modifier = modifier) {
            Text(text = stringResource(id = R.string.something_wrong),
                fontFamily = robotoFamily, fontWeight = FontWeight.W500, maxLines = 2,
                lineHeight = 20.sp, fontSize = 14.sp, modifier = Modifier.padding(bottom = 42.dp)
            )
            ElevatedButton(onClick = { loading.value = 0.toUByte() },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = colorResource(
                    id = R.color.color_button
                ))
            ) {
                Text(text = stringResource(id = R.string.update),
                    fontSize = 14.sp, fontFamily = robotoFamily, fontWeight = FontWeight.W500,
                    maxLines = 1, color = colorResource(id = R.color.white))
            }
        }
    }
}