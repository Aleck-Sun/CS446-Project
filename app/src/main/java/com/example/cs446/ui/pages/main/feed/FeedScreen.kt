import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.cs446.ui.pages.main.formatDate
import com.example.cs446.ui.theme.CS446Theme
import com.example.cs446.view.social.FeedViewModel
import kotlinx.datetime.Instant
import java.util.UUID

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

    fun onLoadMorePosts() {
        viewModel.loadMorePosts()
    }
    fun onLike(postId: UUID) {
        // TODO
    }
    fun onComment(postId: UUID, text: String) {
        // TODO
    }
    fun onShare(postId: UUID) {
        // TODO
    }
    fun onCreatePost(context: Context, text: String, petId: UUID, imageUris: List<Uri>) {
        viewModel.uploadPost(
            context = context,
            petId = petId,
            caption = text,
            imageUris = imageUris
        )
    }

    FeedContent(
        posts,
        pets,
        postResult,
        ::onLoadMorePosts,
        ::onLike,
        ::onComment,
        ::onShare,
        ::onCreatePost,
    )
}

@Composable
fun FeedContent(
    posts: List<Post>,
    pets: List<Pet> = emptyList(),
    postResult: PostResult = PostResult.PostSuccess,
    onLoadMorePosts: () -> Unit = {},
    onLike: (UUID) -> Unit = {},
    onComment: (UUID, String) -> Unit = {_, _->},
    onShare: (UUID) -> Unit = {},
    onCreatePost: (Context, String, UUID, List<Uri>) -> Unit = {_, _, _, _ -> }
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
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(posts) { index, post ->
                    if (index >= posts.size - 3) {
                        // Load more when near end of list
                        onLoadMorePosts()
                    }
                    if (index == 0) {
                        AddPostButton(
                            onClick = {
                                showAddPostDialog = true
                            }
                        )
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
fun AddPostButton(
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            IconWithText(
                Icons.Default.AddCircleOutline,
                "Post Something...",
                fontSize = 24.sp,
                onClick = onClick
            )
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
                if (post.userProfileUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(post.userProfileUrl),
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
                    text = formatDate(post.createdAt),
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

            // Top Comments
            if (isExpanded)
            {
                CommentSection(
                    post,
                    onComment
                )
            } else {
                post.comments.firstOrNull()?.let {
                    CommentText(author = it.authorName?:"User", comment = it.text)
                }
            }


        }
    }
}

@Composable
fun CommentSection(
    post: Post,
    onComment: (UUID, String) -> Unit
) {
    var commentText by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        // Preview of existing comments
        post.comments.forEach {
            CommentText(author = it.authorName?:"User", comment = it.text)
        }

        OutlinedTextField(
            value = commentText ?: "",
            onValueChange = { commentText = it },
            placeholder = {
                Text(
                    "Write a comment...",
                    fontSize = 14.sp // slightly larger font helps readability
                )
            },
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .defaultMinSize(minHeight = 56.dp) // adaptive min height
        )

        Spacer(modifier = Modifier.padding(4.dp))

        Button(
            onClick = {
                commentText?.let{

                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFcb85ed)
            )
        ) {
            Text("Post", color = Color.White)
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
            comments = listOf(
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
            comments = listOf(
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

    FeedContent(posts = samplePosts)
}