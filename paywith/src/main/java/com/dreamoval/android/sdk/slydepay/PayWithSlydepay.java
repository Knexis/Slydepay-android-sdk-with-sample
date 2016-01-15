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
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import go.slydepay_lib.Slydepay_lib;
import go.slydepay_lib.Slydepay_lib.APIResult;

public class PayWithSlydepay extends Activity{

    public static final String MY_PREFS_NAME = "MyPrefsFile";


    static String merchantEmail;
    static String merchantKey;
    static String orderId ;
    static float subTotal ;
    static float shipping ;
    static float tax ;
    static float total ;
    static String comment ;
    static String itemCode ;
    static String description ;

    private String token;

    private TextView transactionStatus;
    private ImageView imgTransactionStatus;

    public  static  ProgressDialog progressDialog;
    private static  int PAY_WITH_SLYDEPAY ;
    private static boolean isLIVE;
    private String slydepayCall = "pay.with.slydepay";

    public static void Pay(Activity context,
                           boolean isLive,
                           String imerchantEmail,
                           String imerchantKey,
                           String iorderId ,
                           float isubTotal ,
                           float ishipping ,
                           float itax ,
                           float itotal ,
                           String icomment ,
                           String iitemCode ,
                           String idescription,
                           int setRequestCode)
    {
        isLIVE            = isLive;
        merchantEmail    = imerchantEmail;
        merchantKey      = imerchantKey;
        orderId          = iorderId;
        subTotal         = isubTotal;
        shipping         = ishipping;
        tax              = itax;
        total            = itotal;
        comment          = icomment;
        itemCode         = iitemCode;
        description      = idescription;
        PAY_WITH_SLYDEPAY   = setRequestCode;
        context.startActivityForResult(new Intent(context, PayWithSlydepay.class), setRequestCode);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_pay_with_slydepay);


        transactionStatus =(TextView)findViewById(R.id.tv_status_text);
        imgTransactionStatus = (ImageView)findViewById(R.id.img_transaction_status);


//        transactionStatus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (resultCodeHere != RESULT_OK) {
//                    startTransaction();
//                }
//            }
//        });

        startTransaction();

    }//bundle



    private void startTransaction(){
        if(!UiUtils.isSlydepayPresent(this))
        {
            downloadSlydepay();
        }
        else
        { continueTransaction();  }
    }


    private void continueTransaction(){
          token = restoreItemPurchase(orderId);
        if((token)!=null){
            createOrder(token,true,null);
        }
        else{
            CreateOrder createOrder = new CreateOrder();
            createOrder.execute();
        }

    }

    private class CreateOrder extends AsyncTask<String, Void, String> {

        APIResult apiResult;
        @Override
        protected String doInBackground(String... urls) {
            apiResult = Slydepay_lib.CreateOrder(merchantEmail, merchantKey, orderId, subTotal, shipping, tax, total, comment, itemCode, description, isLIVE);
            saveItemPurchase(orderId,apiResult.getToken());
            return "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            createOrder(apiResult.getToken(),apiResult.getSuccess(),apiResult.getMessage());
        }
    }

    private class VerifyConfirmOrder extends AsyncTask<String, Void, String> {

        APIResult confirmApiResult;

        public VerifyConfirmOrder(){
        }

        @Override
        protected String doInBackground(String... urls) {

            APIResult verify = Slydepay_lib.VerifyPayment(merchantEmail, merchantKey, orderId,isLIVE);
            String confirm = "";
            try{
            confirmApiResult = Slydepay_lib.ConfirmOrder(merchantEmail,merchantKey,token,verify.getTransactionId(),isLIVE);}
            catch (Exception e)
            {e.printStackTrace();}
            return confirm;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if(confirmApiResult.getSuccess()){
            setResultCodeHere(RESULT_OK);
            }
            else{
            setResultCodeHere(RESULT_CANCELED);
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
            startActivityForResult(intent,PAY_WITH_SLYDEPAY);
        } catch (ActivityNotFoundException e)
        {
            updateApplication();
        }
        }
        else{
            tell(""+message);
            setResultCodeHere(RESULT_CANCELED);
            return;
        }

    }


    private void tell(String what)
    {
        transactionStatus.setText(what);
        Toast.makeText(this, what, Toast.LENGTH_LONG);
    }

    private void saveItemPurchase(String orderId,String token){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(orderId, token);
        editor.commit();
    }

    private String restoreItemPurchase(String orderId) {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(orderId, null);
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
            switch (resultCode){
                case RESULT_OK:
                    transactionStatus.setText("Successful");
                    imgTransactionStatus.setImageResource(R.drawable.ic_success);

                    VerifyConfirmOrder verifyConfirmOrder = new VerifyConfirmOrder();
                    verifyConfirmOrder.execute();

                    break;
                case RESULT_CANCELED:
                    transactionStatus.setText("Try again");
                    imgTransactionStatus.setImageResource(R.drawable.ic_failure);
                    setResultCodeHere(RESULT_CANCELED);
                    break;
                case RESULT_FIRST_USER:
                    transactionStatus.setText("Try again");
                    imgTransactionStatus.setImageResource(R.drawable.ic_failure);
                    setResultCodeHere(RESULT_FIRST_USER);
                    break;
            }
        }

    }


    private void setResultCodeHere(int resultCodeHere)
    {
        setResult(resultCodeHere);
        finish();
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
        new AlertDialog.Builder(this).setTitle("Old Slydepay version")
                .setMessage("Please update Slydepay to continue this transaction.")
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



    public  void showProgressDialog() {
        //LogUtils.LOGD(TAG, "showProgressDialog");
        final Activity activity = this;
        final String message = "Please wait...";

        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!activity.isFinishing()) {
                        progressDialog = new ProgressDialog(activity);
                        if (message != null) {
                            progressDialog.setMessage(message);
                        }
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.isIndeterminate();
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
