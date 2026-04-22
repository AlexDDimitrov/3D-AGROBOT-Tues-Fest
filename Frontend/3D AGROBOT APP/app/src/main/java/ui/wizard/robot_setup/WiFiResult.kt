package ui.wizard.robot_setup

sealed class WifiResult {
    object Sending : WifiResult()
    object Sent : WifiResult()
    object Failed : WifiResult()
}