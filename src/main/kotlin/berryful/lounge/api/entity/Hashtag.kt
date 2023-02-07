package berryful.lounge.api.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Table(name = "hashtag")
@Entity
class Hashtag(

    @Column(name = "tagname", nullable = false, unique = true)
    var tagname: String,

    @Column(name = "count")
    var count: Long? = 1L,

) : BaseEntity()