package com.sc2079.androidcontroller.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LegendToggle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel

import com.sc2079.androidcontroller.features.controller.domain.ControlState
import com.sc2079.androidcontroller.features.language.presentation.AppLanguage
import com.sc2079.androidcontroller.features.language.presentation.LocaleState
import com.sc2079.androidcontroller.features.map.presentation.MapViewModel
import com.sc2079.androidcontroller.features.tts.rememberTextToSpeechManager
import com.sc2079.androidcontroller.navigation.NavGraph
import com.sc2079.androidcontroller.navigation.Screen
import com.sc2079.androidcontroller.ui.theme.ThemeMode
import com.sc2079.androidcontroller.ui.theme.ThemeState
import kotlinx.coroutines.launch

/**
 * Navigation drawer item data class
 */
data class NavigationItem(
    val titleResId: Int,
    val icon: ImageVector,
    val screen: Screen,
    val descriptionResId: Int
)

/**
 * Main app scaffold with navigation drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavHostController,
    bluetoothViewModel: BluetoothViewModel,
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    // Define navigation items - only Home and Bluetooth
    val navigationItems = listOf(
        NavigationItem(
            titleResId = R.string.nav_home,
            icon = Icons.Default.Home,
            screen = Screen.Home,
            descriptionResId = R.string.nav_home
        ),
        NavigationItem(
            titleResId = R.string.nav_bluetooth,
            icon = Icons.Default.Bluetooth,
            screen = Screen.Bluetooth,
            descriptionResId = R.string.bluetooth_connect_message
        ),
        NavigationItem(
            titleResId = R.string.map,
            icon = Icons.Default.Map,
            screen = Screen.Map,
            descriptionResId = R.string.obstacle_robot_placement
        ),
    )
    
    // Get current screen title
    val currentTitle = when (currentRoute) {
        Screen.Home.route -> stringResource(R.string.device_connected)
        Screen.Bluetooth.route -> stringResource(R.string.bluetooth_title)
        else -> stringResource(R.string.app_name)
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 28.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.nav_navigation),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                    )
                    
                    navigationItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.descriptionResId)
                                )
                            },
                            label = { Text(stringResource(item.titleResId)) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                                navController.navigate(item.screen.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Theme Toggle Section
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 28.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = stringResource(R.string.nav_theme),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                    )
                    
                    // Theme toggle buttons
                    ThemeToggleButtons(
                        currentTheme = ThemeState.currentTheme,
                        onThemeChange = { ThemeState.currentTheme = it },
                        modifier = Modifier.padding(horizontal = 28.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Language Section
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 28.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = stringResource(R.string.nav_language),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                    )
                    
                    // Language dropdown
                    LanguageDropdown(
                        currentLanguage = LocaleState.currentLanguage,
                        onLanguageChange = { LocaleState.setLanguage(it) },
                        modifier = Modifier.padding(horizontal = 28.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Control Section
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 28.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = stringResource(R.string.nav_control),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                    )
                    
                    // Right-handed control toggle
                    RightHandedControlToggle(
                        isRightHanded = ControlState.isRightHanded,
                        onToggleChange = { ControlState.isRightHanded = it },
                        modifier = Modifier.padding(horizontal = 28.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleMedium
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showBottomSheet = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.LegendToggle,
                                contentDescription = "Share items"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                bluetoothViewModel = bluetoothViewModel,
                mapViewModel = mapViewModel,
            )

        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                modifier = Modifier.fillMaxWidth(),
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                UserGuideContent(
                    currentRoute = currentRoute,
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * User Guide Content for Bottom Sheet
 * Shows different guides based on the current page
 */
