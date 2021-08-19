package com.github.mrlanu.springredditclone

import java.time.LocalDateTime

fun User.toUserDto() = UserResponseDTO(
    userId = publicId,
    username = username,
    email = email
)

fun Subreddit.toSubredditDto() = SubredditDto(
    id = subredditId,
    name = name,
    description = description,
    numberOfPosts = posts.size
)

fun SubredditDto.toSubreddit() = Subreddit(
    name = name,
    description = description
)

fun PostRequest.toPost(user: User, subreddit: Subreddit) = Post(
    postName = postName,
    url = url,
    description = description,
    voteCount = 0,
    user = user,
    createdDate = LocalDateTime.now(),
    subreddit = subreddit
)

fun Post.toPostResponse() = PostResponse(
    id = postId?: 0,
    postName = postName,
    url = url,
    description = description,
    username = user.username,
    subredditName = subreddit.name,
    voteCount = 0,
    commentCount = 0,
    duration = "",
    upVote = true,
    downVote = true
)
