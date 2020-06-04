package site.pegasis.mc.deceit

object Environment {
    fun hook() {
        GameState.addListener(GameEvent.DARK) {
            consoleCommand("time set midnight")
        }
        GameState.addListener(GameEvent.LIGHT) {
            consoleCommand("time set day")
        }
    }
}
