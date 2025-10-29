package gk1.phannhattan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    RecyclerView rvFoodList;
    FoodAdapter adapter;
    List<String> foodData;
    Button btnQuayLaiMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        rvFoodList = findViewById(R.id.rvMonAn);

        prepareData();

        adapter = new FoodAdapter(this, foodData);

        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
        rvFoodList.setAdapter(adapter);

        btnQuayLaiMain = findViewById(R.id.btnQuayLaiMain);

        btnQuayLaiMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void prepareData() {
        foodData = new ArrayList<>();
        foodData.add("Phở Bò");
        foodData.add("Bún Chả");
        foodData.add("Bánh Mì");
        foodData.add("Cơm Tấm");
    }
    public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

        private List<String> items;
        private Context context;
        private LayoutInflater inflater;

        public FoodAdapter(Context context, List<String> items) {
            this.context = context;
            this.items = items;
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_food, parent, false);
            return new FoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
            String foodName = items.get(position);
            holder.tvFoodName.setText(foodName);
        }

        @Override
        public int getItemCount() {

            return items.size();
        }
        public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView tvFoodName;

            public FoodViewHolder(@NonNull View itemView) {
                super(itemView);

                tvFoodName = itemView.findViewById(R.id.tvMonAn);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    String clickedFood = items.get(position);
                    Toast.makeText(context, "Bạn đã chọn: " + clickedFood, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}