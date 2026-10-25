package com.example.interactive.shared.model

/** 행정동 하나의 영토 상태. scores는 사람 id별 최근 30일 점수 */
data class Territory(
    val dongCode: String,
    val ownerId: String?,
    val scores: Map<String, Int>,
)
