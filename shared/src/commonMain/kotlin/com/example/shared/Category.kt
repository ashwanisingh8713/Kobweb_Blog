package com.example.shared

expect enum class Category {
    Technology,
    Programming,
    Design
}

interface CategoryColor {
    val color: String
}