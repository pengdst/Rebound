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

package com.ankitsuda.rebound.ui.workout_panel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ankitsuda.base.util.NONE_WORKOUT_ID
import com.ankitsuda.base.util.toast
import com.ankitsuda.common.compose.R
import com.ankitsuda.navigation.LocalNavigator
import com.ankitsuda.navigation.Navigator
import com.ankitsuda.rebound.ui.components.dialogs.DiscardActiveWorkoutDialog
import com.ankitsuda.rebound.ui.components.dialogs.FinishActiveWorkoutDialog
import com.ankitsuda.rebound.ui.components.workouteditor.WorkoutEditorComponent
import com.ankitsuda.rebound.ui.workout_panel.components.WorkoutQuickInfo

@Composable
fun WorkoutPanel(
    navController: NavHostController,
    navigator: Navigator = LocalNavigator.current,
) {
    WorkoutPanel1(navController, navigator)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutPanel1(
    navController: NavHostController,
    navigator: Navigator,
    viewModel: WorkoutPanelViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val currentWorkoutId by viewModel.currentWorkoutId.collectAsState(initial = NONE_WORKOUT_ID)
    val workout by viewModel.workout.collectAsState(null)
    val currentDurationStr by viewModel.currentDurationStr.collectAsState("")
    val currentVolumeStr by viewModel.currentVolumeStr.collectAsState("")
    val currentSetsStr by viewModel.currentSetsStr.collectAsState("")
    val logEntriesWithJunction by viewModel.logEntriesWithExerciseJunction.collectAsState()
    val barbells by viewModel.barbells.collectAsState(emptyList())
    var isCancelCurrentWorkoutDialogOpen by remember {
        mutableStateOf(false)
    }
    val isFinishCurrentWorkoutDialogOpen by viewModel.finishWorkoutDialogState.collectAsState()

    val workoutName = workout?.name
    val workoutNote = workout?.note

    if (currentWorkoutId != NONE_WORKOUT_ID && workout != null) {
        WorkoutEditorComponent(
            navController = navController,
            navigator = navigator,
            workoutName = workoutName,
            workoutNote = workoutNote,
            useReboundKeyboard = true,
            addNavigationBarPadding = true,
            cancelWorkoutButtonVisible = true,
            logEntriesWithJunction = logEntriesWithJunction,
            barbells = barbells,
            layoutAtTop = {
                Column(
                    modifier = Modifier.animateItemPlacement()
                ) {
                    WorkoutQuickInfo(
                        currentDurationStr = currentDurationStr,
                        currentVolumeStr = currentVolumeStr,
                        currentSetsStr = currentSetsStr
                    )
                    Divider()
                }
            },
            onChangeWorkoutName = {
                viewModel.updateWorkoutName(it)
            },
            onChangeWorkoutNote = {
                viewModel.updateWorkoutNote(it)
            },
            onAddExerciseToWorkout = {
                viewModel.addExerciseToWorkout(it)
            },
            onCancelCurrentWorkout = {
                isCancelCurrentWorkoutDialogOpen = true
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
            onUpdateWarmUpSets = { j, s ->
                viewModel.updateWarmUpSets(j, s)
            },
            onAddEmptyNote = viewModel::addEmptyNote,
            onChangeNote = viewModel::changeNote,
            onDeleteNote = viewModel::deleteNote,
            onAddToSuperset = viewModel::addToSuperset,
            onRemoveFromSuperset = viewModel::removeFromSuperset,
            onUpdateBarbell = viewModel::updateExerciseBarbellType
        )

        if (isCancelCurrentWorkoutDialogOpen && workout?.startAt != null) {
            DiscardActiveWorkoutDialog(
                onDismissRequest = {
                    isCancelCurrentWorkoutDialogOpen = false
                },
                onClickDiscard = {
                    viewModel.cancelCurrentWorkout()
                    isCancelCurrentWorkoutDialogOpen = false
                },
            )
        }

        if (isFinishCurrentWorkoutDialogOpen && workout?.startAt != null) {
            FinishActiveWorkoutDialog(
                onDismissRequest = {
                    viewModel.closeFinishWorkoutDialog()
                },
                onClickFinish = {
                    viewModel.finishWorkout {
                        context.toast(message = context.getString(R.string.incomplete_sets_error))
                    }
                    viewModel.closeFinishWorkoutDialog()
                },
            )
        }
    }
}
