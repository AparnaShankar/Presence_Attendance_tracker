package aparna.appy.android.example.com.prsence;

/**
 * Created by Administrator on 8/15/2017.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Used by SemesterActivity
 */
public class ToolbarAdapter extends FragmentPagerAdapter {

    private Context context;
    private int t = 0;
    private int a = 0;
    private float p = 0.0f;

    //Constructor
    public ToolbarAdapter(FragmentManager fm, Context c, int ot, int oa, float percent) {
        super(fm);
        context = c;
        t = ot;
        a = oa;
        p = percent;
    }

    //Link fragments to positions
    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        Fragment f = null;

        switch (position) {
            case 0:
                f = mainToolbarFragment.newInstance(context, t, a, p);
                break;
            case 1:
                f = sideToolbarFragment.newInstance(context);
                break;
        }
        return f;
    }

    //No of fragments
    @Override
    public int getCount() {
        return 2;
    }
}
