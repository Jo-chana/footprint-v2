package com.chana.footprint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chana.footprint.data.GPSTracker;
import com.chana.footprint.data.GridAdapter;
import com.chana.footprint.data.PlaceItem;
import com.chana.footprint.data.PreferenceManager;
import com.chana.footprint.data.StaticPlaceItemList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private Button btn_list, btn_stamp;
    private EditText et_name, et_feel;
    private SlidingUpPanelLayout mainPanel;
    long backKeyPressedTime = 0;
    private GridView gv_image;
    private GPSTracker gpsTracker;
    private ArrayList<MarkerOptions> markerOptions = new ArrayList<>();
    ArrayList<Bitmap> images;
    GridAdapter adapter;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        images = new ArrayList<>();

        mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        images.add(0, BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.cam));

        setContentViews();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gpsTracker = new GPSTracker(this);
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
            googleMap.addMarker(options);
            markerOptions.add(options);
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current,11));

    }

    public void setContentViews(){
        mainPanel = findViewById(R.id.main_panel);
        mainPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        gv_image = findViewById(R.id.gv_image);
        btn_list = findViewById(R.id.btn_list);
        btn_stamp = findViewById(R.id.btn_stamp);
        et_feel = findViewById(R.id.et_placeFeel);
        et_name = findViewById(R.id.et_placeName);

        adapter = new GridAdapter(this,images,true);
        adapter.setListener(v -> {
            if(images.size()>=3){
                Toast.makeText(this, "사진은 최대 3장까지 올릴 수 있어요", Toast.LENGTH_SHORT).show();
            } else {
                tedPermission();
            }
        });
//        gv_image.setAdapter(adapter); 아직 이미지는 서버 개발 필요

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
                    images.clear();
                    images.add(0, BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.cam));
                    adapter.notifyDataSetChanged();
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
            builder.setMessage("장소를 남기시겠어요?");
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
                    images.remove(0);
                    item.setImages(images);
                    StaticPlaceItemList.addPlace(getApplicationContext(),item);
                    item.onBindSharedPreference(getApplicationContext());
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

    public void tedPermission(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent();
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, 0);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(),"권한이 거부되어 있어요",Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진을 업로드하기 위하여 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 0) {
                if (resultCode == RESULT_OK) {

                    InputStream in = getContentResolver().openInputStream(data.getData());

                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                    images.add(img);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(this, "사진 업로드 완료", Toast.LENGTH_SHORT).show();

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
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
}