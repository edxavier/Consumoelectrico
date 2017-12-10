package com.nicrosoft.consumoelectrico;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

/**
 * @author  : Eder Xavier Rojas
 * @date    : 26/08/2016 - 11:45
 * @package : com.vynil.helper
 * @project : Vynil
 */
public class MySnackbar {

    @NonNull
    private static Snackbar colorSnackBar(@NonNull View container, @NonNull String message, int background, int textColor , int length) {
        Snackbar snack = Snackbar.make(container, message,length);
        View view = snack.getView();
        view.setBackgroundColor(background);
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(textColor);
        return snack;
    }

    @NonNull
    public static Snackbar info(@NonNull View container, @NonNull String message, int length ) {
        int background = container.getResources().getColor(R.color.accent);
        int textColor = container.getResources().getColor(R.color.md_white_1000);
        Snackbar snack = colorSnackBar(container, message, background,textColor, length);
        return snack;
    }

    @NonNull
    public static Snackbar warning(@NonNull View container, @NonNull String message, int length) {
        int background = container.getResources().getColor(R.color.md_orange_500);
        int textColor = container.getResources().getColor(R.color.md_white_1000);
        Snackbar snack = colorSnackBar(container, message, background,textColor, length);
        return snack;
    }

    @NonNull
    public static Snackbar alert(@NonNull View container, @NonNull String message, int length) {
        int background = container.getResources().getColor(R.color.md_pink_500);
        int textColor = container.getResources().getColor(R.color.md_white_1000);
        Snackbar snack = colorSnackBar(container, message, background,textColor, length);
        return snack;
    }

    @NonNull
    public static Snackbar confirm(@NonNull View container, @NonNull String message, int length) {
        int background = container.getResources().getColor(R.color.md_blue_grey_500);
        int textColor = container.getResources().getColor(R.color.md_white_1000);
        Snackbar snack = colorSnackBar(container, message, background,textColor, length);
        return snack;
    }
}
