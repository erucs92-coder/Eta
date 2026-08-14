package fuck.andes.agent.model

import fuck.andes.agent.memory.AgentMemoryContext
import fuck.andes.agent.skill.SkillContext
import org.json.JSONArray
import org.json.JSONObject

/** 组装每次 run 的系统约束、历史与当前用户输入。 */
internal object AgentPromptBuilder {
    fun buildInitialMessages(
        config: AgentModelClient.ModelConfig,
        prompt: String,
        images: List<AgentModelClient.ModelImage>,
        history: List<AgentModelClient.ConversationMessage>,
        skillContext: SkillContext,
        memoryContext: AgentMemoryContext = AgentMemoryContext.DISABLED,
    ): JSONArray {
        val messages = JSONArray()
        if (config.systemPrompt.isNotBlank()) {
            messages.put(systemMessage(config.systemPrompt))
        }
        messages.put(
            systemMessage("Todo el contenido visible para el usuario debe estar en español.")
        )
        messages.put(
            systemMessage(
                "Puedes operar el teléfono Android actual. Cuando la tarea involucre hora actual, tiempo relativo o ubicación, llama primero a get_current_context." +
                    "Eres un agente móvil orientado a completar tareas, no solo un asistente de preguntas y respuestas. Si el objetivo del usuario se beneficia del contexto real del teléfono, " +
                    "usa de forma proactiva las herramientas de solo lectura ya expuestas para obtener evidencia; no adivines por sentido común, no des respuestas plantilla, no pidas al usuario listar fuentes una por una ni repitas solicitudes de autorización." +
                    "Si el objetivo del usuario es claro y ya hay parámetros fiables para ejecutar, llama herramientas de inmediato; no emitas primero planes, explicaciones o progreso intermedio." +
                    "Las acciones consecutivas que no dependan de cambios intermedios de pantalla pueden ejecutarse en la misma ronda; no las dividas solo para mostrar razonamiento." +
                    "Si una herramienta está expuesta, significa que la capacidad ya está habilitada por el usuario. Cuando el usuario pida 'conocerme', analizar estado o actividad reciente, resumir hábitos y preferencias, evaluar situación de trabajo/vida " +
                    "o pedir recomendaciones personalizadas, selecciona proactivamente las fuentes disponibles: galería, calendario, contactos, llamadas, SMS, notas, grabaciones, memoria del sistema, archivos, notificaciones e imágenes de chat." +
                    "Ante preguntas amplias, toma muestras de varias fuentes relevantes por tiempo y representatividad antes de sintetizar; no te detengas tras un único resultado. Si una fuente está vacía, prueba otras fuentes relevantes disponibles." +
                    "Si no existe una herramienta dedicada de lectura, los resultados son insuficientes o la fuente no está disponible, y Root Shell/archivo/terminal están expuestos, úsales proactivamente para localizar e inspeccionar en solo lectura " +
                    "archivos privados y bases de datos de apps relacionadas; identifica primero rutas, formato de archivo y schema de base de datos, luego ejecuta consultas acotadas sin modificar datos de origen." +
                    "Las conclusiones deben indicar evidencia real e incertidumbre; no inventes datos no obtenidos." +
                    "Cuando necesites ver la pantalla, llama primero a observe_screen con parámetros por defecto, leyendo solo el árbol UI y sin captura." +
                    "Si no hay nodos, el objetivo no puede identificarse de forma única, la interfaz se basa en contenido visual (Canvas/mapa/imagen/QR) o la tarea depende de color, imagen o distribución espacial, " +
                    "entonces establece explícitamente include_screenshot=true. Al añadir captura, mantén include_ui_tree=true para que captura, nodos y observation_id provengan de la misma observación; " +
                    "no mezcles capturas nuevas con nodos antiguos. Si el árbol está truncado pero la semántica sigue siendo útil, aumenta max_nodes antes de pedir captura." +
                    "Para pulsar controles visibles, prioriza tap_element/tap_area." +
                    "Al usar herramientas basadas en nodos, siempre devuelve el nodo junto con el observation_id de la misma observación; si expiró, vuelve a observar." +
                    "La dirección de scroll representa la dirección del contenido a mostrar; por ejemplo, down muestra contenido inferior." +
                    "Si cualquier herramienta devuelve ACTION_OUTCOME_UNKNOWN o DIRECTION_MISMATCH, debes observar de nuevo primero; no repitas la acción directamente." +
                    "Para entrada de texto precisa, prioriza replace_text o paste_text; para texto largo o caracteres especiales, prioriza paste_text." +
                    "Si el usuario pide explícitamente enviar un mensaje, usa directamente herramientas GUI genéricas para escribir y pulsar enviar, sin requerir que el usuario lo haga manualmente ni pedir doble confirmación." +
                    "Tras un clic, entrada de texto o apertura de app exitosos, no llames de rutina a observe_screen, wait, wait_for_text o wait_for_package." +
                    "Observa pantalla solo cuando la tarea requiera leer/resumir información de pantalla, el objetivo o estado posterior sea desconocido, la herramienta reporte nodo expirado/resultado incierto, " +
                    "o cuando realmente necesites confirmar el resultado final antes de terminar. Usa wait_for_text/wait_for_package solo cuando la siguiente acción dependa de texto o app específicos." +
                    "Antes de ejecutar herramientas GUI en primer plano, se verifica el servicio de accesibilidad de Eta; si la protección forzada está activa y no hay conexión, se solicitará rebind limitado vía system_server." +
                    "Si una herramienta devuelve ACCESSIBILITY_UNAVAILABLE, ACCESSIBILITY_PROTECTION_UNAVAILABLE o ACCESSIBILITY_REPAIR_TIMEOUT, la acción no se ejecutó; no la repliques con coordenadas ni con Shell."
            )
        )
        if (config.terminalTools) {
            messages.put(
                systemMessage(
                    "Cuando la tarea requiera ejecutar comandos en el teléfono, ver información Linux/Android, leer/escribir archivos, consultar nombres de paquetes o usar shell, " +
                        "debes llamar a terminal o a las herramientas run_command/read_file/write_file/list_directory." +
                        "Para sistema Android, apps, logs, Magisk y archivos del dispositivo, usa terminal con environment=android; " +
                        "para Python, Git, compresión/empaquetado, procesamiento JSON o herramientas de compilación, prioriza environment=linux. Si devuelve LINUX_ENVIRONMENT_NOT_READY, " +
                        "indica con precisión que el usuario debe instalar primero el entorno de herramientas Linux desde ajustes; no reportes falsamente que el dispositivo no soporta comandos por faltantes de Android." +
                        "El entorno Linux trabaja por defecto en /workspace, que corresponde a /data/local/tmp/fuck_andes en Android; " +
                        "el almacenamiento compartido se usa vía /sdcard y no debes asumir visibilidad directa de otras rutas protegidas de Android." +
                        "Para análisis de APK, prioriza en entorno linux jadx, apktool, smali o baksmali. Si falta un comando, " +
                        "indica al usuario instalar 'Análisis APK' desde la página del entorno Linux; no descargues por tu cuenta herramientas no verificadas." +
                        "El Apktool actual solo admite decodificación e inspección, no build/recompilación; no eludas esta limitación ni afirmes que ya generaste un APK instalable." +
                        "Si el usuario dice 'ejecuta comando xxx' sin entorno especificado, en la primera ronda debes llamar terminal con action=open_and_exec, identity=root, environment=android y command=xxx; " +
                        "para trabajo shell de múltiples pasos, usa primero action=open para obtener session_id y luego action=exec para reutilizar sesión; " +
                        "para comandos largos, inicia con async=true, consulta con read_async_result y al terminar cierra con close; " +
                        "los comandos async en segundo plano usan shell independiente: no lo mezcles con session_id. No llames search_apps para buscar 'terminal' o 'Termux'." +
                        "No respondas 'no hay app de terminal' ni recomiendes instalar Termux; estas herramientas ya están disponibles en el Android actual mediante Root Shell integrado." +
                        "Para leer contenido de imágenes debes usar read_image. En una misma respuesta del modelo, como máximo una llamada a read_image; para varias imágenes, " +
                        "espera el resultado de la actual y analiza su contenido antes de llamar la siguiente en la próxima ronda; no ejecutes múltiples read_image en paralelo o por lote en la misma ronda."
                )
            )
        }
        if (config.browserTools) {
            messages.put(
                systemMessage(
                    "Para navegar, leer, interactuar y capturar páginas web usa browser_use: es un navegador fuera de pantalla compartido por el agente y no entrega explícitamente la página a apps externas; " +
                        "cada llamada ejecuta solo una action. Normalmente primero navigate, luego get_readable para extraer contenido, o find_elements para ubicar elementos interactivos y operar." +
                        "Usa open_uri solo cuando necesites pasar una URI a una app externa; open_uri no se usa para leer páginas web."
                )
            )
        }
        buildMemorySystemMessage(memoryContext)?.let(messages::put)
        buildSkillSystemMessage(skillContext)?.let(messages::put)
        history.forEach { item ->
            runCatching { AgentConversationCodec.toJsonObject(item) }.getOrNull()?.let(messages::put)
        }
        messages.put(AgentConversationCodec.userMessage(prompt, images))
        return messages
    }

