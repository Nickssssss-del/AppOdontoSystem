package com.odontosystem.app.ui.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.odontosystem.app.R
import com.odontosystem.app.data.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: LoginViewModel,
    onRegisterSuccess: (UserRole) -> Unit
) {
    val loginState by viewModel.loginState.observeAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Paciente, 1: Odontólogo
    
    // Campos comunes
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Campos específicos Odontólogo
    var copNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeDocumentType by remember { mutableStateOf<DocumentType?>(null) }
    var uploadingDocumentType by remember { mutableStateOf<DocumentType?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val selectedDocuments = remember { mutableStateMapOf<DocumentType, Uri>() }

    val documentPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val selectedType = activeDocumentType
        activeDocumentType = null

        if (selectedType == null) return@rememberLauncherForActivityResult

        if (uri == null) {
            uploadError = "No se seleccionó archivo para ${selectedType.label}."
            uploadingDocumentType = null
            return@rememberLauncherForActivityResult
        }

        uploadError = null
        uploadingDocumentType = selectedType
        scope.launch {
            try {
                // Simula procesamiento de archivo y evita bloqueo infinito de estado.
                delay(350)
                selectedDocuments[selectedType] = uri
                Toast.makeText(context, "${selectedType.label} cargado correctamente.", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                uploadError = "No se pudo procesar ${selectedType.label}. Intenta nuevamente."
            } finally {
                uploadingDocumentType = null
            }
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginViewModel.LoginState.Success) {
            onRegisterSuccess((loginState as LoginViewModel.LoginState.Success).role)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Profesional", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.surface),
                    titleContentColor = colorResource(id = R.color.primary)
                )
            )
        },
        containerColor = colorResource(id = R.color.background)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Únete a OdontoSystem",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.primary_night)
            )
            
            Text(
                text = "Crea tu cuenta para gestionar tus citas",
                fontSize = 14.sp,
                color = colorResource(id = R.color.text_secondary),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Selector de Rol (Dual Dinámico)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = colorResource(id = R.color.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Soy Paciente") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Soy Odontólogo") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Formulario Moderno
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Celular") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Campos Dinámicos para Odontólogo
            if (selectedTab == 1) {
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = copNumber,
                    onValueChange = { copNumber = it },
                    label = { Text("Número de Colegiatura (COP)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Documentación Obligatoria",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                DocUploadButton(
                    text = "📄 Subir DNI (PDF/Imagen)",
                    isUploaded = selectedDocuments.containsKey(DocumentType.DNI),
                    isLoading = uploadingDocumentType == DocumentType.DNI,
                    onClick = {
                        uploadError = null
                        activeDocumentType = DocumentType.DNI
                        documentPickerLauncher.launch("*/*")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                DocUploadButton(
                    text = "📄 Subir Título Profesional",
                    isUploaded = selectedDocuments.containsKey(DocumentType.TITLE),
                    isLoading = uploadingDocumentType == DocumentType.TITLE,
                    onClick = {
                        uploadError = null
                        activeDocumentType = DocumentType.TITLE
                        documentPickerLauncher.launch("*/*")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                DocUploadButton(
                    text = "📄 Subir CV Actualizado",
                    isUploaded = selectedDocuments.containsKey(DocumentType.CV),
                    isLoading = uploadingDocumentType == DocumentType.CV,
                    onClick = {
                        uploadError = null
                        activeDocumentType = DocumentType.CV
                        documentPickerLauncher.launch("*/*")
                    }
                )

                if (uploadError != null) {
                    Text(
                        text = uploadError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val role = if (selectedTab == 0) UserRole.PATIENT else UserRole.DENTIST
                    if (role == UserRole.DENTIST) {
                        if (copNumber.isBlank()) {
                            Toast.makeText(context, "Ingresa tu número de colegiatura.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        val requiredDocs = setOf(DocumentType.DNI, DocumentType.TITLE, DocumentType.CV)
                        if (!selectedDocuments.keys.containsAll(requiredDocs)) {
                            Toast.makeText(context, "Debes subir DNI, Título y CV para registrarte como odontólogo.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                    }

                    viewModel.register(
                        name = name,
                        lastName = lastName,
                        email = email,
                        phone = phone,
                        pass = password,
                        role = role,
                        copNumber = if (selectedTab == 1) copNumber else null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary)),
                enabled = loginState !is LoginViewModel.LoginState.Loading
                    && uploadingDocumentType == null
            ) {
                if (loginState is LoginViewModel.LoginState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Crear Cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (loginState is LoginViewModel.LoginState.Error) {
                Text(
                    text = (loginState as LoginViewModel.LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DocUploadButton(
    text: String,
    isUploaded: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUploaded) colorResource(id = R.color.primary) else colorResource(id = R.color.text_muted)
        ),
        enabled = !isLoading
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            val label = when {
                isLoading -> "Subiendo documento..."
                isUploaded -> "✅ Documento cargado"
                else -> text
            }
            Text(label, fontSize = 13.sp, color = colorResource(id = R.color.text_secondary))
        }
    }
}

private enum class DocumentType(val label: String) {
    DNI("DNI"),
    TITLE("Título Profesional"),
    CV("CV Actualizado")
}
