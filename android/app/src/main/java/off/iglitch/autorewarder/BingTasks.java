package off.iglitch.autorewarder;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Check-in / news in a WebView. Done only when getuserinfo says so. */
final class BingTasks {
    static final String UA =
            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36 "
                    + "BingSapphire/30.0.410309301";

    private static final String CLICK_JS =
            "(function(){"
                    + "var nodes=document.querySelectorAll('button,a,[role=button],span,div,p');"
                    + "for(var i=0;i<nodes.length;i++){"
                    + " var el=nodes[i];"
                    + " var t=((el.innerText||el.getAttribute('aria-label')||'')+'').replace(/\\s+/g,' ').trim();"
                    + " if(!t||t.length>90) continue;"
                    + " if(/redeem|canjear|donate|donar/i.test(t)) continue;"
                    + " if(/check[\\s-]?in|registrar(se)?|fichar|asistencia/i.test(t)){"
                    + "  (el.closest('button,a,[role=button]')||el).click(); return true;"
                    + " }"
                    + "}"
                    + "return false;"
                    + "})();";

    private static final String SNAPSHOT_JS =
            "(async function(){"
                    + "try{"
                    + " const r=await fetch('https://rewards.bing.com/api/getuserinfo?type=1',{credentials:'include'});"
                    + " if(!r.ok) return JSON.stringify({readable:false,status:r.status});"
                    + " const data=await r.json();"
                    + " const dash=data.dashboard||{};"
                    + " const counters=((dash.userStatus||{}).counters)||{};"
                    + " function frac(keys){"
                    + "  for(var i=0;i<keys.length;i++){"
                    + "   var blob=counters[keys[i]];"
                    + "   if(Array.isArray(blob)) blob=blob[0];"
                    + "   if(!blob) continue;"
                    + "   var done=blob.pointProgress, max=blob.pointProgressMax;"
                    + "   if(typeof done==='number'&&typeof max==='number'&&max>0) return [done,max];"
                    + "  }"
                    + "  return null;"
                    + " }"
                    + " var news=frac(['readArticle','readarticle','newsSearch']);"
                    + " var checkin=null;"
                    + " var promos=[].concat(dash.promotionalItems||[], dash.morePromotions||[]);"
                    + " for(var i=0;i<promos.length;i++){"
                    + "  var p=promos[i]||{}; var parent=p.parentPromotion||p;"
                    + "  var ptype=String(parent.promotionType||'').toLowerCase();"
                    + "  var title=((parent.name||'')+' '+(parent.title||'')+' '+(parent.description||'')).toLowerCase();"
                    + "  if(ptype==='checkin'||title.indexOf('check-in')>=0||title.indexOf('check in')>=0){"
                    + "   checkin=parent.complete?[1,1]:[0,1]; break;"
                    + "  }"
                    + " }"
                    + " return JSON.stringify({readable:true,checkin:checkin,news:news});"
                    + "}catch(e){return JSON.stringify({readable:false,error:String(e)});}"
                    + "})();";

    private static final String NEWS_HREFS_JS =
            "(function(){"
                    + "var sel=['a.title','a.card-title','.title-container a','a.card-link','.news-card a',"
                    + "'a[href*=\"msn.com/\"]','a[href*=\"microsoftnews\"]','[class*=\"news\"] a[href]'];"
                    + "var out=[], seen={};"
                    + "for(var s=0;s<sel.length;s++){"
                    + " var els=document.querySelectorAll(sel[s]);"
                    + " for(var i=0;i<els.length;i++){"
                    + "  var h=els[i].href||'';"
                    + "  if(!h||seen[h]) continue;"
                    + "  if(h.indexOf('bing.com/search')>=0) continue;"
                    + "  if(h.indexOf('login')>=0) continue;"
                    + "  if(h.indexOf('http')!==0) continue;"
                    + "  seen[h]=1; out.push(h);"
                    + "  if(out.length>=8) return JSON.stringify(out);"
                    + " }"
                    + "}"
                    + "return JSON.stringify(out);"
                    + "})();";

