package com.chana.footprint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chana.footprint.data.AmazonS3Util;
import com.chana.footprint.data.GPSTracker;
import com.chana.footprint.data.PlaceItem;
import com.chana.footprint.data.StaticPlaceItemList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private Button btn_list, btn_stamp;
    private EditText et_name, et_feel;
    private SlidingUpPanelLayout mainPanel;
    long backKeyPressedTime = 0;
    private GPSTracker gpsTracker;
    private ArrayList<Marker> markers = new ArrayList<>();
    Context mContext;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentViews();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gpsTracker = new GPSTracker(this);
        markers.clear();
        googleMap.clear();
        LatLng current = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());

        for(PlaceItem item : StaticPlaceItemList.placeItems){
            double latitude = item.getLatitude();
            double longitude = item.getLongitude();
            LatLng position = new LatLng(latitude,longitude);
            MarkerOptions options = new MarkerOptions();
            options.title(item.getPlaceName());
            String feel = item.getPlaceFeel();
            if(feel.length()>10){
                feel = feel.substring(0,10) + "...";
            }
            options.snippet(feel);
            options.position(position);
            markers.add(googleMap.addMarker(options));
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current,11));
        googleMap.setOnInfoWindowClickListener(marker -> {
            int index = markers.indexOf(marker);
            PlaceItem item = StaticPlaceItemList.placeItems.get(index);
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            intent.putExtra("item",item);
            startActivity(intent);
        });

        googleMap.setOnMarkerClickListener(marker -> {
            bitmap = null;
            marker.showInfoWindow();
            int index = markers.indexOf(marker);
            PlaceItem item = StaticPlaceItemList.placeItems.get(index);
            String id = item.getId();
            Glide.with(getApplicationContext()).asBitmap()
                    .load(Uri.parse(AmazonS3Util.getDownloadUrl(getApplicationContext(), id) + id + "_" + 0))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            bitmap = resource;
                            marker.showInfoWindow();
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
            return true;
        });

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) { // 여기가 인포윈도우 커스텀 하는건데, 보면은
                View view = getLayoutInflater().inflate(R.layout.marker_layout, null); // 레이아웃 인플레이터 <- 이걸로 커스텀
                int index = markers.indexOf(marker);
                PlaceItem item = StaticPlaceItemList.placeItems.get(index);
                TextView title = view.findViewById(R.id.tv_snippet_title); // 여기서 설정하면 될듯?
                TextView feel = view.findViewById(R.id.tv_snippet_feel);
                ImageView imageView = view.findViewById(R.id.iv_snippet);
                title.setText(item.getPlaceName());
                feel.setText(item.getPlaceFeel());
                if(bitmap!= null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.getLayoutParams().height=imageView.getLayoutParams().width;
                }
                return view;
            }
        });
    }

    public void setContentViews(){
        mainPanel = findViewById(R.id.main_panel);
        mainPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        btn_list = findViewById(R.id.btn_list);
        btn_stamp = findViewById(R.id.btn_stamp);
        et_feel = findViewById(R.id.et_placeFeel);
        et_name = findViewById(R.id.et_placeName);


        btn_list.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
        });

        mainPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){

                }
                else {

                }
            }
        });

        et_name.setOnClickListener(v -> {
            if(mainPanel.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED)
                et_name.requestFocus();
            else
                mainPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        });

        btn_stamp.setOnClickListener(v -> {
            String placeName = et_name.getText().toString();
            String placeFeel = et_feel.getText().toString();
            if(placeName.equals("")) {
                Toast.makeText(this,"어떤 장소인지 알려주세요 :)",Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("추억을 남기시겠어요?");
            builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    et_name.setText(null);
                    et_feel.setText(null);
                    PlaceItem item = new PlaceItem();
                    item.setId(String.valueOf(System.currentTimeMillis()));
                    item.setVisitTime(getTime());
                    item.setPlaceName(placeName);
                    item.setPlaceFeel(placeFeel);
                    item.setLatitude(gpsTracker.getLatitude());
                    item.setLongitude(gpsTracker.getLongitude());
                    item.onBindSharedPreference(getApplicationContext());
                    StaticPlaceItemList.loadPlaceItems(getApplicationContext());
                    //이미지
                    mainPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    mapFragment.getMapAsync((MainActivity)mContext);
                }
            }).setNegativeButton("아니요", null);
            builder.create();
            builder.show();
        });
    }

    private String getTime(){
        Date date = new Date(System.currentTimeMillis());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    @Override
    public void onBackPressed() {
        if(mainPanel.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED)
            mainPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else {
            Toast toast = null;
            // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
            // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
            // 2000 milliseconds = 2 seconds
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
            // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
            // 현재 표시된 Toast 취소
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                this.finishAffinity();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mapFragment.getMapAsync(this);
    }
}