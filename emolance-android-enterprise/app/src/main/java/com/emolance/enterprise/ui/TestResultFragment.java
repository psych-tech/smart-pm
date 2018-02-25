package com.emolance.enterprise.ui;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.LevelResult;
import com.emolance.enterprise.data.Tip;
import com.emolance.enterprise.util.Constants;
import com.emolance.enterprise.util.DateUtils;
import com.emolance.enterprise.util.ResultManager;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestResultFragment extends Fragment {

    @InjectView(R.id.send_btn)
    Button sendButton;
    @InjectView(R.id.backButtonResultScreen)
    ImageButton backButton;
    @InjectView(R.id.resultName)
    TextView resultName;
    @InjectView(R.id.resultDate)
    TextView resultDate;
    @InjectView(R.id.resultDescription)
    TextView resultDescription;
    @InjectView(R.id.tipListContainer)
    LinearLayout tipsListContainer;
    @InjectView(R.id.levelImage)
    ImageView levelImage;
    @InjectView(R.id.resultColorOne)
    View resultColorOne;
    @InjectView(R.id.resultColorTwo)
    View resultColorTwo;
    @InjectView(R.id.resultScrollView)
    ScrollView resultScrollView;
    @InjectView(R.id.resultError)
    ImageView resultErrorImage;
    private int level;
    private int valOne;
    private int valTwo;
    private int color;
    private String date;
    private String status;


    public TestResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_result, container, false);
        ButterKnife.inject(this,rootView);
        level = getArguments().getInt(Constants.RESULT_LEVEL,-1);
        valOne = getArguments().getInt(Constants.RESULT_VALONE,-1);
        valTwo = getArguments().getInt(Constants.RESULT_VALTWO,-1);
        color = getArguments().getInt(Constants.RESULT_COLOR, -1);
        date = getArguments().getString(Constants.RESULT_DATE,"");
        status = getArguments().getString(Constants.RESULT_STATUS, "");
        setLayout();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        // Inflate the layout for this fragment
        return rootView;
    }

    private void setLayout(){
        if(status.equals(getActivity().getString(R.string.test_reports_user_profile_done)) &&
                level >= 1 && level <= 6){
            LevelResult result =  ResultManager.getInstance(getActivity()).getLevelResult(level);
            String description = result.getDescription();
            String name = result.getName();
            int percentage = result.getPercentile();
            resultName.setText(name);
            resultDescription.setText(description);
            String formattedDate = DateUtils.getDateInMMDDYYYYFormat(date);
            if(formattedDate == null || formattedDate.equals("")){
                resultDate.setText(getActivity().getResources().getString(R.string.test_reports_user_date_unknown));
            }
            else{
                resultDate.setText(formattedDate);
            }
            List<Tip> tips = result.getTips();
            TipsListAdapter adapter = new TipsListAdapter(tips, getActivity());
            for(int i = 0; i < adapter.getCount(); i++){
                View item = adapter.getView(i,null,null);
                tipsListContainer.addView(item);
            }
            resultColorOne.setBackgroundColor(color);
            resultColorTwo.setBackgroundColor(color);
            BitmapDrawable drawable = (BitmapDrawable) levelImage.getDrawable();
            Bitmap bm = drawable.getBitmap();
            Bitmap circleBm = BitmapFactory.decodeResource(getResources(),R.drawable.blue_circle);
            int circleWidth = circleBm.getWidth();
            int circleHeight = circleBm.getHeight();
            Log.i("TEST", "w: " + bm.getWidth() + " h: " + bm.getHeight());
            int cLevel = valOne;
            int dLevel = valTwo;
            int yOffset = (cLevel - 2) * 100;
            int xOffset = (dLevel - 2) * 100;

            Bitmap bmOverlay = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
            Canvas canvas = new Canvas(bmOverlay);

            canvas.drawBitmap(bm, new Matrix(), null);
            canvas.drawBitmap(circleBm,
                    bm.getWidth() / 2 - circleWidth / 2 + xOffset,
                    bm.getHeight() / 2 - circleHeight / 2 + yOffset,
                    null);
            levelImage.setImageBitmap(bmOverlay);
        }
        else{
            resultScrollView.setVisibility(View.GONE);
            resultErrorImage.setVisibility(View.VISIBLE);
        }
    }


}
