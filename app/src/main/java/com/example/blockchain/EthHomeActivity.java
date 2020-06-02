package com.example.blockchain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class EthHomeActivity extends AppCompatActivity {

    private TextView tvEthAddr;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_home);

        sharedPreferences = getSharedPreferences("block_chain", Context.MODE_PRIVATE);
        String addr = sharedPreferences.getString("eth_addr", "");
        tvEthAddr = findViewById(R.id.tvEtherAddr);
        tvEthAddr.setText(addr);

        String password = sharedPreferences.getString("pwd","");
        String walletPath = sharedPreferences.getString("path","");

        Credentials credentials = loadCredential(password,walletPath);

    }

    private Credentials loadCredential(String password,String walletPath){
        try {
            return WalletUtils.loadCredentials(
                   password,
                    new File(walletPath));

        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }

        return  null;
    }


    private void makeFundTransfer(Credentials credentials,String toAddress,Double amountInEther){
        Web3j web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/YOUR_TOKEN"));
        try {

            TransactionReceipt transactionReceipt = Transfer.sendFunds(
                    web3, credentials, toAddress,
                    BigDecimal.valueOf(amountInEther), Convert.Unit.ETHER)
                    .send();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
