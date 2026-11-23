# Changelog

Cambios de cada versión de la API de SEGUIMORBI. Formato: [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/);
versiones: [SemVer](https://semver.org/lang/es/).

## v1.0.0 — 2026-11-23

Primera versión completa de la API (Spring Boot 2.6, Java 17, MySQL), con arquitectura hexagonal.

### Nuevo

- **Autenticación con JWT**: inicio de sesión con usuario o email, perfil en `/api/v1/user/me` y primer
  administrador creado al arrancar. Sin registro público.
- **Roles ADMIN, MEDICO y EPIDEMIOLOGO** con acceso por área de salud.
- **Administración**: usuarios, áreas de salud (con reactivación) y catálogo de desenlaces.
- **Pacientes** del área del médico, con reactivación de pacientes desactivados.
- **Casos de fiebre de Oropouche** con clasificación según la OPS y vista previa de la clasificación antes de
  guardar.
- **Atención clínica**: evaluaciones, dictamen, plan de atención, seguimiento, cierre y reapertura, con alertas
  clínicas.
- **Vigilancia epidemiológica**: validación de clasificaciones y estadísticas (indicadores, curva epidémica, áreas).
- **Despliegue**: entorno de GitHub Codespaces y despliegue gratuito en Render con TiDB Cloud
  (`Dockerfile`, `render.yaml`, `docs/despliegue-render.md`).

### Arreglado

- CORS se aplica en la cadena de seguridad, para que los 401 lleguen al frontend.
- Alertas y eventos de los casos muestran signos y fechas legibles.

### Cambios importantes

- El proyecto pasa a llamarse **SEGUIMORBI** (antes Oropouche): paquete `com.seguimorbi`, propiedades
  `seguimorbi.*` y base `seguimorbidb`. La enfermedad conserva su nombre.

### Seguridad

- Los datos clínicos no se escriben en los registros (logs).
