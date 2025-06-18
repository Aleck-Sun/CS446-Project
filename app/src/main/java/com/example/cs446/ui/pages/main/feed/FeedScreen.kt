import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.cs446.backend.data.model.Comment
import com.example.cs446.backend.data.model.Post
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
    val posts by viewModel.posts.collectAsState()

    fun onLoadMorePosts() {
        viewModel.loadMorePosts()
    }
    fun onLike(postId: UUID) {
        // TODO
    }
    fun onComment(postId: UUID) {
        // TODO
    }
    fun onShare(postId: UUID) {
        // TODO
    }
    fun onCreatePost() {

    }

    FeedContent(
        posts,
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
    onLoadMorePosts: () -> Unit = {},
    onLike: (UUID) -> Unit = {},
    onComment: (UUID) -> Unit = {},
    onShare: (UUID) -> Unit = {},
    onCreatePost: () -> Unit = {}
) {
    var showAddPostDialog by remember { mutableStateOf(false) }
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
                            onClick = { showAddPostDialog = true }
                        )
                    }
                    PostItem(post, onLike, onComment, onShare)
                }
            }
            if (showAddPostDialog) {
                CreatePostDialog(
                    onPost = onCreatePost,
                    onDismiss = { showAddPostDialog = false }
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
    onLike: (UUID) -> Unit,
    onComment: (UUID) -> Unit,
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
            if (post.photoUrls.isNotEmpty()) {
                ImageCarousel(post.photoUrls)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Description + location + timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = post.text)
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
                        onComment(post.id)
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
            post.comments.forEach { comment ->
                CommentText(author = comment.authorName?:"User", comment = comment.text)
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
            photoUrls = listOf(""),
            text = "Cute puppy at play!",
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
            photoUrls = listOf(""),
            text = "Look at this kitty!",
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