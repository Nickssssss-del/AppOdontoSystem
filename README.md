# OdontoSystemMobile — Matriz de Requerimientos Oficial (RF01-RF12)

Plataforma móvil integral para la gestión de citas odontológicas en la provincia de **Ica**, optimizada para conectar pacientes con odontólogos independientes mediante un sistema de reserva express y validación profesional rigurosa.

---

## 1. Descripción y Arquitectura

**OdontoSystemMobile** es una solución desarrollada en **Kotlin** para Android, siguiendo el patrón arquitectónico **MVVM (Model-View-ViewModel)**. La aplicación garantiza una separación clara entre la lógica de negocio, el acceso a datos y la interfaz de usuario, permitiendo escalabilidad y mantenibilidad.

### Stack Técnico:
- **Lenguaje:** Kotlin 1.9+
- **Arquitectura:** MVVM con Repositorios.
- **UI:** XML + Material Design 3 (M3) + View Binding.
- **Red:** Retrofit 2 + OkHttp (con interceptores JWT).
- **Seguridad:** EncryptedSharedPreferences + BCrypt (simulado en backend).
- **Asincronía:** Coroutines y LiveData.

---

## 2. Matriz de Requerimientos Funcionales (RF)

| ID | Requerimiento | Criterio de Aceptación |
| --- | --- | --- |
| **RF01** | Autenticación Segura | Inicio de sesión con JWT y contraseñas encriptadas. Soporte para roles Paciente y Odontólogo. |
| **RF02** | Búsqueda por Distrito | Filtro de odontólogos por los distritos de Ica (Parcona, Los Aquijes, etc.). |
| **RF03** | Reserva Express | Reserva de turno libre en **4 clics o menos** desde la vista de resultados. |
| **RF04** | Historial de Citas | Visualización de citas en estados: **Atendida, Cancelada, Pendiente**. |
| **RF05** | Cancelación Flexible | Cancelación permitida hasta **12 horas antes** con liberación automática e inmediata del turno. |
| **RF06** | Frecuencia de Disponibilidad | Odontólogo puede configurar agenda de forma **Diaria o Semanal**. |
| **RF07** | Bloqueo Express | Switch de emergencia para pausar turnos de hoy sin afectar semanas futuras. |
| **RF08** | Registro Profesional | Carga obligatoria de 4 documentos: **DNI, Título, Colegiatura y CV**. |
| **RF09** | Panel Administrativo | Gestión de validación con estados: **Aprobado, Observado, Rechazado**. |
| **RF10** | Canales de Recordatorio | Notificación en tarjeta de próxima cita e integración con **WhatsApp Business**. |
| **RF11** | Confirmación de Citas | Toggle para alternar entre **Confirmación Manual o Automática**. |
| **RF12** | Nivel de Ocupación | Indicador visual (0-100%) en el perfil público del odontólogo. |

---

## 3. Requerimientos No Funcionales (RNF)

- **RNF01 (Seguridad):** Encriptación de datos sensibles y tokens con expiración controlada.
- **RNF02 (Usabilidad):** Flujo de reserva optimizado para completarse en <4 interacciones.
- **RNF03 (Rendimiento):** Tiempo de respuesta del sistema inferior a **2 segundos** en condiciones normales.
- **RNF04 (Disponibilidad):** Arquitectura preparada para un uptime del **99.9%**.

---

## 4. Integraciones y Canales (RF10)

La plataforma está preparada para la comunicación multicanal:
- **Correo Electrónico:** Envío automático de confirmaciones y cambios de estado.
- **WhatsApp Business API:** Botón de contacto directo y recordatorios automatizados 24h antes de la cita.

---

## 5. Guía de Ejecución

### Prerrequisitos
- Android Studio Ladybug / Meerkat.
- JDK 17 y Android SDK 34 (minSdk 26).

### Instrucciones
1. **Clonar Repositorio:** `git clone https://github.com/usuario/OdontoSystemMobile.git`
2. **Gradle Sync:** Abrir en Android Studio y esperar la sincronización de dependencias.
3. **Compilación:** `Build -> Make Project`.
4. **Ejecución:** Seleccionar emulador o dispositivo físico y presionar `Run 'app'`.

---
