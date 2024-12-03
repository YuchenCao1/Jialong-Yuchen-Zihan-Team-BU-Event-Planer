package com.example.bueventplaner.data.model

data class User(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val userProfileURL: String? = null, // 头像URL
    val userBUID: String = "",          // 学号（如适用）
    val userEmail: String = "",         // 邮箱
    val userSchool: String = "",        // 学校信息
    val userYear: String = "",          // 入学年份
    val userImage: String = "",         // 头像URL
    val userSavedEvents: List<Event> = emptyList() // 保存的事件列表
)
