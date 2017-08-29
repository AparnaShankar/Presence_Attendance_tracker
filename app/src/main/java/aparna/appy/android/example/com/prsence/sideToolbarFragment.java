package aparna.appy.android.example.com.prsence;

/**
 * Created by Administrator on 8/15/2017.
 */


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Display statistics and predictor options in the toolbar
 */
public class sideToolbarFragment extends Fragment {

    static private Context context;

    public static Fragment newInstance(Context mcontext) {
        context = mcontext;
        return new sideToolbarFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_side_toolbar_fragment, container, false);

        //Clicked on statistics image
        v.findViewById(R.id.statistics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, StatisticsActivity.class);
                startActivity(intent);
            }
        });

        //Clicked on predictor image
        v.findViewById(R.id.predictor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PredictorActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }
}
