package berryful.lounge.api.repository.lounge.article.impl

import berryful.lounge.api.data.*
import berryful.lounge.api.entity.*
import berryful.lounge.api.entity.QInterestedArticle.interestedArticle
import berryful.lounge.api.entity.QMember.member
import berryful.lounge.api.entity.QThumbsUp.thumbsUp
import berryful.lounge.api.entity.QMemberFollow.memberFollow
import berryful.lounge.api.entity.QClipReadCheck.clipReadCheck
import berryful.lounge.api.repository.lounge.article.ArticleDslRepository
import berryful.lounge.api.repository.support.QuerydslCustomRepositorySupport
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

class ArticleDslRepositoryImpl(
    private val challenge: QArticle = QArticle("challenge"),
    private val post: QArticle = QArticle("post"),
    private val clip: QArticle = QArticle("clip"),
    private val comment: QArticle = QArticle("comment"),
    private val reply: QArticle = QArticle("reply"),

    private val blockingMember: QBlockedMember = QBlockedMember("blockingMember"),
    private val blockerMember: QBlockedMember = QBlockedMember("blockerMember")
) : QuerydslCustomRepositorySupport(Article::class.java), ArticleDslRepository {
    override fun findAllPost(pageable: Pageable, memberId: Long, params: PostParams): Page<PostRes> {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .leftJoin(blockingMember).on(post.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(post.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                categoryEq(post, params.category),
                keywordMatch(post, params.keyword),
                hashtagLike(post, params.hashtag),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(orderByPost(params.sort))

        val postCountQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .leftJoin(blockingMember).on(post.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(post.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                categoryEq(post, params.category),
                keywordMatch(post, params.keyword),
                hashtagLike(post, params.hashtag),
            )

        val posts = postQuery.fetch()
        val postIds = posts.stream().map { post -> post.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.`in`(postIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(postIds + clipIds))
        val interestedArticleQuery = selectFrom(interestedArticle)
            .where(interestedArticle.member.id.eq(memberId), interestedArticle.article.id.`in`(postIds))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(memberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val interestedArticles = interestedArticleQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        val result = postsConvertToPostRes(posts, thumbsUps, interestedArticles, clips, clipReadChecks, memberId)
        return PageImpl(result, pageable, postCountQuery.fetch().size.toLong())
    }

    override fun findAllPost(pageable: Pageable, params: PostParams): Page<PostRes> {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                categoryEq(post, params.category),
                keywordMatch(post, params.keyword),
                hashtagLike(post, params.hashtag),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(orderByPost(params.sort))

        val postCountQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                categoryEq(post, params.category),
                keywordMatch(post, params.keyword),
                hashtagLike(post, params.hashtag),
            )

        val posts = postQuery.fetch()
        val postIds = posts.stream().map { post -> post.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .where(
                clip.parentId.`in`(postIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()

        val result = postsConvertToPostRes(posts, null, null, clips, null, 0)
        return PageImpl(result, pageable, postCountQuery.fetch().size.toLong())
    }

    override fun findPost(id: Long, memberId: Long): PostRes? {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .leftJoin(blockingMember).on(post.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(post.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                post.id.eq(id),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )

        val post = postQuery.fetchOne()
            ?: return null

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.`in`(post.id),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(mutableListOf(post.id) + clipIds))
        val interestedArticleQuery = selectFrom(interestedArticle)
            .where(interestedArticle.member.id.eq(memberId), interestedArticle.article.id.`in`(post.id))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(memberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val interestedArticles = interestedArticleQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        return postConvertToPostRes(post, thumbsUps, interestedArticles, clips, clipReadChecks, memberId)
    }

    override fun findPost(id: Long): PostRes? {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(
                post.id.eq(id),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )

        val post = postQuery.fetchOne()
            ?: return null

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .where(
                clip.parentId.`in`(post.id),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()

        return postConvertToPostRes(post, null, null, clips, null, 0L)
    }

    override fun findAllInterestedPost(pageable: Pageable, memberId: Long, params: InterestedPostParams): Page<PostRes> {
        val interestedPostQuery = select(post)
            .from(member)
            .join(interestedArticle).on(member.id.eq(interestedArticle.member.id))
            .join(post).on(interestedArticle.article.id.eq(post.id))
            .leftJoin(blockingMember).on(post.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(post.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                member.id.eq(memberId),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                categoryEq(post, params.category),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(orderByPost(params.sort))

        val interestedPostCountQuery = select(post)
            .from(member)
            .join(interestedArticle).on(member.id.eq(interestedArticle.member.id))
            .join(post).on(interestedArticle.article.id.eq(post.id))
            .leftJoin(blockingMember).on(post.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(post.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                member.id.eq(memberId),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                categoryEq(post, params.category),
            )


        val interestedPosts = interestedPostQuery.fetch()
        val postIds = interestedPosts.stream().map { post -> post.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.`in`(postIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(postIds + clipIds))
        val interestedArticleQuery = selectFrom(interestedArticle)
            .where(interestedArticle.member.id.eq(memberId), interestedArticle.article.id.`in`(postIds))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(memberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val interestedArticles = interestedArticleQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        val result = postsConvertToPostRes(interestedPosts, thumbsUps, interestedArticles, clips, clipReadChecks, memberId)
        return PageImpl(result, pageable, interestedPostCountQuery.fetch().size.toLong())
    }

    override fun findAllLikedPostList(pageable: Pageable, memberId: Long): Page<PostRes> {
        val likedPostQuery = select(post)
            .from(thumbsUp)
            .join(post).on(thumbsUp.article.id.eq(post.id))
            .join(post.member, member).fetchJoin()
            .where(
                thumbsUp.member.id.eq(memberId),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(post.createAt.desc())

        val interestedPostCountQuery = select(post)
            .from(thumbsUp)
            .join(post).on(thumbsUp.article.id.eq(post.id))
            .join(post.member, member).fetchJoin()
            .where(
                thumbsUp.member.id.eq(memberId),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )

        val likedPosts = likedPostQuery.fetch()
        val postIds = likedPosts.stream().map { post -> post.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.`in`(postIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(postIds + clipIds))
        val interestedArticleQuery = selectFrom(interestedArticle)
            .where(interestedArticle.member.id.eq(memberId), interestedArticle.article.id.`in`(postIds))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(memberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val interestedArticles = interestedArticleQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        val result = postsConvertToPostRes(likedPosts, thumbsUps, interestedArticles, clips, clipReadChecks, memberId)
        return PageImpl(result, pageable, interestedPostCountQuery.fetch().size.toLong())
    }

    override fun findAllUploadedPost(pageable: Pageable, targetMemberId: Long, loginMemberId: Long, sort: String): Page<PostRes> {
        val uploadedPostQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(
                post.member.id.eq(targetMemberId),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(orderByPost(sort))

        val uploadedPostCountQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(
                post.member.id.eq(targetMemberId),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )

        val uploadedPosts = uploadedPostQuery.fetch()
        val postIds = uploadedPosts.stream().map { post -> post.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(loginMemberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(loginMemberId))
            .where(
                clip.parentId.`in`(postIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(loginMemberId), thumbsUp.article.id.`in`(postIds + clipIds))
        val interestedArticleQuery = selectFrom(interestedArticle)
            .where(interestedArticle.member.id.eq(loginMemberId), interestedArticle.article.id.`in`(postIds))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(loginMemberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val interestedArticles = interestedArticleQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        val result = postsConvertToPostRes(uploadedPosts, thumbsUps, interestedArticles, clips, clipReadChecks, loginMemberId)
        return PageImpl(result, pageable, uploadedPostCountQuery.fetch().size.toLong())
    }

    override fun findAllUploadedPost(pageable: Pageable, targetMemberId: Long, sort: String): Page<PostRes> {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(
                post.member.id.eq(targetMemberId),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(orderByPost(sort))

        val postCountQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .where(
                post.member.id.eq(targetMemberId),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )

        val posts = postQuery.fetch()
        val postIds = posts.stream().map { post -> post.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .where(
                clip.parentId.`in`(postIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()


        val result = postsConvertToPostRes(posts, null, null, clips, null, 0)
        return PageImpl(result, pageable, postCountQuery.fetch().size.toLong())
    }

    override fun findAllUploadedClip(pageable: Pageable, targetMemberId: Long, loginMemberId: Long): Page<Any> {
        val uploadedClipQuery = select(QPostClip(post, clip)).from(clip)
            .join(post).on(clip.parentId.eq(post.id))
            .join(post.member, member).fetchJoin()
            .where(
                clip.member.id.eq(targetMemberId),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(clip.createAt.desc())

        val uploadedClipCountQuery = select(QPostClip(post, clip)).from(clip)
            .join(post).on(clip.parentId.eq(post.id))
            .join(post.member, member).fetchJoin()
            .where(
                clip.member.id.eq(targetMemberId),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
            )

        val uploadedClips = uploadedClipQuery.fetch()
        val clipIds = uploadedClips.stream().map { it.clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(loginMemberId), thumbsUp.article.id.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()

        val result = postClipsConvertToClipResOrChallengeClipRes(uploadedClips, thumbsUps, loginMemberId)
        return PageImpl(result, pageable, uploadedClipCountQuery.fetch().size.toLong())
    }

    override fun findAllUploadedClip(pageable: Pageable, targetMemberId: Long): Page<Any> {
        val uploadedClipQuery = select(QPostClip(post, clip)).from(clip)
            .join(post).on(clip.parentId.eq(post.id))
            .join(post.member, member).fetchJoin()
            .where(
                clip.member.id.eq(targetMemberId),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(clip.createAt.desc())

        val uploadedClipCountQuery = select(QPostClip(post, clip)).from(clip)
            .join(post).on(clip.parentId.eq(post.id))
            .join(post.member, member).fetchJoin()
            .where(
                clip.member.id.eq(targetMemberId),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
            )

        val uploadedClips = uploadedClipQuery.fetch()
        val result = postClipsConvertToClipResOrChallengeClipRes(uploadedClips, null, 0)
        return PageImpl(result, pageable, uploadedClipCountQuery.fetch().size.toLong())
    }

    override fun findAllClip(postId: Long, memberId: Long): List<Article> {
        val query = select(clip)
            .from(post)
            .join(clip).on(post.id.eq(clip.parentId))
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                post.id.eq(postId),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
            .orderBy(clip.createAt.desc())

        return query.fetch()
    }

    override fun findAllClip(pageable: Pageable, memberId: Long): Page<Any> {
        val parent = QArticle("parent")
        val clipsQuery = select(QClipWithParent(parent, clip))
            .from(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(parent).on(parent.id.eq(clip.parentId))
            .leftJoin(parent.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                parent.status.isNull.or(parent.status.eq(ArticleStatus.UNBLOCKED)),
                (member.isNull.or(parent.member.status.eq(MemberStatus.ACTIVE))),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(clip.createAt.desc())

        val clipsCountQuery = select(QClipWithParent(parent, clip))
            .from(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(parent).on(parent.id.eq(clip.parentId))
            .leftJoin(parent.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                parent.status.isNull.or(parent.status.eq(ArticleStatus.UNBLOCKED)),
                (member.isNull.or(parent.member.status.eq(MemberStatus.ACTIVE))),
            )

        val clips = clipsQuery.fetch()
        val clipIds = clips.stream().map { it.clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(clipIds))
        val thumbsUps = thumbsUpQuery.fetch()

        val result = clipWithParentConvertToClipResOrChallengeClipRes(clips, thumbsUps, memberId)
        return PageImpl(result, pageable, clipsCountQuery.fetch().size.toLong())
    }

    override fun findAllClip(pageable: Pageable): Page<Any> {
        val parent = QArticle("parent")
        val clipsQuery = select(QClipWithParent(parent, clip))
            .from(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(parent).on(parent.id.eq(clip.parentId))
            .leftJoin(parent.member, member).fetchJoin()
            .where(
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                parent.status.isNull.or(parent.status.eq(ArticleStatus.UNBLOCKED)),
                (member.isNull.or(parent.member.status.eq(MemberStatus.ACTIVE))),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(clip.createAt.desc())

        val clipsCountQuery = select(QClipWithParent(parent, clip))
            .from(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(parent).on(parent.id.eq(clip.parentId))
            .leftJoin(parent.member, member).fetchJoin()
            .where(
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                parent.status.isNull.or(parent.status.eq(ArticleStatus.UNBLOCKED)),
                (member.isNull.or(parent.member.status.eq(MemberStatus.ACTIVE))),
            )

        val clips = clipsQuery.fetch()

        val result = clipWithParentConvertToClipResOrChallengeClipRes(clips, null, 0L)
        return PageImpl(result, pageable, clipsCountQuery.fetch().size.toLong())
    }

    override fun findAllLikedClipList(pageable: Pageable, memberId: Long): Page<Any> {
        val likedClipQuery = select(QPostClip(post, clip))
            .from(thumbsUp)
            .join(clip).on(thumbsUp.article.id.eq(clip.id))
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .join(post).on(post.id.eq(clip.parentId))
            .join(post.member, member).fetchJoin()
            .where(
                thumbsUp.member.id.eq(memberId),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(clip.createAt.desc())

        val likedClipCountQuery = select(QPostClip(post, clip))
            .from(thumbsUp)
            .join(clip).on(thumbsUp.article.id.eq(clip.id))
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .join(post).on(post.id.eq(clip.parentId))
            .join(post.member, member).fetchJoin()
            .where(
                thumbsUp.member.id.eq(memberId),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
            )

        val likedClips = likedClipQuery.fetch()
        val clipIds = likedClips.stream().map { it.clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(clipIds))
        val thumbsUps = thumbsUpQuery.fetch()

        val result = postClipsConvertToClipResOrChallengeClipRes(likedClips, thumbsUps, memberId)
        return PageImpl(result, pageable, likedClipCountQuery.fetch().size.toLong())
    }

    override fun findHotPost(params: PostParams): PostRes? {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .join(clip).on(post.id.eq(clip.parentId)).fetchJoin()
            .where(
                post.member.id.ne(152),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                post.createAt.goe(Instant.now().minus(7, ChronoUnit.DAYS)),
                post.createAt.lt(Instant.now()),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                categoryEq(post, params.category),
            )
            .groupBy(post.id)
            .orderBy(clip.thumbsCount.sum().desc(), clip.id.count().desc())

        val hotPost = postQuery.fetchFirst()
            ?: return null
        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .where(
                clip.parentId.eq(hotPost.id),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()

        return postConvertToPostRes(hotPost, null, null, clips, null, 0L)
    }

    override fun findHotPost(params: PostParams, memberId: Long): PostRes? {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .leftJoin(blockingMember).on(post.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(post.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .join(clip).on(post.id.eq(clip.parentId)).fetchJoin()
            .where(
                post.member.id.ne(152),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                post.createAt.goe(Instant.now().minus(7, ChronoUnit.DAYS)),
                post.createAt.lt(Instant.now()),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                categoryEq(post, params.category),
            )
            .groupBy(post.id)
            .orderBy(clip.thumbsCount.sum().desc(), clip.id.count().desc())

        val hotPost = postQuery.fetchFirst()
            ?: return null
        val hotPostId = listOf(hotPost.id)

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.eq(hotPost.id),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(hotPostId + clipIds))
        val interestedArticleQuery = selectFrom(interestedArticle)
            .where(interestedArticle.member.id.eq(memberId), interestedArticle.article.id.eq(hotPost.id))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(memberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val interestedArticles = interestedArticleQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        return postConvertToPostRes(hotPost, thumbsUps, interestedArticles, clips, clipReadChecks, memberId)
    }

    override fun findBestInterestedPost(params: PostParams, memberId: Long): PostRes? {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .join(interestedArticle).on(post.id.eq(interestedArticle.article.id))
            .leftJoin(blockingMember).on(post.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(post.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                post.createAt.goe(Instant.now().minus(7, ChronoUnit.DAYS)),
                post.createAt.lt(Instant.now()),
                categoryEq(post, params.category),
            )
            .groupBy(post.id)
            .orderBy(interestedArticle.article.id.count().desc())

        val interestedPost = postQuery.fetchFirst()
            ?: return null
        val interestedPostId = listOf(interestedPost.id)

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.eq(interestedPost.id),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(interestedPostId + clipIds))
        val interestedArticleQuery = selectFrom(interestedArticle)
            .where(interestedArticle.member.id.eq(memberId), interestedArticle.article.id.eq(interestedPost.id))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(memberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val interestedArticles = interestedArticleQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        return postConvertToPostRes(interestedPost, thumbsUps, interestedArticles, clips, clipReadChecks, memberId)
    }

    override fun findBestInterestedPost(params: PostParams): PostRes? {
        val postQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .join(interestedArticle).on(post.id.eq(interestedArticle.article.id))
            .where(
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                post.createAt.goe(Instant.now().minus(7, ChronoUnit.DAYS)),
                post.createAt.lt(Instant.now()),
                categoryEq(post, params.category),
            )
            .groupBy(post.id)
            .orderBy(interestedArticle.article.id.count().desc())

        val interestedPost = postQuery.fetchFirst()
            ?: return null

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .where(
                clip.parentId.eq(interestedPost.id),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()

        return postConvertToPostRes(interestedPost, null, null, clips, null, 0L)
    }

    override fun findPopularPost(memberId: Long): List<PopularPostRes> {
        val popularPostQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .join(clip).on(clip.parentId.eq(post.id))
            .leftJoin(blockingMember).on(post.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(post.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                post.member.id.ne(152),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                post.createAt.goe(Instant.now().minus(14, ChronoUnit.DAYS)),
                clip.mediaType.eq(MediaType.VIDEO)
            )
            .groupBy(post.id)
            .limit(10)
            .orderBy(
                post.thumbsCount.desc(), clip.id.count().desc()
            )

        val popularPosts = popularPostQuery.fetch()
        val postIds = popularPosts.stream().map { post -> post.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.`in`(postIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(postIds + clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        return popularPostsConvertToPostRes(popularPosts, thumbsUps, clips, memberId)
    }

    override fun findPopularPost(): List<PopularPostRes> {
        val popularPostQuery = selectFrom(post)
            .join(post.member, member).fetchJoin()
            .join(clip).on(clip.parentId.eq(post.id))
            .where(
                post.member.id.ne(152),
                post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(post.member.status.eq(MemberStatus.ACTIVE))),
                post.createAt.goe(Instant.now().minus(14, ChronoUnit.DAYS)),
                clip.mediaType.eq(MediaType.VIDEO)
            )
            .groupBy(post.id)
            .limit(10)
            .orderBy(
                post.thumbsCount.desc(), clip.id.count().desc()
            )

        val popularPosts = popularPostQuery.fetch()
        val postIds = popularPosts.stream().map { post -> post.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .where(
                clip.parentId.`in`(postIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()

        return popularPostsConvertToPostRes(popularPosts, null, clips, 0)
    }

    override fun findBasicRecommendClips(pageable: Pageable, startAt: Long, endAt: Long): Page<Any> {
        val query = select(QPostClip(post, clip)).from(clip)
            .join(clip.member, member).fetchJoin()
            .join(post).on(clip.parentId.eq(post.id))
            .join(post.member, member).fetchJoin()
            .where(
                clip.member.status.eq(MemberStatus.ACTIVE), post.member.status.eq(MemberStatus.ACTIVE),
                clip.articleType.eq(ArticleType.CLIP), clip.mediaType.eq(MediaType.VIDEO), clip.status.eq(ArticleStatus.UNBLOCKED),
                clip.createAt.goe(Instant.now().minus(startAt, ChronoUnit.DAYS)),
                clip.createAt.lt(Instant.now().minus(endAt, ChronoUnit.DAYS)),
                clip.views.lt(100)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(
                Expressions.numberTemplate(Double::class.java, "function('rand')").asc()
            )

        val countQuery = select(QPostClip(post, clip)).from(clip)
            .join(clip.member, member).fetchJoin()
            .join(post).on(clip.parentId.eq(post.id))
            .join(post.member, member).fetchJoin()
            .where(
                clip.member.status.eq(MemberStatus.ACTIVE), post.member.status.eq(MemberStatus.ACTIVE),
                clip.articleType.eq(ArticleType.CLIP), clip.mediaType.eq(MediaType.VIDEO), clip.status.eq(ArticleStatus.UNBLOCKED),
                clip.createAt.goe(Instant.now().minus(startAt, ChronoUnit.DAYS)),
                clip.createAt.lt(Instant.now().minus(endAt, ChronoUnit.DAYS)),
                clip.views.lt(100)
            )

        val basicRecommendClips = query.fetch()
        val result = postClipsConvertToClipResOrChallengeClipRes(basicRecommendClips, null, 0)
        return PageImpl(result, pageable, countQuery.fetch().size.toLong())
    }

    override fun findPersonalizeRecommendClips(pageable: Pageable, memberId: Long, startAt: Long, endAt: Long): Page<Any> {
        val myFollowingClips = select(QPostClip(post, clip))
            .from(memberFollow)
            .join(clip).on(memberFollow.following.id.eq(clip.member.id))
            .join(clip.member, member).fetchJoin()
            .leftJoin(clipReadCheck).on(clip.id.eq(clipReadCheck.articleId), clipReadCheck.member.id.eq(memberId))
            .join(post).on(post.id.eq(clip.parentId))
            .join(post.member, member).fetchJoin()
            .where(
                memberFollow.follower.id.eq(memberId), clipReadCheck.articleId.isNull,
                clip.member.status.eq(MemberStatus.ACTIVE), post.member.status.eq(MemberStatus.ACTIVE),
                clip.articleType.eq(ArticleType.CLIP), clip.mediaType.eq(MediaType.VIDEO),
                clip.status.eq(ArticleStatus.UNBLOCKED),
                clip.createAt.goe(Instant.now().minus(startAt, ChronoUnit.DAYS)),
                clip.createAt.lt(Instant.now().minus(endAt, ChronoUnit.DAYS)),
            )
            .orderBy(Expressions.numberTemplate(Double::class.java, "function('rand')").asc())

        val myThumbsUpPostClips = select(QPostClip(post, clip))
            .from(thumbsUp)
            .join(post).on(thumbsUp.article.id.eq(post.id), post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED))
            .join(post.member, member).fetchJoin()
            .join(clip).on(post.id.eq(clip.parentId))
            .join(clip.member, member).fetchJoin()
            .leftJoin(clipReadCheck).on(clip.id.eq(clipReadCheck.articleId), clipReadCheck.member.id.eq(memberId))
            .where(
                thumbsUp.member.id.eq(memberId), clipReadCheck.articleId.isNull,
                clip.member.status.eq(MemberStatus.ACTIVE), post.member.status.eq(MemberStatus.ACTIVE),
                clip.articleType.eq(ArticleType.CLIP), clip.mediaType.eq(MediaType.VIDEO),
                clip.status.eq(ArticleStatus.UNBLOCKED),
                clip.createAt.goe(Instant.now().minus(startAt, ChronoUnit.DAYS)),
                clip.createAt.lt(Instant.now().minus(endAt, ChronoUnit.DAYS))
            )
            .orderBy(Expressions.numberTemplate(Double::class.java, "function('rand')").asc())

        val myInterestedPostClips = select(QPostClip(post, clip))
            .from(interestedArticle)
            .join(post).on(interestedArticle.article.id.eq(post.id), post.articleType.eq(ArticleType.POST), post.status.eq(ArticleStatus.UNBLOCKED))
            .join(post.member, member).fetchJoin()
            .join(clip).on(post.id.eq(clip.parentId))
            .join(clip.member, member).fetchJoin()
            .leftJoin(clipReadCheck).on(clip.id.eq(clipReadCheck.articleId), clipReadCheck.member.id.eq(memberId))
            .where(
                interestedArticle.member.id.eq(memberId), clipReadCheck.articleId.isNull,
                clip.member.status.eq(MemberStatus.ACTIVE), post.member.status.eq(MemberStatus.ACTIVE),
                clip.articleType.eq(ArticleType.CLIP), clip.mediaType.eq(MediaType.VIDEO),
                clip.status.eq(ArticleStatus.UNBLOCKED),
                clip.createAt.goe(Instant.now().minus(startAt, ChronoUnit.DAYS)),
                clip.createAt.lt(Instant.now().minus(endAt, ChronoUnit.DAYS))
            )
            .orderBy(Expressions.numberTemplate(Double::class.java, "function('rand')").asc())

        var personalizeRecommendClips = myFollowingClips.fetch() + myThumbsUpPostClips.fetch() + myInterestedPostClips.fetch()
        personalizeRecommendClips = personalizeRecommendClips.stream().distinct().collect(Collectors.toList()).shuffled()
        val totalCount = personalizeRecommendClips.size

        val clipIds = personalizeRecommendClips.stream().map { it.clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(clipIds))
        val thumbsUps = thumbsUpQuery.fetch()

        val result = postClipsConvertToClipResOrChallengeClipRes(personalizeRecommendClips, thumbsUps, memberId)

        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(result.size)
        val emptyList: List<ClipRes> = mutableListOf()
        if (start > end) return PageImpl(emptyList, pageable, totalCount.toLong())
        return PageImpl(result.subList(start, end), pageable, totalCount.toLong())
    }

    override fun findFollowingClips(pageable: Pageable, memberId: Long): Page<Any> {
        val myFollowingClipQuery = select(QPostClip(post, clip))
            .from(memberFollow)
            .join(clip).on(memberFollow.following.id.eq(clip.member.id))
            .join(clip.member, member).fetchJoin()
            .leftJoin(post).on(post.id.eq(clip.parentId))
            .leftJoin(post.member, member).fetchJoin()
            .where(
                memberFollow.follower.id.eq(memberId),
                clip.articleType.eq(ArticleType.CLIP), clip.mediaType.eq(MediaType.VIDEO),
                clip.member.status.eq(MemberStatus.ACTIVE),
                clip.status.eq(ArticleStatus.UNBLOCKED),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(clip.createAt.desc())

        val myFollowingClipCountQuery = select(QPostClip(post, clip))
            .from(memberFollow)
            .join(clip).on(memberFollow.following.id.eq(clip.member.id))
            .join(clip.member, member).fetchJoin()
            .leftJoin(post).on(post.id.eq(clip.parentId))
            .leftJoin(post.member, member).fetchJoin()
            .where(
                memberFollow.follower.id.eq(memberId),
                clip.articleType.eq(ArticleType.CLIP), clip.mediaType.eq(MediaType.VIDEO),
                clip.member.status.eq(MemberStatus.ACTIVE),
                clip.status.eq(ArticleStatus.UNBLOCKED),
            )

        val myFollowingClips = myFollowingClipQuery.fetch()
        val clipIds = myFollowingClips.stream().map { it.clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(clipIds))
        val thumbsUps = thumbsUpQuery.fetch()

        val result = postClipsConvertToClipResOrChallengeClipRes(myFollowingClips, thumbsUps, memberId)
        return PageImpl(result, pageable, myFollowingClipCountQuery.fetch().size.toLong())
    }

    override fun findPopularClip(pageable: Pageable, memberId: Long, params: ClipParams): Page<Any> {
        val popularClipsQuery = select(QPostClip(post, clip))
            .from(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(post).on(post.id.eq(clip.parentId))
            .leftJoin(post.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                categoryEq(clip, params.category),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(
                CaseBuilder()
                .`when`(clip.createAt.goe(Instant.now().minus(7, ChronoUnit.DAYS)))
                    .then(clip.thumbsCount.add(clip.views)).otherwise(0).desc(),
                clip.createAt.desc()
            )

        val popularClipsCountQuery = select(QPostClip(post, clip))
            .from(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(post).on(post.id.eq(clip.parentId))
            .leftJoin(post.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
                categoryEq(clip, params.category),
            )

        val popularClips = popularClipsQuery.fetch()
        val clipIds = popularClips.stream().map { it.clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(clipIds))
        val thumbsUps = thumbsUpQuery.fetch()

        val result = postClipsConvertToClipResOrChallengeClipRes(popularClips, thumbsUps, memberId)
        return PageImpl(result, pageable, popularClipsCountQuery.fetch().size.toLong())
    }

    override fun findPopularClip(pageable: Pageable, params: ClipParams): Page<Any> {
        val popularClipsQuery = select(QPostClip(post, clip))
            .from(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(post).on(post.id.eq(clip.parentId))
            .leftJoin(post.member, member).fetchJoin()
            .where(
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                categoryEq(clip, params.category),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(
                CaseBuilder()
                    .`when`(clip.createAt.goe(Instant.now().minus(7, ChronoUnit.DAYS)))
                    .then(clip.thumbsCount.add(clip.views)).otherwise(0).desc(),
                clip.createAt.desc()
            )

        val popularClipsCountQuery = select(QPostClip(post, clip))
            .from(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(post).on(post.id.eq(clip.parentId))
            .leftJoin(post.member, member).fetchJoin()
            .where(
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                categoryEq(clip, params.category),
            )

        val popularClips = popularClipsQuery.fetch()

        val result = postClipsConvertToClipResOrChallengeClipRes(popularClips, null, 0L)
        return PageImpl(result, pageable, popularClipsCountQuery.fetch().size.toLong())
    }

    override fun findAllChallenge(pageable: Pageable, memberId: Long, params: ChallengeParams): Page<ChallengeRes> {
        val challengeQuery = selectFrom(challenge)
            .where(
                challenge.articleType.eq(ArticleType.CHALLENGE), challenge.status.eq(ArticleStatus.UNBLOCKED),
                keywordMatch(challenge, params.keyword),
                hashtagLike(challenge, params.hashtag),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(challenge.challengeEndAt.desc())

        val challengeCountQuery = selectFrom(challenge)
            .where(
                challenge.articleType.eq(ArticleType.CHALLENGE), challenge.status.eq(ArticleStatus.UNBLOCKED),
                keywordMatch(challenge, params.keyword),
                hashtagLike(challenge, params.hashtag),
            )

        val challenges = challengeQuery.fetch()
        val challengeIds = challenges.stream().map { challenge -> challenge.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.`in`(challengeIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(clipIds))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(memberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        val result = challengesConvertToChallengeRes(challenges, thumbsUps, clips, clipReadChecks, memberId)
        return PageImpl(result, pageable, challengeCountQuery.fetch().size.toLong())
    }

    override fun findAllChallenge(pageable: Pageable, params: ChallengeParams): Page<ChallengeRes> {
        val challengeQuery = selectFrom(challenge)
            .where(
                challenge.articleType.eq(ArticleType.CHALLENGE), challenge.status.eq(ArticleStatus.UNBLOCKED),
                keywordMatch(challenge, params.keyword),
                hashtagLike(challenge, params.hashtag),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(challenge.challengeEndAt.desc())

        val challengeCountQuery = selectFrom(challenge)
            .where(
                challenge.articleType.eq(ArticleType.CHALLENGE), challenge.status.eq(ArticleStatus.UNBLOCKED),
                keywordMatch(challenge, params.keyword),
                hashtagLike(challenge, params.hashtag),
            )

        val challenges = challengeQuery.fetch()
        val challengeIds = challenges.stream().map { challenge -> challenge.id }.collect(Collectors.toList())

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .where(
                clip.parentId.`in`(challengeIds),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()

        val result = challengesConvertToChallengeRes(challenges, null, clips, null, 0L)
        return PageImpl(result, pageable, challengeCountQuery.fetch().size.toLong())
    }

    override fun findChallenge(challengeId: Long, memberId: Long): ChallengeDetailRes? {
        val challengeQuery = selectFrom(challenge)
            .where(
                challenge.id.eq(challengeId),
                challenge.articleType.eq(ArticleType.CHALLENGE),
                challenge.status.eq(ArticleStatus.UNBLOCKED)
            )

        val challenge = challengeQuery.fetchFirst() ?: return null

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .leftJoin(blockingMember).on(clip.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(clip.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                clip.parentId.`in`(challenge.id),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
        val clips = clipQuery.fetch()
        val clipIds = clips.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(clipIds))
        val clipReadCheckQuery = selectFrom(clipReadCheck)
            .where(clipReadCheck.member.id.eq(memberId), clipReadCheck.articleId.`in`(clipIds))

        val thumbsUps = thumbsUpQuery.fetch()
        val clipReadChecks = clipReadCheckQuery.fetch().stream().map { it.articleId }.collect(Collectors.toList())

        return challengeConvertToChallengeRes(challenge, thumbsUps, clips, clipReadChecks, memberId)
    }

    override fun findChallenge(challengeId: Long): ChallengeDetailRes? {
        val challengeQuery = selectFrom(challenge)
            .where(
                challenge.id.eq(challengeId),
                challenge.articleType.eq(ArticleType.CHALLENGE),
                challenge.status.eq(ArticleStatus.UNBLOCKED)
            )

        val challenge = challengeQuery.fetchFirst() ?: return null

        val clipQuery = selectFrom(clip)
            .join(clip.member, member).fetchJoin()
            .where(
                clip.parentId.`in`(challenge.id),
                clip.articleType.eq(ArticleType.CLIP), clip.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(clip.member.status.eq(MemberStatus.ACTIVE))),
            )
        val clips = clipQuery.fetch()

        return challengeConvertToChallengeRes(challenge, null, clips, null, 0L)
    }

    override fun findAllComment(pageable: Pageable, clipId: Long, memberId: Long): Page<CommentRes> {
        val commentQuery = selectFrom(comment)
            .join(comment.member, member).fetchJoin()
            .leftJoin(blockingMember).on(comment.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(comment.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                comment.parentId.eq(clipId),
                comment.articleType.eq(ArticleType.COMMENT), comment.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(comment.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(comment.createAt.desc())

        val commentCountQuery = selectFrom(comment)
            .join(comment.member, member).fetchJoin()
            .leftJoin(blockingMember).on(comment.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(comment.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                comment.parentId.eq(clipId),
                comment.articleType.eq(ArticleType.COMMENT), comment.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(comment.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )

        val comments = commentQuery.fetch()
        val commentIds = comments.stream().map { comment -> comment.id }.collect(Collectors.toList())

        val replyQuery = selectFrom(reply)
            .join(reply.member, member).fetchJoin()
            .leftJoin(blockingMember).on(reply.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(reply.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                reply.parentId.`in`(commentIds),
                reply.articleType.eq(ArticleType.REPLY), reply.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(reply.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
            .orderBy(reply.createAt.desc())

        val replies = replyQuery.fetch()
        val replyIds = replies.stream().map { clip -> clip.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(commentIds + replyIds))

        val thumbsUps = thumbsUpQuery.fetch()

        val result = commentsConvertToCommentRes(comments, replies, thumbsUps, memberId)
        return PageImpl(result, pageable, commentCountQuery.fetch().size.toLong())
    }

    override fun findAllComment(pageable: Pageable, clipId: Long): Page<CommentRes> {
        val commentQuery = selectFrom(comment)
            .join(comment.member, member).fetchJoin()
            .where(
                comment.parentId.eq(clipId),
                comment.articleType.eq(ArticleType.COMMENT), comment.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(comment.member.status.eq(MemberStatus.ACTIVE))),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(comment.createAt.desc())

        val commentCountQuery = selectFrom(comment)
            .join(comment.member, member).fetchJoin()
            .where(
                comment.parentId.eq(clipId),
                comment.articleType.eq(ArticleType.COMMENT), comment.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(comment.member.status.eq(MemberStatus.ACTIVE))),
            )

        val comments = commentQuery.fetch()
        val commentIds = comments.stream().map { comment -> comment.id }.collect(Collectors.toList())

        val replyQuery = selectFrom(reply)
            .join(reply.member, member).fetchJoin()
            .where(
                reply.parentId.`in`(commentIds),
                reply.articleType.eq(ArticleType.REPLY), reply.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(reply.member.status.eq(MemberStatus.ACTIVE))),
            )
            .orderBy(reply.createAt.desc())

        val replies = replyQuery.fetch()

        val result = commentsConvertToCommentRes(comments, replies, null, 0L)
        return PageImpl(result, pageable, commentCountQuery.fetch().size.toLong())
    }

    override fun findAllReply(commentId: Long, memberId: Long): List<ArticleRes> {
        val replyQuery = selectFrom(reply)
            .join(reply.member, member).fetchJoin()
            .leftJoin(blockingMember).on(reply.member.id.eq(blockingMember.blockedMemberId), blockingMember.member.id.eq(memberId))
            .leftJoin(blockerMember).on(reply.member.id.eq(blockerMember.member.id), blockerMember.blockedMemberId.eq(memberId))
            .where(
                reply.parentId.eq(commentId),
                reply.articleType.eq(ArticleType.REPLY), reply.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(reply.member.status.eq(MemberStatus.ACTIVE))),
                blockingMember.blockedMemberId.isNull, blockerMember.blockedMemberId.isNull,
            )
            .orderBy(reply.createAt.desc())

        val replies = replyQuery.fetch()
        val replyIds = replies.stream().map { comment -> comment.id }.collect(Collectors.toList())

        val thumbsUpQuery = selectFrom(thumbsUp)
            .where(thumbsUp.member.id.eq(memberId), thumbsUp.article.id.`in`(replyIds))

        val thumbsUps = thumbsUpQuery.fetch()

        return repliesConvertToArticleRes(replies, thumbsUps, memberId)
    }

    override fun findAllReply(commentId: Long): List<ArticleRes> {
        val replyQuery = selectFrom(reply)
            .join(reply.member, member).fetchJoin()
            .where(
                reply.parentId.eq(commentId),
                reply.articleType.eq(ArticleType.REPLY), reply.status.eq(ArticleStatus.UNBLOCKED),
                (member.isNull.or(reply.member.status.eq(MemberStatus.ACTIVE))),
            )
            .orderBy(reply.createAt.desc())

        val replies = replyQuery.fetch()

        return repliesConvertToArticleRes(replies, null, 0L)
    }

    private fun categoryEq(article: QArticle, category: Int?): BooleanExpression? {
        return if (category != null) article.category.id.eq(category) else null
    }

    private fun orderByPost(sort: String?): OrderSpecifier<*>? {
        return when (sort) {
            "latestPost" -> post.createAt.desc()
            "latestClip" -> post.childArticleList.any().createAt.desc()
            "like" -> post.thumbsCount.desc()
            else -> post.createAt.desc()
        }
    }

    private fun keywordMatch(article: QArticle, keyword: String?): BooleanExpression? {
        return if (keyword != null)
            Expressions.template(
                Boolean::class.java,
                "function('postFullTextSearch',{0},{1},{2})",
                article.title, article.content, "${keyword}*"
            ).eq(true)
        else null
    }

    private fun hashtagLike(article: QArticle, hashtag: String?): BooleanExpression? {
        return if (hashtag != null) article.hashtag.like("%${hashtag}%") else null
    }

    private fun postConvertToPostRes(
        post: Article,
        thumbsUps: List<ThumbsUp>?,
        interestedArticles: List<InterestedArticle>?,
        clips: List<Article>,
        clipReadChecks: List<Long?>?,
        memberId: Long
    ): PostRes {
        val clipList = clips.filter { clip -> clip.parentId == post.id }.sortedByDescending { it.createAt }
        val returnClipList: MutableList<ClipRes> = mutableListOf()
        // 클립 순서 : 채택 클립 -> 포스트 작성자 클립 -> 안본 클립 -> 본 클립
        val adoptClip = clipList.filter { it.id == post.adoptClipId }
        val postWriterClipList = clipList.filter { it.member.id == post.member.id }
        val elseClipList = clipList.filter { it.id != post.adoptClipId && it.member.id != post.member.id }

        val sortClipList =
            if (memberId == 0L) adoptClip + postWriterClipList + elseClipList
            else {
                val unReadClipList = elseClipList.filter { clipReadChecks?.contains(it.id) == null }
                val readClipList = elseClipList.filter { clipReadChecks?.contains(it.id) != null }
                adoptClip + postWriterClipList + unReadClipList + readClipList
            }
        sortClipList.forEach { clip ->
            returnClipList.add(
                ClipRes(
                    id = clip.id!!,
                    createAt = clip.createAt,
                    parentId = clip.parentId,
                    memberId = clip.member.id,
                    memberNickname = clip.member.nickname!!,
                    memberProfilePath = clip.member.profilePath,
                    content = clip.content,
                    categoryId = clip.category?.id,
                    categoryName = clip.category?.name,
                    thumbsUp = thumbsUps?.find { it.article.id == clip.id!! && it.member.id == memberId } != null,
                    thumbsCount = clip.thumbsCount,
                    articleType = clip.articleType!!,
                    mediaType = clip.mediaType,
                    clipUrl = clip.clipUrl,
                    hashtag = clip.hashtag,
                    commentCount = clip.childArticleCount,
                    clipTimeline = clip.clipTimeline,
                    clipReadCheck = clipReadChecks?.contains(clip.id),
                    status = clip.status,
                    views = clip.views,
                    postId = post.id!!,
                    postTitle = post.title,
                    postMemberId = post.member.id,
                    postMemberNickname = post.member.nickname!!,
                    parentArticleType = post.articleType!!,
                    objectStrings = clip.objectStrings
                )
            )
        }

        return PostRes(
            id = post.id!!,
            createAt = post.createAt,
            memberId = post.member.id,
            title = post.title,
            content = post.content,
            thumbsUp = thumbsUps?.find { it.article.id == post.id!! && it.member.id == memberId } != null,
            thumbsCount = post.thumbsCount,
            articleType = post.articleType!!,
            memberNickname = post.member.nickname!!,
            memberProfilePath = post.member.profilePath,
            hashtag = post.hashtag,
            clips = returnClipList,
            status = post.status,
            adoptClipId = post.adoptClipId,
            pick = interestedArticles?.find { it.article.id == post.id!! && it.member.id == memberId } != null,
            pickCount = post.interestedCount,
            views = post.views,
            categoryId = post.category?.id,
            categoryName = post.category?.name,
        )
    }

    private fun postsConvertToPostRes(
        posts: List<Article>,
        thumbsUps: List<ThumbsUp>?,
        interestedArticles: List<InterestedArticle>?,
        clips: List<Article>,
        clipReadChecks: List<Long?>?,
        memberId: Long
    ): MutableList<PostRes> {
        val returnPostList: MutableList<PostRes> = mutableListOf()
        posts.forEach { post ->
            val clipList = clips.filter { clip -> clip.parentId == post.id }.sortedByDescending { it.createAt }
            val returnClipList: MutableList<ClipRes> = mutableListOf()
            // 클립 순서 : 채택 클립 -> 포스트 작성자 클립 -> 안본 클립 -> 본 클립
            val adoptClip = clipList.filter { it.id == post.adoptClipId }
            val postWriterClipList = clipList.filter { it.member.id == post.member.id }
            val elseClipList = clipList.filter { it.id != post.adoptClipId && it.member.id != post.member.id }

            val sortClipList =
                if (memberId == 0L) adoptClip + postWriterClipList + elseClipList
                else {
                    val unReadClipList = elseClipList.filter { clipReadChecks?.contains(it.id) == null }
                    val readClipList = elseClipList.filter { clipReadChecks?.contains(it.id) != null }
                    adoptClip + postWriterClipList + unReadClipList + readClipList
                }
            sortClipList.forEach { clip ->
                returnClipList.add(
                    ClipRes(
                        id = clip.id!!,
                        createAt = clip.createAt,
                        parentId = clip.parentId,
                        memberId = clip.member.id,
                        memberNickname = clip.member.nickname!!,
                        memberProfilePath = clip.member.profilePath,
                        content = clip.content,
                        categoryId = clip.category?.id,
                        categoryName = clip.category?.name,
                        thumbsUp = thumbsUps?.find { it.article.id == clip.id!! && it.member.id == memberId } != null,
                        thumbsCount = clip.thumbsCount,
                        articleType = clip.articleType!!,
                        mediaType = clip.mediaType,
                        clipUrl = clip.clipUrl,
                        hashtag = clip.hashtag,
                        commentCount = clip.childArticleCount,
                        clipTimeline = clip.clipTimeline,
                        clipReadCheck = clipReadChecks?.contains(clip.id),
                        status = clip.status,
                        views = clip.views,
                        postId = post.id!!,
                        postTitle = post.title,
                        postMemberId = post.member.id,
                        postMemberNickname = post.member.nickname!!,
                        parentArticleType = post.articleType!!,
                        objectStrings = clip.objectStrings,
                    )
                )
            }

            returnPostList.add(
                PostRes(
                    id = post.id!!,
                    createAt = post.createAt,
                    memberId = post.member.id,
                    title = post.title,
                    content = post.content,
                    thumbsUp = thumbsUps?.find { it.article.id == post.id!! && it.member.id == memberId } != null,
                    thumbsCount = post.thumbsCount,
                    articleType = post.articleType!!,
                    memberNickname = post.member.nickname!!,
                    memberProfilePath = post.member.profilePath,
                    hashtag = post.hashtag,
                    clips = returnClipList,
                    status = post.status,
                    adoptClipId = post.adoptClipId,
                    pick = interestedArticles?.find { it.article.id == post.id!! && it.member.id == memberId } != null,
                    pickCount = post.interestedCount,
                    views = post.views,
                    categoryId = post.category?.id,
                    categoryName = post.category?.name,
                )
            )
        }

        return returnPostList
    }

    private fun postClipsConvertToClipResOrChallengeClipRes(
        postClips: List<PostClip>,
        thumbsUps: List<ThumbsUp>?,
        memberId: Long
    ): MutableList<Any> {
        val returnClipList: MutableList<Any> = mutableListOf()
        postClips.forEach {
            if (it.post?.articleType == ArticleType.CHALLENGE) {
                returnClipList.add(
                    ChallengeClipRes(
                        id = it.clip.id!!,
                        createAt = it.clip.createAt,
                        parentId = it.clip.parentId,
                        memberId = it.clip.member.id,
                        memberNickname = it.clip.member.nickname!!,
                        memberProfilePath = it.clip.member.profilePath,
                        content = it.clip.content,
                        categoryId = it.clip.category?.id,
                        categoryName = it.clip.category?.name,
                        thumbsUp = thumbsUps?.find { thumbsUp -> thumbsUp.article.id == it.clip.id!! && thumbsUp.member.id == memberId } != null,
                        thumbsCount = it.clip.thumbsCount,
                        articleType = it.clip.articleType!!,
                        mediaType = it.clip.mediaType,
                        clipUrl = it.clip.clipUrl,
                        hashtag = it.clip.hashtag,
                        commentCount = it.clip.childArticleCount,
                        clipTimeline = it.clip.clipTimeline,
                        clipReadCheck = false,
                        status = it.clip.status,
                        views = it.clip.views,
                        challengeId = it.post?.id!!,
                        challengeTitle = it.post?.title,
                        parentArticleType = it.post?.articleType!!,
                        objectStrings = it.clip.objectStrings,
                    )
                )
            } else {
                returnClipList.add(
                    ClipRes(
                        id = it.clip.id!!,
                        createAt = it.clip.createAt,
                        parentId = it.clip.parentId,
                        memberId = it.clip.member.id,
                        content = it.clip.content,
                        categoryId = it.clip.category?.id,
                        categoryName = it.clip.category?.name,
                        thumbsUp = thumbsUps?.find { thumbsUp -> thumbsUp.article.id == it.clip.id!! && thumbsUp.member.id == memberId } != null,
                        thumbsCount = it.clip.thumbsCount,
                        articleType = it.clip.articleType!!,
                        mediaType = it.clip.mediaType,
                        clipUrl = it.clip.clipUrl,
                        memberNickname = it.clip.member.nickname!!,
                        memberProfilePath = it.clip.member.profilePath,
                        comments = null,
                        commentCount = it.clip.childArticleCount,
                        hashtag = it.clip.hashtag,
                        clipTimeline = it.clip.clipTimeline,
                        clipReadCheck = false,
                        status = it.clip.status,
                        views = it.clip.views,
                        postId = it.post?.id,
                        postTitle = it.post?.title,
                        postMemberId = it.post?.member?.id,
                        postMemberNickname = it.post?.member?.nickname,
                        parentArticleType = it.post?.articleType,
                        objectStrings = it.clip.objectStrings,
                    )
                )
            }
        }

        return returnClipList
    }

    private fun clipWithParentConvertToClipResOrChallengeClipRes(
        postClips: List<ClipWithParent>,
        thumbsUps: List<ThumbsUp>?,
        memberId: Long
    ): MutableList<Any> {
        val returnClipList: MutableList<Any> = mutableListOf()
        postClips.forEach {
            if (it.parent?.articleType == ArticleType.CHALLENGE) {
                returnClipList.add(
                    ChallengeClipRes(
                        id = it.clip.id!!,
                        createAt = it.clip.createAt,
                        parentId = it.clip.parentId,
                        memberId = it.clip.member.id,
                        memberNickname = it.clip.member.nickname!!,
                        memberProfilePath = it.clip.member.profilePath,
                        content = it.clip.content,
                        categoryId = it.clip.category?.id,
                        categoryName = it.clip.category?.name,
                        thumbsUp = thumbsUps?.find { thumbsUp -> thumbsUp.article.id == it.clip.id!! && thumbsUp.member.id == memberId } != null,
                        thumbsCount = it.clip.thumbsCount,
                        articleType = it.clip.articleType!!,
                        mediaType = it.clip.mediaType,
                        clipUrl = it.clip.clipUrl,
                        hashtag = it.clip.hashtag,
                        commentCount = it.clip.childArticleCount,
                        clipTimeline = it.clip.clipTimeline,
                        clipReadCheck = false,
                        status = it.clip.status,
                        views = it.clip.views,
                        challengeId = it.parent?.id!!,
                        challengeTitle = it.parent?.title,
                        parentArticleType = it.parent?.articleType!!,
                        objectStrings = it.clip.objectStrings,
                    )
                )
            } else {
                returnClipList.add(
                    ClipRes(
                        id = it.clip.id!!,
                        createAt = it.clip.createAt,
                        parentId = it.clip.parentId,
                        memberId = it.clip.member.id,
                        content = it.clip.content,
                        categoryId = it.clip.category?.id,
                        categoryName = it.clip.category?.name,
                        thumbsUp = thumbsUps?.find { thumbsUp -> thumbsUp.article.id == it.clip.id!! && thumbsUp.member.id == memberId } != null,
                        thumbsCount = it.clip.thumbsCount,
                        articleType = it.clip.articleType!!,
                        mediaType = it.clip.mediaType,
                        clipUrl = it.clip.clipUrl,
                        memberNickname = it.clip.member.nickname!!,
                        memberProfilePath = it.clip.member.profilePath,
                        comments = null,
                        commentCount = it.clip.childArticleCount,
                        hashtag = it.clip.hashtag,
                        clipTimeline = it.clip.clipTimeline,
                        clipReadCheck = false,
                        status = it.clip.status,
                        views = it.clip.views,
                        postId = it.parent?.id,
                        postTitle = it.parent?.title,
                        postMemberId = it.parent?.member?.id,
                        postMemberNickname = it.parent?.member?.nickname,
                        parentArticleType = it.parent?.articleType,
                        objectStrings = it.clip.objectStrings,
                    )
                )
            }
        }

        return returnClipList
    }

    private fun challengesConvertToChallengeRes(
        challenges: List<Article>,
        thumbsUps: List<ThumbsUp>?,
        clips: List<Article>,
        clipReadChecks: List<Long?>?,
        memberId: Long
    ): MutableList<ChallengeRes> {
        val returnChallengesList: MutableList<ChallengeRes> = mutableListOf()
        challenges.forEach { challenge ->
            val clipList = clips.filter { clip -> clip.parentId == challenge.id }.sortedByDescending { it.createAt }
            val returnClipList: MutableList<ChallengeClipRes> = mutableListOf()
            val returnChallengeRewards: MutableList<ChallengeRewardRes> = mutableListOf()
            val winnerClipIds: MutableList<Long> = mutableListOf()

            challenge.ChallengeRewardList.forEach {
                val rewardWinners: MutableList<WinnersRes> = mutableListOf()
                it.rewardWinnerList.forEach { winner ->
                    rewardWinners.add(WinnersRes(winner.member.id, winner.member.nickname))
                    winnerClipIds.add(winner.article.id!!)
                }

                returnChallengeRewards.add(
                    ChallengeRewardRes(
                        prize = it.prize,
                        reward = it.reward,
                        numberOfWinners = it.numberOfWinners,
                        winners = rewardWinners,
                    )
                )
            }

            clipList.forEach { clip ->
                returnClipList.add(
                    ChallengeClipRes(
                        id = clip.id!!,
                        createAt = clip.createAt,
                        parentId = clip.parentId,
                        memberId = clip.member.id,
                        memberNickname = clip.member.nickname!!,
                        memberProfilePath = clip.member.profilePath,
                        content = clip.content,
                        categoryId = clip.category?.id,
                        categoryName = clip.category?.name,
                        thumbsUp = thumbsUps?.find { it.article.id == clip.id!! && it.member.id == memberId } != null,
                        thumbsCount = clip.thumbsCount,
                        articleType = clip.articleType!!,
                        mediaType = clip.mediaType,
                        clipUrl = clip.clipUrl,
                        hashtag = clip.hashtag,
                        commentCount = clip.childArticleCount,
                        clipTimeline = clip.clipTimeline,
                        clipReadCheck = clipReadChecks?.contains(clip.id),
                        status = clip.status,
                        views = clip.views,
                        challengeId = challenge.id!!,
                        challengeTitle = challenge.title,
                        parentArticleType = challenge.articleType!!,
                        objectStrings = clip.objectStrings,
                        guideClip = clip.guideClip ?: false,
                        winning = returnChallengeRewards.find { it.winners.find { winners -> winners.id == clip.member.id } != null } != null
                    )
                )
            }

            val guideClips = returnClipList.filter { it.guideClip == true }
            val winnerClips = returnClipList.filter { winnerClipIds.contains(it.id) }
            val elseClips = returnClipList.filter { it.guideClip == false && !winnerClipIds.contains(it.id) }.sortedByDescending { it.createAt }
            val returnOrderClipList = guideClips + winnerClips + elseClips

            val status = checkChallengeStatus(challenge.challengeStartAt, challenge.challengeEndAt)
            returnChallengesList.add(
                ChallengeRes(
                    id = challenge.id!!,
                    title = challenge.title,
                    clips = returnOrderClipList,
                    status = status,
                )
            )
        }

        return returnChallengesList
    }

    private fun challengeConvertToChallengeRes(
        challenge: Article,
        thumbsUps: List<ThumbsUp>?,
        clips: List<Article>,
        clipReadChecks: List<Long?>?,
        memberId: Long
    ): ChallengeDetailRes {
        val clipList = clips.filter { clip -> clip.parentId == challenge.id }
        val returnClipList: MutableList<ChallengeClipRes> = mutableListOf()
        val returnChallengeRewards: MutableList<ChallengeRewardRes> = mutableListOf()
        val winnerClipIds: MutableList<Long> = mutableListOf()

        challenge.ChallengeRewardList.forEach {
            val rewardWinners: MutableList<WinnersRes> = mutableListOf()
            it.rewardWinnerList.forEach { winner ->
                rewardWinners.add(WinnersRes(winner.member.id, winner.member.nickname))
                winnerClipIds.add(winner.article.id!!)
            }

            returnChallengeRewards.add(
                ChallengeRewardRes(
                    prize = it.prize,
                    reward = it.reward,
                    numberOfWinners = it.numberOfWinners,
                    winners = rewardWinners,
                )
            )
        }

        clipList.forEach { clip ->
            returnClipList.add(
                ChallengeClipRes(
                    id = clip.id!!,
                    createAt = clip.createAt,
                    parentId = clip.parentId,
                    memberId = clip.member.id,
                    memberNickname = clip.member.nickname!!,
                    memberProfilePath = clip.member.profilePath,
                    content = clip.content,
                    categoryId = clip.category?.id,
                    categoryName = clip.category?.name,
                    thumbsUp = thumbsUps?.find { it.article.id == clip.id!! && it.member.id == memberId } != null,
                    thumbsCount = clip.thumbsCount,
                    articleType = clip.articleType!!,
                    mediaType = clip.mediaType,
                    clipUrl = clip.clipUrl,
                    hashtag = clip.hashtag,
                    commentCount = clip.childArticleCount,
                    clipTimeline = clip.clipTimeline,
                    clipReadCheck = clipReadChecks?.contains(clip.id),
                    status = clip.status,
                    views = clip.views,
                    challengeId = challenge.id!!,
                    challengeTitle = challenge.title,
                    parentArticleType = challenge.articleType!!,
                    objectStrings = clip.objectStrings,
                    guideClip = clip.guideClip ?: false,
                    winning = returnChallengeRewards.find { it.winners.find { winners -> winners.id == clip.member.id } != null } != null
                )
            )
        }

        val guideClips = returnClipList.filter { it.guideClip == true }
        val winnerClips = returnClipList.filter { winnerClipIds.contains(it.id) }
        val elseClips = returnClipList.filter { it.guideClip == false && !winnerClipIds.contains(it.id) }.sortedByDescending { it.createAt }
        val returnOrderClipList = guideClips + winnerClips + elseClips

        val status = checkChallengeStatus(challenge.challengeStartAt, challenge.challengeEndAt)
        return ChallengeDetailRes(
            id = challenge.id!!,
            title = challenge.title,
            content = challenge.content,
            startAt = challenge.challengeStartAt,
            endAt = challenge.challengeEndAt,
            bannerUrl = challenge.challengeBannerUrl,
            clips = returnOrderClipList,
            status = status,
            challengeRewards = returnChallengeRewards,
        )
    }

    fun checkChallengeStatus(start: Instant?, end: Instant?): String {
        val now = Instant.now()
        if (now > start && now > end)
            return "closed"
        if (now >= start && now <= end)
            return "opened"
        return "waiting"
    }

    private fun popularPostsConvertToPostRes(
        posts: List<Article>,
        thumbsUps: List<ThumbsUp>?,
        clips: List<Article>,
        memberId: Long
    ): MutableList<PopularPostRes> {
        val returnPostList: MutableList<PopularPostRes> = mutableListOf()
        posts.forEach { post ->
            val clipList = clips.filter { clip -> clip.parentId == post.id }.sortedByDescending { it.createAt }

            val comparator: Comparator<Article> = compareBy({ it.thumbsCount }, { it.views })
            val popularClipsOrder = clipList.sortedWith(comparator).reversed().stream().map { clip -> clip.id }.collect(Collectors.toList())

            val mainClipIds = try {
                popularClipsOrder.subList(0, 3).shuffled()
            } catch (e: Exception) {
                popularClipsOrder.subList(0, 1)
            }

            val returnClipList: MutableList<ClipRes> = mutableListOf()
            // 클립 순서 : 채택 클립 -> 포스트 작성자 클립 -> 안본 클립 -> 본 클립
            val adoptClip = clipList.filter { it.id == post.adoptClipId }
            val postWriterClipList = clipList.filter { it.member.id == post.member.id }
            val elseClipList = clipList.filter { it.id != post.adoptClipId && it.member.id != post.member.id }
            val sortClipList = adoptClip + postWriterClipList + elseClipList

            sortClipList.forEach { clip ->
                returnClipList.add(
                    ClipRes(
                        id = clip.id!!,
                        createAt = clip.createAt,
                        parentId = clip.parentId,
                        memberId = clip.member.id,
                        memberNickname = clip.member.nickname!!,
                        memberProfilePath = clip.member.profilePath,
                        content = clip.content,
                        categoryId = clip.category?.id,
                        categoryName = clip.category?.name,
                        thumbsUp = thumbsUps?.find { it.article.id == clip.id!! && it.member.id == memberId } != null,
                        thumbsCount = clip.thumbsCount,
                        articleType = clip.articleType!!,
                        mediaType = clip.mediaType,
                        clipUrl = clip.clipUrl,
                        hashtag = clip.hashtag,
                        commentCount = clip.childArticleCount,
                        clipTimeline = clip.clipTimeline,
                        clipReadCheck = false,
                        status = clip.status,
                        views = clip.views,
                        postId = post.id!!,
                        postTitle = post.title,
                        postMemberId = post.member.id,
                        postMemberNickname = post.member.nickname!!,
                        parentArticleType = post.articleType!!,
                        objectStrings = clip.objectStrings,
                    )
                )
            }

            val mainClip = sortClipList.find { it.id == mainClipIds[0] }
            returnPostList.add(
                PopularPostRes(
                    id = post.id!!,
                    createAt = post.createAt,
                    memberId = post.member.id,
                    title = post.title,
                    content = post.content,
                    thumbsUp = thumbsUps?.find { it.article.id == post.id!! && it.member.id == memberId } != null,
                    thumbsCount = post.thumbsCount,
                    articleType = post.articleType!!,
                    memberNickname = post.member.nickname!!,
                    memberProfilePath = post.member.profilePath,
                    hashtag = post.hashtag,
                    mainClipIndex = sortClipList.indexOf(mainClip),
                    clips = returnClipList,
                    status = post.status,
                    adoptClipId = post.adoptClipId,
                    pick = false,
                    pickCount = post.interestedCount,
                    views = post.views,
                    categoryId = post.category?.id,
                    categoryName = post.category?.name,
                )
            )
        }

        return returnPostList
    }

    fun commentsConvertToCommentRes(
        comments: List<Article>,
        replies: List<Article>,
        thumbsUps: List<ThumbsUp>?,
        memberId: Long
    ): MutableList<CommentRes> {
        val returnCommentList: MutableList<CommentRes> = mutableListOf()
        comments.forEach { comment ->
            val replyList = replies.filter { reply -> reply.parentId == comment.id }
            val returnReplyList: MutableList<ArticleRes> = mutableListOf()

            replyList.forEach { reply ->
                returnReplyList.add(
                    ArticleRes(
                        id = reply.id!!,
                        createAt = reply.createAt,
                        updateAt = if (reply.modified == true) reply.updateAt else null,
                        parentId = reply.parentId,
                        memberId = reply.member.id,
                        content = reply.content,
                        thumbsUp = thumbsUps?.find { it.article.id == reply.id!! && it.member.id == memberId } != null,
                        thumbsCount = reply.thumbsCount,
                        articleType = reply.articleType!!,
                        clipUrl = reply.clipUrl,
                        memberNickname = reply.member.nickname!!,
                        memberProfilePath = reply.member.profilePath,
                    )
                )
            }

            returnCommentList.add(
                CommentRes(
                    id = comment.id!!,
                    createAt = comment.createAt,
                    updateAt = if (comment.modified == true) comment.updateAt else null,
                    parentId = comment.parentId,
                    memberId = comment.member.id,
                    content = comment.content,
                    thumbsUp = thumbsUps?.find { it.article.id == comment.id!! && it.member.id == memberId } != null,
                    thumbsCount = comment.thumbsCount,
                    articleType = comment.articleType!!,
                    clipUrl = comment.clipUrl,
                    memberNickname = comment.member.nickname!!,
                    memberProfilePath = comment.member.profilePath,
                    replys = returnReplyList,
                )
            )
        }

        return returnCommentList
    }

    fun repliesConvertToArticleRes(
        replies: List<Article>,
        thumbsUps: List<ThumbsUp>?,
        memberId: Long
    ): MutableList<ArticleRes> {
        val returnReplyList: MutableList<ArticleRes> = mutableListOf()
        replies.forEach { reply ->
            returnReplyList.add(
                ArticleRes(
                    id = reply.id!!,
                    createAt = reply.createAt,
                    updateAt = if (reply.modified == true) reply.updateAt else null,
                    parentId = reply.parentId,
                    memberId = reply.member.id,
                    content = reply.content,
                    thumbsUp = thumbsUps?.find { it.article.id == reply.id!! && it.member.id == memberId } != null,
                    thumbsCount = reply.thumbsCount,
                    articleType = reply.articleType!!,
                    clipUrl = reply.clipUrl,
                    memberNickname = reply.member.nickname!!,
                    memberProfilePath = reply.member.profilePath,
                )
            )
        }

        return returnReplyList
    }
}