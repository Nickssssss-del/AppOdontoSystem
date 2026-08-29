# OdontoSystemMobile — Plataforma Móvil con IA para Citas Odontológicas Geolocalizadas

Aplicación Android de titulación (Curso Integrador I, Universidad Tecnológica del Perú) que conecta **pacientes** y **odontólogos independientes** en la provincia de **Ica**, sin depender de llamadas ni de la visibilidad de las clínicas grandes.

Proyecto: **ODS-2026-001** · Kotlin · Android SDK 34 · minSdk 26.

---

## Descripción general

En Ica, agendar una limpieza o un control suele significar llamar a varios consultorios, no ver la disponibilidad real y olvidar la cita. Los odontólogos independientes, a su vez, pierden turnos cuando surge una emergencia y no pueden pausar solo el día afectado.

**OdontoSystemMobile** resuelve ambos lados:

| Actor | Problema | Qué hace la app |
| --- | --- | --- |
| Paciente | Llamadas, horarios inciertos, olvido de citas | Filtra por distrito, reserva un turno marcado como libre y cancela desde el inicio para liberar el horario |
| Odontólogo | Agenda rígida ante imprevistos | Publica disponibilidad y activa **Bloqueo Express** solo para el día, sin reescribir las semanas siguientes |
| Administrador (alcance del sistema) | Perfiles no verificados en el buscador | El registro profesional se aprueba antes de aparecer en búsqueda (RF08 / RF09) |

La evidencia de campo que inspira los escenarios está documentada en los requerimientos del curso (entrevistas a pacientes y odontólogos de Ica).

---

## Potencialidades e innovaciones

### Reserva rápida (menos de 4 toques) — RF02, RF03, RNF02

Flujo validado con el escenario de María (limpieza dental en Ica):

1. Filtrar distrito (opcional).  
2. Tocar un **turno libre** en la tarjeta del odontólogo.  
3. Confirmar la reserva (pago en consultorio preseleccionado).

No hay llamadas ni espera de respuesta telefónica. El sistema confirma en pantalla.

### Bloqueo Express de agenda — RF06, RF07

En el panel del odontólogo, **Bloqueo Express** pausa los turnos **de hoy** (emergencia familiar o de fuerza mayor):

- Esos horarios salen de la vista de los pacientes.  
- Quienes ya tenían cita quedan notificados (estado *Pausada por emergencia*).  
- El horario base de las **próximas semanas no se altera**.

### Tarjeta Próxima Cita y liberación de turno — RF05, RF10

En la pantalla principal del paciente aparece la **próxima cita confirmada**, con recordatorio a 24 h (mensaje en tarjeta) y acción **Cancelar y liberar turno**. El horario vuelve a estar disponible para otra persona y se reduce el ausentismo.

### Chatbot con memoria contextual

El asistente 24/7 conserva las últimas intervenciones de la sesión. Si el paciente habla de un distrito (Ica, Parcona, Los Aquijes, La Tinguiña, Subtanjalla) y luego pide “agendar”, la respuesta usa ese contexto.

### Geolocalización por distrito

El buscador no es un listado nacional genérico: filtra odontólogos por distritos de Ica y muestra dirección de consultorio en la ficha, alineado con la necesidad de ver disponibilidad **cerca**.

---

## Tecnologías y arquitectura

| Capa | Tecnología |
| --- | --- |
| Lenguaje | Kotlin 1.9 |
| UI | XML + View Binding + Material 3 (Jetpack). No es Compose; la navegación y listas usan Activities, Fragments y RecyclerView |
| Arquitectura | **MVVM** (ViewModel + LiveData) y repositorios; la UI no habla con Retrofit de forma directa salvo el arranque del cliente |
| Red | Retrofit 2, OkHttp, Gson; interceptor de demo si el API remoto no responde |
| Sesión | EncryptedSharedPreferences (token y rol) |
| Asincronía | Coroutines |
| Build | Gradle 8.13, Android Gradle Plugin 8.13, JDK 17 |

```
ui/ (Activities, ViewModels, adapters)
repository/ (Auth, Dentist, Appointment, Chat)
data/model  data/remote  data/local (sesión + AppointmentStore)
```

`AppointmentStore` mantiene en memoria citas creadas, cancelaciones y el Bloqueo Express para que paciente y odontólogo vean el mismo estado durante la demo.

---

