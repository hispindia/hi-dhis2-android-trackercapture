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
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttribute;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeGeneratedValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowFactory;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.EnrollmentDatePickerRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.IncidentDatePickerRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.ShortTextEditTextRow;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.hisp.dhis.android.sdk.utils.api.ValueType;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DatePickerRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Weeks;

class EnrollmentDataEntryFragmentQuery implements Query<EnrollmentDataEntryFragmentForm> {
    public static final String CLASS_TAG = EnrollmentDataEntryFragmentQuery.class.getSimpleName();
    private static String appliedValue;
    private static DateTime LMPVALUE;
    private static DateTime LMPVALUE_NEW;
    private static DateTime EDDVALUE;
    private static Date LMP_DATE;

    private final String LMPUID="";
    private final String EDDUID="";
    private final String GESTATIONUID="";
    private final String mOrgUnitId;
    private final String mProgramId;
    private final long mTrackedEntityInstanceId;
    private final String enrollmentDate;
    private String incidentDate;
    private TrackedEntityInstance currentTrackedEntityInstance;
    private Enrollment currentEnrollment;

    EnrollmentDataEntryFragmentQuery(String mOrgUnitId, String mProgramId,
                                     long mTrackedEntityInstanceId,
                                     String enrollmentDate, String incidentDate) {
        this.mOrgUnitId = mOrgUnitId;
        this.mProgramId = mProgramId;
        this.mTrackedEntityInstanceId = mTrackedEntityInstanceId;
        this.enrollmentDate = enrollmentDate;
        this.incidentDate = incidentDate;
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
            currentTrackedEntityInstance = TrackerController.getTrackedEntityInstance(
                    mTrackedEntityInstanceId);
        }
        if ("".equals(incidentDate)) {
            incidentDate = null;
        }
        currentEnrollment = new Enrollment(mOrgUnitId,
                currentTrackedEntityInstance.getTrackedEntityInstance(), mProgram, enrollmentDate,
                incidentDate);

        mForm.setProgram(mProgram);
        mForm.setOrganisationUnit(mOrgUnit);
        mForm.setDataElementNames(new HashMap<String, String>());
        mForm.setDataEntryRows(new ArrayList<Row>());
        mForm.setTrackedEntityInstance(currentTrackedEntityInstance);
        mForm.setTrackedEntityAttributeValueMap(new HashMap<String, TrackedEntityAttributeValue>());

        List<TrackedEntityAttributeValue> trackedEntityAttributeValues = new ArrayList<>();
        List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes =
                mProgram.getProgramTrackedEntityAttributes();
        List<Row> dataEntryRows = new ArrayList<>();

        dataEntryRows.add(
                new EnrollmentDatePickerRow(currentEnrollment.getProgram().getEnrollmentDateLabel(),
                        currentEnrollment));

        if (currentEnrollment.getProgram().getDisplayIncidentDate()) {
            dataEntryRows.add(
                    new IncidentDatePickerRow(currentEnrollment.getProgram().getIncidentDateLabel(),
                            currentEnrollment));
        }

        for (ProgramTrackedEntityAttribute ptea : programTrackedEntityAttributes) {
            TrackedEntityAttributeValue value = TrackerController.getTrackedEntityAttributeValue(
                    ptea.getTrackedEntityAttributeId(), currentTrackedEntityInstance.getLocalId());
            if (value != null) {
                trackedEntityAttributeValues.add(value);
            } else {
                TrackedEntityAttribute trackedEntityAttribute =
                        MetaDataController.getTrackedEntityAttribute(
                                ptea.getTrackedEntityAttributeId());
                if (trackedEntityAttribute.isGenerated()) {
                    TrackedEntityAttributeGeneratedValue trackedEntityAttributeGeneratedValue =
                            MetaDataController.getTrackedEntityAttributeGeneratedValue(
                                    ptea.getTrackedEntityAttribute());

                    if (trackedEntityAttributeGeneratedValue != null) {
                        TrackedEntityAttributeValue trackedEntityAttributeValue =
                                new TrackedEntityAttributeValue();
                        trackedEntityAttributeValue.setTrackedEntityAttributeId(
                                ptea.getTrackedEntityAttribute().getUid());
                        trackedEntityAttributeValue.setTrackedEntityInstanceId(
                                currentTrackedEntityInstance.getUid());
                        trackedEntityAttributeValue.setValue(
                                trackedEntityAttributeGeneratedValue.getValue());
                        trackedEntityAttributeValues.add(trackedEntityAttributeValue);
                    } else {
                        mForm.setOutOfTrackedEntityAttributeGeneratedValues(true);
                    }
                }
            }
        }
        currentEnrollment.setAttributes(trackedEntityAttributeValues);

