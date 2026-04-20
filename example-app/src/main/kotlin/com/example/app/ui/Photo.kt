package com.example.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.app.ImagesForPreview


/**
 * @author Miroslav Hýbler <br>
 * created on 23.09.2023
 */
@Composable
fun Photo(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentScale: ContentScale = ContentScale.Crop,
) {

    val isInspection = LocalInspectionMode.current

    if (isInspection) {
        // For better experience with @Preview, local image is used instead of image from url

        val userImage = remember { ImagesForPreview.randomUserImage }

        Image(
            painter = painterResource(id = userImage),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
        return
    }


    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
        loading = { state ->
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(size = 24.dp)
                        .align(alignment = Alignment.Center)
                )
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Red)
            )
        }
    )

}