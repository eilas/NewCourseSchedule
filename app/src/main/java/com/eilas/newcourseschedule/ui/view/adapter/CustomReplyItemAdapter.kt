package com.eilas.newcourseschedule.ui.view.adapter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eilas.newcourseschedule.R
import com.jidcoo.android.widget.commentview.callback.CustomReplyItemCallback
import com.jidcoo.android.widget.commentview.defaults.DefaultCommentModel
import com.jidcoo.android.widget.commentview.defaults.DefaultReplyHolder
import com.jidcoo.android.widget.commentview.utils.ViewUtil
import com.jidcoo.android.widget.commentview.view.RoundAngleImageView

class CustomReplyItemAdapter(
    val bitmap: Bitmap,
    val clickMethod: (Long, DefaultReplyHolder, Boolean) -> Boolean
) : CustomReplyItemCallback<DefaultCommentModel.Comment.Reply> {
    override fun buildReplyItem(
        groupPosition: Int,
        childPosition: Int,
        isLastReply: Boolean,
        reply: DefaultCommentModel.Comment.Reply?,
        inflater: LayoutInflater?,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val defaultReplyHolder: DefaultReplyHolder
        val view: View
        val tmp = reply as DefaultCommentModel.Comment.Reply
        if (convertView == null) {
            view = inflater!!.inflate(R.layout.item_reply, parent, false)
            view.findViewById<RoundAngleImageView>(com.jidcoo.android.widget.commentview.R.id.ico)
                .setImageBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true).apply {
                    Canvas(this).apply {
                        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            textSize = 90F
                            textAlign = Paint.Align.CENTER
                            color = Color.BLACK
                        }
                        val text = tmp.getReplierName().first().toString()
                        drawText(
                            text,
                            width / 2F,
                            (height - (paint.descent() + paint.ascent())) / 2,
                            paint
                        )
                    }
                })
            defaultReplyHolder = DefaultReplyHolder(view)
            view.tag = defaultReplyHolder
        } else {
            view = convertView
            defaultReplyHolder = view.tag as DefaultReplyHolder
        }
        defaultReplyHolder.apply {
            replierName.text = tmp.getReplierName()
            content.text = tmp.getReply()
            time.text = ViewUtil.getTime(tmp.getDate())
            prizes.text = tmp.getPrizes().toString()
            prize.tag = defaultReplyHolder.prizes.text
            var clicked = false
            prize.setOnClickListener {
                clicked = clickMethod(tmp.id, defaultReplyHolder, clicked)
            }
        }
        return view
    }
}