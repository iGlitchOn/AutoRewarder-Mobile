package off.iglitch.autorewarder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

/** Standard ZXing camera preview. The library owns orientation and aspect ratio. */
public class QrScanActivity extends Activity {
    public static final String EXTRA_TEXT = "qr_text";
    public static final String EXTRA_ERROR = "qr_error";

    private DecoratedBarcodeView scanner;
    private boolean finished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        scanner = new DecoratedBarcodeView(this);
        CameraSettings settings = new CameraSettings();
        settings.setRequestedCameraId(0);
        scanner.getBarcodeView().setCameraSettings(settings);
        scanner.setStatusText("Apunta al QR de AutoRewarder en el PC");

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.addView(scanner, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        TextView cancel = new TextView(this);
        cancel.setText("Cancelar");
        cancel.setTextColor(Color.WHITE);
        cancel.setTextSize(16);
        cancel.setGravity(Gravity.CENTER);
        cancel.setPadding(24, 16, 24, 16);
        cancel.setOnClickListener(v -> finishCanceled("Cámara cerrada. Escribe el código o vuelve a escanear."));
        FrameLayout.LayoutParams cancelLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END);
        cancelLp.topMargin = 18;
        cancelLp.rightMargin = 12;
        root.addView(cancel, cancelLp);
        setContentView(root);

        BarcodeCallback callback = result -> {
            if (result == null || result.getText() == null || result.getText().isEmpty() || finished) return;
            finished = true;
            Intent out = new Intent();
            out.putExtra(EXTRA_TEXT, result.getText());
            setResult(RESULT_OK, out);
            finish();
        };
        scanner.decodeSingle(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanner != null) scanner.resume();
    }

    @Override
    protected void onPause() {
        if (scanner != null) scanner.pause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        finishCanceled("Cámara cerrada. Escribe el código o vuelve a escanear.");
    }

    private void finishCanceled(String message) {
        if (finished) return;
        finished = true;
        Intent out = new Intent();
        out.putExtra(EXTRA_ERROR, message);
        setResult(RESULT_CANCELED, out);
        finish();
    }
}
