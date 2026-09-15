package com.masteria.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import android.content.SharedPreferences;
import android.util.Base64;

public class MainActivity extends Activity {
    private static final String PREFS = "master_ia_prefs";
    private static final String DEFAULT_MODEL = "gpt-5.6-sol";
    private static final String API_URL = "https://api.openai.com/v1/responses";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<ChatMessage> history = new ArrayList<>();

    private SharedPreferences prefs;
    private LinearLayout chatList;
    private ScrollView scrollView;
    private EditText input;
    private Button sendButton;
    private Switch webSwitch;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(8, 15, 28));
        window.setNavigationBarColor(Color.rgb(8, 15, 28));
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        buildUi();
        loadHistory();

        if (history.isEmpty()) {
            addAssistant("Olá! Eu sou o Master IA. Posso pesquisar na web e atuar como especialista em software, programação, engenharia, automação, dados e tecnologia. Abra Configurações e informe sua chave de API para começar.", false);
        } else {
            renderHistory();
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(12), dp(14), dp(12));
        root.setBackgroundColor(Color.rgb(8, 15, 28));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleWrap = new LinearLayout(this);
        titleWrap.setOrientation(LinearLayout.VERTICAL);
        titleWrap.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText("Master IA");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("Assistente inteligente com pesquisa online");
        subtitle.setTextColor(Color.rgb(148, 163, 184));
        subtitle.setTextSize(12);

        titleWrap.addView(title);
        titleWrap.addView(subtitle);
        header.addView(titleWrap);

        Button settings = makeButton("Configurações");
        settings.setOnClickListener(v -> openSettings());
        header.addView(settings);
        root.addView(header);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(0, dp(10), 0, dp(8));

        webSwitch = new Switch(this);
        webSwitch.setText("Pesquisar na web");
        webSwitch.setTextColor(Color.WHITE);
        webSwitch.setTextSize(14);
        webSwitch.setChecked(prefs.getBoolean("web", true));
        webSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean("web", isChecked).apply());
        toolbar.addView(webSwitch, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button clear = makeButton("Limpar");
        clear.setOnClickListener(v -> confirmClear());
        toolbar.addView(clear);
        root.addView(toolbar);

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        chatList = new LinearLayout(this);
        chatList.setOrientation(LinearLayout.VERTICAL);
        chatList.setPadding(0, dp(4), 0, dp(4));
        scrollView.addView(chatList);
        root.addView(scrollView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.setMargins(0, dp(4), 0, dp(4));
        root.addView(progress, progressParams);

        LinearLayout composer = new LinearLayout(this);
        composer.setOrientation(LinearLayout.HORIZONTAL);
        composer.setGravity(Gravity.BOTTOM);
        composer.setPadding(0, dp(8), 0, 0);

        input = new EditText(this);
        input.setHint("Pergunte qualquer coisa...");
        input.setHintTextColor(Color.rgb(100, 116, 139));
        input.setTextColor(Color.WHITE);
        input.setTextSize(16);
        input.setMinHeight(dp(52));
        input.setMaxLines(5);
        input.setPadding(dp(14), dp(10), dp(14), dp(10));
        input.setBackground(roundRect(Color.rgb(20, 31, 50), 18));
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        inputParams.setMargins(0, 0, dp(8), 0);
        composer.addView(input, inputParams);

        sendButton = makeButton("Enviar");
        sendButton.setMinHeight(dp(52));
        sendButton.setOnClickListener(v -> sendMessage());
        composer.addView(sendButton);
        root.addView(composer);

        setContentView(root);
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setPadding(dp(12), 0, dp(12), 0);
        b.setBackground(roundRect(Color.rgb(37, 99, 235), 14));
        return b;
    }

    private GradientDrawable roundRect(int color, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(dp(radiusDp));
        return gd;
    }

    private void sendMessage() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;

        String apiKey;
        try {
            apiKey = SecureStore.load(this);
        } catch (Exception e) {
            showToast("Não foi possível ler a chave de API.");
            return;
        }

        if (apiKey == null || apiKey.isEmpty()) {
            openSettings();
            showToast("Configure sua chave de API primeiro.");
            return;
        }

        input.setText("");
        addUser(text, true);
        setLoading(true);

        String model = prefs.getString("model", DEFAULT_MODEL);
        boolean useWeb = webSwitch.isChecked();
        List<ChatMessage> snapshot = new ArrayList<>(history);

        executor.execute(() -> {
            try {
                String answer = callResponsesApi(apiKey, model, useWeb, snapshot);
                runOnUiThread(() -> {
                    addAssistant(answer, true);
                    setLoading(false);
                });
            } catch (Exception e) {
                String message = e.getMessage() == null ? e.toString() : e.getMessage();
                runOnUiThread(() -> {
                    addAssistant("Erro ao consultar a IA: " + message, false);
                    setLoading(false);
                });
            }
        });
    }

    private String callResponsesApi(String apiKey, String model, boolean useWeb, List<ChatMessage> snapshot) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", model == null || model.trim().isEmpty() ? DEFAULT_MODEL : model.trim());
        body.put("instructions", systemInstructions());

        JSONArray inputItems = new JSONArray();
        int start = Math.max(0, snapshot.size() - 16);
        for (int i = start; i < snapshot.size(); i++) {
            ChatMessage m = snapshot.get(i);
            JSONObject item = new JSONObject();
            item.put("role", m.user ? "user" : "assistant");
            item.put("content", m.text);
            inputItems.put(item);
        }
        body.put("input", inputItems);

        if (useWeb) {
            JSONArray tools = new JSONArray();
            JSONObject search = new JSONObject();
            search.put("type", "web_search");
            search.put("search_context_size", "high");
            tools.put(search);
            body.put("tools", tools);
        }

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(180000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
        }

        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String response = readStream(stream);
        conn.disconnect();

        if (code < 200 || code >= 300) {
            try {
                JSONObject err = new JSONObject(response);
                String msg = err.optJSONObject("error") != null ? err.optJSONObject("error").optString("message", response) : response;
                throw new Exception("HTTP " + code + ": " + msg);
            } catch (org.json.JSONException ignored) {
                throw new Exception("HTTP " + code + ": " + response);
            }
        }

        return extractAnswer(new JSONObject(response));
    }

    private String extractAnswer(JSONObject response) {
        StringBuilder text = new StringBuilder();
        Set<String> sources = new LinkedHashSet<>();
        JSONArray output = response.optJSONArray("output");
        if (output != null) {
            for (int i = 0; i < output.length(); i++) {
                JSONObject item = output.optJSONObject(i);
                if (item == null || !"message".equals(item.optString("type"))) continue;
                JSONArray content = item.optJSONArray("content");
                if (content == null) continue;
                for (int j = 0; j < content.length(); j++) {
                    JSONObject part = content.optJSONObject(j);
                    if (part == null || !"output_text".equals(part.optString("type"))) continue;
                    String partText = part.optString("text", "");
                    if (!partText.isEmpty()) {
                        if (text.length() > 0) text.append("\n");
                        text.append(partText);
                    }
                    JSONArray annotations = part.optJSONArray("annotations");
                    if (annotations != null) {
                        for (int k = 0; k < annotations.length(); k++) {
                            JSONObject a = annotations.optJSONObject(k);
                            if (a == null) continue;
                            String u = a.optString("url", "");
                            String t = a.optString("title", "Fonte");
                            if (!u.isEmpty()) sources.add(t + " — " + u);
                        }
                    }
                }
            }
        }

        if (text.length() == 0) {
            text.append("A API respondeu sem texto utilizável.");
        }
        if (!sources.isEmpty()) {
            text.append("\n\nFontes:\n");
            int count = 0;
            for (String s : sources) {
                text.append("• ").append(s).append("\n");
                if (++count >= 8) break;
            }
        }
        return text.toString().trim();
    }

    private String systemInstructions() {
        return "Você é o Master IA, um assistente de alto nível para pesquisa e trabalho. " +
                "Atue como especialista em engenharia de software, programação, arquitetura, Android, Windows, web, bancos de dados, DevOps, segurança, automação, inteligência artificial, análise técnica e pesquisa. " +
                "Quando a pesquisa web estiver disponível, use-a para fatos atuais ou informações que possam ter mudado. " +
                "Seja preciso, explique limitações quando existirem, diferencie fatos de hipóteses e priorize soluções práticas. " +
                "Para desenvolvimento, pense como arquiteto, programador, QA, DevOps e especialista em segurança. " +
                "Não invente fontes nem resultados. Responda em português do Brasil, salvo pedido diferente do usuário.";
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private void addUser(String text, boolean persist) {
        if (persist) {
            history.add(new ChatMessage(true, text));
            trimHistory();
            saveHistory();
        }
        addBubble(text, true);
    }

    private void addAssistant(String text, boolean persist) {
        if (persist) {
            history.add(new ChatMessage(false, text));
            trimHistory();
            saveHistory();
        }
        addBubble(text, false);
    }

    private void addBubble(String text, boolean user) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextColor(Color.WHITE);
        bubble.setTextSize(15);
        bubble.setPadding(dp(14), dp(11), dp(14), dp(11));
        bubble.setTextIsSelectable(true);
        bubble.setBackground(roundRect(user ? Color.rgb(37, 99, 235) : Color.rgb(20, 31, 50), 18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = user ? Gravity.END : Gravity.START;
        lp.setMargins(user ? dp(38) : 0, dp(5), user ? 0 : dp(38), dp(5));
        chatList.addView(bubble, lp);
        scrollToBottom();
    }

    private void renderHistory() {
        chatList.removeAllViews();
        for (ChatMessage m : history) addBubble(m.text, m.user);
    }

    private void trimHistory() {
        while (history.size() > 40) history.remove(0);
    }

    private void saveHistory() {
        JSONArray arr = new JSONArray();
        try {
            for (ChatMessage m : history) {
                JSONObject o = new JSONObject();
                o.put("user", m.user);
                o.put("text", m.text);
                arr.put(o);
            }
            prefs.edit().putString("history", arr.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private void loadHistory() {
        history.clear();
        String raw = prefs.getString("history", "");
        if (raw == null || raw.isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                history.add(new ChatMessage(o.optBoolean("user", false), o.optString("text", "")));
            }
        } catch (Exception ignored) {
            history.clear();
        }
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar conversa")
                .setMessage("Apagar o histórico salvo neste aparelho?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Limpar", (d, w) -> {
                    history.clear();
                    prefs.edit().remove("history").apply();
                    chatList.removeAllViews();
                    addAssistant("Conversa limpa. Como posso ajudar?", false);
                }).show();
    }

    private void openSettings() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(8), dp(20), 0);

        TextView note = new TextView(this);
        note.setText("A chave é criptografada com o Android Keystore e fica somente neste aparelho. O uso da API depende dos limites e cobrança do provedor.");
        note.setTextSize(13);
        note.setPadding(0, 0, 0, dp(12));
        box.addView(note);

        EditText key = new EditText(this);
        key.setHint("Chave de API OpenAI");
        key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        try {
            String saved = SecureStore.load(this);
            if (saved != null && !saved.isEmpty()) key.setText(saved);
        } catch (Exception ignored) {
        }
        box.addView(key);

        EditText model = new EditText(this);
        model.setHint("Modelo");
        model.setSingleLine(true);
        model.setText(prefs.getString("model", DEFAULT_MODEL));
        box.addView(model);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Configurações da IA")
                .setView(box)
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Apagar chave", null)
                .setPositiveButton("Salvar", null)
                .create();

        dialog.setOnShowListener(x -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String k = key.getText().toString().trim();
                String m = model.getText().toString().trim();
                if (m.isEmpty()) m = DEFAULT_MODEL;
                try {
                    if (!k.isEmpty()) SecureStore.save(this, k);
                    prefs.edit().putString("model", m).apply();
                    showToast("Configurações salvas.");
                    dialog.dismiss();
                } catch (Exception e) {
                    showToast("Erro ao proteger a chave: " + e.getMessage());
                }
            });
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                try {
                    SecureStore.delete(this);
                    key.setText("");
                    showToast("Chave removida deste aparelho.");
                } catch (Exception e) {
                    showToast("Não foi possível remover a chave.");
                }
            });
        });
        dialog.show();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        sendButton.setEnabled(!loading);
        input.setEnabled(!loading);
        if (!loading) input.requestFocus();
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private static class ChatMessage {
        final boolean user;
        final String text;
        ChatMessage(boolean user, String text) {
            this.user = user;
            this.text = text;
        }
    }

    private static class SecureStore {
        private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
        private static final String ALIAS = "master_ia_api_key";
        private static final String SECURE_PREFS = "master_ia_secure";
        private static final String VALUE = "encrypted_api_key";
        private static final String IV = "api_key_iv";

        static void save(Activity context, String value) throws Exception {
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] iv = cipher.getIV();
            context.getSharedPreferences(SECURE_PREFS, MODE_PRIVATE).edit()
                    .putString(VALUE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                    .putString(IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                    .apply();
        }

        static String load(Activity context) throws Exception {
            SharedPreferences p = context.getSharedPreferences(SECURE_PREFS, MODE_PRIVATE);
            String enc = p.getString(VALUE, null);
            String ivString = p.getString(IV, null);
            if (enc == null || ivString == null) return null;

            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            SecretKey key = (SecretKey) ks.getKey(ALIAS, null);
            if (key == null) return null;

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = Base64.decode(ivString, Base64.NO_WRAP);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] clear = cipher.doFinal(Base64.decode(enc, Base64.NO_WRAP));
            return new String(clear, StandardCharsets.UTF_8);
        }

        static void delete(Activity context) throws Exception {
            context.getSharedPreferences(SECURE_PREFS, MODE_PRIVATE).edit().clear().apply();
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            if (ks.containsAlias(ALIAS)) ks.deleteEntry(ALIAS);
        }

        private static SecretKey getOrCreateKey() throws Exception {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            if (ks.containsAlias(ALIAS)) return (SecretKey) ks.getKey(ALIAS, null);

            KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();
            kg.init(spec);
            return kg.generateKey();
        }
    }
}
