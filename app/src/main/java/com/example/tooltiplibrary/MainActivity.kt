package com.example.tooltiplibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.tooltiplibrary.ui.theme.TooltipLibraryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val targets = remember { mutableStateMapOf<String, LayoutCoordinates>() }

            var activeKey by remember { mutableStateOf<String?>("home") }
            var showOverlay by remember { mutableStateOf(true) }

            var overlayCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

            val tooltipTexts = mapOf(
                "home" to ("Home" to "ده الشاشة الرئيسية"),
                "search" to ("Search" to "ابحث عن أي حاجة من هنا"),
                "profile" to ("Profile" to "بياناتك الشخصية"),
                "Top App" to ("Top App" to "ده التوب اب بار")
            )
            TooltipLibraryTheme {
                Box(modifier = Modifier.fillMaxSize()) {

                    AppRoot(
                        onRegister = { key, coords -> targets[key] = coords },
                        onSelect = { key ->
                            activeKey = key
                            showOverlay = true
                        }
                    )

                    if (showOverlay) {
                        val boundsOf: () -> Rect? = {
                            val target = activeKey?.let { targets[it] }
                            val overlay = overlayCoords
                            if (target != null && overlay != null &&
                                target.isAttached && overlay.isAttached
                            ) overlay.localBoundingBoxOf(target) else null
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { overlayCoords = it }
                                .graphicsLayer {
                                    compositingStrategy = CompositingStrategy.Offscreen
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures { showOverlay = false }
                                }
                        ) {
                            // القراءة هنا في مرحلة الـ Drawing
                            val target = activeKey?.let { targets[it] }
                            val overlay = overlayCoords

                            drawRect(Color.Black.copy(alpha = 0.65f))

                            if (target != null && overlay != null &&
                                target.isAttached && overlay.isAttached
                            ) {


                                val bounds = overlay.localBoundingBoxOf(target)
                                drawRoundRect(
                                    color = Color.Transparent,
                                    topLeft = bounds.topLeft,
                                    size = bounds.size,
                                    cornerRadius = CornerRadius(16.dp.toPx()),
                                    blendMode = BlendMode.Clear
                                )


                            }
                        }

                        val (title, description) = tooltipTexts[activeKey] ?: ("" to "")
                        SpotlightTooltip(
                            targetBounds = boundsOf,
                            title = title,
                            description = description,
                        )
                    }
                }
            }
        }
    }
}

private data class TabInfo(val key: String, val icon: ImageVector, val label: String)

private val tabs = listOf(
    TabInfo("home", Icons.Default.Home, "Home"),
    TabInfo("search", Icons.Default.Search, "Search"),
    TabInfo("profile", Icons.Default.Person, "Profile"),
)

@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    onRegister: (key: String, coords: LayoutCoordinates) -> Unit,
    onSelect: (key: String) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Spotlight Lab") }, modifier = Modifier.onGloballyPositioned {
                onRegister("Top App", it)
            }.clickable {
                onSelect("Top App")
            })
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            onSelect(tab.key)
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.onGloballyPositioned {
                                    onRegister(tab.key, it)
                                }
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Current Tab : $selectedTab")
        }
    }
}