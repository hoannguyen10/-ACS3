package com.example.dacs3
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.dacs3.navigation.AppNavGraph
import com.example.dacs3.ui.theme.DACS3Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DACS3Theme {
                AppNavGraph()
            }
        }
    }
}