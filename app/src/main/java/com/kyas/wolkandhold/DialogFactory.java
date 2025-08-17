package com.kyas.wolkandhold;

import android.content.Context;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

public class DialogFactory {

    public static void showSaveRouteDialog(Context context, Consumer<String> onSave) {
        EditText input = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_save_route)
                .setMessage(R.string.dialog_message_save_route)
                .setView(input)
                .setPositiveButton("Сохранить", (d, w) -> onSave.accept(input.getText().toString()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    public static void showInfoDialog(Context context, int titleRes, int messageRes) {
        new AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showConfirmDialog(Context context, int titleRes, int messageRes, Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton("Да", (d, w) -> onConfirm.run())
                .setNegativeButton("Нет", null)
                .show();
    }
}
