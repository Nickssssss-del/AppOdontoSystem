# Plan de Implementación: Capa de Datos para Registro

Este plan detalla la creación de la infraestructura de datos (Modelos, API y Repositorio) para soportar el proceso de registro de Pacientes y Odontólogos.

## User Review Required

> [!IMPORTANT]
> Se implementará una simulación de registro en el `MockApiInterceptor`. Esto permitirá que la aplicación "funcione" (navegue al dashboard tras registrarse) incluso sin un backend real conectado.

> [!NOTE]
> Se expandirá el `AuthRepository` para centralizar toda la lógica de identidad, siguiendo el patrón de diseño ya establecido en el proyecto.

## Proposed Changes

### 1. Modelos de Autenticación
#### [MODIFY] [AuthModels.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/model/AuthModels.kt)
- Añadir la clase `RegisterRequest` con soporte para datos personales, colegiatura (COP) y documentos.

### 2. Definición de la API
#### [MODIFY] [ApiService.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/remote/ApiService.kt)
- Añadir el endpoint `POST /auth/register`.

### 3. Lógica del Repositorio
#### [MODIFY] [AuthRepository.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/repository/AuthRepository.kt)
- Implementar la función `register(request: RegisterRequest)`.
- Añadir lógica de persistencia temporal (demo) para que el usuario recién registrado sea recordado durante la sesión.

### 4. Integración con el ViewModel
#### [MODIFY] [LoginViewModel.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/auth/LoginViewModel.kt)
- Añadir `register(name, lastName, email, phone, password, role, copNumber, documents)`.
- Manejar estados de carga y error específicos para el registro.

### 5. Conexión con la UI (Compose)
#### [MODIFY] [RegisterScreen.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/auth/RegisterScreen.kt)
- Conectar el botón "Crear Cuenta" a la función `register` del ViewModel.
- Observar el estado del registro para navegar al Dashboard tras el éxito.

### 6. Simulación (Backend Mock)
#### [MODIFY] [MockApiInterceptor.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/remote/MockApiInterceptor.kt)
- Añadir intercepción para `/auth/register` que devuelva un `AuthResponse` exitoso.

## Verification Plan

### Automated Tests
- Compilación del proyecto para asegurar que los nuevos modelos no rompen Retrofit.

### Manual Verification
1. **Flujo de Registro**: Completar el formulario en la app y verificar que se produzca una navegación exitosa al Dashboard.
2. **Validación de Datos**: Intentar registrarse con campos vacíos y verificar que el ViewModel devuelva un error adecuado.
