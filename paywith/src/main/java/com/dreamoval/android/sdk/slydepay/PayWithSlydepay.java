package com.dreamoval.android.sdk.slydepay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import go.slydepay_lib.Slydepay_lib;
import go.slydepay_lib.Slydepay_lib.APIResult;

public class PayWithSlydepay extends Activity{

    static String merchantEmail;
    static String merchantKey;
    static String orderId ;
    static double subTotal ;
    static double shipping ;
    static double tax ;
    static double total ;
    static String comment ;
    static String itemCode ;
    static String description ;

    private String token;

    private ImageView imgTransactionStatus;

    private static  int PAY_WITH_SLYDEPAY ;
    private static boolean isLIVE;
    private String slydepayCall = "pay.with.slydepay";
    private ProgressBar pleaseWait;


    public static void Pay(Activity context ,
                           boolean   isLive ,
                           String    imerchantEmail ,
                           String    imerchantKey ,
                           double    itemPrice ,
                           double    idelivery ,
                           double    itax ,
                           String    iitemCode ,
                           String    icomment ,
                           String    idescription ,
                           int       setRequestCode)
    {
        isLIVE                     = isLive;
        merchantEmail              = imerchantEmail;
        merchantKey                = imerchantKey;
        orderId                    = UUID.randomUUID().toString();
        subTotal                   = itemPrice;
        shipping                   = idelivery;
        tax                        = itax;
        total                      = (itemPrice + idelivery + itax);
        comment                    = icomment;
        itemCode                   = iitemCode;
        description                = idescription;
        PAY_WITH_SLYDEPAY          = setRequestCode;
        context.startActivityForResult(new Intent(context, PayWithSlydepay.class), setRequestCode);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_pay_with_slydepay);

        pleaseWait        = (ProgressBar)findViewById(R.id.pb_please_wait);
        imgTransactionStatus = (ImageView)findViewById(R.id.img_transaction_status);

        startTransaction();

    }//bundle



    private void startTransaction(){
        if(!PayWithUiUtils.isSlydepayPresent(this))
        {
            downloadSlydepay();
        }
        else
        { continueTransaction();  }
    }


    private void continueTransaction(){

       CreateOrder createOrder = new CreateOrder();
       createOrder.execute();

    }

    private class CreateOrder extends AsyncTask<String, Void, String> {

        APIResult apiResult;
        @Override
        protected String doInBackground(String... urls) {
            apiResult = Slydepay_lib.CreateOrder(merchantEmail, merchantKey, orderId, subTotal, shipping, tax, total, comment, itemCode, description, isLIVE);
            return "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected void onPostExecute(String result) {
            showProgress(false);
            if(apiResult.getSuccess()){
            createOrder(apiResult.getToken(),apiResult.getSuccess(),apiResult.getMessage());}
            else{
            setResultCodeHere(RESULT_CANCELED,getIntentMessage(PayWithUiUtils.getErrorMessage(apiResult.getMessage())));
            }
        }
    }

    private class VerifyConfirmOrder extends AsyncTask<String, Void, String> {

        APIResult verifyApiResult;
        APIResult confirmApiResult;

        public VerifyConfirmOrder(){
        }

        @Override
        protected String doInBackground(String... urls) {

            verifyApiResult = Slydepay_lib.VerifyPayment(merchantEmail, merchantKey, orderId,isLIVE);

            if(verifyApiResult.getSuccess()){
            confirmApiResult = Slydepay_lib.ConfirmOrder(merchantEmail,merchantKey,token,verifyApiResult.getTransactionId(),isLIVE);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected void onPostExecute(String result) {
            //progressDialog.dismiss();
            showProgress(false);
            if(!verifyApiResult.getSuccess()){
             setResultCodeHere(RESULT_CANCELED,getIntentMessage(verifyApiResult.getMessage()));
             return;
            }
            if(confirmApiResult.getSuccess()){
            setResultCodeHere(RESULT_OK,getIntentMessage("Transaction successfully completed!"));
            }
            else{
            setResultCodeHere(RESULT_CANCELED,getIntentMessage(confirmApiResult.getMessage()));
            }
        }
    }



    private void createOrder(String token,boolean success,String message){


        if(success)
        {
        this.token = token;
        try{
            Intent intent = new Intent(slydepayCall);
            intent.putExtra("isLive",isLIVE);
            intent.putExtra("token",token);
            startActivityForResult(intent, PAY_WITH_SLYDEPAY);
        } catch (Exception e)
        {
            updateApplication();
        }
        }
        else{
            tell(""+message);
            setResultCodeHere(RESULT_CANCELED, getIntentMessage(PayWithUiUtils.TRANSACTION_PENDING));
            return;
        }

    }


    private void tell(String what)
    {
        Toast.makeText(this, what, Toast.LENGTH_LONG);
    }

    private void showProgress(boolean visible){
        if(visible)
        pleaseWait.setVisibility(View.VISIBLE);
        else
        pleaseWait.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if(requestCode==PAY_WITH_SLYDEPAY)
        {
            if(data!=null)
            switch (resultCode){
                case RESULT_OK:
                    imgTransactionStatus.setImageResource(R.drawable.ic_success);
                    VerifyConfirmOrder verifyConfirmOrder = new VerifyConfirmOrder();
                    verifyConfirmOrder.execute();

                    break;
                case RESULT_CANCELED:
                    imgTransactionStatus.setImageResource(R.drawable.ic_failure);
                    setResultCodeHere(RESULT_CANCELED, data);
                    break;
                case RESULT_FIRST_USER:
                    imgTransactionStatus.setImageResource(R.drawable.ic_failure);
                    setResultCodeHere(RESULT_FIRST_USER, data);
                    break;
            }
        }

    }


    private void setResultCodeHere(int resultCodeHere, Intent intent)
    {
        setResult(resultCodeHere, intent);
        this.finish();
    }

    private Intent getIntentMessage(String message){
        Intent intent = new Intent();
        intent.putExtra(PayWithUiUtils.MESSAGE, message);
        return intent;
    }

    private void downloadSlydepay()
    {
        new AlertDialog.Builder(this).setTitle("Alert")
                .setMessage("Continue transaction with Slydepay")
                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("market://details?id=com.dreamoval.slydepay.android.cruise");
                        Intent myIntent = new Intent(Intent.ACTION_VIEW,uri);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    private void updateApplication()
    {
        new AlertDialog.Builder(this).setTitle("Need an update")
                .setMessage("Please update Slydepay to continue this transaction with your Slydepay Account")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("market://details?id=com.dreamoval.slydepay.android.cruise");
                        Intent myIntent = new Intent(Intent.ACTION_VIEW,uri);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }






}
