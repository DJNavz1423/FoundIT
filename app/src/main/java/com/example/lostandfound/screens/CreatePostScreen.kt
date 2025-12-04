package com.example.lostandfound.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lostandfound.viewmodels.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf("LOST") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var contactInfo by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64 by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val categories = listOf("Electronics", "Documents", "Pets", "Jewelry", "Bags", "Keys", "Clothing", "Other")
    var expandedCategory by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val base64 = viewModel.bitmapToBase64(bitmap)
            val sizeInKB = base64.length / 1024
            if (sizeInKB > 900) {
                errorMessage = "Image is too large. Please select a smaller image."
                imageUri = null
                imageBase64 = ""
            } else {
                imageBase64 = base64
                errorMessage = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Type Selection
            Text("Type", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = selectedType == "LOST",
                    onClick = { selectedType = "LOST" },
                    label = { Text("Lost") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = selectedType == "FOUND",
                    onClick = { selectedType = "FOUND" },
                    label = { Text("Found") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Info
            OutlinedTextField(
                value = contactInfo,
                onValueChange = { contactInfo = it },
                label = { Text("Contact Info (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Image Selection
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Image")
            }

            // Display selected image
            if (imageUri != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val inputStream = context.contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Error message
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    when {
                        title.isEmpty() || description.isEmpty() || location.isEmpty() ->
                            errorMessage = "Please fill all required fields"
                        else -> {
                            isLoading = true
                            viewModel.createPost(
                                type = selectedType,
                                title = title,
                                description = description,
                                location = location,
                                category = category,
                                contactInfo = contactInfo,
                                imageBase64 = imageBase64,
                                onSuccess = {
                                    isLoading = false
                                    onNavigateBack()
                                },
                                onError = { error ->
                                    isLoading = false
                                    errorMessage = error
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Post")
                }
            }
        }
    }
}