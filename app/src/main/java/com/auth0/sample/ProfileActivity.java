/*
 * ProfileActivity.java
 *
 * Copyright (c) 2015 Auth0 (http://auth0.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.auth0.sample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.core.UserProfile;
import com.auth0.lock.Lock;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class ProfileActivity extends ActionBarActivity {

    private static final String SAMPLE_API_URL = "http://localhost:3001/secured/ping";
    private static final String TAG = ProfileActivity.class.getName();

    AsyncHttpClient client;

    TextView dob, sex, age;
    String username,fname, sex_details, dob_details;
    int bps, bpd, weight, height, waist_circ, bmi, smoke;
    int totalCholesterol, ldl, hdl;

    int heartpercentage=0;

    EditText bpsshow, bpdshow, weightshow, heightshow, bmishow;
    EditText totalChloesterolshow, ldlshow, hdlshow;
    TextView percentageshow;
    private SharedPreferences pref1, pref2;
    private String prefName = "report";
    String welcome;
    TextView displayresult;
    String hypertension, diabetes, heartdisease;
    Boolean checkname=false;
    String heartrisk="";
    int ages;
    Boolean smoking = false, BPtreated=false;
    String fname_details, lname_details;
    TextView greetingTextView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UserProfile profile = getIntent().getParcelableExtra(Lock.AUTHENTICATION_ACTION_PROFILE_PARAMETER);
        greetingTextView = (TextView) findViewById(R.id.welcome_message);
        greetingTextView.setText("Welcome " + profile.getName());
        ImageView profileImageView = (ImageView) findViewById(R.id.profile_image);
        if (profile.getPictureURL() != null) {
            ImageLoader.getInstance().displayImage(profile.getPictureURL(), profileImageView);
        }



        client = new AsyncHttpClient();
        Button btnresult = (Button) findViewById(R.id.button2);
        Button fetchdata = (Button) findViewById(R.id.fetch);

       // Button callAPIButton = (Button) findViewById(R.id.call_api_button);
        username =profile.getName();
        String[] arr=username.split(" ");

        fname=arr[0];

        dob = (TextView) findViewById(R.id.textView14);
        sex = (TextView) findViewById(R.id.textView15);
        age = (TextView) findViewById(R.id.textView16);
        weightshow = (EditText) findViewById(R.id.weight_kg);
        bpdshow = (EditText) findViewById(R.id.editText3);
        bpsshow = (EditText) findViewById(R.id.editText2);
        heightshow = (EditText) findViewById(R.id.height_feet);
        bmishow = (EditText) findViewById(R.id.height_inch);
        totalChloesterolshow = (EditText) findViewById(R.id.editText);
        ldlshow = (EditText) findViewById(R.id.editText4);
        hdlshow = (EditText) findViewById(R.id.editText5);
        percentageshow = (TextView) findViewById(R.id.percentageShow);


        displayresult = (TextView) findViewById(R.id.resultDisplay);
        //new LongOperation().execute();
        fetchdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LongOperation().execute();
            }
        });
        btnresult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalcHeartRisk();
                ImageView imageToDisplay = (ImageView) findViewById(R.id.imageView2);
                imageToDisplay.setVisibility(View.VISIBLE);
                //imageToDisplay.setImageResource(R.drawable.imagelogo);
                Display();
            }
        });

    }

    private void callAPI() {
        client.get(this, SAMPLE_API_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.v(TAG, "We got the secured data successfully");
                showAlertDialog(ProfileActivity.this, "We got the secured data successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Failed to contact API", error);
                showAlertDialog(ProfileActivity.this, "Please download the API seed so that you can call it.");
            }
        });
    }

    public static AlertDialog showAlertDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.show();
    }

    private class LongOperation extends AsyncTask<String, Void, Void> {

        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(ProfileActivity.this);
        InputStream is=null;
        String result=null;
        String line=null;

        protected void onPreExecute() {

            Dialog.setMessage("Getting Medical info..");
            Dialog.show();
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();


            nameValuePairs.add(new BasicNameValuePair("fname", fname));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://192.168.1.6/script/test.php");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
                Log.e("pass 1", "connection success ");
            } catch (Exception e) {
                Log.e("Fail 1", e.toString());
                Toast.makeText(getApplicationContext(), "Invalid IP Address",
                        Toast.LENGTH_LONG).show();
            }

            try {
                BufferedReader reader = new BufferedReader
                        (new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {

                    sb.append(line);
                }
                is.close();
                result = sb.toString();
                Log.e("pass 2", "connection success ");
            } catch (Exception e) {
                Log.e("Fail 2", e.toString());
            }

            try {
                JSONObject json_data = new JSONObject(result);


                if (json_data.getInt("success") == 1)
                {
                checkname = true;
                //checkemail = (json_data.getString("email"));
                dob_details = (json_data.getString("DOB"));
                sex_details = (json_data.getString("sex"));
                fname_details = (json_data.getString("fname"));
                lname_details = (json_data.getString("lname"));

                //patient_id = (json_data.getString("id"));
                weight = (json_data.getInt("weight"));
                bps = (json_data.getInt("bps"));
                bpd = (json_data.getInt("bpd"));
                height = (json_data.getInt("height"));
                waist_circ = (json_data.getInt("waist_circ"));
                bmi = (json_data.getInt("BMI"));
                totalCholesterol = (json_data.getInt("cholestrol"));
                ldl = (json_data.getInt("LDL"));
                hdl = (json_data.getInt("HDL"));
                smoke = (json_data.getInt("tobacco"));
                diabetes = (json_data.getString("db"));
                hypertension = (json_data.getString("ht"));
                heartdisease = (json_data.getString("hd"));

                }

            } catch (Exception e) {
                Log.e("Fail 3", e.toString());
            }
            return null;

        }

        protected void onPostExecute(Void unused) {
            Dialog.dismiss();

            if (Error != null) { dob.setText("Output : " + Error);
            } else {

                if (checkname == true)
                {
                pref1 = getSharedPreferences(prefName,MODE_PRIVATE);
                SharedPreferences.Editor editor = pref1.edit();

                greetingTextView.setText("Welcome "+fname_details + " "+lname_details);
                sex.setText("Sex: "+ sex_details);

                String year = dob_details.substring(0,4);
                String month = dob_details.substring(5,7);
                String day = dob_details.substring(8,10);
                dob.setText("D.O.B: " + day.toString()+"/"+month.toString()+"/"+year.toString());

                ages = calculateMyAge(Integer.valueOf(year),Integer.valueOf(month),Integer.valueOf(day));
                age.setText("Age: "+ages);

                weightshow.setText(Integer.toString(weight));
                heightshow.setText(Integer.toString(height));
                bpsshow.setText(Integer.toString(bps));
                bpdshow.setText(Integer.toString(bpd));
                bmishow.setText(Integer.toString(bmi));
                totalChloesterolshow.setText(Integer.toString(totalCholesterol));
                hdlshow.setText(Integer.toString(hdl));
                ldlshow.setText(Integer.toString(ldl));

               // int intweight = weightshow.getin
               //int t = Convert.ToInt32(tv.Text);
                RadioButton rbWaistYes = (RadioButton) findViewById(R.id.waistyes);
                RadioButton rbWaistNo = (RadioButton) findViewById(R.id.waistno);
                RadioButton rbsmokeyes = (RadioButton) findViewById(R.id.smokeyes);
                RadioButton rbsmokeno = (RadioButton) findViewById(R.id.smokeno);
                RadioButton rbheartdiseaseyes = (RadioButton) findViewById(R.id.HeartDiseaseYes);
                RadioButton rbheartdiseaseno = (RadioButton) findViewById(R.id.HeartDiseaseNo);
                RadioButton rbdiabeticyes  = (RadioButton) findViewById(R.id.DiabeticYes);
                RadioButton rbdiabeticno = (RadioButton) findViewById(R.id.DiabeticNo);
                RadioButton rbbpmedicationyes = (RadioButton) findViewById(R.id.BPMedicationYes);
                RadioButton rbbpmedicationno = (RadioButton) findViewById(R.id.BPMedicationNo);


                    if (waist_circ > 40) {
                        rbWaistYes.setChecked(true);
                    } else {
                        rbWaistNo.setChecked(true);
                    }
                    if (smoke < 4) {  //smoke even you quit
                        rbsmokeyes.setChecked(true);
                        smoking = true;

                    } else {
                        rbsmokeno.setChecked(true);
                    }
                    if (heartdisease == "false"){
                        rbheartdiseaseno.setChecked(true);
                    }else{
                        rbheartdiseaseyes.setChecked(true);
                    }
                    if(diabetes == "false"){
                        rbdiabeticno.setChecked(true);
                    }else{
                        rbdiabeticyes.setChecked(true);
                    }
                    if (hypertension == "false"){
                        rbbpmedicationno.setChecked(true);
                    }else{
                        rbbpmedicationyes.setChecked(true);
                        BPtreated=true;
                    }


            }else{

                    AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
                    alert.setTitle("Heart Risk Cal Message");
                    alert.setMessage("Sorry, No Record Found ! Use offline mode");
                    alert.setPositiveButton("OK",null);
                    alert.show();

                }
        }

        }

    }

    //for age Calculation
    private static int calculateMyAge(int year, int month, int day) {
        Calendar birthCal = new GregorianCalendar(year, month, day);

        Calendar nowCal = new GregorianCalendar();

        int age = nowCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

        boolean isMonthGreater = birthCal.get(Calendar.MONTH) >= nowCal
                .get(Calendar.MONTH);

        boolean isMonthSameButDayGreater = birthCal.get(Calendar.MONTH) == nowCal
                .get(Calendar.MONTH)
                && birthCal.get(Calendar.DAY_OF_MONTH) > nowCal
                .get(Calendar.DAY_OF_MONTH);

        if (isMonthGreater || isMonthSameButDayGreater) {
            age = age - 1;
        }
        return age;


    }

    public void CalcHeartRisk()
    {
        heartpercentage=0;

        weight = Integer.valueOf(weightshow.getText().toString());
        height = Integer.valueOf(heightshow.getText().toString());
        bps = Integer.valueOf(bpsshow.getText().toString());
        bpd = Integer.valueOf(bpdshow.getText().toString());
        totalCholesterol = Integer.valueOf(totalChloesterolshow.getText().toString());
        hdl = Integer.valueOf(hdlshow.getText().toString());
        ldl = Integer.valueOf(ldlshow.getText().toString());






        if (sex_details.toString().equals("Female"))
        {
            if (ages >=20 && ages <= 34)
            {
                heartpercentage=heartpercentage-7;

                if (smoking = true)
                {
                    heartpercentage=heartpercentage+9;
                }

                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+4;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+8;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+11;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+13;
                }
            }
            else if (ages >=35 && ages <= 39)
            {
                heartpercentage=heartpercentage-3;

                if (smoking = true)
                {
                    heartpercentage=heartpercentage+9;
                }
                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+4;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+8;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+11;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+13;
                }
            }
            else if (ages >=40 && ages <= 44)
            {
                heartpercentage=heartpercentage-0;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+7;
                }
                if (totalCholesterol >= 160 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+3;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+6;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+8;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+10;
                }
            }
            else if (ages >=45 && ages <= 49)
            {
                heartpercentage=heartpercentage+3;

                if (smoking = true)
                {
                    heartpercentage=heartpercentage+7;
                }
                if (totalCholesterol >= 160 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+3;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+6;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+8;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+10;
                }
            }
            else if (ages >=50 && ages <= 54)
            {
                heartpercentage=heartpercentage+6;

                if (smoking = true)
                {
                    heartpercentage=heartpercentage+4;
                }
                if (totalCholesterol >= 160 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+4;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+5;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+7;
                }
            }
            else if (ages >=55 && ages <= 59)
            {
                heartpercentage=heartpercentage+8;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+4;
                }
                if (totalCholesterol >= 160 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+4;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+5;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+7;
                }
            }
            else if (ages >=60 && ages <= 64)
            {
                heartpercentage=heartpercentage+10;

                if (smoking = true)
                {
                    heartpercentage=heartpercentage+2;
                }
                if (totalCholesterol >= 160 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+3;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+4;
                }
            }
            else if (ages >=65 && ages <= 69)
            {
                heartpercentage=heartpercentage+12;

                if (smoking = true)
                {
                    heartpercentage=heartpercentage+2;
                }

                if (totalCholesterol >= 160 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+3;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+4;
                }
            }
            else if (ages >=70 && ages <= 74)
            {
                heartpercentage=heartpercentage+14;

                if (smoking = true)
                {
                    heartpercentage=heartpercentage+1;
                }
                if (totalCholesterol >= 160 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+2;
                }
            }
            else if (ages >=75 && ages <= 79)
            {
                heartpercentage=heartpercentage+16;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+1;
                }
                if (totalCholesterol >= 160 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+2;
                }
            }

            if (hdl <= 40){
                heartpercentage=heartpercentage+2;
            }else if (hdl >40 && hdl <=49){
                heartpercentage=heartpercentage+1;
            }else if (hdl > 60){
                heartpercentage=heartpercentage-1;
            }

            if (BPtreated.equals(true)){
                if (bps <120){
                    heartpercentage=heartpercentage+0;
                }else if (bps >= 120 && bps <= 129)//120-129: 3 points
                {
                    heartpercentage=heartpercentage+3;
                }else if (bps >= 130 && bps <= 139)//130-139: 4 points
                {
                    heartpercentage=heartpercentage+4;
                }else if (bps >= 140 && bps <= 159)//140-159: 5 points.
                {
                    heartpercentage=heartpercentage+5;
                }else if (bps >= 160)//140-159: 5 points.
                {
                    heartpercentage=heartpercentage+6;
                }
            }else if (BPtreated.equals(false))
            {
                if (bps <120){
                    heartpercentage=heartpercentage+0;
                }else if (bps >= 120 && bps <= 129)//120-129: 3 points
                {
                    heartpercentage=heartpercentage+1;
                }else if (bps >= 130 && bps <= 139)//130-139: 4 points
                {
                    heartpercentage=heartpercentage+2;
                }else if (bps >= 140 && bps <= 159)//140-159: 5 points.
                {
                    heartpercentage=heartpercentage+3;
                }else if (bps >= 160)//140-159: 5 points.
                {
                    heartpercentage=heartpercentage+4;
                }
            }



            if (heartpercentage < 9) //Under 9 points: <1%.
            {
                heartrisk = "< 1 ";
            }else if (heartpercentage >= 9 && heartpercentage <= 12) //9-12 points: 1%
            {
                heartrisk = "1";
            }else if (heartpercentage >= 13 && heartpercentage <= 14) //13-14 points: 2%
            {
                heartrisk="2";
            }else if (heartpercentage == 15) //15 points: 3%
            {
                heartrisk="3";
            }else if (heartpercentage == 16) //16 points: 4%
            {
                heartrisk="4";
            }else if (heartpercentage == 17) //17 points: 5%
            {
                heartrisk="5";
            }else if (heartpercentage == 18) //18 points: 6%.
            {
                heartrisk="6";
            }else if (heartpercentage == 19) //19 points: 8%.
            {
                heartrisk="8";
            }else if (heartpercentage == 20) //20 points: 11%.
            {
                heartrisk="11";
            }else if (heartpercentage == 21) // 21=14%
            {
                heartrisk="14";
            }else if (heartpercentage == 22) //22=17%,
            {
                heartrisk="17";
            }else if (heartpercentage == 23) //23=22%
            {
                heartrisk="22";
            }else if (heartpercentage == 24) //24=27%
            {
                heartrisk="27";
            }else if (heartpercentage >= 22) //>25= Over 30%
            {
                heartrisk="> 30";
            }

        }

        if (sex_details.toString().equals("Male"))
        {
            if (ages >=20 && ages <= 34) // 20–34 years: Minus 9 points.
            {
                heartpercentage=heartpercentage-9;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+8;
                }

                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+4;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+7;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+9;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+11;
                }
            }
            else if (ages >=35 && ages <= 39)
            {

                heartpercentage=heartpercentage-4;

                if (smoking = true)
                {
                    heartpercentage=heartpercentage+8;
                }

                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+4;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+7;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+9;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+11;
                }
            }
            else if (ages >=40 && ages <= 44)
            {
                heartpercentage=heartpercentage-0;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+5;
                }

                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+3;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+5;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+6;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+8;
                }
            }
            else if (ages >=45 && ages <= 49)
            {
                heartpercentage=heartpercentage+3;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+5;
                }
                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+3;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+5;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+6;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+8;
                }
            }
            else if (ages >=50 && ages <= 54)
            {
                heartpercentage=heartpercentage+6;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+3;
                }
                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+3;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+4;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+5;
                }
            }
            else if (ages >=55 && ages <= 59)
            {
                heartpercentage=heartpercentage+8;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+3;
                }
                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+3;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+4;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+5;
                }
            }
            else if (ages >=60 && ages <= 64)
            {
                heartpercentage=heartpercentage+10;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+1;
                }
                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+3;
                }
            }
            else if (ages >=65 && ages <= 69)
            {
                heartpercentage=heartpercentage+11;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+1;
                }
                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+2;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+3;
                }
            }
            else if (ages >=70 && ages <= 74)
            {
                heartpercentage=heartpercentage+12;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+1;
                }
                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+0;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+0;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+1;
                }
            }
            else if (ages >=75 && ages <= 79)
            {
                heartpercentage=heartpercentage+13;
                if (smoking = true)
                {
                    heartpercentage=heartpercentage+1;
                }
                if (totalCholesterol > 159 && totalCholesterol <=199)
                {
                    heartpercentage=heartpercentage+0;
                }
                else if (totalCholesterol >= 200 && totalCholesterol <=239)
                {
                    heartpercentage=heartpercentage+0;
                }
                else if (totalCholesterol >= 240 && totalCholesterol <=279)
                {
                    heartpercentage=heartpercentage+1;
                }
                else if (totalCholesterol >= 280)
                {
                    heartpercentage=heartpercentage+1;
                }
            }

            if (BPtreated.equals(true)){
                if (bps <120){
                    heartpercentage=heartpercentage+0;
                }else if (bps >= 120 && bps <= 129)//120-129: 3 points
                {
                    heartpercentage=heartpercentage+3;
                }else if (bps >= 130 && bps <= 139)//130-139: 4 points
                {
                    heartpercentage=heartpercentage+4;
                }else if (bps >= 140 && bps <= 159)//140-159: 5 points.
                {
                    heartpercentage=heartpercentage+5;
                }else if (bps >= 160)//140-159: 5 points.
                {
                    heartpercentage=heartpercentage+6;
                }
            }else if (BPtreated.equals(false)) {
                if (bps < 120) {
                    heartpercentage = heartpercentage + 0;
                } else if (bps >= 120 && bps <= 129)//120-129: 3 points
                {
                    heartpercentage = heartpercentage + 1;
                } else if (bps >= 130 && bps <= 139)//130-139: 4 points
                {
                    heartpercentage = heartpercentage + 2;
                } else if (bps >= 140 && bps <= 159)//140-159: 5 points.
                {
                    heartpercentage = heartpercentage + 3;
                } else if (bps >= 160)//140-159: 5 points.
                {
                    heartpercentage = heartpercentage + 4;
                }
            }

            if (hdl <= 40){
                heartpercentage=heartpercentage+2;
            }else if (hdl >40 && hdl <=49){
                heartpercentage=heartpercentage+1;
            }else if (hdl > 60){
                heartpercentage=heartpercentage-1;
            }

            if (heartpercentage == 0) // 0 point: <1%.
            {
                heartrisk = "< 1 ";
            }else if (heartpercentage >= 1 && heartpercentage <= 4)// 1-4 points: 1%
            {
                heartrisk = "1";
            }else if (heartpercentage >= 5 && heartpercentage <= 6) //5-6 points: 2%
            {
                heartrisk="2";
            }else if (heartpercentage == 7) //7 points: 3%
            {
                heartrisk="3";
            }else if (heartpercentage == 8) //8 points: 4%.
            {
                heartrisk="4";
            }else if (heartpercentage == 9) //9 points: 5%
            {
                heartrisk="5";
            }else if (heartpercentage == 10) //10 points: 6%.
            {
                heartrisk="6";
            }else if (heartpercentage == 11) //11 points: 8%
            {
                heartrisk="8";
            }else if (heartpercentage == 12) //12 points: 10%
            {
                heartrisk="10";
            }else if (heartpercentage == 13) // 13 points: 12%
            {
                heartrisk="12";
            }else if (heartpercentage == 14) //14 points: 16%
            {
                heartrisk="16";
            }else if (heartpercentage == 15) //15 points: 20%.
            {
                heartrisk="20";
            }else if (heartpercentage == 16) //16 points: 25%
            {
                heartrisk="25";
            }else if (heartpercentage >= 17) //17 points or more: Over 30%
            {
                heartrisk="> 30";
            }
        }

    }

    public void Display()
    {
        welcome = "Dear " + fname_details + " "+lname_details + "<br>"+ "<br>"
                + "In according to the Framingham Risk Score assessment, your heart attack risk for the next 10 years is "
                + heartrisk + "%" +"<br>"

                + "Thank you - s99003388 ";

        percentageshow.setText(""+heartrisk+"%");
        displayresult.setText(Html.fromHtml(welcome + " "+heartpercentage));
    }

}
