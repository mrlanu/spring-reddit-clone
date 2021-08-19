package com.github.mrlanu.springredditclone

fun User.toUserDto() = UserResponseDTO(
    userId = publicId,
    username = username,
    email = email
)

fun Subreddit.toSubredditDto() = SubredditDto(
    id = subredditId,
    name = name,
    description = description
)

fun SubredditDto.toSubreddit() = Subreddit(
    name = name,
    description = description
)
