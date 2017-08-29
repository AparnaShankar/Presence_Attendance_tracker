package aparna.appy.android.example.com.prsence;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

/**
 * Created by Administrator on 6/26/2017.
 * Display pie chart
 */

public class pieFragment extends Fragment {

    private static String name[];
    private static float percent[];
    private static int ct;
    private PieChart mChart;
    private Typeface tf;
    private int check = 0;

    public static Fragment newInstance(String mName[], float mPercent[], int n) {

        name = mName;
        percent = mPercent;
        ct = n;

        return new pieFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = null;

        //Conditions for inflating different layout based on the data available to plot
        if (ct != 0) {

            v = inflater.inflate(R.layout.frag_simple_pie, container, false);

            mChart = (PieChart) v.findViewById(R.id.pieChart);
            mChart.getDescription().setEnabled(false);

            tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");

            mChart.setCenterTextTypeface(tf);
            mChart.setCenterText(generateCenterText());
            mChart.setCenterTextSize(10f);
            mChart.setCenterTextTypeface(tf);


            mChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

            mChart.setDrawHoleEnabled(true);
            mChart.setHoleColor(Color.WHITE);

            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setTransparentCircleAlpha(110);

            // radius of the center hole in percent of maximum radius
            mChart.setHoleRadius(58f);
            mChart.setTransparentCircleRadius(61f);

            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);

            mChart.setData(generatePieData());
            mChart.setUsePercentValues(true);

            if (check == 0)
                v = inflater.inflate(R.layout.no_data, container, false);

            mChart.animateY(1500);

            mChart.invalidate();
        } else {
            v = inflater.inflate(R.layout.no_data, container, false);
        }

        return v;
    }

    private PieData generatePieData() {

        int count = name.length;

        ArrayList<PieEntry> entries1 = new ArrayList<PieEntry>();

        for (int i = 0; i < count; i++) {
            if (name[i] == null)
                break;
            if (percent[i] == 0.0)
                continue;
            check = 1;
            if (name[i].length() >= 5) {
                entries1.add(new PieEntry(percent[i], name[i].substring(0, 4)));
            } else {
                entries1.add(new PieEntry(percent[i], name[i]));
            }

        }

        PieDataSet ds1 = new PieDataSet(entries1, getString(R.string.pieChartName));
        ds1.setColors(ColorTemplate.PASTEL_COLORS);
        ds1.setValueFormatter(new PercentFormatter());
        ds1.setSliceSpace(2f);
        ds1.setValueTextColor(Color.WHITE);
        ds1.setValueTextSize(12f);

        ds1.setValueLinePart1OffsetPercentage(80.f);
        ds1.setValueLinePart1Length(0.2f);
        ds1.setValueLinePart2Length(0.4f);
        //dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        ds1.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(ds1);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        data.setValueTypeface(tf);

        return data;
    }

    private SpannableString generateCenterText() {
        SpannableString s = new SpannableString(getString(R.string.pieName));
        s.setSpan(new RelativeSizeSpan(2f), 0, 12, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 12, s.length(), 0);
        return s;
    }
}
