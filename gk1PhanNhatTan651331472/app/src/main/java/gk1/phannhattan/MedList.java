package gk1.phannhattan; // Giữ nguyên package của bạn

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MedList extends AppCompatActivity {

    RecyclerView rvMedList;
    MedicineAdapter adapter;
    List<Medicine> medicineData;
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh Sách Bài Thuốc");
        }

        // Ánh xạ RecyclerView
        rvMedList = findViewById(R.id.rvThuoc);

        prepareData();

        adapter = new MedicineAdapter(this, medicineData);

        rvMedList.setLayoutManager(new LinearLayoutManager(this));
        rvMedList.setAdapter(adapter);
        rvMedList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        btnQuayLaiMain = findViewById(R.id.btnQuayLaiMain);

        btnQuayLaiMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MedList.this, MainActivity.class));
            }
        });
    }

    private void prepareData() {
        medicineData = new ArrayList<>();
        medicineData.add(new Medicine(
                "Bài thuốc 1: Trị cảm cúm",
                "Sáng, Tối",
                R.drawable.placeholder,
                "Nội dung chi tiết bài thuốc trị cảm cúm: Gừng, tỏi, mật ong..."
        ));
        medicineData.add(new Medicine(
                "Bài thuốc 2: Trị ho",
                "Sau bữa ăn",
                R.drawable.placeholder,
                "Nội dung chi tiết bài thuốc trị ho: Húng chanh, quất hấp đường phèn..."
        ));
        medicineData.add(new Medicine(
                "Bài thuốc 3: Bổ gan",
                "Trưa",
                R.drawable.placeholder,
                "Nội dung chi tiết bài thuốc bổ gan: Cà gai leo, atiso..."
        ));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    //adapter
    public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

        private Context context;
        private List<Medicine> items;

        //constructor
        public MedicineAdapter(Context context, List<Medicine> items) {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
            return new MedicineViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
            Medicine med = items.get(position);
            holder.tvTieuDe.setText(med.getTieuDe());
            holder.tvThoiGian.setText(med.getThoiGian());
            holder.imgAnhDaiDien.setImageResource(med.getAnhDaiDien());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class MedicineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView imgAnhDaiDien;
            TextView tvTieuDe;
            TextView tvThoiGian;

            public MedicineViewHolder(@NonNull View itemView) {
                super(itemView);
                imgAnhDaiDien = itemView.findViewById(R.id.imgAnhDaiDien);
                tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
                tvThoiGian = itemView.findViewById(R.id.tvThoiGian);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Medicine clickedMed = items.get(position);

                    Intent intent = new Intent(context, ChiTietMedicineActivity.class);

                    intent.putExtra("TIEU_DE", clickedMed.getTieuDe());
                    intent.putExtra("NOI_DUNG", clickedMed.getNoiDung());

                    context.startActivity(intent);
                }
            }
        }
    }
}