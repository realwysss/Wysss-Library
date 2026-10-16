package dev.wysl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

public final class Pks {
    public record P(String nm, String ds, String fl, String url, long sz) {
    }

    public static final List<P> ALL = load();
    static final Map<String, long[]> prog = new ConcurrentHashMap<>();
    static final Map<String, Boolean> bad = new ConcurrentHashMap<>();

    private Pks() {
    }

    private static List<P> load() {
        List<P> l = new ArrayList<>();
        try (InputStream in = Pks.class.getResourceAsStream("/assets/wysslib/packs.json")) {
            JsonArray a = new Gson().fromJson(new InputStreamReader(in), JsonArray.class);
            for (JsonElement e : a) {
                JsonObject o = e.getAsJsonObject();
                l.add(new P(o.get("nm").getAsString(), o.get("ds").getAsString(), o.get("fl").getAsString(), o.get("url").getAsString(), o.get("sz").getAsLong()));
            }
        } catch (Exception e) {
        }
        return l;
    }

    public static String mb(long b) {
        return b >= 1048576 ? String.format("%.1f MB", b / 1048576.0) : Math.max(1, b / 1024) + " KB";
    }

    static Path dir() {
        return Minecraft.getInstance().getResourcePackDirectory();
    }

    public static int pct(P p) {
        long[] g = prog.get(p.fl());
        return g == null || g[1] <= 0 ? 0 : (int) Math.min(99, g[0] * 100 / g[1]);
    }

    public static int state(P p) {
        if (prog.containsKey(p.fl())) return 1;
        if (!Files.exists(dir().resolve(p.fl()))) return 0;
        return Minecraft.getInstance().getResourcePackRepository().getSelectedIds().contains("file/" + p.fl()) ? 3 : 2;
    }

    public static void get(P p) {
        if (prog.containsKey(p.fl())) return;
        prog.put(p.fl(), new long[]{0, p.sz()});
        Thread t = new Thread(() -> {
            Path tmp = null;
            try {
                HttpClient c = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
                HttpResponse<InputStream> r = c.send(HttpRequest.newBuilder(URI.create(p.url())).header("User-Agent", "wysslib").build(), HttpResponse.BodyHandlers.ofInputStream());
                if (r.statusCode() != 200) throw new RuntimeException("bad status");
                long len = r.headers().firstValueAsLong("content-length").orElse(p.sz());
                long[] g = prog.get(p.fl());
                g[1] = len;
                tmp = Files.createTempFile(dir(), "wysslib", ".part");
                try (InputStream in = r.body(); java.io.OutputStream out = Files.newOutputStream(tmp)) {
                    byte[] b = new byte[65536];
                    int n;
                    while ((n = in.read(b)) > 0) {
                        out.write(b, 0, n);
                        g[0] += n;
                    }
                }
                Files.move(tmp, dir().resolve(p.fl()), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                try {
                    if (tmp != null) Files.deleteIfExists(tmp);
                } catch (Exception x) {
                }
            }
            prog.remove(p.fl());
        }, "wysslib download");
        t.setDaemon(true);
        t.start();
    }

    public static void use(P p, boolean on) {
        Minecraft m = Minecraft.getInstance();
        PackRepository r = m.getResourcePackRepository();
        r.reload();
        String id = "file/" + p.fl();
        List<String> l = new ArrayList<>(r.getSelectedIds());
        l.remove(id);
        if (on) l.add(id);
        r.setSelected(l);
        m.options.updateResourcePacks(r);
    }
}
