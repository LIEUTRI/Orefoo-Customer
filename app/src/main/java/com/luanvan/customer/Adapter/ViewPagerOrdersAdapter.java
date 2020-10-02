package com.luanvan.customer.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.luanvan.customer.Fragments.HistoryFragment;
import com.luanvan.customer.Fragments.OngoingFragment;

public class ViewPagerOrdersAdapter extends FragmentStateAdapter {
    public ViewPagerOrdersAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new OngoingFragment();
            case 1: return new HistoryFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
