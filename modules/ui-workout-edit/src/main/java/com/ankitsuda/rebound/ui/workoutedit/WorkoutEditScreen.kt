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

package com.ankitsuda.rebound.ui.workoutedit

import android.text.TextUtils
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ankitsuda.navigation.LeafScreen
import com.ankitsuda.navigation.LocalNavigator
import com.ankitsuda.navigation.Navigator
import com.ankitsuda.rebound.ui.components.TopBar2
import com.ankitsuda.rebound.ui.components.TopBarBackIconButton
import com.ankitsuda.rebound.ui.components.TopBarIconButton
import com.ankitsuda.rebound.ui.components.workouteditor.WorkoutEditorComponent
import com.ankitsuda.rebound.ui.keyboard.LocalReboundSetKeyboard
import com.ankitsuda.rebound.ui.keyboard.ReboundSetKeyboard
import com.ankitsuda.rebound.ui.keyboard.ReboundSetKeyboardComponent
import com.ankitsuda.rebound.ui.keyboard.models.ClearNumKey
import com.ankitsuda.rebound.ui.keyboard.models.DecimalNumKey
import com.ankitsuda.rebound.ui.keyboard.models.NumKey
import com.ankitsuda.rebound.ui.keyboard.models.NumberNumKey
import com.ankitsuda.rebound.ui.theme.ReboundTheme
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
fun WorkoutEditScreen(
    navController: NavController,
    navigator: Navigator = LocalNavigator.current,
    viewModel: WorkoutEditScreenViewModel = hiltViewModel()
) {
    val workout by viewModel.workout.collectAsState(null)
    val logEntriesWithJunction by viewModel.logEntriesWithExerciseJunction.collectAsState()

    var keyboardVisible by remember {
        mutableStateOf(false)
    }
    var inputConnection: InputConnection? by remember {
        mutableStateOf(null)
    }

    val workoutName = workout?.name
    val workoutNote = workout?.note

    val collapsingState = rememberCollapsingToolbarScaffoldState()

    fun onClickNumKey(numKey: NumKey) {
        if (inputConnection == null) return

        when (numKey) {
            is NumberNumKey, DecimalNumKey -> inputConnection?.commitText(
                numKey.toString(),
                1
            )
            is ClearNumKey -> {
                val selectedText = inputConnection?.getSelectedText(0)

                if (TextUtils.isEmpty(selectedText)) {
                    inputConnection?.deleteSurroundingText(1, 0)
                } else {
                    inputConnection?.commitText("", 1)
                }
            }
        }

    }

    val keyboard = ReboundSetKeyboard(
        onChangeVisibility = {
            keyboardVisible = it
        },
        onChangeInputConnection = {
            inputConnection = it
        }
    )
    CompositionLocalProvider(LocalReboundSetKeyboard provides keyboard) {

        Column() {
            CollapsingToolbarScaffold(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ReboundTheme.colors.background),
                state = collapsingState,
                scrollStrategy = ScrollStrategy.EnterAlwaysCollapsed,
                toolbar = {
                    TopBar2(
                        title = "Edit workout",
                        toolbarState = collapsingState.toolbarState,
                        navigationIcon = {
                            TopBarBackIconButton {
                                navigator.goBack()
                            }
                        },
                        actions = {
                            TopBarIconButton(icon = Icons.Outlined.MoreVert, title = "Open Menu") {

                            }
                        })

                })
            {
                WorkoutEditorComponent(
                    navController = navController,
                    navigator = navigator,
                    workoutName = workoutName,
                    workoutNote = workoutNote,
                    useReboundKeyboard = true,
                    cancelWorkoutButtonVisible = false,
                    logEntriesWithJunction = logEntriesWithJunction,
                    onChangeWorkoutName = {
                        viewModel.updateWorkoutName(it)
                    },
                    onChangeWorkoutNote = {
                        viewModel.updateWorkoutNote(it)
                    },
                    onAddExerciseToWorkout = {
                        viewModel.addExerciseToWorkout(it)
                    },
                    onDeleteExerciseFromWorkout = {
                        viewModel.deleteExerciseFromWorkout(it)
                    },
                    onAddEmptySetToExercise = { setNumber, exerciseWorkoutJunction ->
                        viewModel.addEmptySetToExercise(
                            setNumber = setNumber,
                            exerciseWorkoutJunction = exerciseWorkoutJunction
                        )
                    },
                    onDeleteLogEntry = {
                        viewModel.deleteLogEntry(it)
                    },
                    onUpdateLogEntry = {
                        viewModel.updateLogEntry(it)
                    },
                    onCancelCurrentWorkout = {}
                )
            }



            if (keyboardVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ReboundSetKeyboardComponent(
                        onClickNumKey = {
                            onClickNumKey(it)
                        })
                }
            }
        }
    }
}