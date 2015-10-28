package com.example.siddardha.testingfragmenttutorial;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ActivityWithTutorialFragmentFragment extends Fragment {

    public ActivityWithTutorialFragmentFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Picasso.
        return inflater.inflate(R.layout.fragment_activity_with_tutorial, container, false);

    }
}
