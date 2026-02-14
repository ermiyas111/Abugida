package com.example.abugida;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.abogida.R;
import android.os.Handler;
import android.os.Looper;

public class HomeFragment extends Fragment {

    private static final long CAROUSEL_DELAY_MS = 3000;
    private final Handler carouselHandler = new Handler(Looper.getMainLooper());
    private Runnable carouselRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager2 pager = view.findViewById(R.id.home_image_pager);

        int[] images = new int[] {
                R.drawable.home_preview_light,
                R.drawable.home_preview_dark
        };
        pager.setAdapter(new HomeImageAdapter(requireContext(), images));
        pager.setOffscreenPageLimit(images.length);

        carouselRunnable = () -> {
            if (pager.getAdapter() == null) {
                return;
            }
            int count = pager.getAdapter().getItemCount();
            if (count <= 1) {
                return;
            }
            int next = (pager.getCurrentItem() + 1) % count;
            pager.setCurrentItem(next, true);
            carouselHandler.postDelayed(carouselRunnable, CAROUSEL_DELAY_MS);
        };
        carouselHandler.postDelayed(carouselRunnable, CAROUSEL_DELAY_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        carouselHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (carouselRunnable != null) {
            carouselHandler.postDelayed(carouselRunnable, CAROUSEL_DELAY_MS);
        }
    }
}
