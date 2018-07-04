/*
 *  Copyright (c) 2016, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.trackercapture.fragments.enrollment;

import android.content.Context;
import android.util.Log;

import org.hisp.dhis.android.sdk.controllers.GpsController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.loaders.Query;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.Constant;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttribute;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeGeneratedValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowFactory;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.autocompleterow.AutoCompleteRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.CheckBoxRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DatePickerRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.EditTextRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.EnrollmentDatePickerRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.IncidentDatePickerRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.RadioButtonsRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RefreshListViewEvent;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.hisp.dhis.android.sdk.utils.api.ValueType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class EnrollmentDataEntryFragmentQuery implements Query<EnrollmentDataEntryFragmentForm> {
    public static final String CLASS_TAG = EnrollmentDataEntryFragmentQuery.class.getSimpleName();
    private static String appliedValue;
    private final String mOrgUnitId;
    private final String mProgramId;
    private final long mTrackedEntityInstanceId;
    private final String enrollmentDate;
    private String incidentDate;
    private String  DOHID="yeI6AOQjrqg";
    private String  SETTLEMENTID="iRJYMHN0Rel";
    private TrackedEntityInstance currentTrackedEntityInstance;
    private Enrollment currentEnrollment;
    private List<OrganisationUnit> assignedOrganisationUnits;
    private UserAccount userAccounts;



    List<String> fieldsToDisable = new ArrayList<String>(
            Arrays.asList(DOHID,SETTLEMENTID));
    EnrollmentDataEntryFragmentQuery(String mOrgUnitId, String mProgramId,
                                     long mTrackedEntityInstanceId,
                                     String enrollmentDate, String incidentDate) {
        this.mOrgUnitId = mOrgUnitId;
        this.mProgramId = mProgramId;
        this.mTrackedEntityInstanceId = mTrackedEntityInstanceId;
        this.enrollmentDate = enrollmentDate;
        this.incidentDate = incidentDate;
        appliedValue =null;

    }

    @Override
    public EnrollmentDataEntryFragmentForm query(Context context) {
        EnrollmentDataEntryFragmentForm mForm = new EnrollmentDataEntryFragmentForm();
        final Program mProgram = MetaDataController.getProgram(mProgramId);
        final OrganisationUnit mOrgUnit = MetaDataController.getOrganisationUnit(mOrgUnitId);

        if (mProgram == null || mOrgUnit == null) {
            return mForm;
        }

        if (mTrackedEntityInstanceId < 0) {
            currentTrackedEntityInstance = new TrackedEntityInstance(mProgram, mOrgUnitId);
        } else {
            currentTrackedEntityInstance = TrackerController.getTrackedEntityInstance(mTrackedEntityInstanceId);
        }
        if ("".equals(incidentDate)) {
            incidentDate = null;
        }
        currentEnrollment = new Enrollment(mOrgUnitId, currentTrackedEntityInstance.getTrackedEntityInstance(), mProgram, enrollmentDate, incidentDate);

        mForm.setProgram(mProgram);
        mForm.setOrganisationUnit(mOrgUnit);
        mForm.setDataElementNames(new HashMap<String, String>());
        mForm.setDataEntryRows(new ArrayList<Row>());
        mForm.setTrackedEntityInstance(currentTrackedEntityInstance);
        mForm.setTrackedEntityAttributeValueMap(new HashMap<String, TrackedEntityAttributeValue>());

        List<TrackedEntityAttributeValue> trackedEntityAttributeValues = new ArrayList<>();
        List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes = mProgram.getProgramTrackedEntityAttributes();
        List<Row> dataEntryRows = new ArrayList<>();

        dataEntryRows.add(new EnrollmentDatePickerRow(currentEnrollment.getProgram().getEnrollmentDateLabel(), currentEnrollment));

        if (currentEnrollment.getProgram().getDisplayIncidentDate()) {
            dataEntryRows.add(new IncidentDatePickerRow(currentEnrollment.getProgram().getIncidentDateLabel(), currentEnrollment));
        }

        for (ProgramTrackedEntityAttribute ptea : programTrackedEntityAttributes) {
            TrackedEntityAttributeValue value = TrackerController.getTrackedEntityAttributeValue(ptea.getTrackedEntityAttributeId(), currentTrackedEntityInstance.getLocalId());
            if (value != null) {
                trackedEntityAttributeValues.add(value);
            } else {
                TrackedEntityAttribute trackedEntityAttribute = MetaDataController.getTrackedEntityAttribute(ptea.getTrackedEntityAttributeId());
                if (trackedEntityAttribute.isGenerated()) {
                    TrackedEntityAttributeGeneratedValue trackedEntityAttributeGeneratedValue =
                            MetaDataController.getTrackedEntityAttributeGeneratedValue(ptea.getTrackedEntityAttribute());

                    if (trackedEntityAttributeGeneratedValue != null) {
                        TrackedEntityAttributeValue trackedEntityAttributeValue = new TrackedEntityAttributeValue();
                        trackedEntityAttributeValue.setTrackedEntityAttributeId(ptea.getTrackedEntityAttribute().getUid());
                        trackedEntityAttributeValue.setTrackedEntityInstanceId(currentTrackedEntityInstance.getUid());
                        trackedEntityAttributeValue.setValue(trackedEntityAttributeGeneratedValue.getValue());
                        trackedEntityAttributeValues.add(trackedEntityAttributeValue);
                    } else {
                        mForm.setOutOfTrackedEntityAttributeGeneratedValues(true);
                    }
                }
            }
        }
        currentEnrollment.setAttributes(trackedEntityAttributeValues);
        int paddingForIndex = dataEntryRows.size();
        int ageRowIndex = -1;//added to manupulate or dynamicly change the row value based on user input for the other
        int dobRowIndex = -1;//added to manupulate or dynamicly change the row value based on user input for the other
        int settlementRowIndex = -1;//added to manupulate or dynamicly change the row value based on user input for the other
        for (int i = 0; i < programTrackedEntityAttributes.size(); i++) {
            boolean editable = true;
            boolean shouldNeverBeEdited = false;
            if(programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().isGenerated()) {
                editable = false;
                shouldNeverBeEdited = true;
            }
            //added by ifhaam to diable fields on 9-1-17

            if(fieldsToDisable.contains(programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getUid())){
                editable=false;
                shouldNeverBeEdited=true;
            }
            //change ends here

            if(ValueType.COORDINATE.equals(programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getValueType())) {
                GpsController.activateGps(context);
            }
            Row row = DataEntryRowFactory.createDataEntryView(programTrackedEntityAttributes.get(i).getMandatory(),
                    programTrackedEntityAttributes.get(i).getAllowFutureDate(), programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getOptionSet(),
                    programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getName(),
                    getTrackedEntityDataValue(programTrackedEntityAttributes.get(i).
                            getTrackedEntityAttribute().getUid(), trackedEntityAttributeValues),
                    programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getValueType(),
                    editable, shouldNeverBeEdited);

            if(row.getmLabel().equalsIgnoreCase("DOB")){
                dobRowIndex = i;
            }else if(row.getmLabel().contains("Age")){
                ageRowIndex = i;
            }
            dataEntryRows.add(row);
        }
        for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
            mForm.getTrackedEntityAttributeValueMap().put(trackedEntityAttributeValue.getTrackedEntityAttributeId(), trackedEntityAttributeValue);
        }
        mForm.setDataEntryRows(dataEntryRows);
        mForm.setEnrollment(currentEnrollment);


        // added by ifhaam 9/14/2017
        final EditTextRow ageRow = (EditTextRow) dataEntryRows.get(paddingForIndex+ageRowIndex);
        final DatePickerRow dobRow = (DatePickerRow) dataEntryRows.get(paddingForIndex+dobRowIndex);



        final String ageRowTrackedEntityAttributeUID =programTrackedEntityAttributes.get(ageRowIndex).getTrackedEntityAttribute().getUid();
        final String dobRowTrackedEntityAttribureUID = programTrackedEntityAttributes.get(dobRowIndex).getTrackedEntityAttribute().getUid();





        try{
            Dhis2Application.getEventBus().unregister(new DobAgeSync() {
                @com.squareup.otto.Subscribe
                @Override
                public void eventHandler(RowValueChangedEvent event) {

                }
            });

        }catch (Exception ex){
            ex.printStackTrace();
        }
        Dhis2Application.getEventBus().register(new DobAgeSync(){
            @Override
            @com.squareup.otto.Subscribe
            public void eventHandler(RowValueChangedEvent event){
                // Log.i(" Called ",event.getBaseValue().getValue()+"");
                if(event.getId()!=null && event.getId().equals(ageRowTrackedEntityAttributeUID)){
                    Row row = event.getRow();
                    if(appliedValue==null || !appliedValue.equals(ageRow.getValue().getValue()) ){

                        if(row!=null) {
                            //Log.i(" Called ",row.getValue().getValue());
                            try {
                                dobRow.getValue().setValue(getTheDOB(ageRow.getValue().getValue()));
                                appliedValue = dobRow.getValue().getValue();

                                EnrollmentDataEntryFragment.refreshListView();
                            } catch (Exception ex) {
                                Log.i("Exception ", "Converting to integer not possible");

                            }
                        }
                    }

                }else if(event.getId() !=null && event.getId().equals(dobRowTrackedEntityAttribureUID)){
                    if(appliedValue ==null || !(appliedValue.equals(dobRow.getValue().getValue()) || appliedValue.equals(ageRow.getValue().getValue()))){

                        try{
                            ageRow.getValue().setValue(getTheDifference(dobRow.getValue().getValue()));
                            appliedValue = dobRow.getValue().getValue();

                            EnrollmentDataEntryFragment.refreshListView();

                        }catch (Exception ex){
                            ex.printStackTrace();

                        }
                    }


                }

            }

        });
        return mForm;
    }


    /**
     * @param dateStr Pass the string value of date to the method
     * @return Get the difference from todays date
     */
    private String getTheDifference(String dateStr) throws Exception{
        Calendar today = Calendar.getInstance();
        //Log.i(" today ",today.getTime()+"");
        Calendar cal2 = Calendar.getInstance();
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");

        Date date = Calendar.getInstance().getTime();
        date = format.parse(dateStr);

        cal2.setTime(date);
        int diffYear = today.get(Calendar.YEAR)-cal2.get(Calendar.YEAR);
        int diffMonths = diffYear*12 + today.get(Calendar.MONTH)-cal2.get(Calendar.MONTH);

        int year = diffMonths/12;
        int month = diffMonths % 12;


        //Log.i(" Difference ",year+"."+month);
        return year+"."+month;
    }

    /**
     *
     * @param in pass the notation of age
     * @return get the date of birth
     * Please note since user wont provide the date difference in days
     * we cant calculate the exact date
     *
     */
    public String getTheDOB(String in){
        int indexOfPeriod = in.indexOf(".");

        int year = Integer.parseInt(in.substring(0,indexOfPeriod));
        int month = Integer.parseInt(in.substring(indexOfPeriod+1));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR,(-1*year));
        cal.add(Calendar.MONTH,(-1*month));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(cal.getTime());

    }


    public TrackedEntityAttributeValue getTrackedEntityDataValue(String trackedEntityAttribute, List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
            if (trackedEntityAttributeValue.getTrackedEntityAttributeId().equals(trackedEntityAttribute))
                return trackedEntityAttributeValue;
        }

        //for Doh id:
        TrackedEntityAttributeValue trackedEntityAttributeValue = new TrackedEntityAttributeValue();

        trackedEntityAttributeValue.setTrackedEntityAttributeId(trackedEntityAttribute);
        //@sou DOH ID Auto Sequential
        //ToDO count based on doh
        if(trackedEntityAttribute.equals(DOHID))
        {
            List<Integer> teivalues=new ArrayList<>();
            String code="";
            OrganisationUnit mOrgUnit = MetaDataController.getOrganisationUnit(mOrgUnitId);
            code=mOrgUnit.getCode();
//            List<TrackedEntityInstance> tei_list= MetaDataController.getTrackedEntityInstancesFromLocal();
//            int count=tei_list.size();
            List<TrackedEntityAttributeValue> attributeValues_list=MetaDataController.getteiValues(DOHID);
            if(attributeValues_list.size()>0)
            {
                for (TrackedEntityAttributeValue teivalue:attributeValues_list)
                {
                    teivalues.add(Integer.parseInt(teivalue.getValue().substring(9,14)));
                }
                Integer max_value=Collections.max(teivalues);
                String seq_count = String.format ("%05d", max_value+1);

                int year = Calendar.getInstance().get(Calendar.YEAR);
                String year_=String.valueOf(year);
                String nimhans_=code+"-"+year_.toString()+"-"+seq_count;
                trackedEntityAttributeValue.setTrackedEntityInstanceId(currentTrackedEntityInstance.getTrackedEntityInstance());
                trackedEntityAttributeValue.setValue(nimhans_);
                trackedEntityAttributeValues.add(trackedEntityAttributeValue);
            }
            else
            {
                int year = Calendar.getInstance().get(Calendar.YEAR);
                String year_=String.valueOf(year);
                String nimhans_=code+"-"+year_.toString()+"-"+"00001";
                trackedEntityAttributeValue.setTrackedEntityInstanceId(currentTrackedEntityInstance.getTrackedEntityInstance());
                trackedEntityAttributeValue.setValue(nimhans_);
                trackedEntityAttributeValues.add(trackedEntityAttributeValue);

            }

            return trackedEntityAttributeValue;
        }

        if(trackedEntityAttribute.equals(SETTLEMENTID))
        {
            OrganisationUnit mOrgUnit = MetaDataController.getOrganisationUnit(mOrgUnitId);
            trackedEntityAttributeValue.setValue(mOrgUnit.getLabel());
            trackedEntityAttributeValues.add(trackedEntityAttributeValue);
            return trackedEntityAttributeValue;
        }
        //the datavalue didnt exist for some reason. Create a new one.
        trackedEntityAttributeValue.setTrackedEntityAttributeId(trackedEntityAttribute);
        trackedEntityAttributeValue.setTrackedEntityInstanceId(currentTrackedEntityInstance.getTrackedEntityInstance());
        trackedEntityAttributeValue.setValue("");
        trackedEntityAttributeValues.add(trackedEntityAttributeValue);
        return trackedEntityAttributeValue;
    }

    abstract class DobAgeSync {

        public abstract void eventHandler(RowValueChangedEvent event);

        public boolean equals(Object obj){
            if(obj==null) return false;
            if(obj instanceof DobAgeSync)
                return true;
            else
                return false;
        }

        @Override
        public int hashCode() {
            return 143;
        }
    }

}
