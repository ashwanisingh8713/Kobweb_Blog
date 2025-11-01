package com.example.blogmultiplatform.data

import com.example.shared.Category
import com.example.blogmultiplatform.models.Constants.POSTS_PER_PAGE
import com.example.blogmultiplatform.models.Newsletter
import com.example.blogmultiplatform.models.Post
import com.example.blogmultiplatform.models.PostWithoutDetails
import com.example.blogmultiplatform.models.User
import com.example.blogmultiplatform.models.ChatMessage
import com.example.blogmultiplatform.models.ChatRoom
import com.example.blogmultiplatform.util.Constants.DATABASE_NAME
import com.example.blogmultiplatform.util.Constants.MAIN_POSTS_LIMIT
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes.descending
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.varabyte.kobweb.api.data.add
import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

@InitApi
fun initMongoDB(ctx: InitApiContext) {
    System.setProperty(
        "org.litote.mongo.test.mapping.service",
        "org.litote.kmongo.serialization.SerializationClassMappingTypeService"
    )
    ctx.data.add(MongoDB(ctx))
}

class MongoDB(private val context: InitApiContext) : MongoRepository {
    // For testing with a localhost.
//    private val client = MongoClient.create()
    // For a remote mongo database.

    //val connectionString = "mongodb+srv://ashwani_u:ashwani_p@cluster0.2k09rpz.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
    // Prefer an environment variable for the connection string to avoid committing credentials.
    // If MONGODB_URI is not set, fall back to the existing connection string but log a warning.
    private val connectionString: String = System.getenv("MONGODB_URI")?.takeIf { it.isNotBlank() }
        ?: run {
            // Fallback (already committed in repo). It's recommended to set MONGODB_URI in the environment.
            context.logger.warn("MONGODB_URI not set; falling back to embedded connection string. Set MONGODB_URI for production use.")
            "mongodb+srv://abhisheksfs6892:csS9PX9q1Tx76GMp@cluster0.rhaswg4.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
        }

    private val client = MongoClient.create(connectionString)

    private val database = client.getDatabase(DATABASE_NAME)
    private val userCollection = database.getCollection<User>("user")
    private val postCollection = database.getCollection<Post>("post")
    private val newsletterCollection = database.getCollection<Newsletter>("newsletter")
    private val chatCollection = database.getCollection<ChatMessage>("chat")
    private val chatRoomCollection = database.getCollection<ChatRoom>("chatRoom")

    override suspend fun addPost(post: Post): Boolean {
        return postCollection.insertOne(post).wasAcknowledged()
    }

    override suspend fun updatePost(post: Post): Boolean {
        return postCollection
            .updateOne(
                Filters.eq(Post::_id.name, post._id),
                mutableListOf(
                    Updates.set(Post::title.name, post.title),
                    Updates.set(Post::subtitle.name, post.subtitle),
                    Updates.set(Post::category.name, post.category),
                    Updates.set(Post::thumbnail.name, post.thumbnail),
                    Updates.set(Post::content.name, post.content),
                    Updates.set(Post::main.name, post.main),
                    Updates.set(Post::popular.name, post.popular),
                    Updates.set(Post::sponsored.name, post.sponsored)
                )
            )
            .wasAcknowledged()
    }

    override suspend fun readMyPosts(skip: Int, author: String): List<PostWithoutDetails> {
        return postCollection
            .withDocumentClass(PostWithoutDetails::class.java)
            .find(Filters.eq(PostWithoutDetails::author.name, author))
            .sort(descending(PostWithoutDetails::date.name))
            .skip(skip)
            .limit(POSTS_PER_PAGE)
            .toList()
    }

    override suspend fun readMainPosts(): List<PostWithoutDetails> {
        return postCollection
            .withDocumentClass(PostWithoutDetails::class.java)
            .find(Filters.eq(PostWithoutDetails::main.name, true))
            .sort(descending(PostWithoutDetails::date.name))
            .limit(MAIN_POSTS_LIMIT)
            .toList()
    }

    override suspend fun readLatestPosts(skip: Int): List<PostWithoutDetails> {
        return postCollection
            .withDocumentClass(PostWithoutDetails::class.java)
            .find(
                Filters.and(
                    Filters.eq(PostWithoutDetails::popular.name, false),
                    Filters.eq(PostWithoutDetails::main.name, false),
                    Filters.eq(PostWithoutDetails::sponsored.name, false)
                )
            )
            .sort(descending(PostWithoutDetails::date.name))
            .skip(skip)
            .limit(POSTS_PER_PAGE)
            .toList()
    }

    override suspend fun readSponsoredPosts(): List<PostWithoutDetails> {
        return postCollection
            .withDocumentClass(PostWithoutDetails::class.java)
            .find(Filters.eq(PostWithoutDetails::sponsored.name, true))
            .sort(descending(PostWithoutDetails::date.name))
            .limit(2)
            .toList()
    }

