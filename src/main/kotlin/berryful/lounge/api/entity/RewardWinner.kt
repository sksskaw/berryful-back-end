package berryful.lounge.api.entity

import javax.persistence.*

@Table(name = "reward_winner")
@Entity
class RewardWinner(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_reward_id")
    var challengeReward: ChallengeReward,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    var article: Article,
) : BaseEntity()