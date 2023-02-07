package berryful.lounge.api.repository.crm

import berryful.lounge.api.entity.ForbiddenWord
import org.springframework.data.jpa.repository.JpaRepository

interface ForbiddenWordRepository : JpaRepository<ForbiddenWord, Long> {
}