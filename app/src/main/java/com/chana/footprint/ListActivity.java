package com.chana.footprint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.KeyListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.chana.footprint.data.GridAdapter;
import com.chana.footprint.data.PlaceItem;
import com.chana.footprint.data.StaticPlaceItemList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText et_name, et_feel;
    private Button btn_revise;
    private SlidingUpPanelLayout mainPanel;
    private boolean isRevise = true;
    private int positionIndex;
    private GridView gv_image;
    double latitude, longitude;

    RecyclerView rv_list;
    ArrayList<Bitmap> images = new ArrayList<>();
    GridAdapter gridAdapter;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mapFragment = (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.place_map);

        setContentViews();
    }

    private void setContentViews(){
        mainPanel = findViewById(R.id.list_panel);
        mainPanel.setTouchEnabled(false);
        mainPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState== SlidingUpPanelLayout.PanelState.COLLAPSED){
                    et_feel.setKeyListener(null);
                    et_name.setKeyListener(null);
                    et_feel.clearFocus();
                    et_name.clearFocus();
                    btn_revise.setText("수정");
                }
            }
        });

        gv_image = findViewById(R.id.gv_image);
        gridAdapter = new GridAdapter(this, images, false);
        gridAdapter.setListener(v -> {
            if(images.size()>=3){
                Toast.makeText(this, "사진은 최대 3장까지 올릴 수 있어요", Toast.LENGTH_SHORT).show();
            } else {
                tedPermission();
            }
        });
        gv_image.setAdapter(gridAdapter);

        et_name = findViewById(R.id.et_placeName);
        et_name.setTag(et_name.getKeyListener());
        et_name.setKeyListener(null);

        et_feel = findViewById(R.id.et_placeFeel);
        et_feel.setTag(et_feel.getKeyListener());
        et_feel.setKeyListener(null);

        rv_list = findViewById(R.id.rv_list);
        Adapter adapter = new Adapter(this, StaticPlaceItemList.placeItems);
        rv_list.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv_list.setAdapter(adapter);

        btn_revise = findViewById(R.id.btn_revise);
        btn_revise.setOnClickListener(v -> {
            if(isRevise){
                et_name.setKeyListener((KeyListener)et_name.getTag());
                et_feel.setKeyListener((KeyListener)et_feel.getTag());
                images.add(0,BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.cam));
                gridAdapter.setCanRevise(true);
                gridAdapter.notifyDataSetChanged();
                btn_revise.setText("완료");
                et_name.requestFocus();
                isRevise = false;
            } else {
                PlaceItem item = StaticPlaceItemList.placeItems.get(positionIndex);
                item.setPlaceName(et_name.getText().toString());
                item.setPlaceFeel(et_feel.getText().toString());
                images.remove(0);
                item.setImages(images);
                gridAdapter.setCanRevise(false);
                gridAdapter.notifyDataSetChanged();
                item.onBindSharedPreference(this);
                et_name.setKeyListener(null);
                et_feel.setKeyListener(null);
                btn_revise.setText("수정");
                mainPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                adapter.notifyDataSetChanged();
                isRevise = true;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng position = new LatLng(latitude,longitude);
        MarkerOptions options = new MarkerOptions();
        options.position(position);
        googleMap.addMarker(options);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,15));
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder>{

        ArrayList<PlaceItem> items;
        Context context;

        public Adapter(Context context, ArrayList<PlaceItem> items){
            this.items = items;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(context,R.layout.item_list,null);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PlaceItem item = items.get(position);
            holder.name.setText(item.getPlaceName());
            holder.feel.setText(item.getPlaceFeel());
            holder.time.setText(item.getVisitTime());
            holder.itemView.setOnClickListener(v -> {
                latitude = item.getLatitude();
                longitude = item.getLongitude();
                mapFragment.getMapAsync((ListActivity)context);
                images = item.getImages();
                gridAdapter.notifyDataSetChanged();
                positionIndex = position;
                et_name.setText(item.getPlaceName());
                et_feel.setText(item.getPlaceFeel());
                mainPanel.setTouchEnabled(true);
                mainPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, feel, time;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_place_name);
            feel = itemView.findViewById(R.id.tv_place_feel);
            time = itemView.findViewById(R.id.tv_time);
        }
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
                    gridAdapter.notifyDataSetChanged();

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
        if(mainPanel.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED) {
            mainPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            et_feel.setKeyListener(null);
            et_name.setKeyListener(null);
        }
        else
            super.onBackPressed();
    }
}