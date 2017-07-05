package com.example.android.obscured;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.example.android.obscured.DatabaseUtilities.ImageDetailFragment;

/**
 * Created by Shubham on 30-06-2017.
 */

public class MainActivity extends AppCompatActivity implements MainActivityFragment.onImageClickListener{

    final int MY_PERMISSIONS_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE = 1;
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE);

            /*
            ActivityCompat.requestPermissions(this,
                    new String[]{},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);*/
        }
        else
        {
            if (findViewById(R.id.fragment_container) != null) {

                MainActivityFragment firstFragment = MainActivityFragment.createFragment(this);
                firstFragment.setImageClickListener(this);
                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, firstFragment).commit();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_READ_AND_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (findViewById(R.id.fragment_container) != null) {

                        MainActivityFragment firstFragment = MainActivityFragment.createFragment(this);
                        firstFragment.setImageClickListener(this);
                        // Add the fragment to the 'fragment_container' FrameLayout
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.fragment_container, firstFragment).commit();
                    }

                }
                else finish();

                break;
            }
        }
    }

    @Override
    public void onImageClicked(String imagePath) {

        Fragment imageDetailFragment = new ImageDetailFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Bundle args = new Bundle();
        args.putString(getString(R.string.image_path_key), imagePath);
        imageDetailFragment.setArguments(args);

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        float dpWidth  = outMetrics.widthPixels / density;

        if(dpWidth < 600 )
        {
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.fragment_container, imageDetailFragment);
        }
        else
        {
            fragmentTransaction.replace(R.id.frag_single_activity, imageDetailFragment);
        }


        fragmentTransaction.commit();

    }
/*
    @Override
    public void onArticleSelected(String movieClicked, boolean dualPane) {




    }
    */
}