@Composable
private fun UserGuideContent(
    currentRoute: String?,
    onClose: () -> Unit
) {
    // Initialize TTS Manager
    var ttsInitialized by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    val ttsManager = rememberTextToSpeechManager { initialized ->
        ttsInitialized = initialized
    }
    
    // Get guide content text
    val guideText = remember(currentRoute) {
        when (currentRoute) {
            Screen.Home.route -> getHomeScreenGuideText()
            Screen.Bluetooth.route -> getBluetoothScreenGuideText()
            Screen.Map.route -> getMapScreenGuideText()
            else -> "Select a page to see its user guide"
        }
    }
    
    // Update speaking state
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(100)
            isSpeaking = ttsManager?.isCurrentlySpeaking() ?: false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        // Header with TTS button and close button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Centered title
            Text(
                text = "User Guide",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
            // Buttons on the right
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                // TTS Button - shows Stop icon when speaking
                IconButton(
                    onClick = {
                        if (isSpeaking) {
                            ttsManager?.stop()
                        } else {
                            ttsManager?.speak(guideText) {
                                isSpeaking = false
                            }
                        }
                    },
                    enabled = ttsInitialized
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                        contentDescription = if (isSpeaking) "Stop reading" else "Read aloud"
                    )
                }
                IconButton(onClick = {
                    ttsManager?.stop()
                    onClose()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close"
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Guide content based on current route
        when (currentRoute) {
            Screen.Home.route -> HomeScreenGuide()
            Screen.Bluetooth.route -> BluetoothScreenGuide()
            Screen.Map.route -> MapScreenGuide()
            else -> DefaultGuide()
        }
    }
}

/**
 * Get Home Screen Guide Text for TTS
 */
private fun getHomeScreenGuideText(): String {
    return """
        Home Screen Guide.
        
        Direction Controls.
        Use the arrow buttons to control robot movement.
        Up, Down, Left, Right arrows for directional movement.
        Buttons are square with rounded corners.
        
        Map Actions.
        Manage your map.
        Select map mode from dropdown: Cursor, Set Start, Place Obstacle, etc.
        Save Map: Save your current map configuration.
        Reset Map: Clear and reset the map, requires confirmation.
        Load saved maps from the list below.
        
        Map Grid Interaction.
        Interact with map cells.
        Single tap: Shows red flashing animation, not confirmed.
        Double tap: Confirms the cell, turns green.
        Triple tap or long press for 3 seconds: Resets the cell.
    """.trimIndent()
}

/**
 * Get Bluetooth Screen Guide Text for TTS
 */
private fun getBluetoothScreenGuideText(): String {
    return """
        Bluetooth Setup Guide.
        
        Button Actions.
        All buttons are in one row.
        Load Paired: Load previously paired Bluetooth devices.
        Scan: Search for nearby Bluetooth devices, turns tertiary when active.
        Host: Start as Bluetooth server, turns tertiary when active.
        Discoverable: Make device discoverable to others.
        Connect: Connect to selected device.
        Disconnect: Disconnect current connection, red when connected, disabled when disconnected.
        
        Connecting Steps.
        Step 1: Tap Load Paired to see already paired devices.
        Step 2: Or tap Scan to discover new devices.
        Step 3: Select a device from the list.
        Step 4: Tap Connect to establish connection.
        Step 5: Status will show connection progress.
        
        Hosting as Server.
        To allow others to connect to you.
        Step 1: Tap Host button, turns tertiary when active.
        Step 2: Tap Make Discoverable if needed.
        Step 3: Wait for other device to connect.
        Step 4: Status will show Listening when ready.
        
        Status Indicators.
        Watch the status bar.
        Disconnected: No active connection.
        Listening: Acting as server, waiting for connection.
        Connecting: Attempting to connect.
        Connected: Successfully connected.
        Error: Connection issue, check error message.
    """.trimIndent()
}

/**
 * Get Map Screen Guide Text for TTS
 */
private fun getMapScreenGuideText(): String {
    return """
        Map Screen Guide.
        
        Map Editing.
        Use the map grid to edit your environment.
        Select different edit modes from dropdown.
        Interact with grid cells to place obstacles, set start point, etc.
    """.trimIndent()
}

/**
 * User Guide for Home Screen
 */
@Composable
private fun HomeScreenGuide() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Home Screen Guide",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(12.dp))
        
        GuideSection(
            title = "Direction Controls",
            content = "Use the arrow buttons to control robot movement:\n" +
                    "• Up/Down/Left/Right arrows for directional movement\n" +
                    "• Buttons are square with rounded corners"
        )
        
        GuideSection(
            title = "Map Actions",
            content = "Manage your map:\n" +
                    "• Select map mode from dropdown (Cursor, Set Start, Place Obstacle, etc.)\n" +
                    "• Save Map: Save your current map configuration\n" +
                    "• Reset Map: Clear and reset the map (requires confirmation)\n" +
                    "• Load saved maps from the list below"
        )
        
        GuideSection(
            title = "Map Grid Interaction",
            content = "Interact with map cells:\n" +
                    "• Single tap: Shows red flashing animation (not confirmed)\n" +
                    "• Double tap: Confirms the cell (turns green)\n" +
                    "• Triple tap or long press (3s): Resets the cell"
        )
    }
}

/**
 * User Guide for Bluetooth Screen
 */
