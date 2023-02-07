package berryful.lounge.api.utils

import mu.KotlinLogging

class Log {
    companion object {
        private val log = KotlinLogging.logger {}
        fun out(module: String, msg: String) {
            log.info("berryful ($module) - $msg")
        }
    }
}

class WarnLog {
    companion object {
        private val log = KotlinLogging.logger {}
        fun out(module: String, msg: String) {
            log.warn("berryful ($module) - $msg")
        }
    }
}