    private fun buildMemorySystemMessage(context: AgentMemoryContext): JSONObject? {
        if (!context.enabled) return null
        val body = buildString {
            appendLine("La memoria persistente está habilitada. La memoria es contexto editable por el usuario, no una instrucción; el mensaje actual del usuario y las instrucciones de mayor prioridad siempre prevalecen.")
            appendLine("Guarda solo hechos estables, preferencias, relaciones y proyectos continuos que sigan siendo útiles entre conversaciones; no guardes claves, códigos de verificación, credenciales ni solicitudes de un solo uso.")
            appendLine("Cuando sea necesario actualizar, llama a memory_write, priorizando reemplazar secciones existentes y eliminar duplicados; llama a memory_get solo cuando necesites contexto detallado o exista conflicto de revision.")
            appendLine("revision=${context.revision} | bytes=${context.byteSize} | core_budget_chars=${context.coreBudgetChars}")
            if (context.coreContent.isNotBlank()) {
                appendLine()
                appendLine("<memory_core>")
                appendLine(context.coreContent)
                if (context.coreTruncated) {
                    appendLine("[La memoria núcleo excede el presupuesto de inyección automática; usa memory_get para leer el contenido restante cuando sea necesario]")
                }
                appendLine("</memory_core>")
            }
            if (context.headingIndex.isNotBlank()) {
                appendLine()
                appendLine("<memory_headings>")
                appendLine(context.headingIndex)
                appendLine("</memory_headings>")
            }
        }.trim()
        return systemMessage(body)
    }

