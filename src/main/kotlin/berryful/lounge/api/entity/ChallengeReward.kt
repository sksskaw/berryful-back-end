package berryful.lounge.api.entity

import javax.persistence.*

/**
 *  챌린지 수상, 리워드 정보
 *  @author Taehoon Kim
 *  @version 2.0
 */
@Table(name = "challenge_reward")
@Entity
class ChallengeReward(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    var article: Article,

    @Column(name = "prize")
    var prize: String? = null,

    @Column(name = "reward")
    var reward: String? = null,

    @Column(name = "number_of_winners")
    var numberOfWinners: Int? = null,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "challengeReward")
    var rewardWinnerList: MutableList<RewardWinner> = mutableListOf(),
) : BaseEntity()