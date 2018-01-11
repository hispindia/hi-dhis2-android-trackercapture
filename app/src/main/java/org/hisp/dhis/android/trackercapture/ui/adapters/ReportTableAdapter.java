package org.hisp.dhis.android.trackercapture.ui.adapters;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.trackercapture.R;
import org.hisp.dhis.android.trackercapture.activities.HolderActivity;
import org.hisp.dhis.android.trackercapture.fragments.search.OnlineSearchFragment;
import org.hisp.dhis.android.trackercapture.reports.ReportRowModel;

import java.util.List;

/**
 * Created by xelvias on 11/22/17.
 */

public class ReportTableAdapter {
    List<ReportRowModel> models;
    TableLayout table;
    Activity activity;
    String program;
    String orgunit;

    public ReportTableAdapter(Activity activity, TableLayout table, List<ReportRowModel> rowModels,
                String program,String orgUnit){
        this.models = rowModels;
        this.activity = activity;
        this.table = table;
        this.program = program;
        this.orgunit = orgUnit;

    }

    public void addRows(){

        for(int i=0;i<models.size();i++){

            table.addView(getTableRow(i));

        }
    }

    public TableRow getTableRow(int position){
        if(position>models.size())return null;
        final ReportRowModel model = models.get(position);
        TableRow row = (TableRow) LayoutInflater.from(activity.getApplicationContext())
                .inflate(R.layout.household_report_table_row,table,false);
        //row.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        TextView txt_household_sin = (TextView) row.findViewById(R.id.household_sin);
        ImageView formB = (ImageView) row.findViewById(R.id.form_b);
        ImageView formC = (ImageView) row.findViewById(R.id.form_c);
        ImageView formD = (ImageView) row.findViewById(R.id.form_d);
        ImageView formE = (ImageView) row.findViewById(R.id.form_e);
        ImageView formF = (ImageView) row.findViewById(R.id.form_f);
        ImageView formG1 = (ImageView) row.findViewById(R.id.form_g1);
        ImageView formG2 = (ImageView) row.findViewById(R.id.form_g2);

        //set values
        txt_household_sin.setText(model.getSin());
        formB.setImageResource(model.isFormB()?R.drawable.ic_check_black_18px:R.drawable.ic_close_black_18px);
        formC.setImageResource(model.isFormC()?R.drawable.ic_check_black_18px:R.drawable.ic_close_black_18px);
        formD.setImageResource(model.isFormD()?R.drawable.ic_check_black_18px:R.drawable.ic_close_black_18px);
        formE.setImageResource(model.isFormE()?R.drawable.ic_check_black_18px:R.drawable.ic_close_black_18px);
        formF.setImageResource(model.isFormF()?R.drawable.ic_check_black_18px:R.drawable.ic_close_black_18px);
        formG1.setImageResource(model.isFormG1()?R.drawable.ic_check_black_18px:R.drawable.ic_close_black_18px);
        formG2.setImageResource(model.isFormG2()?R.drawable.ic_check_black_18px:R.drawable.ic_close_black_18px);
        if(position%2==0){
            row.setBackgroundColor(Color.LTGRAY);
        }
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HolderActivity.navigateToProgramOverviewFragment(activity,orgunit,program,model.getTrackedEntityInstanceIdLocal());
            }
        });
        return row;
    }
}