## Guía paso a paso para ejecutar en Android Studio

### Prerrequisitos

- **Android Studio** Ladybug / Meerkat o posterior (compatible con AGP 8.13).  
- **JDK 17** (el módulo `app` usa `sourceCompatibility` / `jvmTarget` 17).  
- **Android SDK**: compileSdk y targetSdk **34**; **minSdk 26** (Android 8.0).  
- Emulador con Google APIs o un teléfono con USB debugging.

### 1. Clonar el repositorio

```bash
git clone https://github.com/<tu-usuario>/OdontoSystemMobile.git
cd OdontoSystemMobile
```

### 2. Abrir y sincronizar Gradle

1. **File → Open** y elige la carpeta raíz (donde está `settings.gradle`).  
2. Espera **Sync Project with Gradle Files**.  
3. Si pide el JDK, selecciona 17.

### 3. Compilar

- Menú **Build → Make Project**, o desde terminal:

```bash
./gradlew assembleDebug
```

En Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/`.

### 4. Emulador o dispositivo

1. **Device Manager**: crea un AVD (por ejemplo Pixel 6, API 34).  
2. O conecta un teléfono: `adb devices` debe listarlo.  
3. **Run → Run 'app'** (Shift+F10).  
4. La actividad de inicio es el login.

**Cuentas de demostración**

| Perfil | Correo (demo) | Contraseña |
| --- | --- | --- |
| Paciente | `nicole@odontosystem.com` | `12345678` |
| Odontólogo | pestaña *Soy Odontólogo* + `dr.ramos@odontosystem.com` | `12345678` |
| Invitado | botón **Entrar como Demo** | — |

El cliente intenta `https://odontosystem-api.onrender.com/` y, si falla, usa datos mock (distritos de Ica, turnos libres y citas).

---

## Escenarios de uso principales (validados en el proyecto)

Los escenarios ilustrativos del documento de requerimientos:

1. **Reserva sin llamar (RF02, RF03)** — María filtra su distrito, elige un odontólogo y un turno *disponible*; la app confirma en menos de 4 toques.  
2. **Emergencia y Bloqueo Express (RF06, RF07)** — El Dr. Ramos pausa los turnos de hoy; los pacientes dejan de ver esos horarios; las semanas siguientes no se tocan.  
3. **Recordatorio y no-show (RF05, RF10)** — Pedro ve la próxima cita, recibe el aviso a 24 h (tarjeta) y, si no puede ir, cancela y libera el turno.  
4. **Aprobación de odontólogo (RF08, RF09, RNF01)** — Un perfil nuevo (DNI, título, colegiatura, CV) no aparece en el buscador hasta que administración lo aprueba.  
5. **Aprobación manual de citas** — Algunos odontólogos pueden exigir confirmar la solicitud antes de dejarla en estado confirmada (alcance de diseño del sistema).

En esta build móvil se priorizan los flujos **1–3** en la UI (paciente + panel odontólogo). El flujo 4 queda como regla de negocio del backend/admin.

---

## Paleta oficial

| Token | Hex | Uso |
| --- | --- | --- |
| Acento | `#0084FF` | Botones de acción, FAB, indicador |
| Primario | `#0E74DF` | Marca y chips |
| Medio | `#0B5EB8` | Secundario Material |
| Navy | `#0B488A` | Tabs, variantes |
| Medianoche | `#092E5B` | Status bar, tarjeta Próxima Cita, texto |

Definida en `app/src/main/res/values/colors.xml` y `themes.xml`.

---

## Estructura del módulo `app`

```
app/src/main/java/com/odontosystem/app/
  ui/auth          Login
  ui/main          Inicio paciente, listados, reserva express
  ui/dentist       Ficha, diálogo de reserva, panel y Bloqueo Express
  ui/chatbot       Asistente con memoria de sesión
  repository/      Acceso a datos
  data/remote      Retrofit + MockApiInterceptor
  data/local       SessionManager, AppointmentStore
```

---

## Autores y curso

Universidad Tecnológica del Perú — Curso Integrador I.  
Docente: Ing. Ana Meliza Garayar Tito.  
Equipo ODS-2026-001: Sebastián (jefe de proyecto), Luana, Nicole.

Este README está pensado para el **portafolio de GitHub**: describe el problema local, las innovaciones medibles y cómo un reclutador o jurado ejecuta el prototipo en Android Studio.