        if(mProgram.getUid().equals("bNhQuFV59bs"))
        {
            int paddingForIndex = dataEntryRows.size();
            int lmpDate = -1;//added to manupulate or dynamicly change the row value based on user input for the other
            int eddDate = -1;//added to manupulate or dynamicly change the row value based on user input for the other
            int periodOfGestation = -1;//added to manupulate or dynamicly change the row value based on user input for the other


            for (int i = 0; i < programTrackedEntityAttributes.size(); i++) {
                boolean editable = true;
                boolean shouldNeverBeEdited = false;
                if (programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().isGenerated()) {
                    editable = false;
                    shouldNeverBeEdited = true;
                }
                if (ValueType.COORDINATE.equals(programTrackedEntityAttributes.get(
                        i).getTrackedEntityAttribute().getValueType())) {
                    GpsController.activateGps(context);
                }
                Row row = DataEntryRowFactory.createDataEntryView(
                        programTrackedEntityAttributes.get(i).getMandatory(),
                        programTrackedEntityAttributes.get(i).getAllowFutureDate(),
                        programTrackedEntityAttributes.get(
                                i).getTrackedEntityAttribute().getOptionSet(),
                        programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getName(),
                        getTrackedEntityDataValue(programTrackedEntityAttributes.get(i).
                                getTrackedEntityAttribute().getUid(), trackedEntityAttributeValues),
                        programTrackedEntityAttributes.get(
                                i).getTrackedEntityAttribute().getValueType(),
                        editable, shouldNeverBeEdited, mProgram.getDataEntryMethod());
                dataEntryRows.add(row);

                if(programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getUid().equals("OQphqQQNLyz"))
                {
                    lmpDate = i;
                }
                else if(programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getUid().equals("Ljp09Kf1Qpl"))
                {
                    eddDate = i;
                }
                else if(programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getUid().equals("Y1Rjo88QvH5"))
                {
                    periodOfGestation = i;
                }

            }
            for (TrackedEntityAttributeValue trackedEntityAttributeValue :
                    trackedEntityAttributeValues) {
                mForm.getTrackedEntityAttributeValueMap().put(
                        trackedEntityAttributeValue.getTrackedEntityAttributeId(),
                        trackedEntityAttributeValue);
            }
            mForm.setDataEntryRows(dataEntryRows);
            mForm.setEnrollment(currentEnrollment);


            final DatePickerRow lmpRow = (DatePickerRow) dataEntryRows.get(paddingForIndex+lmpDate);
            final DatePickerRow eddRow = (DatePickerRow) dataEntryRows.get(paddingForIndex+eddDate);
            final ShortTextEditTextRow gestationRow = (ShortTextEditTextRow) dataEntryRows.get(paddingForIndex+periodOfGestation);
            final String lmpUID =programTrackedEntityAttributes.get(lmpDate).getTrackedEntityAttribute().getUid();
            final String eddUID = programTrackedEntityAttributes.get(eddDate).getTrackedEntityAttribute().getUid();

            //DateTime dateTime1 = new DateTime(date1);
            //DateTime dateTime2 = new DateTime(date2);
            //
            //int weeks = Weeks.weeksBetween(dateTime1, dateTime2).getWeeks();

            Dhis2Application.getEventBus().register(new DobAgeSync(){
                @Override
                @com.squareup.otto.Subscribe
                public void eventHandler(RowValueChangedEvent event){
                    // Log.i(" Called ",event.getBaseValue().getValue()+"");
                    if(event.getId()!=null && event.getId().equals(lmpUID)){
                        if(appliedValue==null || !appliedValue.equals(lmpRow.getValue().getValue()) ){
                            //Log.i(" Called ",row.getValue().getValue());
                            try {
                                LMPVALUE = new DateTime(lmpRow.getValue().getValue());
//                            DateTime CURRENT=new DateTime();
                                DateTime CURRENT=new DateTime(enrollmentDate);
                                LMPVALUE_NEW= LMPVALUE.plusDays(277);
//                            if(!EDDVALUE.equals(null))
                                {
                                    //@Sou Todo EDD/LMP Fix
                                    int weeks = (Weeks.weeksBetween(LMPVALUE, CURRENT).getWeeks());
                                    int days = (Days.daysBetween(LMPVALUE, CURRENT).getDays());
                                    DateTime newEnd = CURRENT.minusWeeks(weeks);
                                    int days_ = Days.daysBetween(newEnd, LMPVALUE).getDays()+1;
                                    weeks = (Weeks.weeksBetween(CURRENT, LMPVALUE).getWeeks());
                                    eddRow.getValue().setValue(LMPVALUE_NEW.toString().substring(0,10));
//                                gestationRow.getValue().setValue(lmpRow.getValue().getValue().toString());

                                    gestationRow.getValue().setValue(String.valueOf(Math.abs(weeks))+" Weeks + "+String.valueOf(Math.abs(days_))+" Days");
                                }
                                EnrollmentDataEntryFragment.refreshListView();
                            }catch (Exception ex) {
                                Log.i("Exception ", "Converting to integer not possible");

                            }

                        }
                    }

                }

            });
            return mForm;
        }
        else if(mProgram.getUid().equals(A_HOUSEHOLD_PROGRAM))
       {
           int paddingForIndex = dataEntryRows.size();

           int region_attr = -1;//added to manupulate or dynamicly change the row value based on user input for the other
           int rsite_attr = -1;//added to manupulate or dynamicly change the row value based on user input for the other


           for (int i = 0; i < programTrackedEntityAttributes.size(); i++) {
               boolean editable = true;
               boolean shouldNeverBeEdited = false;
               if (programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().isGenerated()) {
                   editable = false;
                   shouldNeverBeEdited = true;
               }
               if (ValueType.COORDINATE.equals(programTrackedEntityAttributes.get(
                       i).getTrackedEntityAttribute().getValueType())) {
                   GpsController.activateGps(context);
               }
               Row row = DataEntryRowFactory.createDataEntryView(
                       programTrackedEntityAttributes.get(i).getMandatory(),
                       programTrackedEntityAttributes.get(i).getAllowFutureDate(),
                       programTrackedEntityAttributes.get(
                               i).getTrackedEntityAttribute().getOptionSet(),
                       programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getName(),
                       getTrackedEntityDataValue(programTrackedEntityAttributes.get(i).
                               getTrackedEntityAttribute().getUid(), trackedEntityAttributeValues),
                       programTrackedEntityAttributes.get(
                               i).getTrackedEntityAttribute().getValueType(),
                       editable, shouldNeverBeEdited, mProgram.getDataEntryMethod());
               dataEntryRows.add(row);


             if(programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getUid().equals(REGION_ATTR_UID))
               {
                   region_attr = i;
               }

               else if(programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getUid().equals(SITE_ATTR_UID))
               {
                   rsite_attr = i;
               }

           }
           for (TrackedEntityAttributeValue trackedEntityAttributeValue :
                   trackedEntityAttributeValues) {
               mForm.getTrackedEntityAttributeValueMap().put(
                       trackedEntityAttributeValue.getTrackedEntityAttributeId(),
                       trackedEntityAttributeValue);
           }
           mForm.setDataEntryRows(dataEntryRows);
           mForm.setEnrollment(currentEnrollment);

           final ShortTextEditTextRow region_Row = (ShortTextEditTextRow)  dataEntryRows.get(paddingForIndex+region_attr);
           OrganisationUnit ou=MetaDataController.getOrganisationUnitbyPhone(mForm.getOrganisationUnit().getId());

           region_Row.getValue().setValue(ou.getCode());
           region_Row.setEditable(false);
           region_Row.isShouldNeverBeEdited();
           return mForm;
       }
        else
        {

            for (int i = 0; i < programTrackedEntityAttributes.size(); i++) {
                boolean editable = true;
                boolean shouldNeverBeEdited = false;
                if (programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().isGenerated()) {
                    editable = false;
                    shouldNeverBeEdited = true;
                }
                if (ValueType.COORDINATE.equals(programTrackedEntityAttributes.get(
                        i).getTrackedEntityAttribute().getValueType())) {
                    GpsController.activateGps(context);
                }
                Row row = DataEntryRowFactory.createDataEntryView(
                        programTrackedEntityAttributes.get(i).getMandatory(),
                        programTrackedEntityAttributes.get(i).getAllowFutureDate(),
                        programTrackedEntityAttributes.get(
                                i).getTrackedEntityAttribute().getOptionSet(),
                        programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getName(),
                        getTrackedEntityDataValue(programTrackedEntityAttributes.get(i).
                                getTrackedEntityAttribute().getUid(), trackedEntityAttributeValues),
                        programTrackedEntityAttributes.get(
                                i).getTrackedEntityAttribute().getValueType(),
                        editable, shouldNeverBeEdited, mProgram.getDataEntryMethod());
                dataEntryRows.add(row);

            }
            for (TrackedEntityAttributeValue trackedEntityAttributeValue :
                    trackedEntityAttributeValues) {
                mForm.getTrackedEntityAttributeValueMap().put(
                        trackedEntityAttributeValue.getTrackedEntityAttributeId(),
                        trackedEntityAttributeValue);
            }
            mForm.setDataEntryRows(dataEntryRows);
            mForm.setEnrollment(currentEnrollment);

            return mForm;

        }

    }

    public TrackedEntityAttributeValue getTrackedEntityDataValue(String trackedEntityAttribute,
                                                                 List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        for (TrackedEntityAttributeValue trackedEntityAttributeValue :
                trackedEntityAttributeValues) {
            if (trackedEntityAttributeValue.getTrackedEntityAttributeId().equals(
                    trackedEntityAttribute)) {
                return trackedEntityAttributeValue;
            }
        }

        //the datavalue didnt exist for some reason. Create a new one.
        TrackedEntityAttributeValue trackedEntityAttributeValue = new TrackedEntityAttributeValue();
        trackedEntityAttributeValue.setTrackedEntityAttributeId(trackedEntityAttribute);
        trackedEntityAttributeValue.setTrackedEntityInstanceId(
                currentTrackedEntityInstance.getTrackedEntityInstance());
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
