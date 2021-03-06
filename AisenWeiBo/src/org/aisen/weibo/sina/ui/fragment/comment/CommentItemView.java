package org.aisen.weibo.sina.ui.fragment.comment;

import android.content.DialogInterface;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.m.common.context.GlobalContext;
import com.m.component.bitmaploader.BitmapLoader;
import com.m.component.bitmaploader.core.ImageConfig;
import com.m.support.adapter.ABaseAdapter;
import com.m.support.inject.ViewInject;
import com.m.ui.fragment.ABaseFragment;

import org.aisen.weibo.sina.R;
import org.aisen.weibo.sina.base.AppContext;
import org.aisen.weibo.sina.support.compress.TimelineThumbBitmapCompress;
import org.aisen.weibo.sina.support.utils.AisenUtils;
import org.aisen.weibo.sina.support.utils.ImageConfigUtils;
import org.aisen.weibo.sina.ui.fragment.basic.BizFragment;
import org.aisen.weibo.sina.ui.widget.AisenTextView;
import org.sina.android.bean.StatusComment;
import org.sina.android.bean.StatusContent;
import org.sina.android.bean.WeiBoUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdan on 15/4/23.
 */
public class CommentItemView extends ABaseAdapter.AbstractItemView<StatusComment>
                                        implements View.OnClickListener {

    @ViewInject(id = R.id.imgPhoto)
    ImageView imgPhoto;
    @ViewInject(id = R.id.txtName)
    TextView txtName;
    @ViewInject(id = R.id.txtDesc)
    TextView txtDesc;
    @ViewInject(id = R.id.txtContent)
    AisenTextView txtContent;

    @ViewInject(id = R.id.layRe)
    View layRe;
    @ViewInject(id = R.id.imgRePhoto)
    ImageView imgRePhoto;
    @ViewInject(id = R.id.txtReContent)
    AisenTextView txtReContent;

    @ViewInject(id = R.id.layStatus)
    View layStatus;
    @ViewInject(id = R.id.layDivider)
    View layDivider;
    @ViewInject(id = R.id.txtStatusContent)
    AisenTextView txtStatusContent;
    @ViewInject(id = R.id.img)
    ImageView imgView;

    @ViewInject(id = R.id.btnMenus)
    protected View btnMenus;

    private ABaseFragment fragment;
    private BizFragment bizFragment;

    private StatusContent mStatus;

    public CommentItemView(ABaseFragment fragment) {
        this.fragment = fragment;
    }

    public CommentItemView(ABaseFragment fragment, StatusContent status) {
        this.fragment = fragment;
        this.mStatus = status;
    }

    @Override
    public int inflateViewId() {
        return R.layout.as_item_comment;
    }

    @Override
    public void bindingData(View convertView, StatusComment data) {
        try {
            if (bizFragment == null)
                bizFragment = BizFragment.getBizFragment(fragment);

            if (bizFragment == null)
                return;
        } catch (Exception e) {
        }

        WeiBoUser user = data.getUser();
        if (user != null) {
            BitmapLoader.getInstance().display(fragment,
                    AisenUtils.getUserPhoto(user),
                    imgPhoto, ImageConfigUtils.getLargePhotoConfig());
            bizFragment.userShow(imgPhoto, user);
            txtName.setText(AisenUtils.getUserScreenName(user));
        }
        else {
            bizFragment.userShow(imgPhoto, null);
            txtName.setText(R.string.error_cmts);
            imgPhoto.setImageResource(R.drawable.user_placeholder);
        }

        txtContent.setContent(AisenUtils.getCommentText(data.getText()));
        AisenUtils.setTextSize(txtContent);

        String createAt = AisenUtils.convDate(data.getCreated_at());
        String from = String.format("%s", Html.fromHtml(data.getSource()));
        String desc = String.format("%s %s", createAt, from);
        txtDesc.setText(desc);

        // 源评论
        if (data.getReply_comment() != null) {
            layRe.setVisibility(View.VISIBLE);

            txtReContent.setContent(AisenUtils.getCommentText(data.getReply_comment().getText()));
            AisenUtils.setTextSize(txtReContent);

            if (data.getReply_comment().getUser() != null) {
                BitmapLoader.getInstance().display(fragment,
                        AisenUtils.getUserPhoto(data.getReply_comment().getUser()),
                        imgRePhoto, ImageConfigUtils.getLargePhotoConfig());
                bizFragment.userShow(imgRePhoto, data.getReply_comment().getUser());
            }
            else {
                bizFragment.userShow(imgRePhoto, null);
            }
        }
        else {
            layRe.setVisibility(View.GONE);
        }

        if (layStatus != null) {
            if (data.getStatus() != null && mStatus == null) {
                layDivider.setVisibility(View.VISIBLE);
                layStatus.setVisibility(View.VISIBLE);
                layStatus.setTag(data.getStatus());
                layStatus.setOnClickListener(this);

                txtStatusContent.setContent(data.getStatus().getText());
                AisenUtils.setTextSize(txtStatusContent);

                String image = null;

                // 先取微博的第一张图
                StatusContent status = data.getStatus();
                if (status != null && status.getRetweeted_status() != null)
                    status = status.getRetweeted_status();
                if (status == null || status.getPic_urls() == null || status.getPic_urls().length == 0) {
                }
                else {
                    image = status.getPic_urls()[0].getThumbnail_pic();
                }
                // 没图就取头像
                if (TextUtils.isEmpty(image) && status.getUser() != null) {
                    image = status.getUser().getAvatar_large();
                }
                if (!TextUtils.isEmpty(image)) {
                    imgView.setVisibility(View.VISIBLE);
                    ImageConfig config = new ImageConfig();
                    config.setId("comments");
                    config.setLoadfaildRes(R.drawable.bg_timeline_loading);
                    config.setLoadingRes(R.drawable.bg_timeline_loading);
                    config.setMaxWidth(300);
                    config.setMaxHeight(300);
                    config.setBitmapCompress(TimelineThumbBitmapCompress.class);

                    BitmapLoader.getInstance().display(fragment, image, imgView, config);
                }
                else {
                    imgView.setVisibility(View.GONE);
                }

                bizFragment.bindOnTouchListener(txtStatusContent);
            }
            else {
                layDivider.setVisibility(View.GONE);
                layStatus.setVisibility(View.GONE);
            }
        }

        if (btnMenus != null) {
            btnMenus.setTag(data);
            btnMenus.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layStatus) {
            final StatusContent status = (StatusContent) v.getTag();
            TimelineCommentFragment.launch(fragment.getActivity(), status);
        }
        else if (v.getId() == R.id.btnMenus) {
            final String[] commentMenuArr = GlobalContext.getInstance().getResources().getStringArray(R.array.cmt_menus);
            final StatusComment comment = (StatusComment) v.getTag();
            if (mStatus != null)
                comment.setStatus(mStatus);

            List<String> menuList = new ArrayList<String>();
            // 转发
            if (comment.getStatus() != null &&
                    (comment.getUser() != null && !comment.getUser().getIdstr().equals(AppContext.getUser().getIdstr())))
                menuList.add(commentMenuArr[1]);
            // 复制
            menuList.add(commentMenuArr[0]);
            // 删除
            if (comment.getUser() != null && AppContext.getUser().getIdstr().equals(comment.getUser().getIdstr()))
                menuList.add(commentMenuArr[2]);

            final String[] menus = new String[menuList.size()];
            for (int i = 0; i < menuList.size(); i++)
                menus[i] = menuList.get(i);

            AisenUtils.showMenuDialog(fragment,
                                        v,
                                        menus,
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                AisenUtils.commentMenuSelected(fragment, menus[which], comment);
                                            }
                                        });
        }
    }

}
