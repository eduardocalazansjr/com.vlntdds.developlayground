package com.vlntdds.developlayground.searchbar.ui.fragment;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vlntdds.developlayground.searchbar.ui.view.SearchBar;

/**
 * Created by eduardocjr on 28/08/17.
 */

public class SearchBarFragment extends RelativeLayout implements SearchBar,
        View.OnClickListener, Animation.AnimationListener, View.OnFocusChangeListener,
        TextView.OnEditorActionListener {

    private CardView searchBarCardView;
    private LinearLayout textContainer;
    private EditText textInput;

    public SearchBarFragment(Context context) {

        super(context);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onFocusChange(View view, boolean b) {

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        return false;
    }
}
