package com.aegis.pdf

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aegis.pdf.ui.home.HomeScreen
import com.aegis.pdf.ui.merge.MergeScreen
import com.aegis.pdf.ui.split.SplitScreen
import com.aegis.pdf.ui.compress.CompressScreen
import com.aegis.pdf.ui.security.SecurityScreen
import com.aegis.pdf.ui.watermark.WatermarkScreen
import com.aegis.pdf.ui.scanner.ScannerScreen
import com.aegis.pdf.ui.ocr.OcrScreen
import com.aegis.pdf.ui.viewer.PdfViewerScreen
import com.aegis.pdf.ui.editor.EditorScreen
import com.aegis.pdf.ui.settings.SettingsScreen

@Composable
fun AegisNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Constants.Routes.HOME
    ) {
        composable(Constants.Routes.HOME) {
            HomeScreen(
                onToolClick = { tool ->
                    navController.navigate(tool.route)
                }
            )
        }
        composable(Constants.Routes.MERGE) {
            MergeScreen(onBack = { navController.popBackStack() })
        }
        composable(Constants.Routes.SPLIT) {
            SplitScreen(onBack = { navController.popBackStack() })
        }
        composable(Constants.Routes.COMPRESS) {
            CompressScreen(onBack = { navController.popBackStack() })
        }
        composable(Constants.Routes.SECURITY) {
            SecurityScreen(onBack = { navController.popBackStack() })
        }
        composable(Constants.Routes.WATERMARK) {
            WatermarkScreen(onBack = { navController.popBackStack() })
        }
        composable(Constants.Routes.SCANNER) {
            ScannerScreen(onBack = { navController.popBackStack() })
        }
        composable(Constants.Routes.OCR) {
            OcrScreen(onBack = { navController.popBackStack() })
        }
        composable(Constants.Routes.VIEWER) {
            PdfViewerScreen(onBack = { navController.popBackStack() })
        }
        composable(Constants.Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}