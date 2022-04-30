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

package com.ankitsuda.rebound.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ankitsuda.rebound.ui.keyboard.models.NumKey
import com.ankitsuda.rebound.ui.theme.ReboundTheme
import timber.log.Timber

@Composable
fun ReboundSetKeyboardComponent(
    onClickNumKey: (NumKey) -> Unit
) {
    Box(modifier = Modifier.background(ReboundTheme.colors.background)) {
        NumKeysContainerComponent(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    height = 250.dp,
                ),
            onClickNumKey = {
                Timber.d(it.toString())
                onClickNumKey(it)
            }
        )

    }
}