package de.aploi.spettrobyeyed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import de.aploi.spettrobyeyed.navigation.NavGraph
import de.aploi.spettrobyeyed.ui.theme.SpettroTheme
import de.aploi.spettrobyeyed.ui.viewmodel.SpettroViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpettroTheme {
                val viewModel: SpettroViewModel = viewModel()
                NavGraph(viewModel = viewModel)
            }
        }
    }
}