/*
 * Copyright (c) 2022 Ankit Suda.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.ankitsuda.rebound.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import com.ankitsuda.base.ui.ThemeState
import com.ankitsuda.base.util.LabelVisible
import com.ankitsuda.base.util.NONE_WORKOUT_ID
import com.ankitsuda.common.compose.LocalDialog
import com.ankitsuda.common.compose.MainDialog
import com.ankitsuda.common.compose.rememberFlowWithLifecycle
import com.ankitsuda.navigation.NavigatorHost
import com.ankitsuda.navigation.RootScreen
import com.ankitsuda.rebound.ui.components.panel_tops.PanelTopCollapsed
import com.ankitsuda.rebound.ui.components.panel_tops.PanelTopDragHandle
import com.ankitsuda.rebound.ui.components.panel_tops.PanelTopExpanded
import com.ankitsuda.rebound.ui.ThemeViewModel
import com.ankitsuda.rebound.ui.navigation.AppNavigation
import com.ankitsuda.rebound.ui.theme.LocalThemeState
import com.ankitsuda.rebound.ui.theme.ReboundTheme
import com.ankitsuda.rebound.ui.theme.ReboundThemeWrapper
import com.ankitsuda.rebound.ui.workout_panel.WorkoutPanel
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Root screen of the app
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun MainScreen(
) {
    val navController = rememberNavController()
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator
    MainLayout(
        navController = navController,
        bottomSheetNavigator = bottomSheetNavigator,
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
private fun MainLayout(
    navController: NavHostController,
    bottomSheetNavigator: BottomSheetNavigator,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    viewModel: MainScreenViewModel = hiltViewModel(),
) {
    val themeState by rememberFlowWithLifecycle(themeViewModel.themeState).collectAsState(ThemeState())

    val swipeableState = rememberSwipeableState(0)
    val coroutine = rememberCoroutineScope()


    val currentWorkoutId by viewModel.currentWorkoutId.collectAsState(initial = NONE_WORKOUT_ID)

    val panelHidden = currentWorkoutId == NONE_WORKOUT_ID

    BackHandler(swipeableState.currentValue != 0) {
        coroutine.launch {
            swipeableState.animateTo(0)
        }
    }

    var dialogContent: @Composable () -> Unit by remember {
        mutableStateOf({})
    }

    var dialogVisible by remember {
        mutableStateOf(false)
    }

    // Dialog
    val dialog = MainDialog()
    dialog.showDialog = {
        dialogContent = dialog.dialogContent
        dialogVisible = true
        Timber.d("show dialog")
    }
    dialog.hideDialog = {
        dialogVisible = false
    }

    ReboundThemeWrapper(themeState = themeState) {
        NavigatorHost {
            CompositionLocalProvider(
                LocalDialog provides dialog,
            ) {
                Box() {
                    /**
                     * Temporary using ModalBottomSheetLayout
                     * will create a custom implementation later in MainScreenScaffold with proper status bar padding
                     * and auto corner radius
                     */
                    com.google.accompanist.navigation.material.ModalBottomSheetLayout(
                        sheetElevation = 0.dp,
                        sheetBackgroundColor = Color.Transparent,
                        bottomSheetNavigator = bottomSheetNavigator
                    ) {
                        MainScreenScaffold(
                            modifier = Modifier,
                            panelHidden = panelHidden,
                            swipeableState = swipeableState,
                            bottomBar = {
                                BottomBar(
                                    elevationEnabled = panelHidden,
                                    navController = navController,
                                )
                            },
                            panel = {
                                WorkoutPanel(navController)
                            },
                            panelTopCommon = {
                                PanelTopDragHandle()
                            },
                            panelTopCollapsed = {
                                val currentTimeStr by viewModel.currentTimeStr.collectAsState()
                                PanelTopCollapsed(
                                    currentTimeStr = currentTimeStr,
                                    onTimePause = { viewModel.pauseTime() },
                                    onTimePlay = { viewModel.playTime() },
                                    onTimeReset = { viewModel.resetTime() }
                                )
                            },
                            panelTopExpanded = {
                                PanelTopExpanded(
                                    onCollapseBtnClicked = {
                                        coroutine.launch {
                                            swipeableState.animateTo(0)
                                        }
                                    },
                                    onTimerBtnClicked = { },
                                    onFinishBtnClicked = {})
                            }) {
                            Box(Modifier.fillMaxSize()) {
                                AppNavigation(navController)
                            }
                        }
                    }

                    if (dialogVisible) {
                        AlertDialog(onDismissRequest = {
                            dialogVisible = false
                        },
                            buttons = {
                                dialogContent()
                            })
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    elevationEnabled: Boolean = false,
    navController: NavHostController,
) {
    val bottomNavigationItems = listOf(
        BottomNavigationScreens.Home,
        BottomNavigationScreens.History,
        BottomNavigationScreens.Workout,
        BottomNavigationScreens.Exercises,
        BottomNavigationScreens.More
    )

    val theme = LocalThemeState.current

    val labelVisible = theme.bottomBarLabelVisible
    val labelWeight = theme.bottomBarLabelWeight
    val iconSize = theme.bottomBarIconSize




    BottomNavigation(
        contentColor = MaterialTheme.colors.primary,
        backgroundColor = MaterialTheme.colors.surface,
        elevation = if (elevationEnabled) 8.dp else 0.dp,
        modifier = Modifier
            .navigationBarsHeight(additional = 56.dp)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavigationItems.forEach { screen ->

            BottomNavigationItem(
                icon = { Icon(screen.icon, screen.title, Modifier.size(iconSize.dp)) },
                label = if (labelVisible == LabelVisible.NEVER) {
                    null
                } else {
                    {

                        Text(
                            screen.title, fontWeight = when (labelWeight) {
                                "bold" -> FontWeight.Bold
                                else -> FontWeight.Normal
                            }
                        )

                    }
                },
                selectedContentColor = ReboundTheme.colors.primary,
                unselectedContentColor = ReboundTheme.colors.onBackground.copy(0.4f),
                alwaysShowLabel = labelVisible == LabelVisible.ALWAYS,
                modifier = Modifier.navigationBarsPadding(),
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    // This if check gives us a "singleTop" behavior where we do not create a
                    // second instance of the composable if we are already on that destination
//                               if (currentRoute != screen.route) {
//                                   navController.navigate(screen.route)
//                               }
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }

                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }

                },
            )
        }
    }
}

sealed class BottomNavigationScreens(val route: String, val title: String, val icon: ImageVector) {
    object Home :
        BottomNavigationScreens(RootScreen.HomeTab.route, "Home", Icons.Outlined.Home)

    object History :
        BottomNavigationScreens(RootScreen.HistoryTab.route, "History", Icons.Outlined.AccessTime)

    object Workout :
        BottomNavigationScreens(RootScreen.WorkoutTab.route, "Workout", Icons.Outlined.PlayArrow)

    object Exercises :
        BottomNavigationScreens(
            RootScreen.ExercisesTab.route,
            "Exercises",
            Icons.Outlined.FitnessCenter
        )

    object More :
        BottomNavigationScreens(RootScreen.MoreTab.route, "More", Icons.Outlined.Menu)
}
