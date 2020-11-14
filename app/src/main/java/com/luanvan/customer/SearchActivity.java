package com.luanvan.customer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

public class SearchActivity extends AppCompatActivity {

  private ImageButton ibBack;
  private EditText etSearch;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);

    ibBack = findViewById(R.id.ibBack);
    etSearch = findViewById(R.id.etSearch);

    ibBack.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
    // show keyboard input as default //////////////////////////////////////////////////////////////////////////
    etSearch.requestFocus();
    final InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    // set up to hide keyboard /////////////////////////////////////////////////////////////////////////////////
    setupUI(findViewById(R.id.layoutSearch));
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  }
  public static void hideSoftKeyboard(Activity activity){
    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
  }
  public void setupUI(View view){
    if (!(view instanceof EditText)){
      view.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          hideSoftKeyboard(SearchActivity.this);
          return false;
        }
      });
    }
    if (view instanceof ViewGroup){
      for (int i=0; i<((ViewGroup) view).getChildCount(); i++){
        View innerView = ((ViewGroup) view).getChildAt(i);
        setupUI(innerView);
      }
    }
  }
}