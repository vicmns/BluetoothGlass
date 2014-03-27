package com.vicmns.bluetoothglass.client.views;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vicmns.bluetoothglass.client.R;

/**
 * Created by Victor Cervantes on 3/14/14.
 */
public class OverlayView {
    public static final int HIDDEN = 0;
    public static final int LOADING = 1;

    Context mContext;
    View mContent;
    View mOverlay;
    TextView mMessage;
    ProgressBar mProgressIndicator;

    public OverlayView(Context context, View overlay, View content,
                           int initialState) {

        mContext = context;
        mContent = content;
        mOverlay = overlay;

        mMessage = (TextView) mOverlay.findViewById(R.id.overlay_text_message);
        mProgressIndicator = (ProgressBar) mOverlay
                .findViewById(R.id.overlay_progress);

        switch (initialState) {
            case HIDDEN:
                hide();
                break;
            case LOADING:
                showLoading();
                break;
        }

    }

    public OverlayView(Context context, View overlay, int initialState) {
        this(context, overlay, null, initialState);
    }

    public boolean isLoading() {
        return mProgressIndicator.getVisibility() == View.VISIBLE
                && mOverlay.getVisibility() == View.VISIBLE;
    }

    public void show() {
        mOverlay.setVisibility(View.VISIBLE);
        if (mContent != null)
            mContent.setVisibility(View.GONE);
    }

    public void hide() {
        mOverlay.setVisibility(View.GONE);
        if (mContent != null)
            mContent.setVisibility(View.VISIBLE);
    }

    public void showMessage(String message) {
        mMessage.setVisibility(View.VISIBLE);
        mMessage.setText(message);

        mProgressIndicator.setVisibility(View.GONE);
        show();
    }

    public void showMessageWithLoading(String message) {
        mProgressIndicator.setVisibility(View.VISIBLE);
        mMessage.setVisibility(View.VISIBLE);
        mMessage.setText(message);

        show();
    }

    public void showLoading() {
        mMessage.setVisibility(View.GONE);
        mProgressIndicator.setVisibility(View.VISIBLE);
        show();
    }

    public void showWithMessageLoading() {
        mMessage.setVisibility(View.VISIBLE);
        mProgressIndicator.setVisibility(View.VISIBLE);
        show();
    }

}
