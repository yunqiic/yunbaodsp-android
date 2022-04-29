package com.yunbao.video.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.video.R;
import com.yunbao.video.bean.VideoCommentBean;
import com.yunbao.video.http.VideoHttpUtil;
import com.yunbao.video.utils.VideoTextRender;

import java.util.List;

// +———————————————————————————————————
// | Created by Yunbao
// +———————————————————————————————————
// | Copyright (c) 2013~2022 http://www.yunbaokj.com All rights reserved.
// +———————————————————————————————————
// | Author: https://gitee.com/yunbaokeji
// +———————————————————————————————————
// | Date: 2022-04-30
// +———————————————————————————————————

public class VideoCommentAdapter extends RefreshAdapter<VideoCommentBean> {

    private static final int TEXT_PARENT = 1;
    private static final int TEXT_CHILD = 2;
    private static final int VOICE_PARENT = 3;
    private static final int VOICE_CHILD = 4;
    private Drawable mLikeDrawable;
    private Drawable mUnLikeDrawable;
    private int mLikeColor;
    private int mUnLikeColor;
    private ScaleAnimation mLikeAnimation;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private View.OnClickListener mLikeClickListener;
    private View.OnClickListener mExpandClickListener;
    private View.OnClickListener mCollapsedClickListener;
    private View.OnClickListener mVoiceClickListener;
    private View.OnClickListener mAvatarClickListener;
    private ActionListener mActionListener;
    private ImageView mCurLikeImageView;
    private int mCurLikeCommentPosition;
    private VideoCommentBean mCurLikeCommentBean;
    private HttpCallback mLikeCommentCallback;
    private String mMoreReplyTip1;
    private String mMoreReplyTip2;
    private String mVideoUid;
    private Drawable mVoicePlay;
    private Drawable mVoicePause;
    private VideoCommentBean mCurVoiceCommentBean;

