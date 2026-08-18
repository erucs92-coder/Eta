package fuck.andes.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fuck.andes.R
import fuck.andes.ui.model.PermissionStatusUi
import fuck.andes.ui.model.RunStatusUi
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.ui.res.painterResource
import com.composables.icons.lucide.R as LucideR
import top.yukonga.miuix.kmp.theme.MiuixTheme

// Colores semánticos de estado.
val StatusSuccess = Color(0xFF00BD13)
val StatusWarning = Color(0xFFFFB200)
val StatusError: Color @Composable get() = MiuixTheme.colorScheme.error
val StatusRunning: Color @Composable get() = MiuixTheme.colorScheme.primary
val StatusIdle: Color @Composable get() = MiuixTheme.colorScheme.onSurfaceVariantSummary

// Paleta de tint para iconos (alineada con Ajustes de ColorOS).
// No sustituir con variantes coui_color_*_variant ni aproximaciones Material/iOS.
val IconTintBlue = Color(0xFF0066FF)
val IconTintGreen = Color(0xFF00BD13)
val IconTintPurple = Color(0xFF0066FF)
val IconTintOrange = Color(0xFFFF7700)

// ── Mapeo de RunStatusUi ───────────────────────────────────────────────

@Composable
fun RunStatusUi.color(): Color = when (this) {
    RunStatusUi.Running -> StatusRunning
    RunStatusUi.Success -> StatusSuccess
    RunStatusUi.Failed -> StatusError
    RunStatusUi.Cancelled -> StatusIdle
}

@Composable
fun RunStatusUi.label(): String = when (this) {
    RunStatusUi.Running -> stringResource(R.string.run_status_running)
    RunStatusUi.Success -> stringResource(R.string.run_status_success)
    RunStatusUi.Failed -> stringResource(R.string.run_status_failed)
    RunStatusUi.Cancelled -> stringResource(R.string.run_status_cancelled)
}

// ── Mapeo de PermissionStatusUi ────────────────────────────────────────

@Composable
fun PermissionStatusUi.color(): Color = when (this) {
    PermissionStatusUi.Available -> StatusIdle
    PermissionStatusUi.Warning -> StatusWarning
    PermissionStatusUi.Missing -> StatusError
    PermissionStatusUi.Disabled -> StatusIdle
}

@Composable
fun PermissionStatusUi.label(): String = when (this) {
    PermissionStatusUi.Available -> stringResource(R.string.permission_status_ready)
    PermissionStatusUi.Warning -> stringResource(R.string.permission_status_attention)
    PermissionStatusUi.Missing -> stringResource(R.string.permission_status_not_authorized)
    PermissionStatusUi.Disabled -> stringResource(R.string.permission_status_disabled)
}

// ── Componentes UI compartidos ─────────────────────────────────────────

/** Icono con tint de color para el lado izquierdo de ítems de lista. */
@Composable
fun TintedIcon(
    icon: ImageVector,
    tint: Color,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.padding(end = 16.dp).size(24.dp),
        tint = tint,
    )
}

/** Separador en Card con sangría alineada al texto de BasicComponent. */
@Composable
fun PrefDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
    )
}

/**
 * Ítem de lista con flecha; mantiene 2dp entre título y resumen.
 * Úsalo cuando se requiera título + resumen + flecha a la derecha.
 */
@Composable
fun ArrowItem(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable RowScope.() -> Unit = {},
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    BasicComponent(
        modifier = modifier,
        insideMargin = PaddingValues(16.dp),
        startAction = startAction,
        endActions = {
            Row(
                modifier = Modifier.padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                endActions()
            }
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        },
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            text = title,
            fontSize = MiuixTheme.textStyles.headline1.fontSize,
            fontWeight = FontWeight.Medium,
            color = if (enabled) MiuixTheme.colorScheme.onBackground
                else MiuixTheme.colorScheme.disabledOnSurface,
        )
        if (summary != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = summary,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = if (enabled) MiuixTheme.colorScheme.onSurfaceVariantSummary
                    else MiuixTheme.colorScheme.disabledOnSurface,
            )
        }
    }
}
