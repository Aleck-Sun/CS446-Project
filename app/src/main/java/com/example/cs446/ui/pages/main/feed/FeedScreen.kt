import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cs446.backend.data.model.Comment
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.model.Post
import com.example.cs446.backend.data.result.PostResult
import com.example.cs446.ui.components.feed.CommentText
import com.example.cs446.ui.components.feed.FollowButton
import com.example.cs446.ui.components.feed.IconWithText
import com.example.cs446.ui.components.feed.ImageCarousel
import com.example.cs446.ui.pages.main.MainActivityDestination
import com.example.cs446.ui.pages.main.feed.CreatePostDialog
import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.view.social.FeedViewModel
import kotlinx.datetime.Instant
import java.util.UUID

// helper function to calculate relative time
fun getRelativeTime(timestamp: Instant): String {
    val now = kotlinx.datetime.Clock.System.now()
    val duration = now - timestamp
    
    return when {
        duration.inWholeMinutes < 1 -> "now"
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m"
        duration.inWholeHours < 24 -> "${duration.inWholeHours}h"
        duration.inWholeDays < 7 -> "${duration.inWholeDays}d"
        else -> "${duration.inWholeDays / 7}w"
    }
}

@Composable
fun FeedScreen(
    onNavigate: (MainActivityDestination, String?) -> Unit,
    viewModel: FeedViewModel,
    modifier: Modifier = Modifier,
) {
    viewModel.getPetsWithPostPermissions()
    val posts by viewModel.posts.collectAsState()
    val pets by viewModel.pets.collectAsState()
    val postResult by viewModel.postState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    fun onLoadMorePosts() {
        viewModel.loadMorePosts()
    }
    fun onLike(postId: UUID) {
        viewModel.likePost(postId)
    }
    fun onComment(postId: UUID, text: String) {
        viewModel.uploadComment(postId, text)
    }
    fun onShare(postId: UUID) {
        // TODO
    }
    fun onCreatePost(
        context: Context,
        text: String,
        petId: UUID,
        imageUris: List<Uri>,
        isPublic: Boolean
    ) {
        viewModel.uploadPost(
            context = context,
            petId = petId,
            caption = text,
            imageUris = imageUris,
            isPublic = isPublic,
        )
    }
    fun onSearchQueryChange(query: String) {
        viewModel.updateSearchQuery(query)
    }
    fun onClearSearch() {
        viewModel.clearSearch()
    }

    FeedContent(
        posts,
        pets,
        postResult,
        searchQuery,
        ::onLoadMorePosts,
        ::onLike,
        ::onComment,
        ::onShare,
        ::onCreatePost,
        ::onSearchQueryChange,
        ::onClearSearch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedContent(
    posts: List<Post>,
    pets: List<Pet> = emptyList(),
    postResult: PostResult = PostResult.PostSuccess,
    searchQuery: String = "",
    onLoadMorePosts: () -> Unit = {},
    onLike: (UUID) -> Unit = {},
    onComment: (UUID, String) -> Unit = {_, _->},
    onShare: (UUID) -> Unit = {},
    onCreatePost: (Context, String, UUID, List<Uri>, Boolean) -> Unit = {_, _, _, _, _ -> },
    onSearchQueryChange: (String) -> Unit = {},
    onClearSearch: () -> Unit = {}
) {
    var showAddPostDialog by remember { mutableStateOf(false) }
    var expandedPostId by remember { mutableStateOf<UUID?>(null) }
    LaunchedEffect(postResult) {
        if (postResult is PostResult.PostSuccess)
        {
            showAddPostDialog = false
        }
    }

    fun onOpenCommentSection(postId: UUID) {
        expandedPostId = if (postId == expandedPostId) null else postId
    }
    CS446Theme {
        Column {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onClearSearch = onClearSearch
                    )
                }
                // add post button (only show when not searching)
                if (searchQuery.isBlank()) {
                    item {
                        AddPostButton(
                            onClick = {
                                showAddPostDialog = true
                            }
                        )
                    }
                }
                itemsIndexed(posts) { index, post ->
                    if (index >= posts.size - 2) {
                        // Load more when near end of list
                        onLoadMorePosts()
                    }
                    PostItem(
                        post = post,
                        isExpanded = expandedPostId == post.id,
                        onLike = onLike,
                        onComment = onComment,
                        onOpenCommentSection = ::onOpenCommentSection,
                        onShare = onShare
                    )
                }
                // show a message when search returns no results
                if (searchQuery.isNotBlank() && posts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No posts found for \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Try searching for something else",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            if (showAddPostDialog) {
                CreatePostDialog(
                    pets = pets,
                    onPost = onCreatePost,
                    onDismiss = { showAddPostDialog = false },
                    postResult = postResult
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { 
                Text("Search posts, pets, or owners...") 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color.Gray
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun AddPostButton(
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = "Add post",
                tint = Color(0xFFcb85ed),
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Share something about your pet...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6C757D),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFcb85ed)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Post", color = Color.White)
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    isExpanded: Boolean = false,
    onLike: (UUID) -> Unit,
    onComment: (UUID, String) -> Unit,
    onOpenCommentSection: (UUID) -> Unit,
    onShare: (UUID) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.petImageUrl != null) {
                    println(post.petImageUrl)
                    Image(
                        painter = rememberAsyncImagePainter(post.petImageUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = post.petName?:"", fontWeight = FontWeight.Bold)
                    Text(text = "From ${post.authorName}", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.weight(1f))
                FollowButton(post.isFollowing)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Media Carousel (if multiple images)
            if (post.imageUrls.isNotEmpty()) {
                ImageCarousel(post.imageUrls)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description + location + timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = post.caption)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = getRelativeTime(post.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            post.location?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                    Text(
                        text = it.toString(),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Like, Comment, Share Bar
            Row {
                IconWithText(
                    icon = Icons.Default.Favorite,
                    count = post.likes,
                    tint = if (post.liked) Color.Red else Color.Unspecified,
                    onClick = {
                        println("Click!")
                        onLike(post.id)
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconWithText(
                    icon = Icons.AutoMirrored.Filled.Comment,
                    count = post.comments.size,
                    onClick = {
                        onOpenCommentSection(post.id)
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconWithText(
                    icon = Icons.Default.Share,
                    count = 0,
                    onClick = {
                        onShare(post.id)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
                }
            ) { targetExpanded ->
                if (targetExpanded) {
                    CommentSection(
                        post,
                        onComment
                    )
                } else {
                    CommentsPreview(
                        post = post,
                        onViewAllComments = { onOpenCommentSection(post.id) },
                        onAddComment = onComment
                    )
                }
            }


        }
    }
}

@Composable
fun CommentsPreview(
    post: Post,
    onViewAllComments: () -> Unit,
    onAddComment: (UUID, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val previewCount = 2 // show top 2 comments in preview
    val hasMoreComments = post.comments.size > previewCount
    
    Column(modifier = modifier) {
        // preview
        post.comments.take(previewCount).forEach { comment ->
            CommentText(
                author = comment.authorName ?: "User",
                comment = comment.text,
                timestamp = getRelativeTime(comment.createdAt),
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
        
        // view all
        if (hasMoreComments) {
            Text(
                text = "View all ${post.comments.size} comments",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { onViewAllComments() }
            )
        }
        
        var commentText by remember { mutableStateOf("") }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFcb85ed),
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onAddComment(post.id, commentText)
                        commentText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFcb85ed)
                ),
                modifier = Modifier.padding(start = 4.dp),
                enabled = commentText.isNotBlank()
            ) {
                Text("Post", color = Color.White)
            }
        }
    }
}

@Composable
fun CommentSection(
    post: Post,
    onComment: (UUID, String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        post.comments.forEach { comment ->
            CommentText(
                author = comment.authorName ?: "User",
                comment = comment.text,
                timestamp = getRelativeTime(comment.createdAt),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFcb85ed),
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onComment(post.id, commentText)
                        commentText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFcb85ed)
                ),
                modifier = Modifier.padding(start = 4.dp),
                enabled = commentText.isNotBlank()
            ) {
                Text("Post", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SocialFeedPreview() {
    val samplePosts = listOf(
        Post(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            petId = UUID.randomUUID(),
            createdAt = Instant.fromEpochSeconds(1749934670),
            imageUrls = listOf(""),
            caption = "Cute puppy at play!",
            location = null,
            authorName = "Jane Doe",
            petName = "Rex",
            comments = mutableListOf(
                Comment(
                    id = UUID.randomUUID(),
                    authorId = UUID.randomUUID(),
                    postId = UUID.randomUUID(),
                    createdAt = Instant.fromEpochSeconds(1749934670),
                    text = "So cute! <3",
                    authorName = "John Doe"
                )
            )
        ),
        Post(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            petId = UUID.randomUUID(),
            createdAt = Instant.fromEpochSeconds(1749934670),
            imageUrls = listOf(""),
            caption = "Look at this kitty!",
            location = null,
            authorName = "Bruce Wayne",
            petName = "Mimi",
            comments = mutableListOf(
                Comment(
                    id = UUID.randomUUID(),
                    authorId = UUID.randomUUID(),
                    postId = UUID.randomUUID(),
                    createdAt = Instant.fromEpochSeconds(1749934670),
                    text = "She's adorable xoxo",
                    authorName = "Jenny"
                )
            )
        )
    )

    FeedContent(
        posts = samplePosts,
        searchQuery = "" // defaault
    )
}