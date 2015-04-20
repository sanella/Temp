package com.bitcamp.benjamin.services_lab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitcamp.benjamin.services_lab.service.ServiceRequest;
import com.bitcamp.benjamin.services_lab.singletons.CouponFeed;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences.Editor;


public class CouponActivity extends ActionBarActivity {

    private ListView mCouponList;
    private EditText mFilter;
    private CouponAdapter mAdapter;
    public static final String MyPREFERENCES = "MyPrefs" ;
    static ArrayList<Coupon> coupons = new ArrayList<Coupon>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);

        mCouponList= (ListView)findViewById(R.id.list_view_coupons);
        CouponFeed couponFeed = CouponFeed.getInstance();
        //for oriantation mobile is chabged - don't take a new list
        if(couponFeed.getFeed().size() == 0){
            couponFeed.getFeed(getString(R.string.service_posts));
            coupons = couponFeed.getFeed();
        }

        mAdapter = new CouponAdapter(coupons);
        mCouponList.setAdapter(mAdapter);
        mCouponList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Coupon clicked = coupons.get(position);
                int couponId = clicked.getId();
                String url = getString(R.string.service_single_coupon);
                JSONObject clickedCoupon = new JSONObject();
                try {
                    clickedCoupon.put("id", Integer.toString(couponId));
                    Log.d("TAG", "JSON ID " +id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String json = clickedCoupon.toString();
                Log.d("TAG", json);
                ServiceRequest.post(url, json,  getCoupon());
            }
        });

        mCouponList.setAdapter(mAdapter);

        mFilter = (EditText)findViewById(R.id.edit_text_filter);
        mFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ( (ArrayAdapter<Coupon>)mCouponList.getAdapter()).getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private Callback getCoupon() {
        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                makeToast(R.string.toast_try_again);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseJson = response.body().string();

                try {
                    JSONObject coupon = new JSONObject(responseJson);
                    Intent goToCoupon = new Intent(CouponActivity.this, SingleCouponActivity.class);
                    goToCoupon.putExtra("name", coupon.getString("name"));
                    goToCoupon.putExtra("picture", coupon.getString("picture"));
                    goToCoupon.putExtra("description", coupon.getString("description"));
                    goToCoupon.putExtra("price", coupon.getString("price"));
                    startActivity(goToCoupon);
                } catch (JSONException e) {
                    makeToast(R.string.toast_try_again);
                    e.printStackTrace();
                }
            }
        };
    }

    private void makeToast(final int messageId){

        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CouponActivity.this,
                                messageId,
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_coupon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent login = new Intent(this, FragmentLoginDialog.class);
            startActivity(login);
            return true;
        }

        if (id == R.id.action_logout) {

            SharedPreferences sharedpreferences = getSharedPreferences
                    (CouponActivity.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.clear();
            editor.commit();
            moveTaskToBack(true);
            CouponActivity.this.finish();

        }

        return super.onOptionsItemSelected(item);
    }

    private  class CouponAdapter extends ArrayAdapter<Coupon>{
        public CouponAdapter(ArrayList<Coupon> coupons){
            super(CouponActivity.this, 0,  coupons );
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            Coupon current = getItem(position);
            if (convertView == null) {
                convertView = CouponActivity.this.getLayoutInflater()
                        .inflate(R.layout.row, null);
            }


            TextView couponName = (TextView) convertView.findViewById(R.id.textview_name);
            couponName.setText(current.getName());

            TextView couponPrice = (TextView) convertView.findViewById(R.id.textview_price);

            couponPrice.setText("" + current.getPrice() + getString(R.string.currency));
            ImageView couponImage = (ImageView) convertView.findViewById(R.id.imageview_image);
            String img =getString(R.string.image_path) + current.getPicture();
            img = img.replaceAll("\\\\","/");
            Log.d("IMGTAG", img);
            Picasso.with(getContext()).load(img).into(couponImage);
            return convertView;

        }
    }
}
