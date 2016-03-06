package com.h6ah4i.example.recyclerviewiosmailapplikeswipe;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();

        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        rv.setAdapter(swipeMgr.createWrappedAdapter(new Adapter()));

        rv.setItemAnimator(new SwipeDismissItemAnimator());

        swipeMgr.attachRecyclerView(rv);

        rv.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(this, R.drawable.list_divider), true));
    }

    static final float OPTIONS_AREA_PROPORTION = 0.5f;
    static final float REMOVE_ITEM_THRESHOLD = 0.6f;

    static class ViewHolder extends AbstractSwipeableItemViewHolder {
        View swipeableContainer;
        View optionView1;
        View optionView2;
        View optionView3;
        TextView text1;
        float lastSwipeAmount;

        public ViewHolder(View itemView) {
            super(itemView);

            swipeableContainer = itemView.findViewById(R.id.swipeable_container);
            optionView1 = itemView.findViewById(R.id.option_view_1);
            optionView2 = itemView.findViewById(R.id.option_view_2);
            optionView3 = itemView.findViewById(R.id.option_view_3);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
        }

        @Override
        public View getSwipeableContainerView() {
            return swipeableContainer;
        }

        @Override
        public void onSlideAmountUpdated(float horizontalAmount, float verticalAmount, boolean isSwiping) {
            int itemWidth = itemView.getWidth();
            float optionItemWidth = itemWidth * OPTIONS_AREA_PROPORTION / 3;
            int offset = (int) (optionItemWidth + 0.5f);
            float p = Math.max(0, Math.min(OPTIONS_AREA_PROPORTION, -horizontalAmount)) / OPTIONS_AREA_PROPORTION;

            if (optionView1.getWidth() == 0) {
                setLayoutWidth(optionView1, (int) (optionItemWidth + 0.5f));
                setLayoutWidth(optionView2, (int) (optionItemWidth + 0.5f));
                setLayoutWidth(optionView3, (int) (optionItemWidth + 0.5f));
            }

            optionView1.setTranslationX(-(int) (p * optionItemWidth * 3 + 0.5f) + offset);
            optionView2.setTranslationX(-(int) (p * optionItemWidth * 2 + 0.5f) + offset);
            optionView3.setTranslationX(-(int) (p * optionItemWidth * 1 + 0.5f) + offset);

            if (horizontalAmount < (-REMOVE_ITEM_THRESHOLD)) {
                swipeableContainer.setVisibility(View.INVISIBLE);
                optionView1.setVisibility(View.INVISIBLE);
                optionView2.setVisibility(View.INVISIBLE);
                optionView3.setVisibility(View.INVISIBLE);
            } else {
                swipeableContainer.setVisibility(View.VISIBLE);
                optionView1.setVisibility(View.VISIBLE);
                optionView2.setVisibility(View.VISIBLE);
                optionView3.setVisibility(View.VISIBLE);
            }

            lastSwipeAmount = horizontalAmount;
        }

        private static void setLayoutWidth(View v, int width) {
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.width = width;
            v.setLayoutParams(lp);
        }
    }

    static class ItemData {
        final long id;
        boolean pinned;

        public ItemData(long id) {
            this.id = id;
        }
    }

    static class Adapter extends RecyclerView.Adapter<ViewHolder> implements SwipeableItemAdapter<ViewHolder> {
        interface Swipeable extends SwipeableItemConstants {
        }

        ArrayList<ItemData> mItems;

        public Adapter() {
            setHasStableIds(true);

            mItems = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                mItems.add(new ItemData(i));
            }
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text1.setText("item " + position);

            ItemData item = mItems.get(position);

            // set swiping properties
            holder.setMaxLeftSwipeAmount(-OPTIONS_AREA_PROPORTION);
            holder.setMaxRightSwipeAmount(0);
            holder.setSwipeItemHorizontalSlideAmount(
                    item.pinned ? -OPTIONS_AREA_PROPORTION : 0);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public SwipeResultAction onSwipeItem(ViewHolder holder, int position, int result) {
            if (result == Swipeable.RESULT_SWIPED_LEFT) {
                if (holder.lastSwipeAmount < (-REMOVE_ITEM_THRESHOLD)) {
                    return new SwipeLeftRemoveAction(this, position);
                } else {
                    return new SwipeLeftPinningAction(this, position);
                }
            } else {
                return new SwipeCancelAction(this, position);
            }
        }

        @Override
        public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
            return Swipeable.REACTION_CAN_SWIPE_LEFT;
        }

        @Override
        public void onSetSwipeBackground(ViewHolder holder, int position, int type) {
            if (type == Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND) {
                holder.itemView.setBackgroundColor(0xffff6666);
            }
        }
    }

    static class SwipeLeftRemoveAction extends SwipeResultActionRemoveItem {
        Adapter adapter;
        int position;

        public SwipeLeftRemoveAction(Adapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mItems.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    static class SwipeLeftPinningAction extends SwipeResultActionMoveToSwipedDirection {
        Adapter adapter;
        int position;

        public SwipeLeftPinningAction(Adapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mItems.get(position).pinned = true;
            adapter.notifyItemChanged(position);
        }
    }


    static class SwipeCancelAction extends SwipeResultActionDefault {
        Adapter adapter;
        int position;

        public SwipeCancelAction(Adapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mItems.get(position).pinned = false;
            adapter.notifyItemChanged(position);
        }
    }
}
