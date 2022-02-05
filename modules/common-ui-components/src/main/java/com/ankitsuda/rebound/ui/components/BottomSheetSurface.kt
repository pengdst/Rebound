package com.ankitsuda.rebound.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
//import com.ankitsuda.rebound.ui.screens.main_screen.LocalBottomSheet
import com.ankitsuda.rebound.ui.theme.ReboundTheme
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi

/**
 * Wraps bottom sheet content in surface with better inset support
 */
@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@Composable
fun BottomSheetSurface(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        elevation = 16.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        content = content
    )
}