package berryful.lounge.api.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Table(name = "search_keyword")
@Entity
class SearchKeyword(
    @Column(name = "keyword", columnDefinition = "TEXT")
    var keyword: String? = null,
): BaseEntity()