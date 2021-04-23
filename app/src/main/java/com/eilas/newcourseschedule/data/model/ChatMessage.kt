package com.eilas.newcourseschedule.data.model

import com.jidcoo.android.widget.commentview.defaults.DefaultCommentModel


data class CommentMessage(val any: Any? = null) : DefaultCommentModel.Comment()

data class ReplyMessage(val any: Any? = null) : DefaultCommentModel.Comment.Reply()