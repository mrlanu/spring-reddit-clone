package com.github.mrlanu.springredditclone

import java.time.LocalDateTime
import javax.persistence.*


@Entity
class User (
    val username: String,
    val password: String,
    val email: String,
    val created: LocalDateTime,
    var enabled: Boolean,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var userId: Long? = null)

@Entity
class Post (
    var postName: String,
    var url: String,
    var description: String,
    var voteCount: Int,
    @ManyToOne var user: User,
    var createdDate: LocalDateTime,
    @ManyToOne var subreddit: Subreddit,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var postId: Long? = null)

@Entity
class Subreddit (
    var name: String,
    var description: String,
    @OneToMany var posts: List<Post>,
    var createdDate: LocalDateTime,
    @ManyToOne var user: User,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var subredditId: Long? = null)

@Entity
class Comment (
    var text: String,
    @ManyToOne var post: Post,
    var createdDate: LocalDateTime,
    @ManyToOne var user: User,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var commentId: Long? = null)

@Entity
class Vote (
    var voteType: VoteType,
    @ManyToOne var post: Post,
    @ManyToOne var user: User,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var voteId: Long? = null)

@Entity
class RefreshToken (
    var token: String,
    var createdDate: LocalDateTime,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null)

@Entity(name = "token")
class VerificationToken (
    var token: String,
    @OneToOne var user: User,
    var expiryDate: LocalDateTime,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var tokenId: Long? = null)

class NotificationEmail (
    var subject: String,
    var recipient: String,
    var body: String)

enum class VoteType(direction: Int) {
    UPVOTE(1), DOWNVOTE(-1);

    private val direction = 0

    fun lookup(direction: Int):
            VoteType = values().first { voteType -> voteType.direction == direction }
}
