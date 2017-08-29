package aparna.appy.android.example.com.prsence;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

/**
 * Created by Administrator on 6/26/2017.
 */

/**
 * Code for bar chart
 * Bar chart Fragment of Statistics Activity
 */

public class barFragment extends Fragment {

    private static String name[];
    private static float percent[];

    // To count the records in the database
    private static int ct;

    private static float overall_percent;
    private BarChart mChart;

    //Variable to determine which layout to inflate( no data notification or bar chart ) based on percentage values
    private int check = 0;

    public static Fragment newInstance(String mName[], float mPercent[], int n, float m_overall_percent) {

        name = mName;
        percent = mPercent;
        ct = n;
        overall_percent = m_overall_percent;

        return new barFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = null;

        //If data exists, then plot.
        if (ct != 0) {

            v = inflater.inflate(R.layout.frag_bar, container, false);

            mChart = (BarChart) v.findViewById(R.id.chart1);

            ArrayList<BarEntry> yaxis = new ArrayList<>();
            final ArrayList<String> xaxis = new ArrayList<>();

            //Setting data
            for (int i = 0, j = 0; i < ct; i++) {

                if (name[i] == null)
                    break;

                if (percent[i] == 0.0)
                    continue;

                check = 1;

                //Y axis plotting
                yaxis.add(new BarEntry(j++, percent[i]));

                //Checking the length of the name string to shorten it.
                if (name[i].length() >= 5) {
                    xaxis.add(name[i].substring(0, 4));
                } else {
                    xaxis.add(name[i]);
                }
            }

            //X axis plotting
            XAxis xAxis = mChart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xaxis));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setLabelCount(7);

            //Bar chart title and colors
            BarDataSet barDataSet = new BarDataSet(yaxis, getString(R.string.sem_att) + getString(R.string.colon) + " " + overall_percent + " " + getString(R.string.p));
            barDataSet.setDrawIcons(false);
            barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(barDataSet);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);

            mChart.setData(data);

            mChart.setDrawBarShadow(false);
            mChart.setDrawValueAboveBar(true);

            mChart.getDescription().setEnabled(false);

            // if more than 60 entries are displayed in the chart, no values will be
            // drawn
            mChart.setMaxVisibleValueCount(60);

            // scaling can now only be done on x- and y-axis separately
            mChart.setPinchZoom(false);

            mChart.setDrawGridBackground(false);

            //  mChart.setDrawYLabels(false);
            IAxisValueFormatter custom = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return value + " %";
                }
            };

            //left axis
            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setLabelCount(8, false);
            leftAxis.setValueFormatter(custom);
            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            leftAxis.setSpaceTop(15f);
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            //right axis
            YAxis rightAxis = mChart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setLabelCount(8, false);
            rightAxis.setValueFormatter(custom);
            rightAxis.setSpaceTop(15f);
            rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            //legend
            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setForm(Legend.LegendForm.SQUARE);
            l.setFormSize(9f);
            l.setTextSize(11f);
            l.setXEntrySpace(4f);

            //animation of the bar chart
            mChart.animateY(1500);

            mChart.invalidate();

            //inflate no data notification layout based on percentages = 0.0
            if (check == 0)
                v = inflater.inflate(R.layout.no_data, container, false);


        } else {

            //inflate no data notification layout based on no records
            v = inflater.inflate(R.layout.no_data, container, false);
        }

        return v;
    }
}
