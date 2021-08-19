package com.github.mrlanu.springredditclone

import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long> {
    fun getAllBySubreddit(subreddit: Subreddit): List<Post>?
}

interface SubredditRepository : JpaRepository<Subreddit, Long>{
    fun findByName(name: String): Subreddit?
}

interface UserRepository : JpaRepository<User, Long>{
    fun findByEmail(email: String): User?
    fun getUserByPublicId(publicId: String): User?
}

interface RoleRepository : JpaRepository<Role, Long>{
    fun findByRoleName(name: String): Role?
}

interface VoteRepository : JpaRepository<Vote, Long>

interface CommentRepository : JpaRepository<Comment, Long>

interface VerificationTokenRepository : JpaRepository<VerificationToken, Long> {
    fun findByToken(token: String): VerificationToken?
}
