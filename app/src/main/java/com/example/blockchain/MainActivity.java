package com.example.blockchain;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;


public class MainActivity extends AppCompatActivity {

    private Web3j web3;
    private EditText edtPassword;
    private Button btnCreateWallet;
    private String walletPath;
    private File walletDir;
    private SharedPreferences sharedPreferences;
    private String fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("block_chain", Context.MODE_PRIVATE);

        String ethAddr = sharedPreferences.getString("eth_addr","");
        if(!ethAddr.isEmpty()){
            Intent intent = new Intent(MainActivity.this, EthHomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setupBouncyCastle();
        walletPath = getFilesDir().getAbsolutePath();
        //connectToEthNode();
        new ConnectEthNode().execute();

        edtPassword = findViewById(R.id.edtPassword);
        btnCreateWallet = findViewById(R.id.btnCreateWallet);

        btnCreateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtPassword.getText().toString().trim().isEmpty()) {
                    showToast("Password is empty");
                } else if (edtPassword.getText().toString().trim().length() < 6) {
                    showToast("Password must be minimum 6 digit");
                } else {
                    new CreateWallet().execute(edtPassword.getText().toString());
                }

            }
        });

    }

    private void connectToEthNode() {
        web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/YOUR_TOKEN"));
        try {
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if (!clientVersion.hasError()) {
                showToast("Connected");
            } else {
                showToast("Error to connecting");
            }
        } catch (Exception e) {
            showToast("Error to connecting");
        }


    }

    private class ConnectEthNode extends AsyncTask<Void, Void, Web3ClientVersion> {

        @Override
        protected Web3ClientVersion doInBackground(Void... voids) {
            web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/213fb6bc6c7e44778a28ecb2e486d6a2"));
            try {
                return web3.web3ClientVersion().send();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Web3ClientVersion web3ClientVersionCompletableFuture) {
            super.onPostExecute(web3ClientVersionCompletableFuture);
            if (web3ClientVersionCompletableFuture != null) {
                showToast(web3ClientVersionCompletableFuture.getWeb3ClientVersion() + " Connected");
            } else {
                showToast("Error to connecting");
            }

        }
    }


    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }


    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }


    private class CreateWallet extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Loading..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                fileName = WalletUtils.generateLightNewWalletFile(strings[0], new File(walletPath));
                walletDir = new File(walletPath + "/" + fileName);
                Credentials credentials =
                        WalletUtils.loadCredentials(
                                strings[0],
                                walletDir);
                sharedPreferences.edit().putString("eth_addr", credentials.getAddress()).apply();
                sharedPreferences.edit().putString("public_key", credentials.getEcKeyPair().getPublicKey().toString()).apply();
                sharedPreferences.edit().putString("pwd",strings[0]).apply();
                sharedPreferences.edit().putString("path",walletPath).apply();
                return credentials.getAddress();

            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Error in creating wallet");
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (s != null) {
                showToast("Wallet created");
                Intent intent = new Intent(MainActivity.this, EthHomeActivity.class);
                intent.putExtra("path",walletDir.getAbsolutePath());
                startActivity(intent);
                finish();
            } else {
                showToast("Error in creating wallet");
            }
        }
    }


}