    override suspend fun readPopularPosts(skip: Int): List<PostWithoutDetails> {
        return postCollection
            .withDocumentClass(PostWithoutDetails::class.java)
            .find(Filters.eq(PostWithoutDetails::popular.name, true))
            .sort(descending(PostWithoutDetails::date.name))
            .skip(skip)
            .limit(POSTS_PER_PAGE)
            .toList()
    }

    override suspend fun deleteSelectedPosts(ids: List<String>): Boolean {
        return postCollection
            .deleteMany(Filters.`in`(Post::_id.name, ids))
            .wasAcknowledged()
    }

    override suspend fun searchPostsByTittle(query: String, skip: Int): List<PostWithoutDetails> {
        val regexQuery = query.toRegex(RegexOption.IGNORE_CASE)
        return postCollection
            .withDocumentClass(PostWithoutDetails::class.java)
            .find(Filters.regex(PostWithoutDetails::title.name, regexQuery.pattern))
            .sort(descending(PostWithoutDetails::date.name))
            .skip(skip)
            .limit(POSTS_PER_PAGE)
            .toList()
    }

    override suspend fun searchPostsByCategory(
        category: Category,
        skip: Int
    ): List<PostWithoutDetails> {
        return postCollection
            .withDocumentClass(PostWithoutDetails::class.java)
            .find(Filters.eq(PostWithoutDetails::category.name, category))
            .sort(descending(PostWithoutDetails::date.name))
            .skip(skip)
            .limit(POSTS_PER_PAGE)
            .toList()
    }

    override suspend fun readSelectedPost(id: String): Post {
        return postCollection.find(Filters.eq(Post::_id.name, id)).toList().first()
    }

    override suspend fun checkUserExistence(user: User): User? {
        return try {
            userCollection
                .find(
                    Filters.and(
                        Filters.eq(User::username.name, user.username),
                        Filters.eq(User::password.name, user.password),
                        Filters.eq(User::role.name, user.role)
                    )
                ).firstOrNull()
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            null
        }
    }

    override suspend fun checkUserId(id: String): Boolean {
        return try {
            val documentCount = userCollection.countDocuments(Filters.eq(User::_id.name, id))
            documentCount > 0
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            false
        }
    }

    override suspend fun subscribe(newsletter: Newsletter): String {
        val result = newsletterCollection
            .find(Filters.eq(Newsletter::email.name, newsletter.email))
            .toList()
        return if (result.isNotEmpty()) {
            "You're already subscribed."
        } else {
            val newEmail = newsletterCollection
                .insertOne(newsletter)
                .wasAcknowledged()
            if (newEmail) "Successfully Subscribed!"
            else "Something went wrong. Please try again later."
        }
    }

    override suspend fun createUser(user: User): User? {
        return try {
            val exists = userCollection.countDocuments(Filters.eq(User::username.name, user.username)) > 0
            if (exists) return null
            val result = userCollection.insertOne(user)
            if (result.wasAcknowledged()) user else null
        } catch (e: Exception) {
            context.logger.error(e.message.toString())
            null
        }
    }

    // Chat-related methods
    suspend fun sendMessage(message: ChatMessage): Boolean {
        return try {
            chatCollection.insertOne(message).wasAcknowledged()
        } catch (e: Exception) {
            context.logger.error("Error sending message: ${e.message}")
            false
        }
    }

    suspend fun getMessages(senderId: String, receiverId: String, skip: Int = 0, limit: Int = 50): List<ChatMessage> {
        return try {
            chatCollection
                .find(
                    Filters.or(
                        Filters.and(
                            Filters.eq(ChatMessage::senderId.name, senderId),
                            Filters.eq(ChatMessage::receiverId.name, receiverId)
                        ),
                        Filters.and(
                            Filters.eq(ChatMessage::senderId.name, receiverId),
                            Filters.eq(ChatMessage::receiverId.name, senderId)
                        )
                    )
                )
                .sort(descending(ChatMessage::timestamp.name))
                .skip(skip)
                .limit(limit)
                .toList()
                .reversed() // Reverse to get chronological order
        } catch (e: Exception) {
            context.logger.error("Error getting messages: ${e.message}")
            emptyList()
        }
    }

    suspend fun getChatRooms(userId: String): List<ChatRoom> {
        return try {
            chatRoomCollection
                .find(Filters.`in`(ChatRoom::participants.name, userId))
                .sort(descending(ChatRoom::createdAt.name))
                .toList()
        } catch (e: Exception) {
            context.logger.error("Error getting chat rooms: ${e.message}")
            emptyList()
        }
    }

    suspend fun createChatRoom(chatRoom: ChatRoom): Boolean {
        return try {
            // Check if chat room already exists between these participants
            val existingRoom = chatRoomCollection
                .find(Filters.all(ChatRoom::participants.name, chatRoom.participants))
                .firstOrNull()

            if (existingRoom != null) {
                false // Room already exists
            } else {
                chatRoomCollection.insertOne(chatRoom).wasAcknowledged()
            }
        } catch (e: Exception) {
            context.logger.error("Error creating chat room: ${e.message}")
            false
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            userCollection
                .find()
                .toList()
        } catch (e: Exception) {
            context.logger.error("Error getting all users: ${e.message}")
            emptyList()
        }
    }
}