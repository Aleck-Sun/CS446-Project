package com.example.cs446.backend.data.repository

import android.content.Context
import android.net.Uri
import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.post.Comment
import com.example.cs446.backend.data.model.post.Follow
import com.example.cs446.backend.data.model.post.Like
import com.example.cs446.backend.data.model.post.Post
import com.example.cs446.backend.data.model.post.PostRaw
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.*
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.time.Duration

class PostRepository {
    private val storage = SupabaseClient.supabase.storage
    private val auth = SupabaseClient.supabase.auth
    private val bucketName = "posts"
    private val postTable = SupabaseClient.supabase.from("posts")
    private val commentTable = SupabaseClient.supabase.from("comments")
    private val likeTable = SupabaseClient.supabase.from("likes")
    private val userRepository = UserRepository()
    private val petRepository = PetRepository()
    private val followTable = SupabaseClient.supabase.from("pet-followers")

    suspend fun uploadPost(
        imageUrls: List<String>,
        petId: UUID,
        caption: String,
        isPublic: Boolean = false
    ): UUID {
        return postTable.insert(
            mapOf (
                "user_id" to auth.currentUserOrNull()?.id,
                "pet_id" to petId,
                "image_urls" to imageUrls,
                "caption" to caption,
                "is_public" to isPublic
            )
        ) {
            select()
        }.decodeSingle<PostRaw>().id
    }

