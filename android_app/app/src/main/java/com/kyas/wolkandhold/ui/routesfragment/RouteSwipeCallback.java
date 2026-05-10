package com.kyas.wolkandhold.ui.routesfragment;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class RouteSwipeCallback extends ItemTouchHelper.SimpleCallback {
    private final RecyclerRoutesAdapter adapter;

    public RouteSwipeCallback(RecyclerRoutesAdapter adapter) {
        // CHANGE: Разрешаем свайп в обе стороны: влево — открыть меню, вправо — закрыть
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            adapter.showMenu(position);
        } else if (direction == ItemTouchHelper.RIGHT) {
            // CHANGE: Свайп вправо закрывает меню и возвращает в исходное положение
            adapter.closeMenu(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View content = ((RecyclerRoutesAdapter.ViewHolder) viewHolder).routeContent;
        float buttonWidth = dpToPx(content.getContext(), 82);

        // CHANGE: Клампим в пределах ширины меню слева и 0 справа
        float clampedDX = Math.max(-buttonWidth, Math.min(0f, dX));
        content.setTranslationX(clampedDX);
    }

    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

}
