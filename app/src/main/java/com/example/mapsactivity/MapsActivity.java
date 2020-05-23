package com.example.mapsactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.mapsactivity.R.id;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.Nullable;

import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

public final class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {
    private final static String TAG = "MapsActivity";
    private static GoogleMap map;
    private static NetworkController networkController;
    public static FusedLocationProviderClient fusedLocationClient;
    private static String inputtext = null;
    private static Task<Location> lastLocation;

    StoreFetchTask fTask = new StoreFetchTask();
    GeocodingFetchTask gTask = new GeocodingFetchTask();
    private static List<Store> temp;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);
        final EditText enterText = this.findViewById(R.id.entertext);
        Button searchbtn = this.findViewById(R.id.btn_search);
        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.map);
        if (fragment == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.google.android.gms.maps.SupportMapFragment");
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment)fragment;
            mapFragment.getMapAsync(this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            Intrinsics.checkExpressionValueIsNotNull(fusedLocationClient, "LocationServices.getFuse…ationProviderClient(this)");

            searchbtn.setOnClickListener(new OnClickListener() {
                public final void onClick(View view) {
                    System.out.println("************************************");
                    inputtext = enterText.getText().toString();
                    System.out.println("************************************" + inputtext);
                    networkController = new NetworkController();
                    Location searchedLocation = null;
                    try {
                        searchedLocation = gTask.execute(inputtext).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("fetchGeocoding 성...공?"+ searchedLocation.getLatitude() +" "+ searchedLocation.getLongitude());
                    onLocationChanged(searchedLocation);
                }
            });
        }
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        Log.e(TAG,"hi");
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);
        setUpMap();
        map.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location lastLocation) {
                Log.e(TAG,"testingonSucceess");
                LatLng currentLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
                List<Store> temp = null;
                try {
                    temp = fTask.execute(lastLocation).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                placeMarkerOnMap(temp);
            }
        });
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            Intrinsics.throwNpe();
        }
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        BitmapDescriptor var10000 = BitmapDescriptorFactory.fromBitmap(bitmap);
        Intrinsics.checkExpressionValueIsNotNull(var10000, "BitmapDescriptorFactory.fromBitmap(bitmap)");
        return var10000;
    }
    void placeMarkerOnMap(List<Store> storesByGeo) {
        if(storesByGeo == null )Log.e(TAG,"thisisfucxingnull");
        if (storesByGeo != null) {
            Log.e(TAG,"isnotnull");
            Log.e(TAG,"is: "+storesByGeo.get(0).getAddr());
            for (final Store store : storesByGeo) {
                final LatLng pinLocation = new LatLng(store.getLat(), store.getLng());
                final String remain = store.getRemain_stat();
                this.runOnUiThread(new Runnable() {
                    public final void run() {
                        switch (remain) {
                            case "plenty":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_green)));
                                break;
                            case "some":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_yellow)));
                                break;
                            case "few":
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_red)));
                                break;
                            default:
                                map.addMarker(new MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                        .position(pinLocation)
                                        .title(store.getName())
                                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_gray)));
                                break;
                        }
                    }
                });
            }
        }
    }

    public void onLocationChanged(Location location) {
        Log.e(TAG,"ChangedonSucceess");
        StoreFetchTask storeFetchTask = new StoreFetchTask();

        // 기존 맵 초기화
        map.clear();

        // 새로운 위치 객체 설정
        LatLng changeLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        Location changeLocation = new Location("");
        changeLocation.setLongitude(changeLatLng.longitude);
        changeLocation.setLatitude(changeLatLng.latitude);

        // 변경되는 위치로 이동
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(changeLatLng, 16f));

        // JSON 파싱
        List<Store> temp = null;
        try {
            temp = storeFetchTask.execute(changeLocation).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        placeMarkerOnMap(temp);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}