package com.dreamoval.android.sdk.slydepay.example;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.dreamoval.android.sdk.slydepay.PayWithSlydepay;
import com.dreamoval.android.sdk.slydepay.PayWithUiUtils;

import go.slydepay_lib.Slydepay_lib;


public class MainActivity extends AppCompatActivity {

    final int PAY_WITH_SLYDEPAY = 1;

    public static final String AMOUNT_FONT      = "fonts/Roboto-Thin.ttf";
    View payWithSlyepay;
    ImageView transactionStatus;
    private Double pizzaPrice = 41.30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,//tell the os we need a full screen
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_buy);

        initView();
    }//bundle

    private void initView()
    {
        //hide the action bar too
        getSupportActionBar().hide();

        payWithSlyepay = findViewById(R.id.layout_pay_with_slydepay);
        transactionStatus = (ImageView)findViewById(R.id.img_transaction_status);

        ((TextView)findViewById(R.id.amount_text)).setTypeface(UiUtils.getTypeface(AMOUNT_FONT, this));
        ((TextView)findViewById(R.id.amount_text)).setText("Cost: "+pizzaPrice +" GHS");


        payWithSlyepay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PayWithSlydepay.Pay(MainActivity.this,
                        false,//switch to true when going live with the your app
                        true,//switch to true so that we handle the success message after a payment
                        "iwallet@dreamoval.com",//"xxxxxxx@xxxxxx.com",                    //Replace with Verified Merchant Email
                        "bdVI+jtRl80PG4x6NMvYOwfZTZtwfN",//"xxxxMerchantxxxxKeyxxxxx",              //Replace with Merchant Key
                         pizzaPrice,                             //item cost
                         0,                                      //delivery cost
                         0,                                      //tax cost
                        "Pizza",                                 //name of the item being purchased
                        "You would love this",                   //leave a comment
                        "Medium sized Peri-peri chicken pizza",  //describe the item
                         PAY_WITH_SLYDEPAY);                     //requestcode

            }
        });
    }


    //Very Important section of the code. It listens for the results after a transaction call to the slydepay application
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode==PAY_WITH_SLYDEPAY)
            {
                switch (resultCode){
                    case RESULT_OK:         //Payment was successful
                        transactionStatus.setImageResource(R.drawable.ic_success);
                        break;
                    case RESULT_CANCELED:   //Payment failed
                        transactionStatus.setImageResource(R.drawable.ic_failure);
                        break;
                    case RESULT_FIRST_USER: //Payment was cancelled by user
                        transactionStatus.setImageResource(R.drawable.ic_failure);
                        break;
                }
                if(data!=null){
                String message = data.getStringExtra(PayWithUiUtils.MESSAGE);  //get details of the transaction here
                String orderid = data.getStringExtra(PayWithUiUtils.ORDER_ID);  //get order id of the item being purchased
                Log.i("Order id of item",orderid);
                showAlert(message);
                }
        }
    }


    private void showAlert(String message){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {}});
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }



}//main
