package com.example.textcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textcalc.data.Document
import com.example.textcalc.ui.CalculatorViewModel
import com.example.textcalc.ui.CalculatorViewModelFactory
import com.example.textcalc.ui.components.NamingDialog
import com.example.textcalc.ui.theme.TextcalcTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = (context.applicationContext as TextcalcApplication).database
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModelFactory(database.documentDao())
            )
            TextcalcTheme {
                CalculatorScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val inputText by viewModel.inputText.collectAsState()
    val totalSum by viewModel.totalSum.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val currentDocument by viewModel.currentDocument.collectAsState()
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showNamingDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var initialDialogName by remember { mutableStateOf("") }
    var isRenameMode by remember { mutableStateOf(false) }

    if (showNamingDialog) {
        NamingDialog(
            initialName = initialDialogName,
            title = dialogTitle,
            onDismiss = { showNamingDialog = false },
            onConfirm = { name ->
                if (isRenameMode) {
                    currentDocument?.let { viewModel.renameDocument(it.id, name) }
                } else {
                    viewModel.createNewDocument(name)
                }
                showNamingDialog = false
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Documents",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp)
                ) {
                    items(documents) { doc ->
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    doc.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (doc.id == currentDocument?.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = doc.id == currentDocument?.id,
                            onClick = {
                                viewModel.selectDocument(doc)
                                scope.launch { drawerState.close() }
                            },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Notes,
                                    contentDescription = null
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Add Document", fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = {
                                dialogTitle = "New Document"
                                initialDialogName = ""
                                isRenameMode = false
                                showNamingDialog = true
                                scope.launch { drawerState.close() }
                            },
                            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                    },
                    title = {
                        Text(
                            text = currentDocument?.title ?: "Textcalc",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            dialogTitle = "Rename Document"
                            initialDialogName = currentDocument?.title ?: ""
                            isRenameMode = true
                            showNamingDialog = true
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Rename Document")
                        }
                        IconButton(onClick = {
                            dialogTitle = "New Document"
                            initialDialogName = ""
                            isRenameMode = false
                            showNamingDialog = true
                        }) {
                            Icon(Icons.Rounded.Add, contentDescription = "New Document")
                        }
                        IconButton(onClick = { viewModel.deleteCurrentDocument() }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete Document")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                TotalBar(totalSum)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .imePadding()
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { viewModel.onTextChanged(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(vertical = 16.dp),
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        lineHeight = 28.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Start typing numbers...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    fontSize = 20.sp,
                                    lineHeight = 28.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TotalBar(totalSum: Double) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column {
                    Text(
                        text = "TOTAL SUM",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (totalSum % 1.0 == 0.0) totalSum.toInt().toString() else "%.2f".format(totalSum),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CalculatorPreview() {
    TextcalcTheme {
        // Preview placeholder
        Text("Calculator UI Preview")
    }
}