    public VideoCommentAdapter(Context context) {
        super(context);
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onItemClick((VideoCommentBean) tag);
                }
            }
        };
        mOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onItemLongClick((VideoCommentBean) tag);
                }
                return true;
            }
        };
        mLikeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_comment_like_1);
        mUnLikeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_video_comment_like_0);
        mLikeColor = ContextCompat.getColor(context, R.color.global);
        mUnLikeColor = ContextCompat.getColor(context, R.color.gray3);
        mLikeAnimation = new ScaleAnimation(1f, 0.5f, 1f, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mLikeAnimation.setDuration(200);
        mLikeAnimation.setRepeatCount(1);
        mLikeAnimation.setRepeatMode(Animation.REVERSE);
        mLikeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mCurLikeCommentBean != null) {
                    if (mCurLikeImageView != null) {
                        mCurLikeImageView.setImageDrawable(mCurLikeCommentBean.getIsLike() == 1 ? mLikeDrawable : mUnLikeDrawable);
                    }
                }
            }
        });
        mLikeCommentCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0 && mCurLikeCommentBean != null) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    int like = obj.getIntValue("islike");
                    String likeNum = obj.getString("likes");
                    if (mCurLikeCommentBean != null) {
                        mCurLikeCommentBean.setIsLike(like);
                        mCurLikeCommentBean.setLikeNum(likeNum);
                        notifyItemChanged(mCurLikeCommentPosition, Constants.PAYLOAD);
                    }
                    if (mCurLikeImageView != null && mLikeAnimation != null) {
                        mCurLikeImageView.startAnimation(mLikeAnimation);
                    }
                }
            }
        };
        mLikeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonAppConfig.getInstance().isLogin()) {
                    RouteUtil.forwardLogin(mContext);
                    return;
                }
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                VideoCommentBean bean = (VideoCommentBean) tag;
                String uid = bean.getUid();
                if (!TextUtils.isEmpty(uid) && uid.equals(CommonAppConfig.getInstance().getUid())) {
                    ToastUtil.show(R.string.video_comment_cannot_self);
                    return;
                }
                mCurLikeImageView = (ImageView) v;
                mCurLikeCommentPosition = bean.getPosition();
                mCurLikeCommentBean = bean;
                VideoHttpUtil.setCommentLike(bean.getId(), mLikeCommentCallback);
            }
        };
        mExpandClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag != null && mActionListener != null) {
                    mActionListener.onExpandClicked((VideoCommentBean) tag);
                }
            }
        };
        mCollapsedClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                VideoCommentBean commentBean = (VideoCommentBean) tag;
                if (mCurVoiceCommentBean != null) {
                    VideoCommentBean parentNodeBean = commentBean.getParentNodeBean();
                    if (parentNodeBean != null) {
                        List<VideoCommentBean> childList = parentNodeBean.getChildList();
                        if (childList != null && childList.size() > 0) {
                            for (VideoCommentBean child : childList) {
                                if (mCurVoiceCommentBean.getId().equals(child.getId())) {
                                    if (mActionListener != null) {
                                        mActionListener.onVoiceStop(mCurVoiceCommentBean);
                                    }
                                    mCurVoiceCommentBean = null;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (mActionListener != null) {
                    mActionListener.onCollapsedClicked(commentBean);
                }
            }
        };
        mVoiceClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    return;
                }
                VideoCommentBean commentBean = (VideoCommentBean) tag;
                if (mCurVoiceCommentBean != null) {
                    if (mCurVoiceCommentBean.getId().equals(commentBean.getId())) {
                        commentBean.setVoicePlaying(false);
                        if (mActionListener != null) {
                            mActionListener.onVoiceStop(mCurVoiceCommentBean);
                        }
                        mCurVoiceCommentBean = null;
                    } else {
                        mCurVoiceCommentBean.setVoicePlaying(false);
                        notifyItemChanged(mCurVoiceCommentBean.getPosition(), Constants.PAYLOAD);
                        if (mActionListener != null) {
                            mActionListener.onVoiceStop(mCurVoiceCommentBean);
                            mActionListener.onVoicePlay(commentBean);
                        }
                        commentBean.setVoicePlaying(true);
                        mCurVoiceCommentBean = commentBean;
                    }
                } else {
                    commentBean.setVoicePlaying(true);
                    mCurVoiceCommentBean = commentBean;
                    if (mActionListener != null) {
                        mActionListener.onVoicePlay(commentBean);
                    }
                }
                notifyItemChanged(commentBean.getPosition(), Constants.PAYLOAD);

            }
        };
        mAvatarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toUid = (String) v.getTag(R.id.avatar);
                RouteUtil.forwardUserHome(mContext,toUid);
            }
        };
        mMoreReplyTip1 = WordUtil.getString(R.string.video_comment_expand);
        mMoreReplyTip2 = WordUtil.getString(R.string.video_comment_expand_2);
        mVoicePlay = ContextCompat.getDrawable(context, R.mipmap.icon_comment_voice_1);
        mVoicePause = ContextCompat.getDrawable(context, R.mipmap.icon_comment_voice_0);
    }


    public void stopPlayVoice() {
        if (mCurVoiceCommentBean != null) {
            mCurVoiceCommentBean.setVoicePlaying(false);
            notifyItemChanged(mCurVoiceCommentBean.getPosition(), Constants.PAYLOAD);
            if (mActionListener != null) {
                mActionListener.onVoiceStop(mCurVoiceCommentBean);
            }
        }
        mCurVoiceCommentBean = null;
    }

    @Override
    public void refreshData(List<VideoCommentBean> list) {
        if (mCurVoiceCommentBean != null) {
            if (mActionListener != null) {
                mActionListener.onVoiceStop(mCurVoiceCommentBean);
            }
        }
        mCurVoiceCommentBean = null;
        super.refreshData(list);
    }

    /**
     * 停止语音播放动画
     */
    public void stopVoiceAnim() {
        if (mCurVoiceCommentBean != null) {
            mCurVoiceCommentBean.setVoicePlaying(false);
            notifyItemChanged(mCurVoiceCommentBean.getPosition(), Constants.PAYLOAD);
        }
        mCurVoiceCommentBean = null;
    }

    public void setVideoUid(String videoUid) {
        mVideoUid = videoUid;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int i = 0, size = mList.size(); i < size; i++) {
            count++;
            List<VideoCommentBean> childList = mList.get(i).getChildList();
            if (childList != null) {
                count += childList.size();
            }
        }
        return count;
    }

    private VideoCommentBean getItem(int position) {
        int index = 0;
        for (int i = 0, size = mList.size(); i < size; i++) {
            VideoCommentBean parentNode = mList.get(i);
            if (index == position) {
                return parentNode;
            }
            index++;
            List<VideoCommentBean> childList = mList.get(i).getChildList();
            if (childList != null) {
                for (int j = 0, childSize = childList.size(); j < childSize; j++) {
                    if (position == index) {
                        return childList.get(j);
                    }
                    index++;
                }
            }
        }
        return null;
    }


    /**
     * 展开子评论
     *
     * @param childNode   在这个子评论下面插入子评论
     * @param insertCount 插入的子评论的个数
     */
    public void insertReplyList(VideoCommentBean childNode, int insertCount) {
        //这种方式也能达到  notifyItemRangeInserted 的效果，而且不容易出问题
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(childNode.getPosition());
        }
        notifyItemRangeChanged(childNode.getPosition(), getItemCount(), Constants.PAYLOAD);
    }

    /**
     * 收起子评论
     *
     * @param childNode   在这个子评论下面收起子评论
     * @param removeCount 被收起的子评论的个数
     */
    public void removeReplyList(VideoCommentBean childNode, int removeCount) {
        //这种方式也能达到  notifyItemRangeRemoved 的效果，而且不容易出问题
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(childNode.getPosition());
        }
        notifyItemRangeChanged(childNode.getPosition(), getItemCount(), Constants.PAYLOAD);
    }

    @Override
    public int getItemViewType(int position) {
        VideoCommentBean bean = getItem(position);
        if (bean != null) {
            if (bean.isParentNode()) {
                if (bean.isVoice()) {
                    return VOICE_PARENT;
                }
                return TEXT_PARENT;
            } else {
                if (bean.isVoice()) {
                    return VOICE_CHILD;
                }
            }
        }
        return TEXT_CHILD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TEXT_PARENT) {
            return new TextParentVh(mInflater.inflate(R.layout.item_video_comment_parent, parent, false));
        } else if (viewType == TEXT_CHILD) {
            return new TextChildVh(mInflater.inflate(R.layout.item_video_comment_child, parent, false));
        } else if (viewType == VOICE_PARENT) {
            return new VoiceParentVh(mInflater.inflate(R.layout.item_video_comment_parent_voice, parent, false));
        }
        return new VoiceChildVh(mInflater.inflate(R.layout.item_video_comment_child_voice, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position, @NonNull List payloads) {
        VideoCommentBean bean = getItem(position);
        if (bean != null) {
            bean.setPosition(position);
            Object payload = payloads.size() > 0 ? payloads.get(0) : null;
            ((Vh) vh).setData(bean, payload);
        }
    }


    class Vh extends RecyclerView.ViewHolder {

        TextView mName;
        ImageView mAvatar;
        View mAuthor;

        public Vh(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.name);
            mAvatar = itemView.findViewById(R.id.avatar);
            mAuthor = itemView.findViewById(R.id.author);
            itemView.setOnClickListener(mOnClickListener);
            itemView.setOnLongClickListener(mOnLongClickListener);
            mAvatar.setOnClickListener(mAvatarClickListener);
        }

        void setData(VideoCommentBean bean, Object payload) {
            itemView.setTag(bean);
            if (payload == null) {
                UserBean u = bean.getUserBean();
                if (u != null) {
                    mName.setText(u.getUserNiceName());
                    ImgLoader.displayAvatar(mContext, u.getAvatarThumb(), mAvatar);
                    mAvatar.setTag(R.id.avatar, u.getId());
                }
                if (!TextUtils.isEmpty(mVideoUid) && mVideoUid.equals(bean.getUid())) {
                    if (mAuthor.getVisibility() != View.VISIBLE) {
                        mAuthor.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mAuthor.getVisibility() == View.VISIBLE) {
                        mAuthor.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    class ParentVh extends Vh {

        ImageView mBtnLike;
        TextView mLikeNum;

        public ParentVh(View itemView) {
            super(itemView);
            mBtnLike = (ImageView) itemView.findViewById(R.id.btn_like);
            mLikeNum = (TextView) itemView.findViewById(R.id.like_num);
            mBtnLike.setOnClickListener(mLikeClickListener);
        }

        void setData(VideoCommentBean bean, Object payload) {
            super.setData(bean, payload);
            mBtnLike.setTag(bean);
            boolean like = bean.getIsLike() == 1;
            mBtnLike.setImageDrawable(like ? mLikeDrawable : mUnLikeDrawable);
            mLikeNum.setText(bean.getLikeNum());
            mLikeNum.setTextColor(like ? mLikeColor : mUnLikeColor);
        }
    }

    class TextParentVh extends ParentVh {
        TextView mContent;

        public TextParentVh(View itemView) {
            super(itemView);
            mContent = (TextView) itemView.findViewById(R.id.content);
        }

        void setData(VideoCommentBean bean, Object payload) {
            super.setData(bean, payload);
            if (payload == null) {
                mContent.setText(VideoTextRender.renderVideoComment(bean.getContent(), "  " + bean.getDatetime(), bean.getAtInfo()));
            }
        }
    }

    class VoiceParentVh extends ParentVh {

        TextView mVoiceDuration;
        TextView mTime;
        View mPlayGif;
        ImageView mPlayImg;
        View mBubble;

        public VoiceParentVh(View itemView) {
            super(itemView);
            mVoiceDuration = itemView.findViewById(R.id.voice_duration);
            mTime = itemView.findViewById(R.id.time);
            mPlayGif = itemView.findViewById(R.id.play_gif);
            mPlayImg = itemView.findViewById(R.id.play_img);
            mBubble = itemView.findViewById(R.id.bubble);
            mBubble.setOnClickListener(mVoiceClickListener);
        }

        void setData(VideoCommentBean bean, Object payload) {
            super.setData(bean, payload);
            mBubble.setTag(bean);
            if (payload == null) {
                mVoiceDuration.setText(bean.getVoiceDuration() + "'");
                mTime.setText(bean.getDatetime());
            }
            if (bean.isVoicePlaying()) {
                mPlayImg.setImageDrawable(mVoicePlay);
                if (mPlayGif.getVisibility() != View.VISIBLE) {
                    mPlayGif.setVisibility(View.VISIBLE);
                }
            } else {
                mPlayImg.setImageDrawable(mVoicePause);
                if (mPlayGif.getVisibility() == View.VISIBLE) {
                    mPlayGif.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    class ChildVh extends Vh {

        View mBtnGroup;
        View mBtnExpand;//展开按钮
        View mBtnbCollapsed;//收起按钮
        TextView mCommentNum;

        ImageView mBtnLike;
        TextView mLikeNum;

        public ChildVh(View itemView) {
            super(itemView);
            mBtnGroup = itemView.findViewById(R.id.btn_group);
            mBtnExpand = itemView.findViewById(R.id.btn_expand);
            mBtnbCollapsed = itemView.findViewById(R.id.btn_collapsed);
            mCommentNum = itemView.findViewById(R.id.comment_num);
            mBtnExpand.setOnClickListener(mExpandClickListener);
            mBtnbCollapsed.setOnClickListener(mCollapsedClickListener);

            mBtnLike = (ImageView) itemView.findViewById(R.id.btn_like);
            mLikeNum = (TextView) itemView.findViewById(R.id.like_num);
            mBtnLike.setOnClickListener(mLikeClickListener);
        }

        void setData(VideoCommentBean bean, Object payload) {
            super.setData(bean, payload);
            mBtnExpand.setTag(bean);
            mBtnbCollapsed.setTag(bean);

            mBtnLike.setTag(bean);
            boolean like = bean.getIsLike() == 1;
            mBtnLike.setImageDrawable(like ? mLikeDrawable : mUnLikeDrawable);
            mLikeNum.setText(bean.getLikeNum());
            mLikeNum.setTextColor(like ? mLikeColor : mUnLikeColor);

            VideoCommentBean parentNodeBean = bean.getParentNodeBean();
            if (bean.needShowExpand(parentNodeBean)) {
                if (mBtnGroup.getVisibility() != View.VISIBLE) {
                    mBtnGroup.setVisibility(View.VISIBLE);
                }
                if (mBtnbCollapsed.getVisibility() == View.VISIBLE) {
                    mBtnbCollapsed.setVisibility(View.INVISIBLE);
                }
                if (mBtnExpand.getVisibility() != View.VISIBLE) {
                    mBtnExpand.setVisibility(View.VISIBLE);
                }
                if (bean.isFirstChild(parentNodeBean)) {
                    mCommentNum.setText(String.format(mMoreReplyTip2, String.valueOf(parentNodeBean.getReplyNum() - 1)));
                } else {
                    mCommentNum.setText(mMoreReplyTip1);
                }
            } else if (bean.needShowCollapsed(parentNodeBean)) {
                if (mBtnGroup.getVisibility() != View.VISIBLE) {
                    mBtnGroup.setVisibility(View.VISIBLE);
                }
                if (mBtnExpand.getVisibility() == View.VISIBLE) {
                    mBtnExpand.setVisibility(View.INVISIBLE);
                }
                if (mBtnbCollapsed.getVisibility() != View.VISIBLE) {
                    mBtnbCollapsed.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnGroup.getVisibility() == View.VISIBLE) {
                    mBtnGroup.setVisibility(View.GONE);
                }
            }
        }
    }

    class TextChildVh extends ChildVh {
        TextView mContent;

        public TextChildVh(View itemView) {
            super(itemView);
            mContent = (TextView) itemView.findViewById(R.id.content);
        }

        void setData(VideoCommentBean bean, Object payload) {
            super.setData(bean, payload);
            if (payload == null) {
                mContent.setText(VideoTextRender.renderVideoComment(bean.getContent(), "  " + bean.getDatetime(), bean.getAtInfo()));
            }
        }
    }

    class VoiceChildVh extends ChildVh {

        TextView mVoiceDuration;
        TextView mTime;
        View mPlayGif;
        ImageView mPlayImg;
        View mBubble;

        public VoiceChildVh(View itemView) {
            super(itemView);
            mVoiceDuration = itemView.findViewById(R.id.voice_duration);
            mTime = itemView.findViewById(R.id.time);
            mPlayGif = itemView.findViewById(R.id.play_gif);
            mPlayImg = itemView.findViewById(R.id.play_img);
            mBubble = itemView.findViewById(R.id.bubble);
            mBubble.setOnClickListener(mVoiceClickListener);
        }

        void setData(VideoCommentBean bean, Object payload) {
            super.setData(bean, payload);
            mBubble.setTag(bean);
            if (payload == null) {
                mVoiceDuration.setText(bean.getVoiceDuration() + "'");
                mTime.setText(bean.getDatetime());
            }
            if (bean.isVoicePlaying()) {
                mPlayImg.setImageDrawable(mVoicePlay);
                if (mPlayGif.getVisibility() != View.VISIBLE) {
                    mPlayGif.setVisibility(View.VISIBLE);
                }
            } else {
                mPlayImg.setImageDrawable(mVoicePause);
                if (mPlayGif.getVisibility() == View.VISIBLE) {
                    mPlayGif.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public interface ActionListener {
        void onItemClick(VideoCommentBean bean);

        void onItemLongClick(VideoCommentBean bean);

        void onExpandClicked(VideoCommentBean commentBean);

        void onCollapsedClicked(VideoCommentBean commentBean);

        void onVoicePlay(VideoCommentBean commentBean);

        void onVoiceStop(VideoCommentBean commentBean);
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

}
