package berryful.lounge.api.aop

import berryful.lounge.api.utils.Log
import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class TimeTraceAop {
    private val log = KotlinLogging.logger {}

    /*@Around("execution(* berryful.lounge.api.service.lounge..*(..))")
    fun loungeExecute(joinPoint: ProceedingJoinPoint): Any {
        val start = System.currentTimeMillis()
        try {
            return joinPoint.proceed()
        } finally {
            val timeMs = System.currentTimeMillis() - start
            //log.info(joinPoint.toString() +" time trace: $timeMs" + "ms")
        }
    }

    @Around("execution(* berryful.lounge.api.service.crm..*(..))")
    fun crmExecute(joinPoint: ProceedingJoinPoint): Any {
        val start = System.currentTimeMillis()
        try {
            return joinPoint.proceed()
        } finally {
            val timeMs = System.currentTimeMillis() - start
            //log.info(joinPoint.toString() +" time trace: $timeMs" + "ms")
        }
    }*/
}