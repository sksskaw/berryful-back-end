package berryful.lounge.api.repository.crm

import berryful.lounge.api.entity.EmailCertNumber
import berryful.lounge.api.entity.PhoneCertNumber
import org.springframework.data.jpa.repository.JpaRepository

interface EmailCertNumberRepository : JpaRepository<EmailCertNumber, Long> {
    fun findFirstByEmailOrderByIdDesc(email : String): EmailCertNumber?
}