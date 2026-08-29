# Plan de Implementación - Matriz de Requerimientos Oficial (RF01-RF12)

Este plan detalla las modificaciones necesarias para alinear el proyecto **OdontoSystemMobile** con la Matriz de Requerimientos oficial, cubriendo autenticación, reserva express, gestión de odontólogos y registro profesional.

## User Review Required

> [!IMPORTANT]
> Se implementará una simulación de la lógica de negocio en `MockApiInterceptor` y `AppointmentStore` para que la demo refleje fielmente los requerimientos (ej: validación de 12 horas para cancelación).

> [!WARNING]
> La "Aprobación de Odontólogo" (RF09) se simulará mediante un estado en el modelo de usuario, ya que el sistema móvil es principalmente para Pacientes y Odontólogos. Se añadirá una vista de "Estado de Verificación" para el odontólogo recién registrado.

## Proposed Changes

### 1. Autenticación y Seguridad (RF01, RNF01)

#### [MODIFY] [SessionManager.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/local/SessionManager.kt)
- Asegurar que el manejo de tokens JWT sea consistente.
- Añadir lógica para manejar la expiración del token (simulada).

#### [MODIFY] [AuthInterceptor.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/remote/AuthInterceptor.kt)
- Reforzar la inyección del token Bearer.

### 2. Búsqueda y Reserva Express (RF02, RF03, RNF02)

#### [MODIFY] [MainActivity.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/main/MainActivity.kt)
- Optimizar el flujo de filtros para garantizar el cumplimiento de <4 clics.

#### [MODIFY] [DentistAdapter.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/main/DentistAdapter.kt)
- Asegurar que los slots de tiempo sean directamente accionables desde la lista de resultados.

### 3. Historial y Cancelación (RF04, RF05)

#### [MODIFY] [Appointment.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/model/Appointment.kt)
- Expandir estados: `CONFIRMADA`, `PENDIENTE`, `CANCELADA`, `ATENDIDA`.

#### [MODIFY] [AppointmentStore.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/local/AppointmentStore.kt)
- Implementar la regla de **12 horas de antelación** para cancelaciones.
- Implementar la liberación inmediata del turno tras la cancelación.

#### [MODIFY] [AppointmentAdapter.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/main/AppointmentAdapter.kt)
- Añadir estilos visuales para los diferentes estados del historial.

### 4. Gestión del Odontólogo (RF06, RF07, RF11, RF12)

#### [MODIFY] [Dentist.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/model/Dentist.kt)
- Añadir campos: `occupancyLevel` (0-100), `autoConfirm` (Boolean), `availabilityFrequency` (Enum: DAILY, WEEKLY).

#### [MODIFY] [DentistDashboardActivity.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/dentist/DentistDashboardActivity.kt)
- Añadir Toggle para **Confirmación Manual/Automática** (RF11).
- Añadir Selector de **Frecuencia de Disponibilidad** (RF06).
- Mejorar el feedback visual del **Bloqueo Express** (RF07).

#### [MODIFY] [DentistDetailActivity.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/dentist/DentistDetailActivity.kt)
- Añadir indicador visual de **Nivel de Ocupación** (RF12) usando una barra de progreso o chip de color.

### 5. Registro y Validación Profesional (RF08, RF09)

#### [NEW] [RegisterActivity.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/auth/RegisterActivity.kt)
- Pantalla de registro para odontólogos con selector de archivos para los 4 documentos (DNI, Título, Colegiatura, CV).

#### [NEW] [VerificationStatusActivity.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/ui/dentist/VerificationStatusActivity.kt)
- Vista para que el odontólogo vea el estado de su registro (Aprobado, Observado, Rechazado) y permita la re-subida de documentos.

### 6. Backend Simulador (Infraestructura de Demo)

#### [MODIFY] [MockApiInterceptor.kt](file:///C:/GitHub/OdontoSystemMobile/app/src/main/java/com/odontosystem/app/data/remote/MockApiInterceptor.kt)
- Actualizar los datos de prueba para incluir los nuevos campos y soportar los flujos de registro y confirmación manual.

### 7. Documentación

#### [MODIFY] [README.md](file:///C:/GitHub/OdontoSystemMobile/README.md)
- Reemplazo total por la versión oficial que incluye la tabla de requerimientos, arquitectura MVVM y guías de ejecución.

## Verification Plan

### Automated Tests
- No se requieren tests automatizados nuevos, se validará mediante ejecución manual y logs.

### Manual Verification
1. **Flujo de 4 clics:** Filtrar distrito -> Tocar Slot -> Confirmar.
2. **Cancelación 12h:** Intentar cancelar una cita programada para dentro de 1 hora (debe fallar/bloquearse) vs una para mañana (debe funcionar y liberar el slot).
3. **Bloqueo Express:** Activar desde el panel del odontólogo y verificar que el odontólogo desaparece o sus slots se bloquean en la vista del paciente.
4. **Registro:** Completar el flujo de subida de 4 documentos.
