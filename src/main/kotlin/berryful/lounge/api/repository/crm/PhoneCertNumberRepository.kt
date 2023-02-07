package berryful.lounge.api.repository.crm;

import berryful.lounge.api.entity.PhoneCertNumber
import org.springframework.data.jpa.repository.JpaRepository

interface PhoneCertNumberRepository : JpaRepository<PhoneCertNumber, Long> {
    fun findFirstByPhoneNumberOrderByIdDesc(phoneNumber : String): PhoneCertNumber?
}