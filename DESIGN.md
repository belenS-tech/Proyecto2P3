# Sistema de reserva de recursos

Aplicación de escritorio Swing para funcionarios y administradores universitarios,
en español. La presentación conserva sus pestañas, formularios e identidad lila.
El enunciado del Proyecto 2 define la ubicación y comportamiento de las alertas.

## Runtime y estilo

`view.Tema` es la fuente de colores y controles compartidos. Los nuevos controles
usan sus constantes y `Tema.aplicar`; no se incorpora un sistema visual web.
La tipografía de Swing y los controles nativos mantienen sus convenciones.

## Alertas

`view.AlertasPanel` ocupa el lado derecho de `MainView`, fuera de las pestañas.
Su lista tiene selección única, desplazamiento, nombre accesible y acciones
explícitas. Sin selección las acciones quedan deshabilitadas. El detalle admite
texto largo; descartar pide confirmación y conserva la reserva en el servidor.
El estado vacío explica que no hay reservas nuevas en la sesión.

## Comportamiento y propiedad

El contrato canónico de la extensión está en UX-CONTRACT.md. `AlertasController`
gestiona los eventos y la suscripción; los cambios visuales ocurren en el EDT.
El servidor es responsable de autenticación, permisos, datos e IDs concurrentes.

## Reservas y recursos

`ReservaView` y `RecursoView` construyen los controles Swing, aplican `Tema` y
presentan mensajes y selección de archivos. Sus controladores reciben las vistas,
registran eventos y utilizan contratos `api/`; no son componentes visuales.
El cliente solicita categorías; el backend asigna los recursos disponibles.
Las matrices representan las 24 horas con intervalos de una hora, muestran todas
las actividades que se solapan con cada intervalo y el nombre del funcionario.

## Consulta y estadísticas

`CalendarizacionRecursosPanel`, `ProgramacionActividadesPanel`,
`EstadisticasRecursosPanel` y `EstadisticasActividadesPanel` solo presentan
controles, matrices y gráficos. Sus cuatro controladores independientes manejan
eventos, contratos remotos y reportes. `MainController` compone los módulos por
rol y administra la suscripción de alertas; `MainView` no conoce servicios.
`PanelReporte` centraliza el selector de archivos y los mensajes de error.
Una consulta fallida deshabilita la exportación del resultado anterior.

Los horarios obtenidos por IA se añaden a los selectores cuando es necesario,
conservando sus minutos y segundos. El historial y los conteos incluyen reservas
activas y finalizadas y excluyen canceladas. Los permisos administrativos se
validan también para consultas en el backend; listar categorías sigue disponible
para ambos roles por los flujos de reserva y calendarización.
