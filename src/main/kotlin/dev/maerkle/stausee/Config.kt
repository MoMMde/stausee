package dev.maerkle.stausee

import dev.schlaubi.envconf.getEnv

object Config {
    val STORAGE_PATH by getEnv()
}