@Composable
private fun BluetoothScreenGuide() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Bluetooth Setup Guide",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(12.dp))
        
        GuideSection(
            title = "Button Actions",
            content = "All buttons are in one row:\n" +
                    "• Load Paired: Load previously paired Bluetooth devices\n" +
                    "• Scan: Search for nearby Bluetooth devices (turns tertiary when active)\n" +
                    "• Host: Start as Bluetooth server (turns tertiary when active)\n" +
                    "• Discoverable: Make device discoverable to others\n" +
                    "• Connect: Connect to selected device\n" +
                    "• Disconnect: Disconnect current connection (red when connected, disabled when disconnected)"
        )
        
        GuideSection(
            title = "Connecting Steps",
            content = "1. Tap 'Load Paired' to see already paired devices\n" +
                    "2. Or tap 'Scan' to discover new devices\n" +
                    "3. Select a device from the list\n" +
                    "4. Tap 'Connect' to establish connection\n" +
                    "5. Status will show connection progress"
        )
        
        GuideSection(
            title = "Hosting as Server",
            content = "To allow others to connect to you:\n" +
                    "1. Tap 'Host' button (turns tertiary when active)\n" +
                    "2. Tap 'Make Discoverable' if needed\n" +
                    "3. Wait for other device to connect\n" +
                    "4. Status will show 'Listening' when ready"
        )
        
        GuideSection(
            title = "Status Indicators",
            content = "Watch the status bar:\n" +
                    "• Disconnected: No active connection\n" +
                    "• Listening: Acting as server, waiting for connection\n" +
                    "• Connecting: Attempting to connect\n" +
                    "• Connected: Successfully connected\n" +
                    "• Error: Connection issue (check error message)"
        )
    }
}

/**
 * User Guide for Map Screen
 */
@Composable
private fun MapScreenGuide() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Map Screen Guide",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(12.dp))
        
        GuideSection(
            title = "Map Editing",
            content = "Use the map grid to edit your environment:\n" +
                    "• Select different edit modes from dropdown\n" +
                    "• Interact with grid cells to place obstacles, set start point, etc."
        )
    }
}

/**
 * Default Guide
 */
@Composable
private fun DefaultGuide() {
    Text(
        text = "Select a page to see its user guide",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

/**
 * Guide Section Component
 */
@Composable
private fun GuideSection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

/**
 * Theme toggle button group
 */
@Composable
private fun ThemeToggleButtons(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ThemeButton(
            icon = Icons.Default.LightMode,
            labelResId = R.string.theme_light,
            isSelected = currentTheme == ThemeMode.LIGHT,
            onClick = { onThemeChange(ThemeMode.LIGHT) },
            modifier = Modifier.weight(1f)
        )
        
        ThemeButton(
            icon = Icons.Default.DarkMode,
            labelResId = R.string.theme_dark,
            isSelected = currentTheme == ThemeMode.DARK,
            onClick = { onThemeChange(ThemeMode.DARK) },
            modifier = Modifier.weight(1f)
        )
        
        ThemeButton(
            icon = Icons.Default.Contrast,
            labelResId = R.string.theme_contrast,
            isSelected = currentTheme == ThemeMode.CONTRAST,
            onClick = { onThemeChange(ThemeMode.CONTRAST) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual theme button
 */
@Composable
private fun ThemeButton(
    icon: ImageVector,
    labelResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(labelResId)
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

/**
 * Language dropdown menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val languages = listOf(
        AppLanguage.SYSTEM,
        AppLanguage.ENGLISH,
        AppLanguage.MALAY,
        AppLanguage.TAMIL,
        AppLanguage.CHINESE_SIMPLIFIED
    )
    
    // Get current language label
    val currentLanguageLabel = when (currentLanguage) {
        AppLanguage.SYSTEM -> stringResource(R.string.lang_system)
        AppLanguage.ENGLISH -> stringResource(R.string.lang_english)
        AppLanguage.MALAY -> stringResource(R.string.lang_malay)
        AppLanguage.TAMIL -> stringResource(R.string.lang_tamil)
        AppLanguage.CHINESE_SIMPLIFIED -> stringResource(R.string.lang_chinese_simplified)
        else -> currentLanguage.displayName
    }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentLanguageLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            languages.forEach { language ->
                val languageLabel = when (language) {
                    AppLanguage.SYSTEM -> stringResource(R.string.lang_system)
                    AppLanguage.ENGLISH -> stringResource(R.string.lang_english)
                    AppLanguage.MALAY -> stringResource(R.string.lang_malay)
                    AppLanguage.TAMIL -> stringResource(R.string.lang_tamil)
                    AppLanguage.CHINESE_SIMPLIFIED -> stringResource(R.string.lang_chinese_simplified)
                    else -> language.displayName
                }
                
                DropdownMenuItem(
                    text = { Text(languageLabel) },
                    onClick = {
                        onLanguageChange(language)
                        expanded = false
                    },
                    colors = androidx.compose.material3.MenuDefaults.itemColors(
                        textColor = if (currentLanguage == language) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                )
            }
        }
    }
}

/**
 * Right-handed control toggle switch
 */
@Composable
private fun RightHandedControlToggle(
    isRightHanded: Boolean,
    onToggleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.right_handed_control),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isRightHanded,
            onCheckedChange = onToggleChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
