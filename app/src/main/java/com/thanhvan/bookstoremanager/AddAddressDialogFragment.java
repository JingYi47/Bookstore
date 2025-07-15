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

// Lớp LatLng: Dùng để lưu trữ cặp tọa độ Latitude và Longitude
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

    // Interface để truyền dữ liệu về Activity chứa Fragment
    public interface AddAddressDialogListener {
        void onAddressAdded(Address address);
    }

    private AddAddressDialogListener listener;

    private String currentUserEmail; // Biến để lưu email của người dùng hiện tại

    // XÓA CÁC HẰNG SỐ TRÙNG LẶP Ở ĐÂY. CHÚNG TA SẼ DÙNG AppConstants

    // Phương thức static để tạo một instance mới của DialogFragment và truyền dữ liệu
    public static AddAddressDialogFragment newInstance(String userEmail) {
        AddAddressDialogFragment fragment = new AddAddressDialogFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_LOGGED_IN_USER_EMAIL, userEmail); // SỬ DỤNG AppConstants.KEY_LOGGED_IN_USER_EMAIL
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Đảm bảo Activity chứa Fragment implements AddAddressDialogListener
        try {
            listener = (AddAddressDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddAddressDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy email người dùng từ arguments khi Fragment được tạo
        if (getArguments() != null) {
            currentUserEmail = getArguments().getString(AppConstants.KEY_LOGGED_IN_USER_EMAIL); // SỬ DỤNG AppConstants.KEY_LOGGED_IN_USER_EMAIL
            // Log.d("AddAddressDialog", "Current User Email: " + currentUserEmail); // Có thể thêm log để kiểm tra
        }

        // Khởi tạo ActivityResultLauncher cho việc chọn địa chỉ trên bản đồ
        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String fullAddress = result.getData().getStringExtra(MapAddressPickerActivity.RESULT_FULL_ADDRESS);
                        double lat = result.getData().getDoubleExtra(MapAddressPickerActivity.RESULT_LATITUDE, 0.0);
                        double lon = result.getData().getDoubleExtra(MapAddressPickerActivity.RESULT_LONGITUDE, 0.0);

                        // Cập nhật EditText và TextView tọa độ với dữ liệu trả về từ bản đồ
                        etFullAddress.setText(fullAddress);
                        tvCoordinates.setText(String.format(Locale.getDefault(), "Tọa độ: %.6f, %.6f", lat, lon));
                        tvCoordinates.setTag(new LatLng(lat, lon)); // Lưu tọa độ vào tag để dễ dàng truy xuất
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

        // Ánh xạ các View từ layout
        etName = view.findViewById(R.id.edit_text_add_name);
        etPhone = view.findViewById(R.id.edit_text_add_phone);
        etFullAddress = view.findViewById(R.id.edit_text_full_address);
        tvCoordinates = view.findViewById(R.id.text_view_coordinates);
        btnCancel = view.findViewById(R.id.button_cancel_add_address);
        btnAdd = view.findViewById(R.id.button_add_address);
        btnChooseOnMap = view.findViewById(R.id.button_choose_on_map);

        addressDao = new AddressDao(requireContext());

        // Thiết lập chức năng gợi ý địa chỉ (autocomplete)
        setupAutocomplete(etFullAddress);

        builder.setView(view); // Gán layout cho dialog

        // Xử lý sự kiện khi nhấn nút HỦY
        btnCancel.setOnClickListener(v -> dismiss());

        // Xử lý sự kiện khi nhấn nút THÊM
        btnAdd.setOnClickListener(v -> {
            // Kiểm tra email người dùng, đây là lỗi NOT NULL CONSTRAINT trước đó
            if (currentUserEmail == null || currentUserEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy email người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                dismiss(); // Đóng dialog
                return; // Dừng xử lý
            }

            // Lấy dữ liệu từ các EditText
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String fullAddress = etFullAddress.getText().toString().trim();
            double lat = 0.0;
            double lon = 0.0;

            // Lấy tọa độ từ tag của tvCoordinates (nếu có)
            if (tvCoordinates.getVisibility() == View.VISIBLE && tvCoordinates.getTag() instanceof LatLng) {
                LatLng selectedLatLng = (LatLng) tvCoordinates.getTag();
                lat = selectedLatLng.getLatitude();
                lon = selectedLatLng.getLongitude();
            } else {
                Log.w("Nominatim", "Coordinates not available or not properly set for the selected address.");
                // Cảnh báo người dùng nếu tọa độ không được chọn/tìm thấy
                Toast.makeText(requireContext(), "Vui lòng chọn địa chỉ từ gợi ý hoặc bản đồ để có tọa độ chính xác.", Toast.LENGTH_SHORT).show();
                return; // Dừng thêm địa chỉ nếu không có tọa độ hợp lệ
            }

            // Kiểm tra các trường bắt buộc không được rỗng
            if (name.isEmpty() || phone.isEmpty() || fullAddress.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return; // Dừng xử lý
            }

            // Tạo đối tượng Address MỚI, TRUYỀN currentUserEmail vào constructor
            Address newAddress = new Address(currentUserEmail, name, phone, fullAddress, lat, lon);
            long result = addressDao.addAddress(newAddress); // Gọi phương thức addAddress của DAO

            if (result != -1) {
                Toast.makeText(requireContext(), "Đã thêm địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                newAddress.setId((int) result); // Gán ID tự động tạo cho đối tượng Address
                if (listener != null) {
                    listener.onAddressAdded(newAddress); // Gọi callback để thông báo về Activity
                }
                dismiss(); // Đóng dialog sau khi thêm thành công
            } else {
                Toast.makeText(requireContext(), "Thêm địa chỉ thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                Log.e("AddAddressDialog", "Failed to add address, result = -1. Check Logcat for SQLite errors.");
            }
        });

        // Xử lý sự kiện khi nhấn nút CHỌN TRÊN BẢN ĐỒ
        btnChooseOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapAddressPickerActivity.class);
            mapPickerLauncher.launch(intent); // Khởi chạy Activity chọn bản đồ
        });

        return builder.create(); // Trả về AlertDialog đã được cấu hình
    }

    // Thiết lập AutoCompleteTextView cho gợi ý địa chỉ từ Nominatim
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
                    handler.removeCallbacks(searchRunnable); // Hủy bỏ tìm kiếm trước đó nếu người dùng nhập tiếp
                }

                if (s.length() > 2) { // Bắt đầu tìm kiếm sau khi nhập 3 ký tự
                    final String query = s.toString();
                    searchRunnable = () -> performNominatimSearch(query);
                    handler.postDelayed(searchRunnable, 1000); // Đợi 1 giây trước khi tìm kiếm để giảm request
                } else if (s.length() == 0) {
                    predictionTexts.clear();
                    currentPredictionObjects.clear();
                    adapter.notifyDataSetChanged();
                    tvCoordinates.setVisibility(View.GONE); // Ẩn tọa độ nếu ô trống
                    tvCoordinates.setTag(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý khi người dùng chọn một mục từ danh sách gợi ý
        addressEditText.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPredictionText = (String) parent.getItemAtPosition(position);
            addressEditText.setText(selectedPredictionText); // Đặt text của EditText

            if (position < currentPredictionObjects.size()) {
                JSONObject selectedObject = currentPredictionObjects.get(position);
                try {
                    double lat = selectedObject.getDouble("lat");
                    double lon = selectedObject.getDouble("lon");
                    LatLng latLng = new LatLng(lat, lon);
                    tvCoordinates.setText(String.format(Locale.getDefault(), "Tọa độ: %.6f, %.6f", latLng.getLatitude(), latLng.getLongitude()));
                    tvCoordinates.setTag(latLng); // Lưu tọa độ đã chọn
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

    // Thực hiện tìm kiếm địa chỉ bằng Nominatim API (OpenStreetMap)
    private void performNominatimSearch(String query) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery +
                        "&format=json&addressdetails=1&limit=10&countrycodes=vn"; // Giới hạn tìm kiếm ở Việt Nam

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("User-Agent", "BookstoreManagerApp/1.0"); // Yêu cầu User-Agent

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

                    handler.post(() -> parseNominatimResponse(jsonResponse)); // Phân tích phản hồi trên UI thread
                } else {
                    Log.e("Nominatim", "HTTP error code: " + responseCode);
                    handler.post(() -> Toast.makeText(requireContext(), "Lỗi tìm kiếm: " + responseCode, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                Log.e("Nominatim", "Network/JSON error: " + e.getMessage(), e);
                handler.post(() -> Toast.makeText(requireContext(), "Lỗi kết nối hoặc dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                // Đảm bảo đóng kết nối và reader
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

    // Phân tích phản hồi JSON từ Nominatim API
    private void parseNominatimResponse(String jsonResponse) {
        predictionTexts.clear();
        currentPredictionObjects.clear();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String displayName = obj.getString("display_name");
                predictionTexts.add(displayName);
                currentPredictionObjects.add(obj); // Lưu trữ toàn bộ object để lấy lat/lon sau
            }
            adapter.notifyDataSetChanged(); // Cập nhật danh sách gợi ý trên UI
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