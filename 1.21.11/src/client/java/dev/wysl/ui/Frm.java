package dev.wysl.ui;

import dev.wysl.Colors;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class Frm extends Kit {
    public interface Pv {
        void draw(Canvas g, Frm f, float x, float y, float w, float h);
    }

    private abstract static class Rw {
        String lb;
        String tp;

        abstract float h();

        abstract void draw(Frm f, Canvas g, float x, float y, float w, String id);
    }

    private static final class Tb {
        String nm;
        final List<Rw> rs = new ArrayList<>();
    }

    private final String ttl;
    private final Runnable svr;
    private final List<Tb> tbs = new ArrayList<>();
    private Tb cur;
    private Pv pv;
    private Runnable rst;
    private int tab;
    private float tabK = 1f;
    private float sy;
    private float msy;

    private Frm(String t, Runnable s) {
        ttl = t;
        svr = s;
    }

    public static Frm of(String title, Runnable save) {
        return new Frm(title, save);
    }

    public Frm tab(String n) {
        cur = new Tb();
        cur.nm = n;
        tbs.add(cur);
        return this;
    }

    private Frm add(Rw r, String tp) {
        if (cur == null) tab("Settings");
        r.tp = tp;
        cur.rs.add(r);
        return this;
    }

    public Frm tip(String t) {
        if (cur != null && !cur.rs.isEmpty()) cur.rs.get(cur.rs.size() - 1).tp = t;
        return this;
    }

    public Frm tgl(String lb, BooleanSupplier g, Consumer<Boolean> s) {
        Rw r = new Rw() {
            float h() {
                return 20;
            }

            void draw(Frm f, Canvas c, float x, float y, float w, String id) {
                boolean on = g.getAsBoolean();
                f.row(c, id, lb, x, y, w, on, () -> s.accept(!on));
            }
        };
        r.lb = lb;
        return add(r, null);
    }

    public Frm sld(String lb, IntSupplier g, IntConsumer s, int lo, int hi, String unit) {
        Rw r = new Rw() {
            float h() {
                return 26;
            }

            void draw(Frm f, Canvas c, float x, float y, float w, String id) {
                int v = g.getAsInt();
                f.sld(c, id, lb, x, y, w, v, lo, hi, 1f, d -> s.accept((int) d), v + unit);
            }
        };
        r.lb = lb;
        return add(r, null);
    }

    public Frm cho(String lb, String[] nm, IntSupplier g, IntConsumer s) {
        Rw r = new Rw() {
            float h() {
                return 36;
            }

            void draw(Frm f, Canvas c, float x, float y, float w, String id) {
                c.text(lb, x, y, f.fd(MU));
                f.seg(c, id, x, y + 12, w, 16, nm, g.getAsInt(), s);
            }
        };
        r.lb = lb;
        return add(r, null);
    }

    public Frm sw(String lb, int[] cols, String[] nm, IntSupplier g, IntConsumer s) {
        Rw r = new Rw() {
            float h() {
                return 40;
            }

            void draw(Frm f, Canvas c, float x, float y, float w, String id) {
                c.text(lb, x, y, f.fd(MU));
                String cn = nm[g.getAsInt()];
                c.text(cn, x + w - c.width(cn), y, f.fd(TX));
                int n = cols.length;
                float gap = 3;
                float sz = Math.min(18, (w - gap * (n - 1)) / n);
                for (int i = 0; i < n; i++) {
                    float sx = x + i * (sz + gap);
                    boolean sel = g.getAsInt() == i;
                    boolean over = f.hov(sx, y + 13, sz, sz);
                    float k = f.an(id + i, sel ? 1f : over ? 0.5f : 0f, 14f);
                    int col = 0xFF000000 | cols[i];
                    c.fill(sx, y + 13 - k * 1.5f, sz, sz, 6, f.fd(col), f.fd(Colors.mix(col, 0xFF000000, 0.25f)));
                    c.ring(sx - 1, y + 12 - k * 1.5f, sz + 2, sz + 2, 7, 1.5f, f.fd(Colors.alpha(KN1, k)));
                    if (over) f.tip = nm[i];
                    int idx = i;
                    f.add(id + "s" + i, sx, y + 12, sz, sz + 2, () -> s.accept(idx));
                }
            }
        };
        r.lb = lb;
        return add(r, null);
    }

    public Frm clr(String lb, IntSupplier g, IntConsumer s) {
        Rw r = new Rw() {
            float h() {
                return 66;
            }

            void draw(Frm f, Canvas c, float x, float y, float w, String id) {
                c.text(lb, x, y, f.fd(MU));
                f.pick(c, id, g, s, x, y + 12, w, 38);
            }
        };
        r.lb = lb;
        return add(r, null);
    }

    public Frm btn(String lb, Runnable run) {
        Rw r = new Rw() {
            float h() {
                return 26;
            }

            void draw(Frm f, Canvas c, float x, float y, float w, String id) {
                f.btn(c, id, lb, x, y, w, false, run);
            }
        };
        r.lb = lb;
        return add(r, null);
    }

    public Frm note(String t) {
        Rw r = new Rw() {
            float h() {
                return 14;
            }

            void draw(Frm f, Canvas c, float x, float y, float w, String id) {
                c.text(t, x, y, f.fd(MU));
            }
        };
        r.lb = t;
        return add(r, null);
    }

    public Frm pre(Pv p) {
        pv = p;
        return this;
    }

    public Frm rst(Runnable r) {
        rst = r;
        return this;
    }

    public void draw(Canvas g, int width, int height, double mouseX, double mouseY) {
        begin(mouseX, mouseY);
        tabK = ease(tabK, 1f, 11f);
        float pw = pv != null ? 132 : 0;
        float cw = Math.min(width - 24, pv != null ? 440 : 320);
        float ch = Math.min(height - 12, 252);
        float x = (width - cw) / 2f;
        float y = (height - ch) / 2f + (1f - open) * 16f;
        dim(g, width, height);
        glass(g, x, y, cw, ch);
        g.text(ttl, x + 16, y + 12, fd(TX));
        g.fill(x + 16, y + 32, cw - 32, 1, 0, fd(0x16FFFFFF));
        float top = y + 42;
        float bot = y + ch - 36;
        if (pv != null) {
            well(g, x + 14, top, pw, bot - top, 10);
            pv.draw(g, this, x + 14, top, pw, bot - top);
        }
        float rx = x + 14 + (pv != null ? pw + 12 : 0);
        float rw = x + cw - 14 - rx;
        float cy = top;
        if (tbs.size() > 1) {
            String[] nm = new String[tbs.size()];
            for (int i = 0; i < nm.length; i++) nm[i] = tbs.get(i).nm;
            seg(g, "tabs", rx, top, rw, 18, nm, tab, i -> {
                commit();
                if (tab != i) {
                    tabK = 0f;
                    sy = 0f;
                }
                tab = i;
            });
            cy = top + 26;
        }
        Tb t = tbs.get(Math.min(tab, tbs.size() - 1));
        float so = (1f - tabK) * 18f;
        cf = tabK;
        float vis = bot - cy;
        float tot = 0;
        for (Rw r : t.rs) tot += r.h() + 6;
        msy = Math.max(0, tot - vis);
        sy = Colors.clamp(sy, 0, msy);
        float yy = cy - sy;
        int n = 0;
        for (Rw r : t.rs) {
            float rh = r.h();
            if (yy >= cy - 1 && yy + rh <= bot + 2) {
                r.draw(this, g, rx + so, yy, rw, "r" + tab + "_" + n);
                if (r.tp != null && hov(rx, yy, rw, rh)) tip = r.tp;
            }
            yy += rh + 6;
            n++;
        }
        cf = 1f;
        if (msy > 0) {
            float th = Math.max(16, vis * vis / tot);
            float ty = cy + (vis - th) * (sy / msy);
            g.fill(x + cw - 9, cy, 3, vis, 1.5f, fd(0x22FFFFFF));
            g.fill(x + cw - 9, ty, 3, th, 1.5f, fd(Colors.alpha(AC, 0.8f)));
        }
        float by = y + ch - 28;
        if (rst != null) btn(g, "rst", "Defaults", x + 14, by, 66, false, () -> {
            rst.run();
            save();
        });
        btn(g, "done", "Done", x + cw - 14 - 60, by, 60, true, () -> {
            save();
            close.run();
        });
        tipDraw(g, width);
    }

    @Override
    protected boolean wheel(double a) {
        sy = Colors.clamp(sy - (float) a * 22f, 0, msy);
        return true;
    }

    @Override
    public boolean key(int a) {
        if (typ == null && a == TAB && tbs.size() > 1) {
            tab = (tab + 1) % tbs.size();
            tabK = 0f;
            sy = 0f;
            return true;
        }
        return super.key(a);
    }

    @Override
    public void save() {
        super.save();
        if (svr != null) svr.run();
    }
}
