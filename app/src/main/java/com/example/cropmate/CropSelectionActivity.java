package com.example.cropmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CropSelectionActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private RecyclerView rvCrops;
    private List<Crop> cropList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_selection);

        rvCrops = findViewById(R.id.rv_crops);
        rvCrops.setLayoutManager(new GridLayoutManager(this, 2));

        boolean isManual = getIntent().getBooleanExtra("is_manual", false);

        initCrops();
        CropAdapter adapter = new CropAdapter(cropList, crop -> {
            if (isManual) {
                Intent intent = new Intent(CropSelectionActivity.this, AnalysisResultActivity.class);
                intent.putExtra("crop_name", crop.getName());
                intent.putExtra("N", getIntent().getIntExtra("manual_n", 0));
                intent.putExtra("P", getIntent().getIntExtra("manual_p", 0));
                intent.putExtra("K", getIntent().getIntExtra("manual_k", 0));
                startActivity(intent);
            } else {
                Intent intent = new Intent(CropSelectionActivity.this, RoverConnectionActivity.class);
                intent.putExtra("crop_name", crop.getName());
                intent.putExtra("crop_depth", crop.getDepthCm());
                intent.putExtra("crop_reason", crop.getReason());
                startActivity(intent);
            }
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        rvCrops.setAdapter(adapter);
    }

    private void initCrops() {
        cropList = new ArrayList<>();
        cropList.add(new Crop("Matta Gulla", R.drawable.crop_matta_gulla, 30, "", "Shallow to medium root system; nutrients absorbed mainly from topsoil."));
        cropList.add(new Crop("Paddy", R.drawable.crop_paddy, 15, "", "Nutrient availability is highest in the top flooded puddled layer."));
        cropList.add(new Crop("Ragi", R.drawable.crop_ragi, 30, "", "Medium rooting depth; depends strongly on surface nutrients."));
        cropList.add(new Crop("Sugarcane", R.drawable.crop_sugarcane, 45, "", "Deep-rooted crop with high nutrient and water requirement."));
        cropList.add(new Crop("Cotton", R.drawable.crop_cotton, 30, "", "Tap root goes deep, but nutrient uptake zone is top 30 cm."));
        cropList.add(new Crop("Coconut", R.drawable.crop_coconut, 50, "", "Feeder roots of this perennial crop are mainly in the upper 50 cm."));
        cropList.add(new Crop("Arecanut", R.drawable.crop_arecanut, 45, "", "Fibrous root system; nutrient absorption mostly from upper layers."));
        cropList.add(new Crop("Coffee", R.drawable.crop_coffee, 30, "", "Shallow feeder roots; soil fertility in topsoil is critical."));
        cropList.add(new Crop("Tomato", R.drawable.crop_tomato, 30, "", "Medium root depth; most nutrients absorbed from topsoil."));
        cropList.add(new Crop("Jasmine", R.drawable.crop_jasmine, 30, "", "Shallow to medium roots; responds strongly to surface nutrients."));
    }

    private static class CropAdapter extends RecyclerView.Adapter<CropAdapter.ViewHolder> {
        private final List<Crop> crops;
        private final OnCropClickListener listener;

        public interface OnCropClickListener {
            void onCropClick(Crop crop);
        }

        public CropAdapter(List<Crop> crops, OnCropClickListener listener) {
            this.crops = crops;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Crop crop = crops.get(position);
            holder.tvName.setText(crop.getName());
            
            // Professional Glide Loading
            Glide.with(holder.itemView.getContext())
                    .load(crop.getImageUrl())
                    .placeholder(crop.getImageResId())
                    .error(crop.getImageResId())
                    .centerCrop()
                    .into(holder.ivCrop);

            holder.itemView.setOnClickListener(v -> listener.onCropClick(crop));
        }

        @Override
        public int getItemCount() { return crops.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCrop;
            TextView tvName;
            ViewHolder(View itemView) {
                super(itemView);
                ivCrop = itemView.findViewById(R.id.iv_crop);
                tvName = itemView.findViewById(R.id.tv_crop_name);
            }
        }
    }
}
