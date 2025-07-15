package com.thanhvan.bookstoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;


import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class MapAddressPickerActivity extends AppCompatActivity {

    public static final String RESULT_FULL_ADDRESS = "full_address";
    public static final String RESULT_LATITUDE = "latitude";
    public static final String RESULT_LONGITUDE = "longitude";

    private static final int REQUEST_PERMISSIONS_CODE = 1;

    private MapView map;
    private TextView tvSelectedAddress;
    private Button btnConfirmLocation;
    private Marker centerMarker;
    private GeoPoint currentCenterPoint;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable reverseGeocodeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_address_picker);

        tvSelectedAddress = findViewById(R.id.text_view_selected_address);
        btnConfirmLocation = findViewById(R.id.button_confirm_location);
        map = findViewById(R.id.map_picker);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_PERMISSIONS_CODE);
        } else {
            setupMap();
        }

        btnConfirmLocation.setOnClickListener(v -> {
            if (currentCenterPoint != null && !tvSelectedAddress.getText().toString().contains("Đang tìm...")) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(RESULT_FULL_ADDRESS, tvSelectedAddress.getText().toString().replace("Địa chỉ được chọn: ", ""));
                resultIntent.putExtra(RESULT_LATITUDE, currentCenterPoint.getLatitude());
                resultIntent.putExtra(RESULT_LONGITUDE, currentCenterPoint.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Vui lòng đợi địa chỉ được tìm thấy hoặc di chuyển bản đồ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMap() {
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        GeoPoint startPoint = new GeoPoint(10.762622, 106.660172);
        map.getController().setCenter(startPoint);

        centerMarker = new Marker(map);
        centerMarker.setIcon(ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default));
        centerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(centerMarker);
        centerMarker.setPosition(startPoint);

        map.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                currentCenterPoint = (GeoPoint) map.getMapCenter();
                centerMarker.setPosition(currentCenterPoint);
                if (reverseGeocodeRunnable != null) {
                    handler.removeCallbacks(reverseGeocodeRunnable);
                }
                reverseGeocodeRunnable = () -> performReverseGeocoding(currentCenterPoint);
                handler.postDelayed(reverseGeocodeRunnable, 700);
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                currentCenterPoint = (GeoPoint) map.getMapCenter();
                centerMarker.setPosition(currentCenterPoint);
                if (reverseGeocodeRunnable != null) {
                    handler.removeCallbacks(reverseGeocodeRunnable);
                }
                reverseGeocodeRunnable = () -> performReverseGeocoding(currentCenterPoint);
                handler.postDelayed(reverseGeocodeRunnable, 700);
                return false;
            }
        });

        currentCenterPoint = startPoint;
        performReverseGeocoding(currentCenterPoint);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap();
            } else {
                Toast.makeText(this, "Quyền truy cập bộ nhớ và vị trí bị từ chối. Bản đồ có thể không hoạt động đầy đủ.", Toast.LENGTH_LONG).show();
                setupMap();
            }
        }
    }

    private void performReverseGeocoding(GeoPoint geoPoint) {
        tvSelectedAddress.setText("Địa chỉ được chọn: Đang tìm...");
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                String urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + geoPoint.getLatitude() +
                        "&lon=" + geoPoint.getLongitude() + "&zoom=18&addressdetails=1&accept-language=vi";

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("User-Agent", "BookstoreManagerApp/1.0");

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    String jsonResponse = buffer.toString();
                    Log.d("NominatimReverse", "Response: " + jsonResponse);

                    handler.post(() -> parseNominatimReverseResponse(jsonResponse));
                } else {
                    Log.e("NominatimReverse", "HTTP error code: " + responseCode);
                    handler.post(() -> tvSelectedAddress.setText("Không tìm thấy địa chỉ (Lỗi HTTP: " + responseCode + ")"));
                }

            } catch (Exception e) {
                Log.e("NominatimReverse", "Network/JSON error: " + e.getMessage(), e);
                handler.post(() -> tvSelectedAddress.setText("Lỗi: " + e.getMessage()));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e("NominatimReverse", "Error closing reader", e);
                    }
                }
            }
        }).start();
    }

    private void parseNominatimReverseResponse(String jsonResponse) {
        try {
            JSONObject obj = new JSONObject(jsonResponse);
            String displayName = obj.optString("display_name", "Không tìm thấy địa chỉ.");
            tvSelectedAddress.setText("Địa chỉ được chọn: " + displayName);

        } catch (JSONException e) {
            Log.e("NominatimReverse", "Error parsing reverse geocoding JSON: " + e.getMessage(), e);
            tvSelectedAddress.setText("Lỗi phân tích địa chỉ.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        if (reverseGeocodeRunnable != null) {
            handler.removeCallbacks(reverseGeocodeRunnable);
        }
    }
}