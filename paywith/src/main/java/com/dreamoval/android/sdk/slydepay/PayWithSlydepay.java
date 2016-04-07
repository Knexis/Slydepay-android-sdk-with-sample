package com.dreamoval.android.sdk.slydepay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
    private static boolean showSuccessScreen;
    private String slydepayCall = "pay.with.slydepay";
    private ProgressBar pleaseWait;


    public static void Pay(Activity context ,
                           boolean   isLive ,
                           boolean   sShowSuccessScreen,
                           String    sMerchantEmail ,
                           String    sMerchantKey ,
                           double    itemPrice ,
                           double    sDelivery ,
                           double    sTax ,
                           String    sItemCode ,
                           String    sComment ,
                           String    sDescription ,
                           int       setRequestCode)
    {
        isLIVE                     = isLive;
        showSuccessScreen          = sShowSuccessScreen;
        merchantEmail              = sMerchantEmail;
        merchantKey                = sMerchantKey;
        orderId                    = UUID.randomUUID().toString();
        subTotal                   = itemPrice;
        shipping                   = sDelivery;
        tax                        = sTax;
        total                      = (itemPrice + sDelivery + sTax);
        comment                    = sComment;
        itemCode                   = sItemCode;
        description                = sDescription;
        PAY_WITH_SLYDEPAY          = setRequestCode;
        context.startActivityForResult(new Intent(context, PayWithSlydepay.class), setRequestCode);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_pay_with_slydepay);

        pleaseWait           = (ProgressBar)findViewById(R.id.pb_please_wait);
        imgTransactionStatus = (ImageView)findViewById(R.id.img_transaction_status);
        findViewById(R.id.layout_success_screen).setVisibility(View.GONE);

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
            if(!showSuccessScreen)
            setResultCodeHere(RESULT_OK,getIntentMessage("Transaction successfully completed!"));
            else
            successScreen();
            }
            else{
            setResultCodeHere(RESULT_CANCELED,getIntentMessage(confirmApiResult.getMessage()));
            }
        }
    }



    private void successScreen(){
        findViewById(R.id.layout_success_screen).setVisibility(View.VISIBLE);

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResultCodeHere(RESULT_OK,getIntentMessage("Transaction successfully completed!"));
            }
        });

        ((TextView)findViewById(R.id.tv_message_success)).setText("An amount of GHS "+total+" has been debited from your Slydepay account");
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
        intent.putExtra(PayWithUiUtils.ORDER_ID,orderId);//attached the order id of the item being purchased
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
