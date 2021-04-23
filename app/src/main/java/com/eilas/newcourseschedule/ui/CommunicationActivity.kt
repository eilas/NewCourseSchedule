package com.eilas.newcourseschedule.ui

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.eilas.newcourseschedule.R
import com.eilas.newcourseschedule.data.getCourseForum
import com.eilas.newcourseschedule.data.like
import com.eilas.newcourseschedule.data.model.CommentMessage
import com.eilas.newcourseschedule.data.model.ReplyMessage
import com.eilas.newcourseschedule.data.saveCourseComment
import com.eilas.newcourseschedule.data.saveCourseCommentReply
import com.eilas.newcourseschedule.ui.view.adapter.CustomCommentItemAdapter
import com.eilas.newcourseschedule.ui.view.adapter.CustomReplyItemAdapter
import com.google.gson.Gson
import com.jidcoo.android.widget.commentview.CommentView
import com.jidcoo.android.widget.commentview.callback.*
import com.jidcoo.android.widget.commentview.defaults.DefaultCommentModel
import com.jidcoo.android.widget.commentview.defaults.DefaultCommentModel.Comment.Reply
import com.jidcoo.android.widget.commentview.defaults.DefaultViewStyleConfigurator
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


class CommunicationActivity : AppCompatActivity() {
    private lateinit var commentView: CommentView
    private var gson: Gson = Gson()
    private var isReply = false
    private var isChildReply = false
    private var cp = 0
    private var rp = 0
    private var fid: Long = 0
    private var pid: Long = 0

