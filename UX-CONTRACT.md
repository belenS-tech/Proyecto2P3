# Contrato de la extensión distribuida

| Capacidad | Propietario | Comportamiento | Evidencia |
|---|---|---|---|
| Table Selection | AlertasPanel / JList nativo Swing | Selección única; botones habilitados sólo al seleccionar | AlertasPanelTest |
| Scrollbar | JScrollPane nativo Swing | Lista y detalle desplazan texto largo | AlertasPanelTest |
| Alertas | AlertasController | Suscripción por sesión; actualizaciones en EDT | SocketServicesIT / AlertasPanelTest |
| Confirmación | JOptionPane Swing, igual a MainView.confirmar | Sí/No antes de retirar alerta; cancelar conserva aviso | Inspección de AlertasController |
| CRUD | Servicios remotos / BackendServer | Descartar es local; permisos y persistencia en servidor | SocketServicesIT |
| Reservas | ReservaView / ReservaController / ReservaService | Solicitar categorías; asignación y guardado en backend; conservar formulario ante errores | VistasMvcTest / CorreccionesEnunciadoTest / SocketServicesIT |
| Cancelación | ReservaService | Solo antes del inicio guardado; no confiar en la fecha enviada por cliente | CorreccionesEnunciadoTest / OracleRepositoryIT |
| Recursos | RecursoView / RecursoController | Vista construye controles; controlador gestiona CRUD, búsqueda y selección | VistasMvcTest |
| Matrices | Servicios de calendarización y actividades | 24 horas; solapamiento de intervalos; nombre del funcionario | CorreccionesEnunciadoTest / OracleSocketIT |
| Consultas y reportes | Cuatro controladores de consulta / PanelReporte | La vista presenta datos; el controlador consulta y exporta; error deshabilita exportación anterior | ConsultasMvcTest |
| Extracción IA | ReservaController | Conservar el horario exacto al llenar, guardar y volver a seleccionar | VistasMvcTest |
| Permisos administrativos | BackendServer | Consultas y escrituras administrativas solo para administrador; listar categorías para ambos roles | SocketServicesIT / OracleSocketIT |
| Historial | Servicios de matrices y estadísticas | Incluir ACTIVA y FINALIZADA, excluir CANCELADA | CorreccionesEnunciadoTest / SocketServicesIT / OracleSocketIT |

Idioma español. Identificadores y fechas del dominio mantienen el formato de los
otros módulos. La selección y los botones usan navegación de teclado nativa.
Los diálogos siguen el patrón existente de Swing. Las alertas no son mensajes
transitorios: permanecen hasta descartarlas o terminar la sesión.
