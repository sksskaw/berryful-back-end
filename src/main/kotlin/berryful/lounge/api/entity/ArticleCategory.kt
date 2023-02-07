package berryful.lounge.api.entity

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "article_category")
@Entity
class ArticleCategory(

    @Id
    @Column(name = "id")
    var id: Int,

    @Column(name = "name")
    var name: String,

    @Column(name = "weight")
    var weight: Int?,
) : Serializable