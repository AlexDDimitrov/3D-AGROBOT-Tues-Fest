package ui.wizard

data class ReportData(
    val id: Int,
    val plantType: String?,
    val health: String,
    val issues: List<String>,
    val recommendations: List<String>,
    val receivedAt: String?,
    val hasPlant: Boolean
)