    suspend fun uploadPostImages(
        context: Context,
        imageUris: List<Uri>,
        petId: UUID
    ): List<String> {
        // TODO - will test image uploading later when post backend is made
        val imageUrls = mutableListOf<String>()
        try {
            imageUris.forEach {
                imageUri -> val fileName = "${petId}/${System.currentTimeMillis()}.jpg"

                // get input stream from URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes()

                if (imageBytes != null) {
                    // upload to supabase
                    storage.from(bucketName).upload(fileName, imageBytes)

                    // return public url
                    imageUrls.add(fileName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageUrls
    }

    suspend fun uploadComment(postId: UUID, text: String): Comment? {
        return try {
            val authorId = auth.currentUserOrNull()!!.id
            val result = commentTable.insert(
                mapOf(
                    "author_id" to authorId,
                    "text" to text,
                    "post_id" to postId
                )
            ) {
                select()
            }.decodeSingle<Comment>()
            val user = userRepository.getUserById(UUID.fromString(authorId))
            return result.copy(authorName = user?.username)
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun getSignedImageUrl(imageUrl: String): String {
        return try {
            storage.from(bucketName).createSignedUrl(
                path = imageUrl,
                expiresIn = Duration.parse("1h")  // expires in 1 hour
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun getCommentsForPost(postId: UUID, amount: Long? = null): List<Comment> {
        return try {
            val comments = commentTable.select {
                filter { eq("post_id", postId) }
                amount?.let {
                    limit(it)
                }
            }.decodeList<Comment>()

            return comments.map {
                val user = userRepository.getUserById(it.authorId)
                it.copy(authorName = user?.username)
            }
        } catch(e: Exception) {
            e.printStackTrace()
            listOf<Comment>()
        }
    }

    suspend fun getLikesForPost(postId: UUID): Int {
        return try {
            likeTable.select {
                filter {
                    eq("post_id", postId)
                    eq("liked", true)
                }
                count(Count.ESTIMATED)
            }.countOrNull()!!.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    suspend fun getIfUserLikedPost(postId: UUID): Boolean {
        return try {
            likeTable.select {
                filter {
                    eq("post_id", postId)
                    eq("user_id", auth.currentUserOrNull()?.id?:"")
                    eq("liked", true)
                }
            }.decodeSingleOrNull<Like>() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateLikeStatus(postId: UUID): Boolean {
        return try {
            likeTable.upsert(
                mapOf(
                    "user_id" to auth.currentUserOrNull()?.id,
                    "post_id" to postId,
                    "liked" to !getIfUserLikedPost(postId)
                )
            )
            {
                select()
            }.decodeSingle<Like>().liked
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun loadPost(
        postId: UUID
    ): Post? {
        return try {
            var postRaw = postTable
                .select {
                    order("created_at", Order.DESCENDING)
                    filter{
                        eq("id", postId)
                    }
                }
                .decodeSingle<PostRaw>()

            // Get all users associated with loaded posts
            val user = userRepository.getUserById(userRepository.getCurrentUserId()!!)

            // Get all pets associated with loaded posts
            val pet = petRepository.getPet(postRaw.petId)

            // Get all likes associated with loaded posts
            val userPostLikes = likeTable.select {
                filter {
                    eq("post_id", postId)
                    eq("liked", true)
                }
            }.decodeList<Like>()

            val posts = postRaw.let {
                val likes = userPostLikes.count { like ->
                    like.postId == it.id
                }
                val liked = userPostLikes.any { like ->
                    like.postId == it.id && like.userId == user?.id
                }
                val followed = true
                Post(
                    id = it.id,
                    userId = it.userId,
                    petId = it.petId,
                    createdAt = it.createdAt,
                    caption = it.caption,
                    imageUrls = it.imageUrls.map {
                            url -> getSignedImageUrl(url)
                    },
                    petImageUrl = pet.imageUrl,
                    authorName = user?.username,
                    petName = pet.name,
                    comments = getCommentsForPost(it.id),
                    likes = likes,
                    liked = liked,
                    isFollowing = followed
                )
            }
            return posts
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun loadPosts(
        createdAfter: Instant? = null,
        createdBefore: Instant? = null,
        maxPosts: Long = 3,
        maxPublic: Long = 1
    ): List<Post> {
        return try {
            var postsRaw = postTable
                .select {
                    order("created_at", Order.DESCENDING)
                    filter{
                        or {
                            createdBefore?.let {
                                lt("created_at", it)
                            }
                            createdAfter?.let{
                                gt("created_at", createdAfter)
                            }
                        }
                        eq("is_public", true)
                    }
                    limit(maxPublic)
                }
                .decodeList<PostRaw>()

            postsRaw = postsRaw + postTable
                .select {
                    order("created_at", Order.DESCENDING)
                    filter{
                        or {
                            createdBefore?.let {
                                lt("created_at", it)
                            }
                            createdAfter?.let{
                                gt("created_at", createdAfter)
                            }
                        }
                        eq("is_public", false)
                    }
                    limit(maxPosts-postsRaw.size)
                }
                .decodeList<PostRaw>()

            postsRaw = postsRaw.distinctBy { it.id }

            // Get all users associated with loaded posts
            val allUsers = userRepository.getUsersByIds(
                postsRaw.map {
                    it.userId
                }.toSet().toList()
            ).associateBy { it.id }

            // Get all pets associated with loaded posts
            val allPets = petRepository.getPetsByIds(
                postsRaw.map {
                    it.petId
                }.toSet().toList()
            ).associateBy { it.id }

            // Get list of pets following associated with loaded posts
            val followedPets = followTable.select {
                filter {
                    eq("user_id", userRepository.getCurrentUserId()?:"")
                    isIn("pet_id", allPets.keys.toList())
                }
            }.decodeList<Follow>()

            // Get all likes associated with loaded posts
            val allPostIds = postsRaw.map{ it.id }.toSet().toList()
            val userPostLikes = likeTable.select {
                filter {
                    isIn("post_id", allPostIds)
                    eq("liked", true)
                }
            }.decodeList<Like>()

            val posts = postsRaw.map {
                val user = allUsers.getValue(it.userId)
                val pet = allPets.getValue(it.petId)
                val likes = userPostLikes.count { like ->
                    like.postId == it.id
                }
                val liked = userPostLikes.any { like ->
                    like.postId == it.id && like.userId == user.id
                }
                val followed = followedPets.any {
                    follow -> follow.petId == pet.id
                }
                Post(
                    id = it.id,
                    userId = it.userId,
                    petId = it.petId,
                    createdAt = it.createdAt,
                    caption = it.caption,
                    imageUrls = it.imageUrls.map {
                        url -> getSignedImageUrl(url)
                    },
                    petImageUrl = pet.imageUrl,
                    authorName = user.username,
                    petName = pet.name,
                    comments = getCommentsForPost(it.id),
                    likes = likes,
                    liked = liked,
                    isFollowing = followed
                )
            }
            return posts
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList<Post>()
        }
    }

    suspend fun loadProfilePosts(
        userID: UUID? = null,
        petID: UUID? = null
    ): List<Post> {
        return try {
            val postsRaw = postTable
                .select {
                    order("created_at", Order.DESCENDING)
                    filter{
                        userID?.let {
                            eq("user_id", userID)
                        }
                    }
                }
                .decodeList<PostRaw>()

            // Get all users associated with loaded posts
            val allUsers = userRepository.getUsersByIds(
                postsRaw.map {
                    it.userId
                }.toSet().toList()
            ).associateBy { it.id }

            // Get all pets associated with loaded posts
            val allPets = petRepository.getPetsByIds(
                postsRaw.map {
                    it.petId
                }.toSet().toList()
            ).associateBy { it.id }

            // Get all likes associated with loaded posts
            val allPostIds = postsRaw.map{ it.id }.toSet().toList()
            val userPostLikes = likeTable.select {
                filter {
                    isIn("post_id", allPostIds)
                    eq("liked", true)
                }
            }.decodeList<Like>()

            val posts = postsRaw.map {
                val user = allUsers.getValue(it.userId)
                val pet = allPets.getValue(it.petId)
                val likes = userPostLikes.count { like ->
                    like.postId == it.id
                }
                val liked = userPostLikes.any { like ->
                    like.postId == it.id && like.userId == user.id
                }
                Post(
                    id = it.id,
                    userId = it.userId,
                    petId = it.petId,
                    createdAt = it.createdAt,
                    caption = it.caption,
                    imageUrls = it.imageUrls.map {
                            url -> getSignedImageUrl(url)
                    },
                    petImageUrl = pet.imageUrl,
                    authorName = user.username,
                    petName = pet.name,
                    comments = getCommentsForPost(it.id),
                    likes = likes,
                    liked = liked,
                )
            }
            return posts
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList<Post>()
        }
    }
}
