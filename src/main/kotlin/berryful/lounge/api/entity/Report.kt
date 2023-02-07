package berryful.lounge.api.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Table(name = "report")
@Entity
class Report(

    @Column(name = "article_id")
    var articleId: Long? = null,

    @Column(name = "member_id")
    var memberId: Long? = null,

    // 1: 도배, 2: 광고/홍보, 3: 비방/욕설, 4: 음란/선정성, 5: 기타
    @Column(name="report_type")
    var reportType: Int? = null,

    @Column(name="report_description")
    var reportDescription: String? = null,

): BaseEntity()