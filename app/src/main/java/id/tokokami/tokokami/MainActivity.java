package id.tokokami.tokokami;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private WebView print_webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //<< this
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.activity_main_webview);
        mWebView.setWebViewClient(new MyAppWebViewClient());
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
        webSettings.setAllowFileAccess( true );
        webSettings.setAppCacheEnabled( true );
        webSettings.setCacheMode( WebSettings.LOAD_DEFAULT ); // load online by default

        if ( !isNetworkAvailable() ) { // loading offline
            webSettings.setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
        }
        if (savedInstanceState == null)
        {
            mWebView.loadUrl("https://mobile.tokokami.id");
        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog show = (new AlertDialog.Builder(this))
                .setTitle("Tokokami - Konfirmasi")
                .setMessage("Apakah anda mau keluar dari Aplikasi?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                        return;
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }

    private boolean isNetworkAvailable () {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class JavaScriptInterface {
        Context mContext;

        // Instantiate the interface and set the context
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void closeMyActivity() {
            finish();
        }

        @JavascriptInterface
        public void scanBarcode() {
            IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
            integrator.initiateScan();
        }

        @JavascriptInterface
        public void print() {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    createWebPagePrint(mWebView);

                }
            });
        }
    }   //JavascriptInterface


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            //here is where you get your result
             String barcode = scanResult.getContents();
             mWebView.evaluateJavascript("hasilScan('"+ barcode +"');",null);
        }
    }

    public void createWebPagePrint(WebView webView) {

        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
        String jobName = getString(R.string.app_name) + " Document";
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        PrintJob printJob = printManager.print(jobName, printAdapter, builder.build());

        if (printJob.isCompleted()) {
            Toast.makeText(getApplicationContext(), R.string.print_complete, Toast.LENGTH_LONG).show();
        } else if (printJob.isFailed()) {
            Toast.makeText(getApplicationContext(), R.string.print_failed, Toast.LENGTH_LONG).show();
        }
    }
}

