package com.luanvan.customer.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.luanvan.customer.Fragments.HomeFragment;
import com.luanvan.customer.Fragments.MeFragment;
import com.luanvan.customer.Fragments.OrderFragment;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {
  public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
    super(fragmentManager, lifecycle);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {

    switch (position){
      case 0: return new HomeFragment();
      case 1: return new OrderFragment();
      case 2: return new MeFragment();
    }
    return null;
  }

  @Override
  public int getItemCount() {
    return 3;
  }
}
