package berryful.lounge.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurerSupport
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurerSupport() {
    @Bean(name = ["mailSendExecutor"])
    fun getMailSendExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 10
        executor.setQueueCapacity(10)
        executor.setThreadNamePrefix("mailSendExecutor-")
        executor.initialize()
        return executor
    }

    @Bean(name = ["deleteMemberS3FilesExecutor"])
    fun deleteMemberS3FilesExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.setThreadNamePrefix("deleteMemberS3FilesExecutor")
        executor.initialize()
        return executor
    }
}