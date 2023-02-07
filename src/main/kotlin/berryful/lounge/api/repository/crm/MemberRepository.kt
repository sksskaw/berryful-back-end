package berryful.lounge.api.repository.crm

import berryful.lounge.api.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByPhoneNumber(phoneNumber: String): Member?
    fun findByNickname(nickname: String): Member?
    fun findByUppercaseNickname(uppercaseNickname: String): Member?
    fun findByEmail(email: String): Member?
    fun findBySnsKakao(snsKakao: String): Member?
    fun findBySnsFacebook(snsFacebook: String): Member?
    fun findBySnsApple(snsApple: String): Member?
    fun findBySnsNaver(snsNaver: String): Member?

    fun findAllByPhoneNumber(phoneNumber: String): List<Member>

    @Transactional
    @Modifying
    @Query(value = "UPDATE `member` m SET m.notification_check = true WHERE m.id = :memberId", nativeQuery = true)
    fun notificationCheck(@Param("memberId") memberId: Long)


    @Transactional
    @Modifying
    @Query(
        value = "INSERT IGNORE INTO member ( create_at, update_at, notification_check," +
                "email, nickname, uppercase_nickname, status, profile_path, birthday, agree_personal_info, agree_service, agree_marketing, " +
                "sns_email, sns_phone_number, phone_number, gender, sns_apple, sns_facebook, sns_kakao, sns_naver, sns_type) " +
                "VALUES (NOW(6), NOW(6), 1, NULL, NULL, NULL, 'ACTIVE', NULL, :birthday, 0,0,0, :snsEmail, :snsPhoneNumber, :phoneNumber, :gender," +
                "        :snsApple, :snsFacebook, :snsKakao, :snsNaver, :snsType)",
        nativeQuery = true
    )
    fun insertSnsMenber(
        @Param("birthday") birthday: String?,
        @Param("snsEmail") snsEmail: String?,
        @Param("snsPhoneNumber") snsPhoneNumber: String?,
        @Param("phoneNumber") phoneNumber: String?,
        @Param("gender") gender: String?,
        @Param("snsApple") snsApple: String?,
        @Param("snsFacebook") snsFacebook: String?,
        @Param("snsKakao") snsKakao: String?,
        @Param("snsNaver") snsNaver: String?,
        @Param("snsType") snsType: String?,
    ) : Int
}