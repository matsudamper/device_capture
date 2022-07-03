package net.matsudamper.device_capture.lib

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTab(
    modifier: Modifier,
    items: List<String>,
    selectedIndex: Int,
    textColor: Color = LocalContentColor.current,
    contentColor: Color = LocalContentColor.current,
    onSelectedIndex: (Int) -> Unit,
) {
    LazyRow(modifier = modifier) {
        itemsIndexed(items) { index, item ->
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .clip(RectangleShape)
                    .clickable { onSelectedIndex(index) },
            ) {
                Text(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    text = item,
                    color = textColor,
                    fontSize = 20.sp
                )
                Spacer(
                    modifier = Modifier.height(2.dp)
                        .fillMaxWidth()
                        .background(
                            if (index == selectedIndex) {
                                contentColor
                            } else {
                                Color.Transparent
                            }
                        )
                )
            }
        }
    }
}