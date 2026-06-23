package com.focusflow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingStep(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val description: String
)

private val onboardingSteps = listOf(
    OnboardingStep(
        icon = Icons.Default.Star,
        iconTint = Color(0xFFF59E0B),
        title = "欢迎使用 FocusFlow",
        description = "这是一款帮助你制定学习计划、追踪进度、保持学习习惯的应用。所有数据都保存在你的手机上，无需联网。"
    ),
    OnboardingStep(
        icon = Icons.Default.List,
        iconTint = Color(0xFF4F46E5),
        title = "创建学习计划",
        description = "点击底部\"计划\"标签 → 点击右下角 + 按钮 → 输入计划名称。进入计划后，可以添加里程碑和具体任务。"
    ),
    OnboardingStep(
        icon = Icons.Default.DateRange,
        iconTint = Color(0xFF8B5CF6),
        title = "周计划与日计划",
        description = "在计划页面顶部，有\"周计划\"和\"日计划\"快捷按钮。周计划查看本周安排，日计划查看今天要完成的任务。"
    ),
    OnboardingStep(
        icon = Icons.Default.PlayArrow,
        iconTint = Color(0xFF10B981),
        title = "学习计时",
        description = "点击底部\"计时\"标签 → 点击开始按钮。计时会以后台服务运行，即使锁屏也不会中断。每天学习≥5分钟算一天。"
    ),
    OnboardingStep(
        icon = Icons.Default.Refresh,
        iconTint = Color(0xFFEF4444),
        title = "复习打卡",
        description = "点击底部\"复习\"标签查看待复习任务。完成复习后点击\"标记已复习\"，系统会按间隔（1天→3天→7天→14天→30天）安排下一次复习。"
    ),
    OnboardingStep(
        icon = Icons.Default.Favorite,
        iconTint = Color(0xFFEF4444),
        title = "连续天数 (Streak)",
        description = "每天学习≥5分钟，连续天数+1。可以设置\"冻结天数\"，允许中断几天而不归零。在设置页面可以调整。"
    ),
    OnboardingStep(
        icon = Icons.Default.Home,
        iconTint = Color(0xFF3B82F6),
        title = "仪表盘",
        description = "首页显示连续天数、今日学习时长、任务完成情况和16周学习热力图。点击快捷卡片可直接跳转到日计划和周计划。"
    ),
    OnboardingStep(
        icon = Icons.Default.Person,
        iconTint = Color(0xFF6B7280),
        title = "设置与备份",
        description = "在\"我的\"页面可以设置主题、每日目标、通知开关。数据导出功能可以将所有数据备份为JSON文件，导入可恢复数据。"
    )
)

@Composable
fun OnboardingGuide(
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val step = onboardingSteps[currentStep]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${currentStep + 1}/${onboardingSteps.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(step.iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        step.icon,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = step.iconTint
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Title
                Text(
                    step.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // Description
                Text(
                    step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(24.dp))

                // Progress dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    onboardingSteps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("上一步")
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (currentStep < onboardingSteps.size - 1) {
                        Button(onClick = { currentStep++ }) {
                            Text("下一步")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Button(onClick = onDismiss) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("开始使用")
                        }
                    }
                }
            }
        }
    }
}
