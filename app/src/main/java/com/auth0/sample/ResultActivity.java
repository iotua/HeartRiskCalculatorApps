

package com.auth0.sample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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


public class ResultActivity extends ActionBarActivity {



    AsyncHttpClient client;

    TextView dob, sex, age;
    String username,fname,lname, sex_details, dob_details, age_details;

    private SharedPreferences prefs;
    private String prefName = "report";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        username = prefs.getString("fullname", "");
        dob_details = prefs.getString("dob", "");
        sex_details = prefs.getString("sex", "");
        age_details = prefs.getString("ages", "");

        TextView greetingTextView = (TextView) findViewById(R.id.welcome_message);
        greetingTextView.setText("Welcome " + username);

        dob = (TextView) findViewById(R.id.textView14);
        sex = (TextView) findViewById(R.id.textView15);
        age = (TextView) findViewById(R.id.textView16);

        dob.setText("Date of Birth: "+dob_details);
        sex.setText("Sex: "+sex_details);
        age.setText("Age: "+age_details);


    }


}
