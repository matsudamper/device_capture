package net.matsudamper.device_capture.screen.settings

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.matsudamper.device_capture.lib.CustomTab


@Composable
fun SettingsScreen(
    modifier: Modifier,
    uiState: SettingsUiState,
) {
    Column(modifier = modifier) {
        Row(
            Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(42.dp)
                    .clip(CircleShape)
                    .clickable { uiState.onDismissRequest() },
                imageVector = Icons.Default.Close, contentDescription = "Close"
            )
        }
        val tabs = remember { SettingsUiState.SettingsScreenTabType.values() }
        CustomTab(
            modifier = Modifier.fillMaxWidth(),
            items = remember { tabs.map { it.title } },
            selectedIndex = tabs.indexOf(uiState.currentTabType),
            onSelectedIndex = { index ->
                uiState.onClickTab(tabs[index])
            },
        )
        Spacer(
            modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Gray)
        )

        when (val pageUiState = uiState.currentPage) {
            is SettingsUiState.SettingTab.AudioInput -> {
                if (pageUiState.isLoading) {
                    Loading(modifier = Modifier.fillMaxWidth().weight(1f))
                } else {
                    NamesTabContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        pageUiState.itemsUiState
                    )
                }
            }
            is SettingsUiState.SettingTab.AudioOutput -> {
                if (pageUiState.isLoading) {
                    Loading(modifier = Modifier.fillMaxWidth().weight(1f))
                } else {
                    NamesTabContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        pageUiState.itemsUiState
                    )
                }
            }
            is SettingsUiState.SettingTab.Display -> {
                if (pageUiState.isLoading) {
                    Loading(modifier = Modifier.fillMaxWidth().weight(1f))
                } else {
                    NamesTabContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        pageUiState.itemsUiState
                    )
                }
            }
        }
    }
}

@Composable
private fun NamesTabContent(
    modifier: Modifier,
    nameUiStateList: List<SettingsUiState.NameUiState>
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(nameUiStateList) { item ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        item.onClick()
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Name(
                    modifier = Modifier.weight(1f),
                    uiState = item
                )
                Checkbox(
                    modifier = Modifier,
                    checked = item.isSelected,
                    onCheckedChange = null,
                )
            }
            Divider(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Loading(
    modifier: Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
        )
    }
}

@Composable
private fun Name(
    modifier: Modifier,
    uiState: SettingsUiState.NameUiState
) {
    Text(
        modifier = modifier
            .padding(12.dp),
        text = uiState.name,
        fontSize = 24.sp
    )
}

@Preview
@Composable
private fun Preview() {
    var page by remember { mutableStateOf(SettingsUiState.SettingsScreenTabType.Display) }
    SettingsScreen(
        modifier = Modifier.fillMaxWidth(),
        uiState = SettingsUiState(
            currentTabType = page,
            currentPage = SettingsUiState.SettingTab.Display(
                (0..10).map {
                    SettingsUiState.NameUiState(
                        name = "$it",
                        isSelected = it % 2 == 0,
                        onClick = {

                        }
                    )
                },
                isLoading = false,
            ),
            onDismissRequest = {},
            onClickTab = {
                page = it
            },
        ),
    )
}
