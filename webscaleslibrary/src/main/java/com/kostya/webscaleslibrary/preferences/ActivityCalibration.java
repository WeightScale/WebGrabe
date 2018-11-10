package com.kostya.webscaleslibrary.preferences;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kostya.webscaleslibrary.R;
import com.kostya.webscaleslibrary.module.Module;


public class ActivityCalibration extends AppCompatActivity {
    private WebView mWebView;
    public String m;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        mWebView = findViewById(R.id.webView);
        // включаем поддержку JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.resumeTimers();
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl("http://"+ Module.HOST +"/calibr.html");
        // указываем страницу загрузки
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mWebContainer.removeAllViews();

        mWebView.clearHistory();

        // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
        // Probably not a great idea to pass true if you have other WebViews still alive.
        mWebView.clearCache(true);

        // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
        mWebView.loadUrl("about:blank");

        mWebView.onPause();
        mWebView.removeAllViews();
        mWebView.destroyDrawingCache();

        // NOTE: This pauses JavaScript execution for ALL WebViews,
        // do not use if you have other WebViews still alive.
        // If you create another WebView after calling this,
        // make sure to call mWebView.resumeTimers().
        mWebView.pauseTimers();

        // NOTE: This can occasionally cause a segfault below API 17 (4.2)
        mWebView.destroy();

        // Null out the reference so that you don't end up re-using it.
        //mWebView = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
        //WebScalesClient.Cmd.WT.getParam();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
    }
    private class MyWebViewClient extends WebViewClient {
        private WebView myView;
        private HttpAuthHandler httpAuthHandler;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            finish();
            return true;
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            this.httpAuthHandler = handler;
            this.myView = view;
            final EditText usernameInput = new EditText(ActivityCalibration.this);
            usernameInput.setHint("Username");

            final EditText passwordInput = new EditText(ActivityCalibration.this);
            passwordInput.setHint("Password");
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            LinearLayout linearLayout = new LinearLayout(ActivityCalibration.this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(usernameInput);
            linearLayout.addView(passwordInput);

            AlertDialog.Builder authDialog = new AlertDialog
                .Builder(ActivityCalibration.this)
                .setTitle("Authentication")
                .setView(linearLayout)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        httpAuthHandler.proceed(usernameInput.getText().toString(), passwordInput.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        myView.stopLoading();
                        finish();
                        //onLoadListener.onAuthCancel((MyWebView)mView, mTitleTextView);
                    }
                });

        if(view!=null)
            authDialog.show();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

    }

    /*private class MyWebViewClient extends WebViewClient {
        private WebView myView;
        private HttpAuthHandler httpAuthHandler;
        private String host;
        private String realm;

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            AuthRequestDialogFragment authRequestDialogFragment = new AuthRequestDialogFragment();
            FragmentManager fragmentManager = ((AppCompatActivity) getApplicationContext()).getSupportFragmentManager();
            authRequestDialogFragment.setTargetFragment(WebViewFragment.this, 0);
            authRequestDialogFragment.show(fragmentManager, "dialog");
            this.httpAuthHandler = handler;
            this.myView = view;
            this.host = host;
            this.realm = realm;
        }

        public void login(String username, String password) {
            httpAuthHandler.proceed(username, password);
            myView = null;
            httpAuthHandler = null;
            host = null;
            realm = null;
        }

        public void cancel() {
            super.onReceivedHttpAuthRequest(myView, httpAuthHandler, host, realm);
            myView = null;
            httpAuthHandler = null;
            host = null;
            realm = null;
        }
    }*/

    /*public static class WebViewFragment extends Fragment implements AuthRequestDialogFragment.Callback {

        @Override
        public void login(String username, String password) {
            Log.d(this.getClass().getName(), "Login");
            myWebViewClient.login(username, password);
        }

        @Override
        public void cancel() {
            Log.d(this.getClass().getName(), "Cancel");
            myWebViewClient.cancel();
        }
    }*/

    /*public static class AuthRequestDialogFragment extends DialogFragment {
        @InjectView(R.id.dauth_userinput)
        public EditText userinput;

        @InjectView(R.id.dauth_passinput)
        public EditText passinput;

        @OnClick(R.id.dauth_login)
        public void login(View view) {
            ((Callback) getTargetFragment()).login(userinput.getText().toString(), passinput.getText().toString());
            this.dismiss();
        }

        @OnClick(R.id.dauth_cancel)
        public void cancel(View view) {
            ((Callback) getTargetFragment()).cancel();
            this.dismiss();
        }

        public interface Callback
        {
            public void login(String username, String password);
            public void cancel();
        }

        @Override
        public void onStart() {
            super.onStart();
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            getDialog().getWindow().setLayout(width*2/3, height/5*2);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.dialog_authrequest, container);
            ButterKnife.inject(this, view);
            getDialog().setTitle("Authorization required");
            return view;
        }
    }*/
}
