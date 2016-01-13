package com.dreamoval.android.sdk.slydepay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import go.slydepay_lib.Slydepay_lib;

public class PayWithSlydepay extends Activity{

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
    static  private int requestCodeHere = 555;

    private TextView transactionStatus;
    private ImageView imgTransactionStatus;

    public static ProgressDialog progressDialog;
    public static boolean DEBUG;

    public static void Pay(Activity context,
                           boolean debug,
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
        DEBUG            = debug;
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
        requestCodeHere   = setRequestCode;
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
//        else if(!UiUtils.isUpdatedVersion(this))
//        {
//            updateApplication();
//        }
        else
        { CreateOrder createOrder = new CreateOrder();
          createOrder.execute();  }
    }


    private class CreateOrder extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

//            return token = Slydepay_lib.CreateOrder("iwallet@dreamoval.com","bdVI+jtRl80PG4x6NMvYOwfZTZtwfN","test4",2,0,0,2,"Item purchase for le secreto santa","airty50","Item purchase for le santa secreto");
          return token = Slydepay_lib.CreateOrder(merchantEmail,merchantKey,orderId,subTotal,shipping,tax,total,comment,itemCode,description);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            createOrder(result);
        }
    }

    private class VerifyConfirmOrder extends AsyncTask<String, Void, String> {


        public VerifyConfirmOrder(){
        }

        @Override
        protected String doInBackground(String... urls) {

            String verify = Slydepay_lib.VerifyPayment(merchantEmail, merchantKey, orderId);
            String confirm = "";
            try{
                confirm = Slydepay_lib.ConfirmOrder(merchantEmail,merchantKey,token,verify);}
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
            setResultCodeHere(RESULT_OK);
        }
    }



    private void createOrder(String id){
        token = id;

        if(token.equalsIgnoreCase("Order Id already exists. please use a different one")){
            tell("Sorry Order Id already exists. please use a different one");
            setResultCodeHere(RESULT_CANCELED);
            //return;
        }
        else if(token.startsWith("error"))
        {
            tell("Sorry an error occurred");
            setResultCodeHere(RESULT_CANCELED);
            //return;
        }

        try{
            Intent intent = new Intent("pay.with.slydepay");
            intent.putExtra("debug",DEBUG);
            intent.putExtra("token",token);
            startActivityForResult(intent,1);
        } catch (ActivityNotFoundException e)
        {
            updateApplication();
        }
    }


    private void tell(String what)
    {
        transactionStatus.setText(what);
        Toast.makeText(this, what, Toast.LENGTH_LONG);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==requestCodeHere)
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
//                    progressDialog.setCanceledOnTouchOutside(false);
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
