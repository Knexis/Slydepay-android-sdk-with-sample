
##Getting Paid
-------------

This is a sample project called "Pizza Yourself". It's a demo of how to connect to the Slydepay Android App of your users for payments with Slydepay.

![alt tag](https://docs.google.com/uc?authuser=0&id=0B6IcQWXC0MzvcjRjZXFEdGhFOXM&export=download)


This is a quick guide to get you ready to receive payments in your app via the Slydepay android SDK
```
PayWithSlydepay.Pay( MainActivity.this,                      //Calling Activity\n   
                     isLive,                                 //switch to true when going live with the your app\n
                     Merchant Email address,                 //Replace with Verified Merchant Email\n
                     Merchant Key,                           //Replace with Merchant Key\n
                     Item Cost,                              //item cost\n
                     Delivery Cost,                          //(Optional) delivery cost\n
                     Tax,                                    //(Optional) tax cost\n
                     Item name,                              //name of the item being purchased\n
                     Optional comment,                       //(Optional) leave a comment\n
                     Item description,                       //describe the item\n
                     Request code);                          //requestcode\n  
```

###Sample entry
-------------
```
PayWithSlydepay.Pay( MainActivity.this,                      //Calling Activity
                     false,                                  //switch to true when going live with the your app
                    "xxxxxxMerchant@email.com",              //Replace with Verified Merchant Email
                    "xxxxxMerchant-keyxxxx",                 //Replace with Merchant Key
                     41.00,                                  //item cost
                     5.0,                                    //(Optional) delivery cost
                     0,                                      //(Optional) tax cost
                    "Pizza",                                 //name of the item being purchased
                    "You would love this",                   //(Optional) leave a comment
                    "Medium sized Peri-peri chicken pizza",  //describe the item
                     PAY_WITH_SLYDEPAY);                     //requestcode
```

#Skip this if you have an active Merchant Account
-------------------------------------------------

Create a Merchant Account on Slydepay

Follow the link to get started on that:
([Slydepay Merchant Account](https://app.slydepay.com.gh/auth/signup#business_reg))

After the account has been verified and recognised as a Merchant Account.
Guess what?
You are good to go. Wait a minute


#Get the SLydepay SDK into your Project
---------------------------------------

One simple way is to download this Sample:
([Slydepay Android SDK with Sample](https://github.com/Knexis/Slydepay-android-sdk-with-sample))

Unzip the Slydepay-android-sdk-with-sample then Go to Import Module on your Android Studio (Tested with version 1.0)
Navigate into the Slydepay-android-sdk-with-sample and import the "pathwith" module the slydepay_lib module would automatically be selected
because it is a dependency of the "paywith".
From your app add "paywith" as a dependency.
From Android Studio :
->Right click your project folder 
        ->Open Module Settings 
                ->Select the Dependency Tab 
                          ->Tap on the green plus sign on the right and select "Module dependency" 
                                    -You would see the "paywith" module. Select it. 

Tap on OK and you are good to go. Not really but you are close


#Next
-----

You are now ready to call your user's Slydepay app for payment.

We know it is advisable to do some tests before you ship the product to your itching users.
We have set a test user account to help in testing your payment simulations.

To access it, all you need to do is set the isLive param to false meaning you are in test mode

Put 
```
PayWithSlydepay.Pay(...,false, ......);
```
under ur item pay button

and then wait for the response at

```
 //Very Important section of the code. It listens for the results after a transaction call to the slydepay application
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode==PAY_WITH_SLYDEPAY)
            {
                switch (resultCode){
                    case RESULT_OK:
                        //Payment was successful
                        break;
                    case RESULT_CANCELED:
                        //Payment failed
                        break;
                    case RESULT_FIRST_USER:
                        //Payment was cancelled by user
                        break;
                }
                if(data!=null){
                String message = data.getStringExtra(PayWithUiUtils.MESSAGE); //get details of the transaction here
                showAlert(message);
            }
        }
    }
```


#NOTE:
------
Make sure to switch the Call to Slydepay to your Live account when releasing to your user's
```
PayWithSlydepay.Pay(...,true, ......);
```
Measures are being put in place to help you avoid such mistakes.


To start testing from today access the Slydepay SDK enabled apk in the Slydepay-apk folder at
([Slydepay SDK Enabled APK](https://github.com/Knexis/Slydepay-android-sdk-with-sample/tree/master/slydepay-apk))
