# Formulación de Capa de Datos para Registro

Se ha implementado la infraestructura de datos necesaria para soportar el proceso de registro de Pacientes y Odontólogos, integrando la UI de Compose con un repositorio sólido y simulación de backend.

## Cambios Realizados

### 🏗️ Modelos y Contratos (RF08)
- **RegisterRequest:** Se ha definido el modelo de datos que agrupa la información de registro, incluyendo campos dinámicos como `copNumber` para odontólogos.
- **ApiService:** Se añadió el endpoint `POST /auth/register` a la interfaz de Retrofit.

### 💾 Repositorio e Identidad
- **AuthRepository:** Se implementó la función `register`, la cual gestiona la llamada a la API y la persistencia de la sesión en el `SessionManager`.
- **Persistencia de Sesión:** Tras un registro exitoso (o simulado), el sistema guarda automáticamente el token, nombre y rol del usuario para permitir la navegación inmediata al Dashboard.

### 🧠 Lógica de Negocio y UI (Compose)
- **LoginViewModel:** Se añadió la función `register` que maneja los estados de carga (`Loading`), éxito (`Success`) y errores (`Error`).
- **RegisterScreen (Conexión):**
    - El botón "Crear Cuenta" ahora invoca la lógica del repositorio.
    - Se observa el estado de autenticación para realizar el salto automático al Dashboard correspondiente (Paciente o Odontólogo).
    - Se incluyó manejo de errores visuales en la parte inferior del formulario.

### ⚡ Simulación (Mock Backend)
- **MockApiInterceptor:** Se añadió una respuesta simulada para el endpoint de registro, garantizando que el flujo sea funcional en entornos de desarrollo sin API activa.

## Verificación

### Compilación Exitosa
El proyecto ha sido sincronizado y compilado satisfactoriamente, validando la integridad de los nuevos modelos y dependencias de Compose.

### Flujo de Usuario Validado
1. **Registro:** Al completar los campos y presionar "Crear Cuenta", la app muestra el indicador de carga.
2. **Dashboard:** Tras el registro, el sistema reconoce al nuevo usuario y lo redirige automáticamente a su pantalla principal.
3. **Persistencia:** Si se reinicia la app (simulado), el `SessionManager` mantiene la sesión del usuario registrado.

[AuthRepository.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/repository/AuthRepository.kt)
[RegisterScreen.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/auth/RegisterScreen.kt)
