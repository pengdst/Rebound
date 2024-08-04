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

package com.ankitsuda.rebound.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.ThumbsUpDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ankitsuda.navigation.LeafScreen
import com.ankitsuda.navigation.LocalNavigator
import com.ankitsuda.navigation.Navigator
import com.ankitsuda.rebound.domain.DistanceUnit
import com.ankitsuda.rebound.domain.WeightUnit
import com.ankitsuda.rebound.ui.components.MoreItemCard
import com.ankitsuda.rebound.ui.components.MoreSectionHeader
import com.ankitsuda.rebound.ui.components.TopBar2
import com.ankitsuda.rebound.ui.components.TopBarBackIconButton
import com.ankitsuda.rebound.ui.components.settings.PopupItemsSettingsItem
import com.ankitsuda.rebound.ui.icons.Barbell
import com.ankitsuda.rebound.ui.icons.Plates
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun SettingsScreen(
    navController: NavController,
    navigator: Navigator = LocalNavigator.current,
    viewModel: SettingsScreenViewModel = hiltViewModel()
) {
    val collapsingState = rememberCollapsingToolbarScaffoldState()

    val weightUnit by viewModel.weightUnit.collectAsState(initial = WeightUnit.KG)
    val distanceUnit by viewModel.distanceUnit.collectAsState(initial = DistanceUnit.KM)
    val firstDayOfWeek by viewModel.firstDayOfWeek.collectAsState(initial = 1)
    val restTimerSound by viewModel.restTimerSound.collectAsState(initial = "")

    val context = LocalContext.current

    fun copyUriToInternalStorage(context: Context, uri: Uri): Uri? {
        try {
            var name = "selected_ringtone.mp3"
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = it.getString(nameIndex)
                    }
                }
            }
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, name)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { uri ->
        val urir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uri.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
        } else {
            uri.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        }


        urir?.let {
            try {
                context.grantUriPermission(
                    context.packageName,
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.setRestTimerSound(it.toString())
            } catch (e: SecurityException) {
                e.printStackTrace()
                // Fallback: Copy the file to internal storage
                val copiedUri = copyUriToInternalStorage(context, it)
                if (copiedUri != null) {
                    viewModel.setRestTimerSound(copiedUri.toString())
                }
            }
        }
    }

    fun launchAudioPicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Rest Timer Tone")
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )
        }
        audioPicker.launch(intent)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchAudioPicker()
        } else {
            // Inform the user about the necessity of the permission
            Toast.makeText(
                context,
                "Permission is required to select audio files.",
                Toast.LENGTH_LONG
            ).show()

            // Optionally, provide a way to open app settings for permission
            // Open app settings
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
    }


    CollapsingToolbarScaffold(
        scrollStrategy = ScrollStrategy.EnterAlwaysCollapsed,
        state = collapsingState,
        toolbar = {
            TopBar2(
                title = stringResource(R.string.settings),
                toolbarState = collapsingState.toolbarState,
                navigationIcon = {
                    TopBarBackIconButton {
                        navController.popBackStack()
                    }
                })
        },
        modifier = Modifier.background(MaterialTheme.colors.background)
    ) {
        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
        ) {
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.BubbleChart,
                    text = stringResource(R.string.personalization),
                    description = stringResource(R.string.make_rebound_yours),
                    onClick = {
                        navigator.navigate(LeafScreen.Personalization().createRoute())
                    })
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Filled.Plates,
                    text = stringResource(R.string.plates),
                    description = stringResource(R.string.customize_barbell_plates),
                    onClick = {
                        navigator.navigate(LeafScreen.Plates().createRoute())
                    })
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Filled.Barbell,
                    text = stringResource(R.string.barbells),
                    description = stringResource(R.string.customize_barbells),
                    onClick = {
                        navigator.navigate(LeafScreen.Barbells().createRoute())
                    })
            }
            item {
                MoreSectionHeader(text = stringResource(R.string.defaults))
            }
            item {
                val getStringByWeightUnit: @Composable ((WeightUnit) -> String) = {
                    when (it) {
                        WeightUnit.KG -> stringResource(R.string.metric_kg)
                        WeightUnit.LBS -> stringResource(R.string.imperial_lbs)
                    }
                }

                PopupItemsSettingsItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.FitnessCenter,
                    text = stringResource(R.string.weight_unit),
                    description = getStringByWeightUnit(weightUnit),
                    selectedItem = weightUnit,
                    items = WeightUnit.values().map {
                        Pair(
                            it, getStringByWeightUnit(it)
                        )
                    },
                    onItemSelected = {
                        viewModel.setWeightUnit(it)
                    }
                )
            }
            item {
                val getStringByDistanceUnit: @Composable ((DistanceUnit) -> String) = {
                    when (it) {
                        DistanceUnit.KM -> stringResource(R.string.metric_m_km)
                        DistanceUnit.MILES -> stringResource(R.string.imperial_ft_miles)
                    }
                }

                PopupItemsSettingsItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.DirectionsRun,
                    text = stringResource(R.string.distance_unit),
                    description = getStringByDistanceUnit(distanceUnit),
                    selectedItem = distanceUnit,
                    items = DistanceUnit.values().map {
                        Pair(
                            it, getStringByDistanceUnit(it)
                        )
                    },
                    onItemSelected = {
                        viewModel.setDistanceUnit(it)
                    }
                )
            }
            item {
                fun getDayName(day: Int) =
                    DayOfWeek.of(day).getDisplayName(TextStyle.FULL, Locale.getDefault())

                PopupItemsSettingsItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.Event,
                    text = stringResource(R.string.first_day_of_the_week),
                    description = getDayName(firstDayOfWeek),
                    selectedItem = firstDayOfWeek,
                    items = DayOfWeek.values().map { Pair(it.value, getDayName(it.value)) },
                    onItemSelected = {
                        viewModel.setFirstDayOfWeek(it)
                    }
                )
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.Folder,
                    text = stringResource(R.string.rest_timer_sound),
                    description = restTimerSound,
                    onClick = {
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {

                                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                    Manifest.permission.READ_MEDIA_AUDIO
                                else Manifest.permission.READ_EXTERNAL_STORAGE
                                when (ContextCompat.checkSelfPermission(
                                    context,
                                    permission
                                )) {
                                    PackageManager.PERMISSION_GRANTED -> {
                                        launchAudioPicker()
                                    }

                                    else -> {
                                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                                    }
                                }
                            }
                            else -> {
                                launchAudioPicker()
                            }
                        }
                    })
            }
            item {
                MoreSectionHeader(text = stringResource(R.string.your_data))
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.Folder,
                    text = stringResource(R.string.backup_data),
                    description = stringResource(R.string.to_json),
                    onClick = {

                    })
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.Restore,
                    text = stringResource(R.string.restore_data),
                    description = stringResource(R.string.from_a_previous_backup),
                    onClick = {

                    })
            }

            item {
                MoreSectionHeader(text = stringResource(R.string.feedback))
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.ThumbsUpDown,
                    text = stringResource(R.string.write_a_review),
                    description = stringResource(R.string.write_review_description),
                    onClick = {

                    })
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.BugReport,
                    text = stringResource(R.string.suggestions_and_bug_report),
                    description = stringResource(R.string.suggestions_and_bug_report_description),
                    onClick = {

                    })
            }
            item {
                MoreSectionHeader(text = stringResource(R.string.about))
            }
            item {
                MoreItemCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    icon = Icons.Outlined.Info,
                    text = stringResource(R.string.about_app),
                    onClick = {

                    })
            }
//            item {
//                Text(
//                    text = "v${BuildConfig.VERSION_NAME}",
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .fillMaxWidth(),
//                    style = MaterialTheme.typography.caption,
//                    textAlign = TextAlign.Center
//                )
//            }
        }

    }
}