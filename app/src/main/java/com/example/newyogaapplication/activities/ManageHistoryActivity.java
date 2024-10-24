package com.example.newyogaapplication.activities;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.adapters.YogaHistoryAdapter;
import com.example.newyogaapplication.classes.HistoryItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private YogaHistoryAdapter adapter;
    private List<HistoryItem> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_history);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách và adapter
        historyList = new ArrayList<>();
        adapter = new YogaHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        // Tải dữ liệu lịch sử từ Firebase
        loadHistoryData();
    }

    // Phương thức tải dữ liệu lịch sử từ Firebase
    private void loadHistoryData() {
        DatabaseReference historyRef = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("history");

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                historyList.clear();  // Xóa danh sách cũ
                Log.d("FirebaseData", "Number of users: " + dataSnapshot.getChildrenCount());

                // Duyệt qua từng UserId
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Log.d("FirebaseUser", "Loading history for UserId: " + userSnapshot.getKey());

                    // Duyệt qua từng lịch sử của UserId
                    for (DataSnapshot historySnapshot : userSnapshot.getChildren()) {
                        HistoryItem historyItem = historySnapshot.getValue(HistoryItem.class);
                        if (historyItem != null) {
                            Log.d("HistoryItem", "Loaded: " + historyItem.getClassName() + ", Total: " + historyItem.getTotalPrice());
                            historyList.add(historyItem);
                        } else {
                            Log.e("HistoryItemError", "Error loading history item.");
                        }
                    }
                }
                adapter.notifyDataSetChanged();  // Cập nhật RecyclerView sau khi dữ liệu thay đổi
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error loading data from Firebase.", databaseError.toException());
            }
        });
    }
}
