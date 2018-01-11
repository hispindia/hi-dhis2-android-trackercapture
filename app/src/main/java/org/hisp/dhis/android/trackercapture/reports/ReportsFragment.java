package org.hisp.dhis.android.trackercapture.reports;


import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.persistence.models.Constant;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Relationship;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.ui.activities.OnBackPressedListener;
import org.hisp.dhis.android.trackercapture.R;
import org.hisp.dhis.android.trackercapture.activities.HolderActivity;
import org.hisp.dhis.android.trackercapture.ui.adapters.ReportTableAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xelvias on 11/22/17.
 */

public class ReportsFragment extends Fragment implements OnBackPressedListener{
    static final String programID = "FcRm8N8glra";//program id for Household  Member Assessment

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.report_single_entity,container,false);
        View view = inflater.inflate(R.layout.fragment_report,container,false);
        if(getActionBar() != null) {
            getActionBar().setTitle("Reports");
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        List<ReportSingleEntityModel> singleEntityModels = new ArrayList<>();
/*
        for(int j=0;j<4;j++){
            String id = "household SIN "+j;
            ReportSingleEntityModel m = new ReportSingleEntityModel(id);
            for(int i=0;i<3;i++){
                ReportRowModel model = new ReportRowModel();
                model.setSin("ID X djllds "+i);
                model.setFormC(true);
                model.setFormD(false);
                model.setFormE(true);
                model.setFormF(false);
                model.setFormG(false);
                m.getRows().add(model);
            }
            singleEntityModels.add(m);

        }*/
        singleEntityModels = getData();
        TextView totalCount = (TextView) view.findViewById(R.id.household_count);
        totalCount.setText(singleEntityModels.size()+" Entries" );
        ListView listView = (ListView)view.findViewById(R.id.household_report_list);
        //listView.addView(getSingleEntityReport(inflater,container,m));
        ListAdapter listAdapter = new ReportListAdapter(singleEntityModels);
        listView.setAdapter(listAdapter);

        return view;
    }


    public View getSingleEntityReport(LayoutInflater inflater,ViewGroup container
            ,ReportSingleEntityModel reportSingleEntityModel){
        View view = inflater.inflate( R.layout.report_single_entity,container,false);
        TableLayout table = (TableLayout) view.findViewById(R.id.household_report_table);
        TextView householdSIN = (TextView) table.findViewById(R.id.household_header);
        //householdSIN.setText(reportSingleEntityModel.getHouseHoldSIN());


        //ReportTableAdapter adapter = new ReportTableAdapter(getActivity(),table,reportSingleEntityModel.getRows());
        //adapter.addRows();
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }




    public ActionBar getActionBar() {
        if (getActivity() != null &&
                getActivity() instanceof AppCompatActivity) {
            return ((AppCompatActivity) getActivity()).getSupportActionBar();
        } else {
            throw new IllegalArgumentException("Fragment should be attached to ActionBarActivity");
        }
    }

    @Override
    public boolean doBack() {
        onDetach();
        getActivity().finish();
        return false;
    }




    class ReportListAdapter implements ListAdapter {
        List<ReportSingleEntityModel> models;


        ReportListAdapter(List<ReportSingleEntityModel> models){
            this.models = models;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            //do nothing
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            //do nothing
        }

        @Override
        public int getCount() {
            return models.size();
        }

        @Override
        public Object getItem(int i) {
            return models.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getActivity().getLayoutInflater().inflate( R.layout.report_single_entity,null,true);
            TableLayout table = (TableLayout) view.findViewById(R.id.household_report_table);
            TextView householdSIN = (TextView) view.findViewById(R.id.household_header);
            ReportSingleEntityModel reportSingleEntityModel = (ReportSingleEntityModel) getItem(i);
            householdSIN.setText(reportSingleEntityModel.getHouseHoldSIN());
            String logedInOrgunit = MetaDataController.getAssignedOrganisationUnits().get(0).getId();

            ReportTableAdapter adapter = new ReportTableAdapter(getActivity()
                    ,table,reportSingleEntityModel.getRows(),
                    programID,logedInOrgunit
                    );
            adapter.addRows();

            return view;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }



    public List<ReportSingleEntityModel> getData(){

        HashMap<String,List<TrackedEntityInstance>> mapTrackedEntityInstances = new HashMap<>();
        String logedInOrgunit = MetaDataController.getAssignedOrganisationUnits().get(0).getId();

        List<TrackedEntityInstance> trackedEntityInstances = TrackerController.getTrackedEntityInstances(logedInOrgunit);
        for(TrackedEntityInstance tei:trackedEntityInstances){
            if(tei.getRelationships()!=null && tei.getRelationships().size()>0){
                for(Relationship relationship :tei.getRelationships()){
                    String key = relationship.getTrackedEntityInstanceB();
                    if(!mapTrackedEntityInstances.containsKey(key)) {
                        mapTrackedEntityInstances.put(key, new ArrayList<TrackedEntityInstance>());
                    }
                    mapTrackedEntityInstances.get(key).add(tei);

                }
            }
        }

        List<ReportSingleEntityModel> singleEntityModels = new ArrayList<>();

        for(String key:mapTrackedEntityInstances.keySet()){
            List<TrackedEntityInstance> teiList = mapTrackedEntityInstances.get(key);
            TrackedEntityAttributeValue attributeValue = TrackerController
                    .getTrackedEntityAttributeValue("d4w0DR8LnEZ",key);//atribute for SIN
            String val = attributeValue==null?"null":attributeValue.getValue();
            ReportSingleEntityModel singleEntityModel =
                    new ReportSingleEntityModel(val);


            for(TrackedEntityInstance tei:teiList) {
                ReportRowModel row  = new ReportRowModel();
                row.setTrackedEntityInstanceIdLocal(tei.getLocalId());
                TrackedEntityAttributeValue trackedEntityAttributeValue  =TrackerController.getTrackedEntityAttributeValue("d4w0DR8LnEZ",tei.getTrackedEntityInstance() );
                row.setSin(trackedEntityAttributeValue==null?"null":trackedEntityAttributeValue.getValue());
                Enrollment enrollment = TrackerController.getLastEnrollment(programID,tei);//, logedInOrgunit);
                if(enrollment!=null) {
                    List<Event> events = TrackerController.getEventsByEnrollment(enrollment.getEnrollment());//enrollment.getEvents();
                    if(events!=null)
                        for (Event event : events) {
                        switch (event.getProgramStageId()) {

                            case "Jdp0p9vqqrK":
                                row.setFormB(event.getStatus().equals("COMPLETED"));
                                break;
                            case "WdjuUKQtWIA":
                                row.setFormC(event.getStatus().equals("COMPLETED"));
                                break;

                            case "pGsWKvoPrWD":
                                row.setFormD(event.getStatus().equals("COMPLETED"));
                                break;

                            case "KsU7V32yLmR":
                                row.setFormE(event.getStatus().equals("COMPLETED"));
                                break;

                            case "aalKtfYAAGW":
                                row.setFormF(event.getStatus().equals("COMPLETE"));
                                break;

                            case "hEEnzI6agUl":
                                row.setFormG1(event.getStatus().equals("COMPLETED"));
                                break;

                            case "DCli6WFSiGK":
                                row.setFormG2(event.getStatus().equals("COMPLETED"));
                                break;

                                default: break;
                        }
                    }
                    singleEntityModel.getRows().add(row);
                }else{

                }

            }
            singleEntityModels.add(singleEntityModel);
        }

        return singleEntityModels;




    }


}