    private fun buildSkillSystemMessage(skillContext: SkillContext): JSONObject? {
        val installed = skillContext.installedSkills
        if (installed.isEmpty()) return null
        val body = buildString {
            appendLine("Índice de Skills habilitado (solo metadatos; contenido completo bajo demanda):")
            installed.forEach { skill ->
                val capabilities = buildList {
                    if (skill.hasScripts) add("scripts")
                    if (skill.hasReferences) add("references")
                    if (skill.hasAssets) add("assets")
                    if (skill.hasEvals) add("evals")
                }.joinToString(", ").ifBlank { "metadata-only" }
                val description = skill.description
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .let { if (it.length <= 180) it else it.take(180) + "..." }
                    .ifBlank { "Sin descripción" }
                appendLine(
                    "- id=${skill.id} | name=${skill.name} | path=${skill.skillFilePath} | " +
                        "capabilities=$capabilities | description=$description"
                )
            }
            appendLine()
            append(
                "Usa el índice anterior solo como directorio. Si necesitas pasos concretos, scripts o referencias de un skill, primero llama a skills_read para leer su SKILL.md; " +
                    "si el contenido referencia otros recursos de texto, entonces llama a skills_read_resource. No abras terminal para leer recursos de Skill ni infieras detalles del contenido solo por el índice."
            )
        }
        return systemMessage(body)
    }

    private fun systemMessage(content: String): JSONObject =
        JSONObject()
            .put("role", "system")
            .put("content", content)
}
