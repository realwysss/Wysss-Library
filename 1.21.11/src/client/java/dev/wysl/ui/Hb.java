package dev.wysl.ui;

import dev.wysl.Colors;
import dev.wysl.Pks;
import dev.wysl.Wl;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class Hb extends Kit {
    private record Md(String id, String nm, String ds, String repo, String cls) {
    }

    private static final Md[] MODS = {
        new Md("glowlib", "Glow Library", "all glow mods in one place, sync colors and rainbow", "Glow-Library", "dev.glib.Gl"),
        new Md("hitglow", "Hit Glow", "glow when something gets hit", "Hit-Glow", "dev.htgl.ui.Panel"),
        new Md("hitcolor", "Hit Color", "color, gradient and fade of the hit flash", "Hit-Color", "dev.htcl.ui.Panel"),
        new Md("xpglow", "XP Glow", "experience orbs glow", "XP-Glow", "dev.xpgl.ui.Panel"),
        new Md("crystalglow", "Crystal Glow", "end crystals glow", "Crystal-Glow", "dev.crgl.ui.Panel"),
        new Md("anchorglow", "Anchor Glow", "respawn anchors glow with their charge", "Anchor-Glow", "dev.angl.ui.Panel"),
        new Md("totemparticleglow", "Totem Particle Glow", "totem particles glow", "Totem-Particle-Glow", "dev.tpgl.ui.Panel"),
        new Md("klfx", "Kill Effects", "effects and sounds when you kill", "Kill-Effects", "dev.klfx.Gui"),
        new Md("tuffglint", "Tuff Glint", "stronger enchant glint with presets", "Tuff-Glint", "dev.tfgl.Gui"),
        new Md("itemsopacity", "Items Opacity", "change how see through blocks and items are", "Items-Opacity", "dev.itop.Gui")
    };

    private static final String[] TABS = {"Mods", "Packs"};

    private int tab;
    private float tabK = 1f;
    private float sy;
    private float msy;

    private static boolean has(Md m) {
        return FabricLoader.getInstance().isModLoaded(m.id);
    }

    private static String ver(Md m) {
        return FabricLoader.getInstance().getModContainer(m.id).map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
    }

    private static void go(Md m) {
        Minecraft mc = Minecraft.getInstance();
        try {
            Screen p = mc.screen;
            Class<?> k = Class.forName(m.cls);
            try {
                mc.setScreen((Screen) k.getMethod("open", Screen.class).invoke(null, p));
            } catch (NoSuchMethodException e) {
                mc.setScreen((Screen) k.getConstructor(Screen.class).newInstance(p));
            }
        } catch (Throwable t) {
        }
    }

    public void draw(Canvas g, int width, int height, double mouseX, double mouseY) {
        begin(mouseX, mouseY);
        tabK = ease(tabK, 1f, 11f);
        float cw = Math.min(width - 24, 400);
        float ch = Math.min(height - 12, 260);
        float x = (width - cw) / 2f;
        float y = (height - ch) / 2f + (1f - open) * 16f;
        dim(g, width, height);
        glass(g, x, y, cw, ch);
        g.text("Wysss Library", x + 16, y + 12, fd(TX));
        String v = "v" + FabricLoader.getInstance().getModContainer("wysslib").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("");
        g.text(v, x + cw - 16 - g.width(v), y + 12, fd(MU));
        g.fill(x + 16, y + 32, cw - 32, 1, 0, fd(0x16FFFFFF));
        float rx = x + 14;
        float rw = cw - 28;
        seg(g, "tabs", rx, y + 40, rw, 18, TABS, tab, i -> {
            commit();
            if (tab != i) {
                tabK = 0f;
                sy = 0f;
            }
            tab = i;
        });
        float top = y + 66;
        float bot = y + ch - 12;
        float so = (1f - tabK) * 18f;
        cf = tabK;
        if (tab == 0) mods(g, rx + so, top, rw, bot);
        else packs(g, rx + so, top, rw, bot);
        cf = 1f;
        tipDraw(g, width);
    }

    private void scrollbar(Canvas g, float x, float top, float bot, float tot) {
        float vis = bot - top;
        msy = Math.max(0, tot - vis);
        sy = Colors.clamp(sy, 0, msy);
        if (msy <= 0) return;
        float th = Math.max(16, vis * vis / tot);
        float ty = top + (vis - th) * (sy / msy);
        g.fill(x, top, 3, vis, 1.5f, fd(0x22FFFFFF));
        g.fill(x, ty, 3, th, 1.5f, fd(Colors.alpha(AC, 0.8f)));
    }

    private void mods(Canvas g, float x, float top, float w, float bot) {
        float rh = 34;
        scrollbar(g, x + w + 2, top, bot, MODS.length * rh);
        float yy = top - sy;
        for (int i = 0; i < MODS.length; i++, yy += rh) {
            if (yy < top - 1 || yy + rh - 4 > bot + 1) continue;
            Md m = MODS[i];
            boolean on = has(m);
            boolean over = hov(x - 4, yy, w + 8, rh - 4);
            float k = an("m" + i, over ? 1f : 0f, 14f);
            g.fill(x - 4, yy, w + 8, rh - 4, 9, fd(Colors.alpha(0x1CFFFFFF, 0.4f + k * 0.6f)), fd(Colors.alpha(0x0AFFFFFF, 0.4f + k * 0.6f)));
            g.text(m.nm, x + 6, yy + 6, fd(on ? TX : MU));
            String ds = on ? m.ds + "  v" + ver(m) : m.ds;
            g.text(ds, x + 6, yy + 17, fd(MU));
            if (on) btn(g, "open" + i, "Open", x + w - 52, yy + 5, 46, true, () -> go(m));
            else btn(g, "get" + i, "Get", x + w - 52, yy + 5, 46, false, () -> net.minecraft.util.Util.getPlatform().openUri("https://github.com/realwysss/" + m.repo));
        }
    }

    private void packs(Canvas g, float x, float top, float w, float bot) {
        List<Pks.P> ps = Pks.ALL;
        float rh = 34;
        scrollbar(g, x + w + 2, top, bot, ps.size() * rh);
        float yy = top - sy;
        for (int i = 0; i < ps.size(); i++, yy += rh) {
            if (yy < top - 1 || yy + rh - 4 > bot + 1) continue;
            Pks.P p = ps.get(i);
            boolean over = hov(x - 4, yy, w + 8, rh - 4);
            float k = an("p" + i, over ? 1f : 0f, 14f);
            g.fill(x - 4, yy, w + 8, rh - 4, 9, fd(Colors.alpha(0x1CFFFFFF, 0.4f + k * 0.6f)), fd(Colors.alpha(0x0AFFFFFF, 0.4f + k * 0.6f)));
            g.text(p.nm(), x + 6, yy + 6, fd(TX));
            String ds = p.ds();
            int mw = (int) (w - 120);
            while (g.width(ds) > mw && ds.length() > 4) ds = ds.substring(0, ds.length() - 4) + "...";
            g.text(ds, x + 6, yy + 17, fd(MU));
            String sz = Pks.mb(p.sz());
            g.text(sz, x + w - 72 - g.width(sz), yy + 11, fd(MU));
            int st = Pks.state(p);
            if (st == 1) {
                int pc = Pks.pct(p);
                g.fill(x + w - 58, yy + 11, 52, 4, 2, fd(WL));
                g.fill(x + w - 58, yy + 11, Math.max(4, 52 * pc / 100f), 4, 2, fd(Colors.mix(AC, 0xFFFFFFFF, 0.25f)), fd(AC));
                g.text(pc + "%", x + w - 58, yy + 18, fd(MU));
            } else if (st == 0) btn(g, "dl" + i, "Download", x + w - 62, yy + 5, 56, false, () -> Pks.get(p));
            else if (st == 2) btn(g, "use" + i, "Use", x + w - 62, yy + 5, 56, true, () -> Pks.use(p, true));
            else btn(g, "off" + i, "Using", x + w - 62, yy + 5, 56, false, () -> Pks.use(p, false));
        }
    }

    @Override
    protected boolean wheel(double a) {
        sy = Colors.clamp(sy - (float) a * 26f, 0, msy);
        return true;
    }

    @Override
    public boolean key(int a) {
        if (typ == null && a == TAB) {
            tab = (tab + 1) % TABS.length;
            tabK = 0f;
            sy = 0f;
            return true;
        }
        return super.key(a);
    }
}
