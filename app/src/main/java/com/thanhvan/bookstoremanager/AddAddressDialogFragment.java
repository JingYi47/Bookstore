package com.thanhvan.bookstoremanager;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.thanhvan.bookstoremanager.model.Address;
import com.thanhvan.bookstoremanager.sqlite.AddressDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


class LatLng {
    private double latitude;
    private double longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

public class AddAddressDialogFragment extends DialogFragment {

    private EditText etName, etPhone;
    private AutoCompleteTextView etFullAddress;
    private TextView tvCoordinates;
    private Button btnCancel, btnAdd, btnChooseOnMap;

    private AddressDao addressDao;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private ArrayAdapter<String> adapter;
    private List<String> predictionTexts = new ArrayList<>();
    private List<JSONObject> currentPredictionObjects = new ArrayList<>();

    private ActivityResultLauncher<Intent> mapPickerLauncher;


    public interface AddAddressDialogListener {
        void onAddressAdded(Address address);
    }

    private AddAddressDialogListener listener;

    private String currentUserEmail;


    public static AddAddressDialogFragment newInstance(String userEmail) {
        AddAddressDialogFragment fragment = new AddAddressDialogFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_LOGGED_IN_USER_EMAIL, userEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (AddAddressDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddAddressDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            currentUserEmail = getArguments().getString(AppConstants.KEY_LOGGED_IN_USER_EMAIL);

        }


        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String fullAddress = result.getData().getStringExtra(MapAddressPickerActivity.RESULT_FULL_ADDRESS);
                        double lat = result.getData().getDoubleExtra(MapAddressPickerActivity.RESULT_LATITUDE, 0.0);
                        double lon = result.getData().getDoubleExtra(MapAddressPickerActivity.RESULT_LONGITUDE, 0.0);


                        etFullAddress.setText(fullAddress);
                        tvCoordinates.setText(String.format(Locale.getDefault(), "Tọa độ: %.6f, %.6f", lat, lon));
                        tvCoordinates.setTag(new LatLng(lat, lon));
                        tvCoordinates.setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_address, null);

        etName = view.findViewById(R.id.edit_text_add_name);
        etPhone = view.findViewById(R.id.edit_text_add_phone);
        etFullAddress = view.findViewById(R.id.edit_text_full_address);
        tvCoordinates = view.findViewById(R.id.text_view_coordinates);
        btnCancel = view.findViewById(R.id.button_cancel_add_address);
        btnAdd = view.findViewById(R.id.button_add_address);
        btnChooseOnMap = view.findViewById(R.id.button_choose_on_map);

        addressDao = new AddressDao(requireContext());

        setupAutocomplete(etFullAddress);

        builder.setView(view);

        btnCancel.setOnClickListener(v -> dismiss());

        btnAdd.setOnClickListener(v -> {
            if (currentUserEmail == null || currentUserEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy email người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                dismiss();
                return;
            }

            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String fullAddress = etFullAddress.getText().toString().trim();
            double lat = 0.0;
            double lon = 0.0;

            if (tvCoordinates.getVisibility() == View.VISIBLE && tvCoordinates.getTag() instanceof LatLng) {
                LatLng selectedLatLng = (LatLng) tvCoordinates.getTag();
                lat = selectedLatLng.getLatitude();
                lon = selectedLatLng.getLongitude();
            } else {
                Log.w("Nominatim", "Coordinates not available or not properly set for the selected address.");
                Toast.makeText(requireContext(), "Vui lòng chọn địa chỉ từ gợi ý hoặc bản đồ để có tọa độ chính xác.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.isEmpty() || phone.isEmpty() || fullAddress.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }


            Address newAddress = new Address(currentUserEmail, name, phone, fullAddress, lat, lon);
            long result = addressDao.addAddress(newAddress);

            if (result != -1) {
                Toast.makeText(requireContext(), "Đã thêm địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                newAddress.setId((int) result);
                if (listener != null) {
                    listener.onAddressAdded(newAddress);
                }
                dismiss();
            } else {
                Toast.makeText(requireContext(), "Thêm địa chỉ thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                Log.e("AddAddressDialog", "Failed to add address, result = -1. Check Logcat for SQLite errors.");
            }
        });


        btnChooseOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapAddressPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });

        return builder.create();
    }

    private void setupAutocomplete(final AutoCompleteTextView addressEditText) {
        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, predictionTexts);
        addressEditText.setAdapter(adapter);

        addressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                if (s.length() > 2) {
                    final String query = s.toString();
                    searchRunnable = () -> performNominatimSearch(query);
                    handler.postDelayed(searchRunnable, 1000);
                } else if (s.length() == 0) {
                    predictionTexts.clear();
                    currentPredictionObjects.clear();
                    adapter.notifyDataSetChanged();
                    tvCoordinates.setVisibility(View.GONE);
                    tvCoordinates.setTag(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        addressEditText.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPredictionText = (String) parent.getItemAtPosition(position);
            addressEditText.setText(selectedPredictionText);

            if (position < currentPredictionObjects.size()) {
                JSONObject selectedObject = currentPredictionObjects.get(position);
                try {
                    double lat = selectedObject.getDouble("lat");
                    double lon = selectedObject.getDouble("lon");
                    LatLng latLng = new LatLng(lat, lon);
                    tvCoordinates.setText(String.format(Locale.getDefault(), "Tọa độ: %.6f, %.6f", latLng.getLatitude(), latLng.getLongitude()));
                    tvCoordinates.setTag(latLng);
                    tvCoordinates.setVisibility(View.VISIBLE);
                    Log.d("Nominatim", "Selected Place: " + selectedPredictionText + ", LatLng: " + lat + ", " + lon);
                } catch (JSONException e) {
                    Log.e("Nominatim", "Error parsing selected Nominatim object: " + e.getMessage());
                    tvCoordinates.setVisibility(View.GONE);
                    tvCoordinates.setTag(null);
                }
            }
        });
    }

    private void performNominatimSearch(String query) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery +
                        "&format=json&addressdetails=1&limit=10&countrycodes=vn";

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
                    Log.d("Nominatim", "Response: " + jsonResponse);

                    handler.post(() -> parseNominatimResponse(jsonResponse));
                } else {
                    Log.e("Nominatim", "HTTP error code: " + responseCode);
                    handler.post(() -> Toast.makeText(requireContext(), "Lỗi tìm kiếm: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                Log.e("Nominatim", "Network/JSON error: " + e.getMessage(), e);
                handler.post(() -> Toast.makeText(requireContext(), "Lỗi kết nối hoặc dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e("Nominatim", "Error closing reader", e);
                    }
                }
            }
        }).start();
    }

    private void parseNominatimResponse(String jsonResponse) {
        predictionTexts.clear();
        currentPredictionObjects.clear();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String displayName = obj.getString("display_name");
                predictionTexts.add(displayName);
                currentPredictionObjects.add(obj);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e("Nominatim", "Error parsing JSON: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi phân tích dữ liệu địa chỉ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}