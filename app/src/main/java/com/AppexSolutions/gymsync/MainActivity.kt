package com.AppexSolutions.gymsync

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import com.AppexSolutions.gymsync.core.navigation.AppNavigation
import com.AppexSolutions.gymsync.ui.theme.GymSyncTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            GymSyncTheme {
                AppNavigation()
            }
        }
    }
}
