package com.example.app


/**
 * @author Miroslav Hýbler <br>
 * created on 23.09.2023
 */
object ImagesForPreview {

    private val usersList: List<Int> = listOf(
        R.mipmap.img_user,
        R.mipmap.img_user_2
    )


    val randomUserImage: Int
        get() = usersList.random()
}