    private val handler: Handler = Handler(WeakReference(Handler.Callback {
        when (it.what) {
            1 -> {
                commentView.loadComplete(
                    gson.fromJson(
                        it.obj as String,
                        DefaultCommentModel::class.java
                    )
                )
                //commentView.loadFailed(true);//实际网络请求中如果加载失败调用此方法
            }
            2 ->                     //commentView.refreshFailed();//实际网络请求中如果加载失败调用此方法
                commentView.refreshComplete(
                    gson.fromJson(
                        it.obj as String,
                        DefaultCommentModel::class.java
                    )
                )
            3 ->                     //commentView.loadFailed();//实际网络请求中如果加载失败调用此方法
                commentView.loadMoreComplete(
                    gson.fromJson(
                        it.obj as String,
                        DefaultCommentModel::class.java
                    )
                )
            4 ->                     //commentView.loadMoreReplyFailed();//实际网络请求中如果加载失败调用此方法
                commentView.loadMoreReplyComplete(
                    gson.fromJson(
                        it.obj as String,
                        DefaultCommentModel::class.java
                    )
                )
/*
            5 -> {
                (commentView.commentList.first() as CommentMessage).id = it.obj as Long
            }
*/
            else -> {
            }
        }
        true
    }).get())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communication)
        val intent = intent
        title = intent.getStringExtra("courseName")
        val courseId = intent.getStringExtra("courseId")
        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")
        val user = findViewById<EditText>(R.id.user)
        user.apply {
            setText(userName)
            keyListener = null
        }
        val editor = findViewById<EditText>(R.id.editor)
        val button = findViewById<Button>(R.id.button)
        commentView = findViewById<CommentView>(R.id.myCommentView) //初始化控件
        commentView.apply {
            setViewStyleConfigurator(DefaultViewStyleConfigurator(this@CommunicationActivity))
            setEmptyView(TextView(this@CommunicationActivity).apply { text = "没有评论哦，快去抢沙发吧" })
            setErrorView(TextView(this@CommunicationActivity).apply { text = "ERROR" })

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.f7eed6)
            //获取callbackBuilder添加事件回调
            callbackBuilder()
                //自定义CommentItem
                .customCommentItem(CustomCommentItemAdapter(bitmap) { messageId, holder, clicked ->
                    if (!clicked) {
                        val prizes: Int = holder.prizes.text.toString().toInt()
                        holder.prizes.text = (prizes + 1).toString()
                        like(messageId, courseId, userId, this@CommunicationActivity.handler)
                    }
                    true
                })
                //自定义ReplyItem
                .customReplyItem(CustomReplyItemAdapter(bitmap) { messageId, holder, clicked ->
                    if (!clicked) {
                        val prizes: Int = holder.prizes.text.toString().toInt()
                        holder.prizes.text = (prizes + 1).toString()
                        like(messageId, courseId, userId, this@CommunicationActivity.handler)
                    }
                    true
                })
                //下拉刷新回调
                .setOnPullRefreshCallback(object : OnPullRefreshCallback {
                    override fun refreshing() {
                        getCourseForum(userId, courseId, true, this@CommunicationActivity.handler)
                    }

                    override fun complete() {
                        //加载完成后的操作
                    }

                    override fun failure(msg: String) {
                        Toast.makeText(this@CommunicationActivity, msg, Toast.LENGTH_LONG).show()
                    }
                })
                //上拉加载更多回调（加载更多评论数据）
                .setOnCommentLoadMoreCallback(object : OnCommentLoadMoreCallback {
                    override fun loading(
                        currentPage: Int,
                        willLoadPage: Int,
                        isLoadedAllPages: Boolean
                    ) {
                        //因为测试数据写死了，所以这里的逻辑也是写死的
                        if (!isLoadedAllPages) {
                            if (willLoadPage == 2) {
                                load(2, 3)
                            } else if (willLoadPage == 3) {
                                load(3, 3)
                            }
                        }
                    }

                    override fun complete() {
                        //加载完成后的操作
                    }

                    override fun failure(msg: String) {
                        Toast.makeText(this@CommunicationActivity, msg, Toast.LENGTH_LONG).show()
                    }

                })
                //回复数据加载更多回调（加载更多回复）
                .setOnReplyLoadMoreCallback(object : OnReplyLoadMoreCallback<Reply> {
                    override fun loading(reply: Reply, willLoadPage: Int) {
                        //因为测试数据写死了，所以这里的逻辑也是写死的
                        //在默认回复数据模型中，kid作为父级索引
                        //为了扩展性，把对应的具体模型传了出来，可根据具体需求具体使用
                        if (reply.getKid() == 1593699394031L) {
                            load(4, 4)
                        } else {
                            if (willLoadPage == 2) {
                                load(5, 4)
                            } else if (willLoadPage == 3) {
                                load(6, 4)
                            }
                        }
                    }

                    override fun complete() {
                        //加载完成后的操作
                    }

                    override fun failure(msg: String) {
                        Toast.makeText(this@CommunicationActivity, msg, Toast.LENGTH_LONG).show()
                    }
                })
                //评论、回复Item的点击回调（点击事件回调）
                .setOnItemClickCallback(object :
                    OnItemClickCallback<DefaultCommentModel.Comment, Reply> {
                    override fun commentItemOnClick(
                        position: Int,
                        comment: DefaultCommentModel.Comment,
                        view: View
                    ) {
                        isReply = true
                        cp = position
                        isChildReply = false
                        fid = comment.getId()
                        editor!!.hint = "回复@" + comment.getPosterName() + ":"
                    }

                    override fun replyItemOnClick(
                        c_position: Int,
                        r_position: Int,
                        reply: Reply,
                        view: View
                    ) {
                        isReply = true
                        cp = c_position
                        rp = r_position
                        isChildReply = true
                        fid = reply.getId()
                        pid = reply.getId()
                        editor!!.hint = "回复@" + reply.getReplierName() + ":"
                    }
                })
                //滚动事件回调
                .setOnScrollCallback(object : OnScrollCallback {
                    override fun onScroll(
                        view: AbsListView,
                        firstVisibleItem: Int,
                        visibleItemCount: Int,
                        totalItemCount: Int
                    ) {
                        isReply = false
                        if (editor.text.toString().isNotEmpty()) {
                            editor.setText("")
                        }
                        editor.hint = "发表你的评论吧~"
                    }

                    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
                    override fun onScrollChange(
                        v: View,
                        scrollX: Int,
                        scrollY: Int,
                        oldScrollX: Int,
                        oldScrollY: Int
                    ) {
                    }
                })
                //设置完成后必须调用CallbackBuilder的buildCallback()方法，否则设置的回调无效
                .buildCallback()
        }

        getCourseForum(userId, courseId, handler = handler)


        button.setOnClickListener {
            val userStr = user.text.toString()
            val data = editor.text.toString()
            if (userStr.isNotEmpty() && data.isNotEmpty()) {
                if (isReply && isChildReply) {
                    //现在需要构建一个回复数据实体类
                    val reply = ReplyMessage().apply {
                        setKid(fid)
                        setReplierName(userName)
                        setReply(data)
                        setDate(System.currentTimeMillis())
                        setPid(pid)
                        setPrizes(0)
                    }
                    commentView.addReply(reply, cp)
                    saveCourseCommentReply(userId, courseId, reply, handler)
                } else if (isReply && !isChildReply) {
                    //现在需要构建一个回复数据实体类
                    val reply = ReplyMessage().apply {
                        setKid(fid)
                        setReplierName(userName)
                        setReply(data)
                        setDate(System.currentTimeMillis())
                        setPid(0)
                        setPrizes(0)
                    }
                    commentView.addReply(reply, cp)
                    saveCourseCommentReply(userId, courseId, reply, handler)
                } else {
                    val comment = CommentMessage().apply {
                        setDate(System.currentTimeMillis())
                        setPid(0)
                        setPosterName(userName)
                        setComment(data)
                        setPrizes(0)
                        replies = ArrayList<ReplyMessage>() as List<Reply>?
                    }

                    commentView.addComment(comment)
                    saveCourseComment(userId, courseId, comment, handler)
                }
            } else {
                Toast.makeText(this, "用户名和内容都不能为空", Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun load(code: Int, handlerId: Int) {
//        getCourseForum(userId, "1111111", handler)
    }
}

