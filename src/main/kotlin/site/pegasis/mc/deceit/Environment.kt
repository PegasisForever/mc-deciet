package site.pegasis.mc.deceit

object Environment {
    fun hook() {
        GameState.addListener(GameEvent.START) {
            consoleCommand("time set midnight")
        }
    }
}
