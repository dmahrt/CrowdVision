package net.dividedattention.crowdvision.firebaselogin;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.R.id;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseAuthProvider;
import com.firebase.ui.auth.core.FirebaseLoginError;
import com.firebase.ui.auth.core.TokenAuthHandler;
import com.firebase.ui.auth.google.GoogleAuthProvider;

import net.dividedattention.crowdvision.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CustomFirebaseLoginDialog extends DialogFragment {
    Map<AuthProviderType, FirebaseAuthProvider> mEnabledProvidersByType = new HashMap();
    TokenAuthHandler mHandler;
    AuthProviderType mActiveProvider;
    Firebase mRef;
    Context mContext;
    View mView;

    public CustomFirebaseLoginDialog() {
    }

    public void onStop() {
        super.onStop();
        this.cleanUp();
    }

    public void onDestroy() {
        super.onDestroy();
        this.cleanUp();
    }

    public void onPause() {
        super.onPause();
        this.cleanUp();
    }

    public void cleanUp() {
        if(this.getGoogleAuthProvider() != null) {
            this.getGoogleAuthProvider().cleanUp();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Iterator i$ = this.mEnabledProvidersByType.values().iterator();

        while(i$.hasNext()) {
            FirebaseAuthProvider provider = (FirebaseAuthProvider)i$.next();
            provider.onActivityResult(requestCode, resultCode, data);
        }

    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(this.getActivity());
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        this.mView = inflater.inflate(R.layout.custom_fragment_firebase_login, (ViewGroup)null);
        AuthProviderType[] arr$ = AuthProviderType.values();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$-1; ++i$) {
            AuthProviderType providerType = arr$[i$];
            if(this.mEnabledProvidersByType.keySet().contains(providerType)) {
                this.showLoginOption((FirebaseAuthProvider)this.mEnabledProvidersByType.get(providerType), providerType.getButtonId());
            } else {
                System.out.println("TYPE: "+providerType.getName());
                this.mView.findViewById(providerType.getButtonId()).setVisibility(View.GONE);
            }
        }

        if(this.mEnabledProvidersByType.containsKey(AuthProviderType.PASSWORD) && !this.mEnabledProvidersByType.containsKey(AuthProviderType.FACEBOOK) && !this.mEnabledProvidersByType.containsKey(AuthProviderType.GOOGLE) && !this.mEnabledProvidersByType.containsKey(AuthProviderType.TWITTER)) {
            this.mView.findViewById(id.or_section).setVisibility(View.GONE);
        }

        this.mView.findViewById(id.loading_section).setVisibility(View.GONE);
        setCancelable(false);
        builder.setView(this.mView);
        this.setRetainInstance(true);
        return builder.create();
    }

    public CustomFirebaseLoginDialog setRef(Firebase ref) {
        this.mRef = ref;
        return this;
    }

    public CustomFirebaseLoginDialog setContext(Context context) {
        this.mContext = context;
        return this;
    }

    public void reset() {
        this.mView.findViewById(id.login_section).setVisibility(View.VISIBLE);
        this.mView.findViewById(id.loading_section).setVisibility(View.GONE);
    }

    public void logout() {
        Iterator i$ = this.mEnabledProvidersByType.values().iterator();

        while(i$.hasNext()) {
            FirebaseAuthProvider provider = (FirebaseAuthProvider)i$.next();
            provider.logout();
        }

        this.mRef.unauth();
    }

    public CustomFirebaseLoginDialog setHandler(final TokenAuthHandler handler) {
        this.mHandler = new TokenAuthHandler() {
            public void onSuccess(AuthData auth) {
                CustomFirebaseLoginDialog.this.dismiss();
                handler.onSuccess(auth);
            }

            public void onUserError(FirebaseLoginError err) {
                handler.onUserError(err);
            }

            public void onProviderError(FirebaseLoginError err) {
                handler.onProviderError(err);
            }
        };
        return this;
    }

    public CustomFirebaseLoginDialog setEnabledProvider(AuthProviderType provider) {
        if(!this.mEnabledProvidersByType.containsKey(provider)) {
            this.mEnabledProvidersByType.put(provider, provider.createProvider(this.mContext, this.mRef, this.mHandler));
        }

        return this;
    }

    private void showLoginOption(final FirebaseAuthProvider helper, int id) {
        this.mView.findViewById(id).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if(AuthProviderType.getTypeForProvider(helper) == AuthProviderType.PASSWORD) {
                    EditText emailText = (EditText)CustomFirebaseLoginDialog.this.mView.findViewById(R.id.email);
                    EditText passwordText = (EditText)CustomFirebaseLoginDialog.this.mView.findViewById(R.id.password);
                    helper.login(emailText.getText().toString(), passwordText.getText().toString());
                    passwordText.setText("");
                } else {
                    helper.login();
                }

                CustomFirebaseLoginDialog.this.mActiveProvider = helper.getProviderType();
                CustomFirebaseLoginDialog.this.mView.findViewById(R.id.login_section).setVisibility(View.GONE);
                CustomFirebaseLoginDialog.this.mView.findViewById(R.id.loading_section).setVisibility(View.VISIBLE);
            }
        });
    }

    public GoogleAuthProvider getGoogleAuthProvider() {
        return (GoogleAuthProvider)this.mEnabledProvidersByType.get(AuthProviderType.GOOGLE);
    }
}
