package dev.wysl.ui;

import java.util.function.Supplier;

public interface Ui {
    int BACK = 1;
    int ENTER = 2;
    int ESCAPE = 3;
    int TAB = 4;
    int PASTE = 5;
    int COPY = 6;

    void init(Runnable close, Supplier<String> clip);

    void draw(Canvas g, int w, int h, double mx, double my);

    boolean click(double x, double y, int b);

    boolean release(double x, double y, int b);

    boolean dragged(double x, double y);

    boolean scroll(double x, double y, double a);

    boolean key(int a);

    boolean type(int cp);

    boolean typing();

    void save();
}