    private final MainActivity activity;
    private final WebView bing;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String kind = "";
    private int step = 0;
    private boolean waitingLogin = false;
    private final List<String> newsHrefs = new ArrayList<>();
    private int newsIndex = 0;
    private boolean running = false;
    private boolean sawReadable = false;
    private boolean newsSnapshotted = false;
    private int newsStart = -1;
    private String[] checkinUrls = new String[0];
    private static final int TASK_DEADLINE_MS = 90000;

    BingTasks(MainActivity activity, WebView bing) {
        this.activity = activity;
        this.bing = bing;
        WebSettings s = bing.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setUserAgentString(UA);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(bing, true);
        bing.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request == null || request.getUrl() == null) return false;
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null
                        ? ""
                        : uri.getScheme().toLowerCase(Locale.US);
                if ("http".equals(scheme) || "https".equals(scheme)) return false;
                try {
                    Intent intent = "intent".equals(scheme)
                            ? Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                            : new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        activity.startActivity(intent);
                        return true;
                    }
                } catch (Exception ignored) {}
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                onLoaded(url);
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                if (running) finish(false, "no se pudo");
                return true;
            }
        });
    }

    void start(String kind) {
        this.kind = kind == null ? "checkin" : kind;
        this.step = 0;
        this.waitingLogin = false;
        this.newsHrefs.clear();
        this.newsIndex = 0;
        this.running = true;
        this.sawReadable = false;
        this.newsSnapshotted = false;
        this.newsStart = -1;
        String mkt = BingMarket.get();
        this.checkinUrls = new String[] {
                "https://www.bing.com/?form=APMCS1&setmkt=" + mkt,
                "https://rewards.bing.com/dashboard?setmkt=" + mkt,
                "https://rewards.bing.com/?form=ML2N2V&setmkt=" + mkt
        };
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            if (running) finish(false, "no se pudo");
        }, TASK_DEADLINE_MS);
        if ("news".equals(this.kind)) {
            activity.setBingStatus("Abriendo noticias Bing…");
            bing.loadUrl("https://www.bing.com/news?form=APMCS1&setmkt=" + mkt + "&dcf=1");
        } else {
            activity.setBingStatus("Abriendo Rewards para check-in…");
            bing.loadUrl(checkinUrls[0]);
        }
    }

    void cancel() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    private boolean isLogin(String url) {
        String u = url == null ? "" : url.toLowerCase(Locale.US);
        return u.contains("login.live.com")
                || u.contains("login.microsoftonline.com")
                || u.contains("account.live.com")
                || u.contains("account.microsoft.com");
    }

    private void onLoaded(String url) {
        if (!running) return;
        if (isLogin(url)) {
            waitingLogin = true;
            activity.setBingStatus("Inicia sesión con la misma cuenta Microsoft. Solo una vez; luego sigue solo.");
            return;
        }
        if (waitingLogin) {
            waitingLogin = false;
            activity.setBingStatus("Sesión guardada. Continuando…");
        }
        if ("news".equals(kind)) {
            handleNews(url);
        } else {
            handleCheckin();
        }
    }

    private void handleCheckin() {
        handler.postDelayed(() -> {
            if (!running) return;
            bing.evaluateJavascript(SNAPSHOT_JS, value -> {
                if (!running) return;
                JSONObject o = parseObj(value);
                if (o.optBoolean("readable")) {
                    sawReadable = true;
                    if (checkinDone(o)) {
                        finish(true, checkinLabel(o));
                        return;
                    }
                }
                bing.evaluateJavascript(CLICK_JS, clicked -> {
                    if (!running) return;
                    step++;
                    if (step < checkinUrls.length) {
                        activity.setBingStatus("Check-in, intento " + (step + 1) + "…");
                        bing.loadUrl(checkinUrls[step]);
                    } else {
                        confirmCheckin();
                    }
                });
            });
        }, 2500);
    }

    private void confirmCheckin() {
        handler.postDelayed(() -> {
            if (!running) return;
            bing.evaluateJavascript(SNAPSHOT_JS, value -> {
                if (!running) return;
                JSONObject o = parseObj(value);
                if (o.optBoolean("readable") && checkinDone(o)) {
                    finish(true, checkinLabel(o));
                    return;
                }
                if (!o.optBoolean("readable") && !sawReadable) {
                    finish(false, "ask:no se pudo leer getuserinfo");
                    return;
                }
                finish(false, "no se pudo");
            });
        }, 2500);
    }

    private void handleNews(String url) {
        if (!newsSnapshotted) {
            newsSnapshotted = true;
            final String page = url;
            handler.postDelayed(() -> {
                if (!running) return;
                bing.evaluateJavascript(SNAPSHOT_JS, value -> {
                    if (!running) return;
                    newsSnapshotted = true;
                    JSONObject o = parseObj(value);
                    if (!o.optBoolean("readable")) {
                        finish(false, "ask:no se pudo leer getuserinfo");
                        return;
                    }
                    JSONArray news = o.optJSONArray("news");
                    if (news == null || news.length() < 2 || news.optInt(1, 0) <= 0) {
                        finish(false, "no se pudo");
                        return;
                    }
                    int done = news.optInt(0, 0);
                    int max = news.optInt(1, 0);
                    if (done >= max) {
                        finish(true, "Noticias " + done + "/" + max);
                        return;
                    }
                    newsStart = done;
                    collectNews(page);
                });
            }, 1500);
            return;
        }
        if (newsIndex > 0 && newsIndex <= newsHrefs.size()) {
            handler.postDelayed(this::openNextArticle, 12000);
        }
    }

    private void collectNews(String url) {
        if (url != null && url.contains("/news")) {
            handler.postDelayed(() -> bing.evaluateJavascript(NEWS_HREFS_JS, value -> {
                parseHrefs(value);
                openNextArticle();
            }), 2000);
            return;
        }
        openNextArticle();
    }

    private static String decodeJs(String value) throws Exception {
        if (value == null || value.equals("null")) return "{}";
        Object parsed = new org.json.JSONTokener(value).nextValue();
        return parsed == null ? "{}" : parsed.toString();
    }

    private JSONObject parseObj(String value) {
        try {
            return new JSONObject(decodeJs(value));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static boolean checkinDone(JSONObject o) {
        JSONArray c = o.optJSONArray("checkin");
        if (c == null || c.length() < 2) return false;
        int total = c.optInt(1, 0);
        return total > 0 && c.optInt(0, 0) >= total;
    }

    private static String checkinLabel(JSONObject o) {
        JSONArray c = o.optJSONArray("checkin");
        if (c == null || c.length() < 2) return "Check-in listo";
        return "Check-in " + c.optInt(0, 0) + "/" + c.optInt(1, 0);
    }

    private void parseHrefs(String value) {
        newsHrefs.clear();
        if (value == null) return;
        try {
            String raw = decodeJs(value);
            org.json.JSONArray arr = new org.json.JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                newsHrefs.add(arr.getString(i));
            }
        } catch (Exception ignored) {}
    }

    private void openNextArticle() {
        if (!running) return;
        if (newsIndex >= newsHrefs.size() || newsIndex >= 6) {
            verifyNews();
            return;
        }
        String href = newsHrefs.get(newsIndex);
        newsIndex++;
        activity.setBingStatus("Leyendo artículo " + newsIndex + "…");
        bing.loadUrl(href);
    }

    private void verifyNews() {
        bing.evaluateJavascript(SNAPSHOT_JS, value -> {
            if (!running) return;
            JSONObject o = parseObj(value);
            JSONArray news = o.optJSONArray("news");
            int now = news == null ? -1 : news.optInt(0, -1);
            int max = news == null ? 0 : news.optInt(1, 0);
            if (newsStart >= 0 && now > newsStart) {
                finish(true, "Noticias " + now + "/" + max);
                return;
            }
            finish(false, "no se pudo");
        });
    }

    private void finish(boolean ok, String detail) {
        running = false;
        handler.removeCallbacksAndMessages(null);
        try { CookieManager.getInstance().flush(); } catch (Exception ignored) {}
        activity.onBingTaskDone(ok, detail);
    }
}
