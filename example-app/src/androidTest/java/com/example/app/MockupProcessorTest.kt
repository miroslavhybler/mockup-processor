package com.example.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mockup.core.Mockup
import com.mockup.exampledata.Article
import com.mockup.exampledata.Category
import com.mockup.exampledata.Publisher
import com.mockup.exampledata.Reader
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MockupProcessorTest {

    @Test
    fun testArticle() {
        val article = Mockup.get<Article>()
        assertNotNull(article)
        assertTrue(article.title.isNotEmpty())
        assertTrue(article.content.isNotEmpty())
        assertNotNull(article.author)
        assertTrue(article.tags.isNotEmpty())
        assertTrue(article.categories.isNotEmpty())
        assertNotNull(article.isSpecialEdition)
        assertTrue(article.imageUrl.isNotEmpty())
        assertTrue(article.gallery.isNotEmpty())
        assertTrue(article.createdAt.isNotEmpty())
        assertNotNull(article.type)
        assertNotNull(article.rating)
        assertNotNull(article.readersCount)
        assertNotNull(article.minutesReading)
        assertNotNull(article.item)

        // Test lazy property
        assertTrue(article.createdAtFormatted.isNotEmpty())
    }

    @Test
    fun testCategory() {
        val category = Mockup.get<Category>()
        assertNotNull(category)
        assertTrue(category.name.isNotEmpty())
        assertNotNull(category.color)

        // Test computed property
        assertTrue(category.formattedName.isNotEmpty())
    }

    @Test
    fun testPublisher() {
        val publisher = Mockup.get<Publisher>()
        assertNotNull(publisher)
        assertTrue(publisher.firstName.isNotEmpty())
        assertTrue(publisher.lastName.isNotEmpty())
        assertTrue(publisher.dateOfBirth.isNotEmpty())
        // description is nullable
        // themeImageUrl is nullable
        // avatarUrl is nullable
        assertNotNull(publisher.articlesCount)
        assertNotNull(publisher.authorRank)

        // Test @IgnoreOnMockup
        assertNull(publisher.someUnknownObject)

        // Test computed property
        assertTrue(publisher.fullName.isNotEmpty())
        assertTrue(publisher.fullName != "John Doe")
    }

    @Test
    fun testNestedClasses() {
        val item = Mockup.get<Article.Item>()
        assertNotNull(item)
        assertTrue(item.name.isNotEmpty())

        val galleryPhoto = Mockup.get<Article.GalleryPhoto>()
        assertNotNull(galleryPhoto)
        assertTrue(galleryPhoto.imageUrl.isNotEmpty())
        assertTrue(galleryPhoto.type.isNotEmpty())
    }

    @Test
    fun testClassesWithSameName() {
        val articleItem = Mockup.get<Article.Item>()
        assertNotNull(articleItem)
        assertTrue(articleItem.name.isNotEmpty())

        val readerItem = Mockup.get<Reader.Item>()
        assertNotNull(readerItem)
        assertTrue(readerItem.name.isNotEmpty())
    }
}
