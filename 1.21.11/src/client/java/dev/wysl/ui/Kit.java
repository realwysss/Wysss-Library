package dev.wysl.ui;

import dev.wysl.Colors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public abstract class Kit implements Ui {
    public static final int TX = 0xFFE6E0F5;
    public static final int MU = 0xFF9A90B8;
    public static final int WL = 0x520E0620;
    public static final int ED = 0x30D2BCFF;
    public static final int AC = 0xFFB06BFF;
    public static final int KN1 = 0xFFF2EDFC;
    public static final int KN2 = 0xFFD4C9EC;

    public interface Drag {
        void to(double x, double y);
    }

    protected record Ht(String id, float x, float y, float w, float h, Runnable ck, Drag dg, DoubleConsumer sc) {
        boolean in(double px, double py) {
            return px >= x && py >= y && px < x + w && py < y + h;
        }
    }

    protected final List<Ht> hits = new ArrayList<>();
    protected final Map<String, Float> fx = new HashMap<>();
    protected final Map<String, float[]> hsv = new HashMap<>();
    protected Runnable close;
    protected Supplier<String> clip;
    protected float open;
    protected float cf = 1f;
    protected float dt;
    protected float clock;
    protected double mx;
    protected double my;
    protected String act;
    protected String typ;
    protected String hex = "";
    protected Drag drag;
    protected String tip;
    private long last;

    public void init(Runnable c, Supplier<String> cl) {
        close = c;
        clip = cl;
    }

    protected void begin(double x, double y) {
        long now = System.nanoTime();
        dt = last == 0L ? 0f : Math.min((now - last) / 1e9f, 0.1f);
        last = now;
        clock += dt;
        mx = x;
        my = y;
        hits.clear();
        tip = null;
        open = ease(open, 1f, 9f);
        cf = 1f;
    }

    protected int fd(int argb) {
        return Colors.alpha(argb, open * cf);
    }

    protected void dim(Canvas g, int w, int h) {
        g.fill(0, 0, w, h, 0, fd(0x4A0A0418), fd(0xA8050210));
    }

    protected void glass(Canvas g, float x, float y, float w, float h) {
        g.shadow(x + w * 0.12f, y + h * 0.1f, w * 0.76f, h * 0.76f, 60, 80, fd(Colors.alpha(AC, 0.2f)));
        g.shadow(x, y + 10, w, h, 16, 28, fd(0xAA05010F));
        g.fill(x, y, w, h, 14, fd(0xB4301860), fd(0xD0140A2C));
        g.fill(x + 1, y + 1, w - 2, h * 0.45f, 13, fd(0x22E0C8FF), fd(0x04B08CFF));
        g.ring(x, y, w, h, 14, 1, fd(0x70E6D2FF), fd(0x18A078FF));
    }

    protected void well(Canvas g, float x, float y, float w, float h, float r) {
        g.fill(x, y, w, h, r, fd(0x38100620), fd(0x66080214));
        g.ring(x, y, w, h, r, 1, fd(0x48E6D2FF), fd(0x0EA078FF));
    }

    protected void tipDraw(Canvas g, int width) {
        if (tip == null) return;
        List<String> ls = new ArrayList<>();
        String cur = "";
        for (String wd : tip.split(" ")) {
            String t = cur.isEmpty() ? wd : cur + " " + wd;
            if (g.width(t) > 190 && !cur.isEmpty()) {
                ls.add(cur);
                cur = wd;
            } else cur = t;
        }
        ls.add(cur);
        float tw = 12;
        for (String l : ls) tw = Math.max(tw, g.width(l) + 12);
        float th = ls.size() * 10 + 7;
        float tx = (float) Math.max(2, Math.min(mx + 10, width - tw - 4));
        float ty = (float) Math.max(my - th - 4, 2);
        g.shadow(tx, ty, tw, th, 5, 6, fd(0x90000000));
        g.fill(tx, ty, tw, th, 5, fd(0xEE3A2268), fd(0xEE1C0F3A));
        g.ring(tx, ty, tw, th, 5, 1, fd(0x44E6D2FF), fd(0x12A078FF));
        for (int i = 0; i < ls.size(); i++) g.text(ls.get(i), tx + 6, ty + 4 + i * 10, fd(TX));
    }

    protected boolean hov(float x, float y, float w, float h) {
        return drag == null && mx >= x && my >= y && mx < x + w && my < y + h;
    }

    protected float an(String k, float to, float sp) {
        Float c = fx.get(k);
        float v = c == null ? to : ease(c, to, sp);
        fx.put(k, v);
        return v;
    }

    protected float ease(float c, float to, float sp) {
        float v = c + (to - c) * (1f - (float) Math.exp(-sp * dt));
        return Math.abs(to - v) < 0.0005f ? to : v;
    }

    protected void add(String id, float x, float y, float w, float h, Runnable ck) {
        hits.add(new Ht(id, x, y, w, h, ck, null, null));
    }

    protected void tgl(Canvas g, String id, float x, float y, boolean on, Runnable flip) {
        float w = 22;
        float h = 12;
        float t = an(id, on ? 1f : 0f, 14f);
        boolean over = hov(x - 2, y - 2, w + 4, h + 4);
        float hl = an(id + "h", over ? 1f : 0f, 14f);
        g.fill(x, y, w, h, h / 2f, fd(Colors.mix(Colors.mix(0x30FFFFFF, Colors.mix(AC, 0xFFFFFFFF, 0.18f), t), 0xFFFFFFFF, hl * 0.08f)), fd(Colors.mix(WL, AC, t)));
        g.ring(x, y, w, h, h / 2f, 1, fd(0x33D2BCFF), fd(0x0DD2BCFF));
        float k = 8 + hl;
        float kx = x + 2 + (w - 4 - k) * t;
        float ky = y + (h - k) / 2f;
        g.shadow(kx, ky + 0.5f, k, k, k / 2f, 2, fd(0x70000000));
        g.fill(kx, ky, k, k, k / 2f, fd(KN1), fd(KN2));
        add(id, x - 2, y - 2, w + 4, h + 4, flip);
    }

    protected void row(Canvas g, String id, String label, float x, float y, float w, boolean on, Runnable flip) {
        boolean over = hov(x - 4, y - 3, w + 8, 18);
        float k = an(id + "row", over ? 1f : 0f, 14f);
        if (k > 0.01f) g.fill(x - 4, y - 3, w + 8, 18, 7, fd(Colors.alpha(0x16FFFFFF, k)), fd(Colors.alpha(0x0AFFFFFF, k)));
        g.text(label, x + 2, y + 1, fd(on ? TX : MU));
        tgl(g, id, x + w - 24, y - 1, on, flip);
        add(id + "row", x - 4, y - 3, w + 8, 18, flip);
    }

    protected void sld(Canvas g, String id, String label, float x, float y, float w, float v, float lo, float hi, float step, DoubleConsumer set, String shown) {
        g.text(label, x, y, fd(MU));
        g.text(shown, x + w - g.width(shown), y, fd(TX));
        float ty = y + 11;
        float th = 4;
        float t = Colors.clamp((v - lo) / (hi - lo), 0f, 1f);
        boolean held = id.equals(act);
        float k = an(id, held || hov(x - 4, ty - 6, w + 8, th + 12) ? 1f : 0f, 16f);
        float at = an(id + "v", t, 22f);
        g.fill(x, ty, w, th, th / 2f, fd(WL));
        g.fill(x, ty, Math.max(th, w * at), th, th / 2f, fd(Colors.mix(AC, 0xFFFFFFFF, 0.25f)), fd(AC));
        float s = 8 + k * 2;
        float kx = x + w * at - s / 2f;
        float ky = ty + th / 2f - s / 2f;
        if (k > 0.01f) g.fill(kx - 3 * k, ky - 3 * k, s + 6 * k, s + 6 * k, (s + 6 * k) / 2f, fd(Colors.alpha(AC & 0xFFFFFF | 0x40000000, k)));
        g.shadow(kx, ky + 0.5f, s, s, s / 2f, 2.5f, fd(0x80000000));
        g.fill(kx, ky, s, s, s / 2f, fd(KN1), fd(KN2));
        Drag d = (px, py) -> set.accept(snap(lo + (hi - lo) * Colors.clamp((float) ((px - x) / w), 0f, 1f), step, lo, hi));
        hits.add(new Ht(id, x - 4, ty - 7, w + 8, th + 14, null, d, a -> set.accept(snap(v + (float) Math.signum(a) * Math.max(step, (hi - lo) / 20f), step, lo, hi))));
    }

    protected void seg(Canvas g, String id, float x, float y, float w, float h, String[] it, int sel, IntConsumer pick) {
        g.fill(x, y, w, h, h / 2f, fd(WL));
        g.ring(x, y, w, h, h / 2f, 1, fd(ED), fd(0x0AFFFFFF));
        int n = it.length;
        float[] e = new float[n + 1];
        float sum = 0;
        for (String s : it) sum += g.width(s);
        float pad = (w - sum) / n;
        e[0] = x;
        for (int i = 0; i < n; i++) e[i + 1] = e[i] + g.width(it[i]) + pad;
        e[n] = x + w;
        float px = an(id, e[sel], 16f);
        float pw = an(id + "w", e[sel + 1] - e[sel], 16f);
        g.shadow(px + 2, y + 2, pw - 4, h - 4, (h - 4) / 2f, 3, fd(0x60000000));
        g.fill(px + 2, y + 2, pw - 4, h - 4, (h - 4) / 2f, fd(0x36FFFFFF), fd(0x1CFFFFFF));
        g.ring(px + 2, y + 2, pw - 4, h - 4, (h - 4) / 2f, 1, fd(Colors.alpha(AC, 0.75f)), fd(Colors.alpha(AC, 0.25f)));
        for (int i = 0; i < n; i++) {
            float ix = e[i];
            float iw = e[i + 1] - ix;
            float k = an(id + i, hov(ix, y, iw, h) ? 1f : 0f, 14f);
            int col = i == sel ? TX : Colors.mix(MU, TX, k * 0.6f);
            g.text(it[i], ix + (iw - g.width(it[i])) / 2f, y + (h - 8) / 2f, fd(col));
            int idx = i;
            hits.add(new Ht(id + i, ix, y, iw, h, () -> pick.accept(idx), null, a -> pick.accept(Math.floorMod(sel - (int) Math.signum(a), n))));
        }
    }

    protected void btn(Canvas g, String id, String label, float x, float y, float w, boolean main, Runnable run) {
        float h = 18;
        float k = an(id, hov(x, y, w, h) ? 1f : 0f, 14f);
        g.shadow(x, y + 1, w, h, 7, 5, fd(0x70000000));
        if (main) {
            g.fill(x, y, w, h, 7, fd(Colors.mix(Colors.mix(AC, 0xFFFFFFFF, 0.22f), 0xFFFFFFFF, k * 0.12f)), fd(Colors.mix(AC, 0xFFFFFFFF, k * 0.08f)));
            g.ring(x, y, w, h, 7, 1, fd(0x55FFFFFF), fd(0x14FFFFFF));
        } else {
            g.fill(x, y, w, h, 7, fd(Colors.mix(0x26FFFFFF, 0x3DFFFFFF, k)), fd(Colors.mix(0x12FFFFFF, 0x22FFFFFF, k)));
            g.ring(x, y, w, h, 7, 1, fd(0x2EFFFFFF), fd(0x0AFFFFFF));
        }
        g.text(label, x + (w - g.width(label)) / 2f, y + 5, fd(main ? 0xFF1A0B33 : TX));
        add(id, x, y, w, h, run);
    }

    protected void pick(Canvas g, String id, IntSupplier get, IntConsumer set, float x, float y, float w, float h) {
        int col = get.getAsInt();
        float[] hs = hsv.computeIfAbsent(id, k -> Colors.hsv(col));
        if (typ == null && drag == null && (Colors.fromHsv(hs[0], hs[1], hs[2]) & 0xFFFFFF) != (col & 0xFFFFFF)) System.arraycopy(Colors.hsv(col), 0, hs, 0, 3);
        float sw = Math.round(w * 0.52f);
        int hc = 0xFF000000 | Colors.fromHsv(hs[0], 1f, 1f);
        g.shadow(x, y + 1, sw, h, 7, 5, fd(0x70000000));
        g.sv(x, y, sw, h, 7, fd(hc));
        g.ring(x, y, sw, h, 7, 1, fd(0x33FFFFFF), fd(0x14FFFFFF));
        float mxp = x + hs[1] * sw;
        float myp = y + (1f - hs[2]) * h;
        g.shadow(mxp - 4.5f, myp - 4.5f, 9, 9, 4.5f, 2, fd(0x90000000));
        g.fill(mxp - 4.5f, myp - 4.5f, 9, 9, 4.5f, fd(KN1));
        g.fill(mxp - 3, myp - 3, 6, 6, 3, fd(0xFF000000 | col));
        hits.add(new Ht(id + "sv", x, y, sw, h, null, (px, py) -> {
            hs[1] = Colors.clamp((float) ((px - x) / sw), 0f, 1f);
            hs[2] = 1f - Colors.clamp((float) ((py - y) / h), 0f, 1f);
            set.accept(Colors.fromHsv(hs[0], hs[1], hs[2]));
            typ = null;
        }, null));
        float rx = x + sw + 10;
        float rw = w - sw - 10;
        g.shadow(rx, y + 1, rw, 12, 6, 3, fd(0x60000000));
        g.hue(rx, y, rw, 12, 6, fd(KN1));
        g.ring(rx, y, rw, 12, 6, 1, fd(0x33FFFFFF), fd(0x14FFFFFF));
        float hx = rx + hs[0] * rw;
        g.shadow(hx - 3.5f, y - 2, 7, 16, 3.5f, 2, fd(0x90000000));
        g.fill(hx - 3.5f, y - 2, 7, 16, 3.5f, fd(KN1), fd(KN2));
        g.fill(hx - 2, y + 0.5f, 4, 11, 2, fd(hc));
        hits.add(new Ht(id + "hue", rx - 3, y - 3, rw + 6, 18, null, (px, py) -> {
            hs[0] = Colors.clamp((float) ((px - rx) / rw), 0f, 0.9999f);
            set.accept(Colors.fromHsv(hs[0], hs[1], hs[2]));
            typ = null;
        }, a -> {
            hs[0] = (hs[0] + (float) Math.signum(a) * 0.01f + 1f) % 1f;
            set.accept(Colors.fromHsv(hs[0], hs[1], hs[2]));
        }));
        float fy = y + 20;
        float sq = 18;
        float fw = rw - sq - 6;
        boolean foc = id.equals(typ) && act == null;
        float k = an(id + "hex", foc ? 1f : hov(rx, fy, fw, sq) ? 0.4f : 0f, 14f);
        g.fill(rx, fy, fw, sq, 7, fd(WL), fd(0x30000000));
        g.ring(rx, fy, fw, sq, 7, 1, fd(Colors.mix(ED, AC, k)), fd(Colors.mix(0x0AFFFFFF, AC, k * 0.5f)));
        String sh = foc ? "#" + hex : Colors.hex(col);
        g.text(sh, rx + 7, fy + 5, fd(foc && !vld(hex) ? 0xFFFF8080 : TX));
        if (foc && (clock % 1f) < 0.55f) g.fill(rx + 7 + g.width(sh) + 1, fy + 4, 1, 10, 0, fd(TX));
        hits.add(new Ht(id + "hexf", rx, fy, fw, sq, () -> {
            typ = id;
            hex = Colors.hex(get.getAsInt()).substring(1);
            cset = set;
        }, null, null));
        float sx = rx + rw - sq;
        g.shadow(sx, fy + 1, sq, sq, 7, 4, fd(Colors.alpha(0xFF000000 | col, 0.45f)));
        g.fill(sx, fy, sq, sq, 7, fd(0xFF000000 | Colors.mix(0xFF000000 | col, 0xFFFFFFFF, 0.18f)), fd(0xFF000000 | col));
        g.ring(sx, fy, sq, sq, 7, 1, fd(0x55FFFFFF), fd(0x14FFFFFF));
    }

    private IntConsumer cset;

    protected static boolean vld(String s) {
        return (s.length() == 6 || s.length() == 3) && s.chars().allMatch(ch -> Character.digit(ch, 16) >= 0);
    }

    protected void commit() {
        if (typ == null) return;
        String id = typ;
        typ = null;
        if (vld(hex) && cset != null) {
            int c = Colors.parse(hex, 0);
            cset.accept(c);
            float[] hs = hsv.get(id);
            if (hs != null) System.arraycopy(Colors.hsv(c), 0, hs, 0, 3);
        }
    }

    protected static float snap(float v, float step, float lo, float hi) {
        return Colors.clamp(Math.round(v / step) * step, lo, hi);
    }

    public boolean click(double x, double y, int b) {
        mx = x;
        my = y;
        if (b != 0) return false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Ht h = hits.get(i);
            if (!h.in(x, y)) continue;
            if (!h.id.endsWith("hexf")) commit();
            if (h.dg != null) {
                act = h.id;
                drag = h.dg;
                drag.to(x, y);
            }
            if (h.ck != null) h.ck.run();
            return true;
        }
        commit();
        return false;
    }

    public boolean release(double x, double y, int b) {
        boolean was = drag != null;
        drag = null;
        act = null;
        return was;
    }

    public boolean dragged(double x, double y) {
        mx = x;
        my = y;
        if (drag == null) return false;
        drag.to(x, y);
        return true;
    }

    public boolean scroll(double x, double y, double a) {
        if (a == 0) return false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Ht h = hits.get(i);
            if (h.sc != null && h.in(x, y)) {
                h.sc.accept(a);
                return true;
            }
        }
        return wheel(a);
    }

    protected boolean wheel(double a) {
        return false;
    }

    public boolean key(int a) {
        if (typ == null) return false;
        switch (a) {
            case BACK -> {
                if (!hex.isEmpty()) hex = hex.substring(0, hex.length() - 1);
                return true;
            }
            case ENTER -> {
                commit();
                return true;
            }
            case ESCAPE -> {
                typ = null;
                return true;
            }
            case PASTE -> {
                String s = clip.get();
                if (s != null) {
                    String t = s.trim().replace("#", "");
                    if (vld(t)) {
                        hex = t.toUpperCase(Locale.ROOT);
                        commit();
                        typ = null;
                    }
                }
                return true;
            }
            case COPY -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public boolean type(int cp) {
        if (typ == null) return false;
        if (Character.digit(cp, 16) < 0) return true;
        if (hex.length() < 6) {
            hex = hex + Character.toUpperCase((char) cp);
            if (hex.length() == 6) {
                String id = typ;
                IntConsumer cs = cset;
                int c = Colors.parse(hex, 0);
                cs.accept(c);
                float[] hs = hsv.get(id);
                if (hs != null) System.arraycopy(Colors.hsv(c), 0, hs, 0, 3);
            }
        }
        return true;
    }

    public boolean typing() {
        return typ != null;
    }

    public void save() {
        commit();
    }
}
