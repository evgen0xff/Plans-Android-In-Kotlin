package com.planscollective.plansapp.models.dataModels

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.planscollective.plansapp.extension.toArrayList
import kotlinx.parcelize.Parcelize

@Parcelize
open class PostModel(
    @Expose
    @SerializedName("_id")
    var id: String? = null,

    @Expose
    @SerializedName("postType")
    var postType: String? = null,

    @Expose
    @SerializedName("postThumbnail")
    var postThumbnail: String? = null,

    @Expose
    @SerializedName("postText")
    var postText: String? = null,

    @Expose
    @SerializedName("postMedia")
    var postMedia: String? = null,

    @Expose
    @SerializedName("commentType")
    var commentType: String? = null,

    @Expose
    @SerializedName("commentThumbnail")
    var commentThumbnail: String? = null,

    @Expose
    @SerializedName("commentText")
    var commentText: String? = null,

    @Expose
    @SerializedName("commentMedia")
    var commentMedia: String? = null,

    @Expose
    @SerializedName("message")
    var message: String? = null,

    @Expose
    @SerializedName("createdAt")
    var createdAt: Number? = null,

    @Expose
    @SerializedName("userId")
    var user: UserModel? = null,

    @Expose
    @SerializedName("likes")
    var likes: ArrayList<UserLikedModel>? = null,

    @Expose
    @SerializedName("comments")
    var comments: ArrayList<PostModel>? = null,

    @Expose
    @SerializedName("likesCounts")
    var likesCounts: Int? = null,

    @Expose
    @SerializedName("commentsCounts")
    var commentsCounts: Int? = null,

    @Expose
    @SerializedName("width")
    var width: Number? = null,

    @Expose
    @SerializedName("height")
    var height: Number? = null,

    @Expose
    @SerializedName("isLike")
    var isLike: Boolean? = null,

) : Parcelable {

    val type : String?
        get() = postType ?: commentType

    val text: String?
        get() = postText ?: commentText

    val isMediaType: Boolean
        get() = (type == "image" || type == "video")

    val urlMedia: String?
        get() = postMedia ?: commentMedia

    val urlThumbnail: String?
        get() = postThumbnail ?: commentThumbnail

    val isComment: Boolean
        get(){
            var result = false
            if (postText != null || (postType != null && postMedia != null)) {
                result = false
            }else if (commentText != null || (commentType != null && commentMedia != null)) {
                result = true
            }
            return result
        }

    fun prepare() : PostModel {
        likes = likes?.sortedByDescending { it.createdAt?.toLong() }?.toArrayList()
        comments?.forEach { it.prepare() }

        return this
    }
}
