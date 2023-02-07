package berryful.lounge.api.entity

import javax.persistence.*

/**
 *  금지어 테이블
 *  @author sumin Hong
 *  @version 2.0
 */
@Table(name = "forbidden_word")
@Entity
class ForbiddenWord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "word", unique = true)
    var word: String,
)