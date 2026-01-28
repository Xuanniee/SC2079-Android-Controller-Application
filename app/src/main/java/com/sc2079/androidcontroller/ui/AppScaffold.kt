package com.sc2079.androidcontroller.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.language.presentation.AppLanguage
import com.sc2079.androidcontroller.features.language.presentation.LocaleState
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
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
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
        )
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
                modifier = Modifier.padding(paddingValues)
            )
        }
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
