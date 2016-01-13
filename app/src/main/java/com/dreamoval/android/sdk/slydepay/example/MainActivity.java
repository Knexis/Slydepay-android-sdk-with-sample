package com.dreamoval.android.sdk.slydepay.example;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.dreamoval.android.sdk.slydepay.PayWithSlydepay;


public class MainActivity extends AppCompatActivity {

    final int PAY_WITH_SLYDEPAY = 1;

    public static final String AMOUNT_FONT      = "fonts/Roboto-Thin.ttf";
    View payWithSlyepay;
    ImageView transactionStatus;

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

        ((TextView)findViewById(R.id.amount_text)).setTypeface(UiUtils.getTypeface(AMOUNT_FONT,this));
        payWithSlyepay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PayWithSlydepay.Pay(MainActivity.this,
                        true,
                        "xxxxx@email.com",
                        "xxxxxxMerchant-keyxxxx",
                        "testE20frgd",
                        41, 0, 0, 41,
                        "Item purchase for le secreto santa",
                        "airty501",
                        "Item purchase for le santa secreto",PAY_WITH_SLYDEPAY);

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
                    case RESULT_OK:
                        transactionStatus.setImageResource(R.drawable.ic_success);
                        break;
                    case RESULT_CANCELED:
                        transactionStatus.setImageResource(R.drawable.ic_failure);
                        break;
                    case RESULT_FIRST_USER:
                        transactionStatus.setImageResource(R.drawable.ic_failure);
                        break;
                }
        }

    }



}//main
