package com.ankitsuda.rebound.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ankitsuda.navigation.LeafScreen
import com.ankitsuda.navigation.LocalNavigator
import com.ankitsuda.navigation.Navigator
import com.ankitsuda.rebound.ui.components.MoreItemCard
import com.ankitsuda.rebound.ui.components.MoreSectionHeader
import com.ankitsuda.rebound.ui.components.TopBar
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
fun MoreScreen(
      navController: NavController,navigator: Navigator = LocalNavigator.current) {
    val collapsingState = rememberCollapsingToolbarScaffoldState()

    CollapsingToolbarScaffold(
        state = collapsingState,
        scrollStrategy = ScrollStrategy.EnterAlwaysCollapsed,
        toolbar = {
            TopBar(title = "More", elevationEnabled = true)
        },
        modifier = Modifier.background(MaterialTheme.colors.background)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    icon = Icons.Outlined.Straighten,
                    text = "Measure",
                    description = "Body measurements",
                    onClick = {
//                        navController.navigate(Route.Measure.route)
                        navigator.navigate(LeafScreen.Measure().route)
                    })
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    icon = Icons.Outlined.SportsScore,
                    text = "Achievements",
                    description = "0 achievements",
                    onClick = {
                    })
            }
            item {
                MoreSectionHeader(text = "Settings")
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    icon = Icons.Outlined.BubbleChart,
                    text = "Personalization",
                    description = "Make rebound yours",
                    onClick = {
//                        navController.navigate(Route.Personalization.route)
                        navigator.navigate(LeafScreen.Personalization().route)
                    })
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    icon = Icons.Outlined.Settings,
                    text = "Settings",
                    description = "Units, backups etc.",
                    onClick = {
                        navigator.navigate(LeafScreen.Settings().route)
//                        navController.navigate(Route.Settings.route)
                    })
            }
        }

    }
}

