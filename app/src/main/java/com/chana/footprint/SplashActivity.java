package com.chana.footprint;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chana.footprint.data.PlaceItem;
import com.chana.footprint.data.PreferenceManager;
import com.chana.footprint.data.StaticPlaceItemList;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        PreferenceManager.clear(this);
        testMode();
        tedPermission();

    }

    public void tedPermission(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                loadPlaceItems();
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                        finish();
                    }
                }, 1000);

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(),"권한이 거부되어 있어요",Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("위치정보에 엑세스하기 위하여 권한이 필요합니다.")
                .setDeniedMessage("거부 시 서비스 이용이 제한됩니다.")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    public void loadPlaceItems(){
        ArrayList<String> placeItemsId = PreferenceManager.getStringArrayList(this,"placeId");
        for(String id : placeItemsId){
            PlaceItem item = new PlaceItem();
            item.setId(id);
            item.setLatitude((double) PreferenceManager.getFloat(this,id+"latitude"));
            item.setLongitude((double) PreferenceManager.getFloat(this, id+"longitude"));
            item.setPlaceName(PreferenceManager.getString(this, id+"placeName"));
            item.setPlaceFeel(PreferenceManager.getString(this, id+"placeFeel"));
            item.setVisitTime(PreferenceManager.getString(this, id+"visitTime"));

            StaticPlaceItemList.placeItems.add(0,item);
        }
    }

    public void testMode(){
        PlaceItem item1 = new PlaceItem();
        item1.setLatitude(37.526960);
        item1.setLongitude(126.932762);
        item1.setPlaceName("여의나루");
        item1.setPlaceFeel("수능끝나고 추위에 떨면서 라면먹고 야경도 보고 좋았던곳 ㅎ");
        item1.setVisitTime("2015-12-12 23:32");
        item1.setId("testdata1");
        PlaceItem item2 = new PlaceItem();
        item2.setLatitude(37.512736);
        item2.setLongitude(127.102007);
        item2.setPlaceName("잠실 롯데 타워");
        item2.setPlaceFeel("3일동안 work flex 체험 해봤는데 넘좋았음");
        item2.setVisitTime("2020-07-23 15:30");
        item2.setId("testdata2");
        PlaceItem item3 = new PlaceItem();
        item3.setLatitude(37.546650);
        item3.setLongitude(126.993477);
        item3.setPlaceName("남산 타워");
        item3.setPlaceFeel("더웠지만 불꽃놀이도 보고 너무 좋았다");
        item3.setVisitTime("2018-08-12 21:12");
        item3.setId("testdata3");
        PlaceItem item4 = new PlaceItem();
        item4.setLatitude(37.624492);
        item4.setLongitude(127.098704);
        item4.setPlaceName("육군사관학교");
        item4.setPlaceFeel("친구 면회가서 배터지게 먹여주고 옴");
        item4.setVisitTime("2019-05-05 14:10");
        item4.setId("testdata4");
        PlaceItem item5 = new PlaceItem();
        item5.setLatitude(37.549486);
        item5.setLongitude(126.915117);
        item5.setPlaceName("합정동");
        item5.setPlaceFeel("추억 참 많은 곳. 여자친구랑도 와보고, 스터디고 하고");
        item5.setVisitTime("2020-04-12 13:40");
        item5.setId("testdata5");
        PlaceItem item6 = new PlaceItem();
        item6.setLatitude(37.485175);
        item6.setLongitude(126.901669);
        item6.setPlaceName("구로디지털단지역");
        item6.setPlaceFeel("그 누나가 살았던 곳. 치맥도 하고, 브런치도 먹고, 영화도 봤던 곳..");
        item6.setVisitTime("2017-08-31 17:00");
        item6.setId("testdata6");
        PlaceItem item7 = new PlaceItem();
        item7.setLatitude(37.498107);
        item7.setLongitude(127.028354);
        item7.setPlaceName("강남역");
        item7.setPlaceFeel("지긋지긋하게 많이 갔던 곳. ");
        item7.setVisitTime("2020-08-07 00:39");
        item7.setId("testdata7");
        PlaceItem item8 = new PlaceItem();
        item8.setLatitude(37.595096);
        item8.setLongitude(127.087027);
        item8.setPlaceName("상봉역");
        item8.setPlaceFeel("개노답 형제들이랑 정복에 소주마신 곳");
        item8.setVisitTime("2018-10-09 18:00");
        item8.setId("testdata8");
        item1.onBindSharedPreference(this);
        item2.onBindSharedPreference(this);
        item3.onBindSharedPreference(this);
        item4.onBindSharedPreference(this);
        item5.onBindSharedPreference(this);
        item6.onBindSharedPreference(this);
        item7.onBindSharedPreference(this);
        item8.onBindSharedPreference(this);
    }
}