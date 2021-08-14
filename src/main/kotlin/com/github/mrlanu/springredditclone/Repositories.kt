package com.github.mrlanu.springredditclone

import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long>

interface SubredditRepository : JpaRepository<Subreddit, Long>

interface UserRepository : JpaRepository<User, Long>

interface VoteRepository : JpaRepository<Vote, Long>

interface CommentRepository : JpaRepository<Comment, Long>

interface VerificationTokenRepository : JpaRepository<VerificationToken, Long>
