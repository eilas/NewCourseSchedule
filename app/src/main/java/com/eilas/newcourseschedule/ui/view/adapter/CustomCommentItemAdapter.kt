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
import com.jidcoo.android.widget.commentview.callback.CustomCommentItemCallback
import com.jidcoo.android.widget.commentview.defaults.DefaultCommentHolder
import com.jidcoo.android.widget.commentview.defaults.DefaultCommentModel
import com.jidcoo.android.widget.commentview.utils.ViewUtil
import com.jidcoo.android.widget.commentview.view.RoundAngleImageView

/**
 * @param bitmap 自定义评论item的头像背景
 * @param clickMethod holder里prize的点击事件
 *
 * @author Eilas
 */
class CustomCommentItemAdapter(
    val bitmap: Bitmap,
    val clickMethod: (Long, DefaultCommentHolder, Boolean) -> Boolean
) : CustomCommentItemCallback<DefaultCommentModel.Comment> {
    override fun buildCommentItem(
        groupPosition: Int,
        comment: DefaultCommentModel.Comment?,
        inflater: LayoutInflater?,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val defaultCommentHolder: DefaultCommentHolder
        val view: View
        val tmp = comment as DefaultCommentModel.Comment
        if (convertView == null) {
            view = inflater!!.inflate(R.layout.item_comment, parent, false)
            view.findViewById<RoundAngleImageView>(com.jidcoo.android.widget.commentview.R.id.ico)
                .setImageBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true).apply {
                    Canvas(this).apply {
                        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                            textSize = 90F
                            textAlign = Paint.Align.CENTER
                            color = Color.BLACK
                        }
                        val text = tmp.getPosterName().first().toString()
                        drawText(
                            text,
                            width / 2F,
                            (height - (paint.descent() + paint.ascent())) / 2,
                            paint
                        )
                    }
                })
            defaultCommentHolder = DefaultCommentHolder(view)
            view.tag = defaultCommentHolder
        } else {
            view = convertView
            defaultCommentHolder = view.tag as DefaultCommentHolder
        }
        defaultCommentHolder.apply {
            posterName.text = tmp.getPosterName()
            content.text = tmp.getComment()
            time.text = ViewUtil.getTime(tmp.getDate())
            prizes.text = tmp.getPrizes().toString()
            prize.tag = defaultCommentHolder.prizes.text
            var clicked = false
            prize.setOnClickListener {
                clicked = clickMethod(tmp.id, defaultCommentHolder, clicked)
            }
        }
        return view
    }
}