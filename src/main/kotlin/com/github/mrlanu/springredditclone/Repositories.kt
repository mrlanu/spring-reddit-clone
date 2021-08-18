package com.github.mrlanu.springredditclone

import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long>

interface SubredditRepository : JpaRepository<Subreddit, Long>

interface UserRepository : JpaRepository<User, Long>{
    fun findByEmail(email: String): User?
}

interface RoleRepository : JpaRepository<Role, Long>{
    fun findByRoleName(name: String): Role?
}

interface VoteRepository : JpaRepository<Vote, Long>

interface CommentRepository : JpaRepository<Comment, Long>

interface VerificationTokenRepository : JpaRepository<VerificationToken, Long> {
    fun findByToken(token: String): VerificationToken?
}
