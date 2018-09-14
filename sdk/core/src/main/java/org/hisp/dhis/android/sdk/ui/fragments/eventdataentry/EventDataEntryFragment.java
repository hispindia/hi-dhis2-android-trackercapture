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

package org.hisp.dhis.android.sdk.ui.fragments.eventdataentry;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import  java.math.BigDecimal;

import com.facebook.stetho.common.ArrayListAccumulator;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.raizlabs.android.dbflow.structure.Model;
import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.ErrorType;
import org.hisp.dhis.android.sdk.controllers.GpsController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.loaders.DbLoader;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramIndicator;
import org.hisp.dhis.android.sdk.persistence.models.ProgramRule;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.ui.adapters.SectionAdapter;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.IndicatorRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.RunProgramRulesEvent;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.OnCompleteEventClick;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.OnDetailedInfoButtonClick;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.DataEntryFragment;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.DataEntryFragmentSection;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.HideLoadingDialogEvent;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RefreshListViewEvent;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.hisp.dhis.android.sdk.utils.UiUtils;
import org.hisp.dhis.android.sdk.utils.comparators.EventDateComparator;
import org.hisp.dhis.android.sdk.utils.services.ProgramIndicatorService;
import org.hisp.dhis.android.sdk.utils.services.VariableService;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventDataEntryFragment extends DataEntryFragment<EventDataEntryFragmentForm> {

    public static final String TAG = EventDataEntryFragment.class.getSimpleName();
    private static final String PARTOGRAPH_STAGE_ID = "kl1qQxjtlyx" ;
    private Map<String, List<ProgramRule>> programRulesForDataElements;
    private Map<String, List<ProgramIndicator>> programIndicatorsForDataElements;

    private IndicatorEvaluatorThread indicatorEvaluatorThread;
    private EventSaveThread saveThread;

    public static final String ORG_UNIT_ID = "extra:orgUnitId";
    public static final String PROGRAM_ID = "extra:ProgramId";
    public static final String PROGRAM_STAGE_ID = "extra:ProgramStageId";
    public static final String EVENT_ID = "extra:EventId";
    public static final String ENROLLMENT_ID = "extra:EnrollmentId";

    public static final String CERVICAL_TIME1  = "VEBl8LyIilQ";
    public static final String CERVICAL_TIME2  = "epVofEFHmRZ";
    public static final String CERVICAL_TIME3  = "LSGPziwH7yq";
    public static final String CERVICAL_TIME4  = "jX947N9qqXB";
    public static final String CERVICAL_TIME5  = "SzVB5GT6prJ";
    public static final String CERVICAL_TIME6  = "MAH4sUdMP3t";
    public static final String CERVICAL_TIME7  = "fFtRXroQja2";
    public static final String CERVICAL_TIME8  = "Ctu2UFjMSYC";
    public static final String CERVICAL_TIME9  = "Rjg6ET2Odna";
    public static final String CERVICAL_TIME10 = "Peq77iP5YTW";

    public static final String CERVICAL_READING1 = "FmVJzYVDVcz";
    public static final String CERVICAL_READING2 = "pVowz22vGbF";
    public static final String CERVICAL_READING3 = "AXFTeswZfLi";
    public static final String CERVICAL_READING4 = "WBgaNvjJYrE";
    public static final String CERVICAL_READING5 = "HNpSco5aovr";
    public static final String CERVICAL_READING6 = "ThVQ8cJuMw5";
    public static final String CERVICAL_READING7 = "AMS7Bj0pVX9";
    public static final String CERVICAL_READING8 = "c6F35sCSFV3";
    public static final String CERVICAL_READING9 = "J2MElelRH9p";
    public static final String CERVICAL_READING10 = "Aqr0MvXJ0Zz";




    private ImageView previousSectionButton;
    private ImageView nextSectionButton;
    private View spinnerContainer;
    private Spinner spinner;
    private SectionAdapter spinnerAdapter;
    private EventDataEntryFragmentForm form;
    private DateTime scheduledDueDate;

    public EventDataEntryFragment() {
        setProgramRuleFragmentHelper(new EventDataEntryRuleHelper(this));
    }

    public static EventDataEntryFragment newInstance(String unitId, String programId, String programStageId) {
        EventDataEntryFragment fragment = new EventDataEntryFragment();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        args.putString(PROGRAM_STAGE_ID, programStageId);
        fragment.setArguments(args);
        return fragment;
    }

    public static EventDataEntryFragment newInstance(String unitId, String programId, String programStageId,
                                                     long eventId) {
        EventDataEntryFragment fragment = new EventDataEntryFragment();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        args.putString(PROGRAM_STAGE_ID, programStageId);
        args.putLong(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    public static EventDataEntryFragment newInstanceWithEnrollment(String unitId, String programId, String programStageId,
                                                                   long enrollmentId) {
        EventDataEntryFragment fragment = new EventDataEntryFragment();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        args.putString(PROGRAM_STAGE_ID, programStageId);
        args.putLong(ENROLLMENT_ID, enrollmentId);
        fragment.setArguments(args);
        return fragment;
    }

    public static EventDataEntryFragment newInstanceWithEnrollment(String unitId, String programId, String programStageId,
                                                                   long enrollmentId, long eventId) {
        EventDataEntryFragment fragment = new EventDataEntryFragment();
        Bundle args = new Bundle();
        args.putString(ORG_UNIT_ID, unitId);
        args.putString(PROGRAM_ID, programId);
        args.putString(PROGRAM_STAGE_ID, programStageId);
        args.putLong(ENROLLMENT_ID, enrollmentId);
        args.putLong(EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        detachSpinner();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        GpsController.disableGps();
        super.onDetach();
    }

    private void detachSpinner() {
        if (isSpinnerAttached()) {
            if (spinnerContainer != null) {
                ((ViewGroup) spinnerContainer.getParent()).removeView(spinnerContainer);
                spinnerContainer = null;
                spinner = null;
                if (spinnerAdapter != null) {
                    spinnerAdapter.swapData(null);
                    spinnerAdapter = null;
                }
            }
        }
    }

    private boolean isSpinnerAttached() {
        return spinnerContainer != null;
    }

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        VariableService.reset();
        if (saveThread == null || saveThread.isKilled()) {
            saveThread = new EventSaveThread();
            saveThread.start();
        }
        saveThread.init(this);
        if (indicatorEvaluatorThread == null || indicatorEvaluatorThread.isKilled()) {
            indicatorEvaluatorThread = new IndicatorEvaluatorThread();
            indicatorEvaluatorThread.start();
        }

        indicatorEvaluatorThread.init(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

    }

    @Override
    public void onDestroy() {
        new Thread() {
            public void run() {
                saveThread.kill();
                indicatorEvaluatorThread.kill();
                indicatorEvaluatorThread = null;
                saveThread = null;
            }
        }.start();
        super.onDestroy();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_new_event);
        menuItem.setVisible(false);
    }

    void hideSection(String programStageSectionId) {
        if (spinnerAdapter != null) {
            spinnerAdapter.hideSection(programStageSectionId);
        }
    }

    @Override
    public Loader<EventDataEntryFragmentForm> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            // Adding Tables for tracking here is dangerous (since MetaData updates in background
            // can trigger reload of values from db which will reset all fields).
            // Hence, it would be more safe not to track any changes in any tables
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            Bundle fragmentArguments = args.getBundle(EXTRA_ARGUMENTS);
            return new DbLoader<>(
                    getActivity(), modelsToTrack, new EventDataEntryFragmentQuery(
                    fragmentArguments.getString(ORG_UNIT_ID),
                    fragmentArguments.getString(PROGRAM_ID),
                    fragmentArguments.getString(PROGRAM_STAGE_ID),
                    fragmentArguments.getLong(EVENT_ID, -1),
                    fragmentArguments.getLong(ENROLLMENT_ID, -1)
            )
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<EventDataEntryFragmentForm> loader, EventDataEntryFragmentForm data) {
        if (loader.getId() == LOADER_ID && isAdded()) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            form = data;

            saveThread.setEvent(form.getEvent());

            if (form.getStatusRow() != null) {
                form.getStatusRow().setFragmentActivity(getActivity());
            }
            if (data.getStage() != null &&
                    data.getStage().getCaptureCoordinates()) {
                GpsController.activateGps(getActivity().getBaseContext());
            }else{
                if(hasCoordinateQuestion()){
                    GpsController.activateGps(getActivity().getBaseContext());
                }
            }
            if (data.getStage() != null &&
                    data.getStage().getCaptureCoordinates()) {
                GpsController.activateGps(getActivity().getBaseContext());
            }
            if (data.getSections() != null && !data.getSections().isEmpty()) {
                if (data.getSections().size() > 1) {
                    attachSpinner();
                    spinnerAdapter.swapData(data.getSections());
                } else {
                    if (form.getStage() != null) {//added by ifhaam for icmr graph

                        getActionBarToolbar().setTitle(form.getStage().getName());
                    }
                    DataEntryFragmentSection section = data.getSections().get(0);
                    listViewAdapter.swapData(section.getRows());
                }
            }
            if (form.getStage()!= null) {//added by ifhaam for icmr graph
                if (form.getStage().getUid().equals(PARTOGRAPH_STAGE_ID)) {
                    chart.setVisibility(View.VISIBLE);
                    xAxisLabel.setVisibility(View.VISIBLE);
                    yAxisLabel.setVisibility(View.VISIBLE);
                    graphLabelHolder.setVisibility(View.VISIBLE);
                    drawGraph();
                }
            }

            if (form.getEvent() == null) {
                // form is null - show error message and disable editing
                showErrorAndDisableEditing(getContext().getString(R.string.no_event_present));
            } else {
                OrganisationUnit eventOrganisationUnit = MetaDataController.getOrganisationUnit(form.getEvent().getOrganisationUnitId());
                if (eventOrganisationUnit == null) {
                    showErrorAndDisableEditing(getContext().getString(R.string.missing_ou));
                } else if (!OrganisationUnit.TYPE.ASSIGNED.equals(eventOrganisationUnit.getType())) { // if user is not assigned to the event's OrgUnit. Disable data entry screen
                    setEditableDataEntryRows(form, false, false);
                }
                if (Event.STATUS_COMPLETED.equals(form.getEvent().getStatus()) && form.getStage().isBlockEntryForm()) { // if event is completed and should be blocked. Disable data entry screen
                    setEditableDataEntryRows(form, false, true);
                }
            }

            initiateEvaluateProgramRules();

        }
    }

    private boolean hasCoordinateQuestion() {
        List<Row> rows = new ArrayList<>();
        if(form.getSections()!=null) {
            List<DataEntryFragmentSection> sections = form.getSections();
            for(DataEntryFragmentSection section : sections){
                rows.addAll(section.getRows());
            }
        }
        if(form.getCurrentSection()!=null){
            rows.addAll(form.getCurrentSection().getRows());
        }
        for(Row row:rows){
            if(row.getViewType()==(DataEntryRowTypes.QUESTION_COORDINATES.ordinal())){
                return  true;
            }
        }
        return false;
    }

    private void showErrorAndDisableEditing(String extraInfo) {
        Toast.makeText(getContext(), getContext().getString(R.string.error_with_form) + extraInfo +getContext().getString(R.string.please_retry), Toast.LENGTH_LONG).show();
        setEditableDataEntryRows(form, false, false);
    }

    public void setEditableDataEntryRows(EventDataEntryFragmentForm form, boolean editableDataEntryRows, boolean editableStatusRow) {
        List<Row> rows = new ArrayList<>();
        if (form.getSections() != null && !form.getSections().isEmpty()) {
            if (form.getSections().size() > 1) {
                for (DataEntryFragmentSection section : form.getSections()) {
                    rows.addAll(section.getRows());
                }
            } else {
                rows = form.getSections().get(0).getRows();
            }
        }
        listViewAdapter.swapData(null);
        if (editableDataEntryRows) {
            for (Row row : rows) {
                row.setEditable(true);
            }
        } else {
            for (Row row : rows) {
                row.setEditable(false);
            }
        }
        if (editableStatusRow) {
            form.getStatusRow().setEditable(true);
        }

        listView.setAdapter(null);
//        listViewAdapter.swapData(rows);
        if (form.getSections() != null) {
            listViewAdapter.swapData(form.getSections().get(0).getRows()); //TODO find a better solution for this hack
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.an_error_ocurred), Toast.LENGTH_SHORT).show();
        }
        listView.setAdapter(listViewAdapter);
    }

    private void attachSpinner() {
        if (!isSpinnerAttached()) {
            final Toolbar toolbar = getActionBarToolbar();
            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            spinnerContainer = inflater.inflate(
                    R.layout.toolbar_spinner, toolbar, false);
            previousSectionButton = (ImageView) spinnerContainer
                    .findViewById(R.id.previous_section);
            nextSectionButton = (ImageView) spinnerContainer
                    .findViewById(R.id.next_section);
            final ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(spinnerContainer, lp);
            spinnerAdapter = new SectionAdapter(inflater);
            spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
            spinner.setAdapter(spinnerAdapter);
            spinner.setOnItemSelectedListener(this);
            previousSectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentPosition = spinner.getSelectedItemPosition();
                    if (!(currentPosition - 1 < 0)) {
                        currentPosition = currentPosition - 1;
                        spinner.setSelection(currentPosition);
                    }
                }
            });
            nextSectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentPosition = spinner.getSelectedItemPosition();
                    if (!(currentPosition + 1 >= spinnerAdapter.getCount())) {
                        currentPosition = currentPosition + 1;
                        spinner.setSelection(currentPosition);
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<EventDataEntryFragmentForm> loader) {
        if (loader.getId() == LOADER_ID) {
            if (spinnerAdapter != null) {
                spinnerAdapter.swapData(null);
            }
            if (listViewAdapter != null) {
                listViewAdapter.swapData(null);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectSection(position);
    }

    private void selectSection(int position) {
        DataEntryFragmentSection section = (DataEntryFragmentSection)
                spinnerAdapter.getItem(position);
        form.setCurrentSection(section);
        if (section != null) {
            listView.smoothScrollToPosition(INITIAL_POSITION);
            listViewAdapter.swapData(section.getRows());
        }
        updateSectionNavigationButtons();
    }

    @Override
    protected boolean isValid() {
        if (form.getEvent() == null || form.getStage() == null) {
            return false;
        }
        if (isEmpty(form.getEvent().getEventDate())) {
            return false;
        }
        Map<String, ProgramStageDataElement> dataElements = toMap(
                form.getStage().getProgramStageDataElements()
        );

        for (DataEntryFragmentSection dataEntryFragmentSection:form.getSections()) {
            for (Row row : dataEntryFragmentSection.getRows()) {
                if (row.getValidationError() != null) {
                    return false;
                }
            }
        }
        for (DataValue dataValue : form.getEvent().getDataValues()) {
            ProgramStageDataElement dataElement = dataElements.get(dataValue.getDataElement());
            if (dataElement == null) {
                return false;
            }
            if (dataElement.getCompulsory() && isEmpty(dataValue.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void save() {
        if (form != null && form.getEvent() != null) {
            flagDataChanged(false);
        }
    }

    @Override
    protected void proceed() {

    }

    @Override
    public HashMap<ErrorType, ArrayList<String>> getValidationErrors() {
        HashMap<ErrorType, ArrayList<String>> errors = new HashMap<>();
        if (form.getEvent() == null || form.getStage() == null) {
            return errors;
        }
        if (isEmpty(form.getEvent().getEventDate())) {
            String reportDateDescription = form.getStage().getReportDateDescription() == null ?
                    getString(R.string.report_date) : form.getStage().getReportDateDescription();
            if(!errors.containsKey(ErrorType.MANDATORY)){
                errors.put(ErrorType.MANDATORY, new ArrayList<String>());
            }
            errors.get(ErrorType.MANDATORY).add(reportDateDescription);
        }
        Map<String, ProgramStageDataElement> dataElements = toMap(
                form.getStage().getProgramStageDataElements()
        );
        for (DataValue dataValue : form.getEvent().getDataValues()) {
            ProgramStageDataElement dataElement = dataElements.get(dataValue.getDataElement());
            if (dataElement == null) {
                // don't do anything
            } else if (dataElement.getCompulsory() && isEmpty(dataValue.getValue())) {
                if(!errors.containsKey(ErrorType.MANDATORY)){
                    errors.put(ErrorType.MANDATORY, new ArrayList<String>());
                }
                errors.get(ErrorType.MANDATORY).add(MetaDataController.getDataElement(dataElement.getDataelement()).getDisplayName());
            }
        }
        return errors;
    }

    private void evaluateRulesAndIndicators(String dataElement) {
        if (dataElement == null || form == null || form.getIndicatorRows() == null) {
            return;
        }

        if (hasRules(dataElement)) {
            getProgramRuleFragmentHelper().getProgramRuleValidationErrors().clear();
            initiateEvaluateProgramRules();
        }
        if (hasIndicators(dataElement)) {
            initiateEvaluateProgramIndicators(dataElement);
        }
    }

    private boolean hasRules(String dataElement) {
        if (programRulesForDataElements == null) {
            return false;
        }
        return programRulesForDataElements.containsKey(dataElement);
    }

    private boolean hasIndicators(String dataElement) {
        if (programIndicatorsForDataElements == null) {
            return false;
        }
        return programIndicatorsForDataElements.containsKey(dataElement);
    }

    /**
     * Schedules evaluation and updating of views based on ProgramRules in a thread.
     * This is used to avoid stacking up calls to evaluateAndApplyProgramRules
     */
    public void initiateEvaluateProgramRules() {
        if (rulesEvaluatorThread != null) {
            rulesEvaluatorThread.schedule();
        }
    }

    /**
     * Schedules evaluation and updating of views based on ProgramIndicators in a thread.
     * This is used to avoid stacking up calls to evaluateAndApplyProgramIndicators
     *
     * @param dataElement
     */
    private synchronized void initiateEvaluateProgramIndicators(String dataElement) {
        if (programIndicatorsForDataElements == null) {
            return;
        }
        List<ProgramIndicator> programIndicators = programIndicatorsForDataElements.get(dataElement);
        indicatorEvaluatorThread.schedule(programIndicators);
    }

    void evaluateAndApplyProgramIndicator(ProgramIndicator programIndicator) {
        if (VariableService.getInstance().getProgramRuleVariableMap() == null) {
            VariableService.initialize(form.getEnrollment(), form.getEvent());
        }
        IndicatorRow indicatorRow = form.getIndicatorToIndicatorRowMap().get(programIndicator.getUid());
        updateIndicatorRow(indicatorRow, form.getEvent());
        refreshListView();
    }

    /**
     * Calculates and updates the value in a IndicatorRow view for the corresponding Indicator
     *
     * @param indicatorRow
     * @param event
     */
    private static void updateIndicatorRow(IndicatorRow indicatorRow, Event event) {
        String newValue = ProgramIndicatorService.
                getProgramIndicatorValue(event, indicatorRow.getIndicator());
        if (newValue == null) {
            newValue = "";
        }
        if (!newValue.equals(indicatorRow.getValue())) {
            indicatorRow.updateValue(newValue);
        }
    }

    private static Map<String, ProgramStageDataElement> toMap(List<ProgramStageDataElement> dataElements) {
        Map<String, ProgramStageDataElement> dataElementMap = new HashMap<>();
        if (dataElements != null && !dataElements.isEmpty()) {
            for (ProgramStageDataElement dataElement : dataElements) {
                dataElementMap.put(dataElement.getDataelement(), dataElement);
            }
        }
        return dataElementMap;
    }

    private void updateSectionNavigationButtons() {
        if (nextSectionButton != null && previousSectionButton != null) {
            if (spinner.getSelectedItemPosition() - 1 < 0) {
                previousSectionButton.setVisibility(View.INVISIBLE);
            } else {
                previousSectionButton.setVisibility(View.VISIBLE);
            }
            if (spinner.getSelectedItemPosition() + 1 >= spinnerAdapter.getCount()) {
                nextSectionButton.setVisibility(View.INVISIBLE);
            } else {
                nextSectionButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public SectionAdapter getSpinnerAdapter() {
        return spinnerAdapter;
    }

    public static ArrayList<String> getValidationErrors(Event event, ProgramStage programStage, Context context) {
        ArrayList<String> errors = new ArrayList<>();
        if (event == null || programStage == null) {
            return errors;
        }
        if (isEmpty(event.getEventDate())) {
            String reportDateDescription = programStage.getReportDateDescription() == null ?
                    context.getString(R.string.report_date) : programStage.getReportDateDescription();
            errors.add(reportDateDescription);
        }
        Map<String, ProgramStageDataElement> dataElements = toMap(
                programStage.getProgramStageDataElements()
        );
        for (DataValue dataValue : event.getDataValues()) {
            ProgramStageDataElement dataElement = dataElements.get(dataValue.getDataElement());
            if (dataElement.getCompulsory() && isEmpty(dataValue.getValue())) {
                errors.add(MetaDataController.getDataElement(dataElement.getDataelement()).getDisplayName());
            }
        }
        return errors;
    }

    private static ArrayList<String> getRowsErrors(Context context, EventDataEntryFragmentForm form) {
        ArrayList<String> errors = new ArrayList<>();
        for (DataEntryFragmentSection dataEntryFragmentSection:form.getSections()){
            for(Row row: dataEntryFragmentSection.getRows()) {
                if (row.getValidationError() != null) {
                    Integer stringId = row.getValidationError();
                    if(stringId!=null) {
                        errors.add(context.getString(stringId));
                    }
                }
            }
        }
        return errors;
    }

    @Subscribe
    public void onHideLoadingDialog(HideLoadingDialogEvent event) {
        super.onHideLoadingDialog(event);
    }

    @Subscribe
    public void onUpdateSectionsSpinner(UpdateSectionsEvent event) {
        if (spinnerAdapter != null) {
            spinnerAdapter.notifyDataSetChanged();
            if (form != null && form.getCurrentSection() != null && form.getCurrentSection()
                    .isHidden()) {
                selectSection(0);
            }
        }
    }

    @Subscribe
    public void onRefreshListView(RefreshListViewEvent event) {
        super.onRefreshListView(event);
    }

    @Subscribe
    public void onDetailedInfoClick(OnDetailedInfoButtonClick eventClick) {
        super.onShowDetailedInfo(eventClick);
    }

    @Subscribe
    public void onItemClick(final OnCompleteEventClick eventClick) {
        if (isValid()) {
            if (!eventClick.getEvent().getStatus().equals(Event.STATUS_COMPLETED)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        UiUtils.showConfirmDialog(getActivity(), eventClick.getLabel(), eventClick.getAction(),
                                eventClick.getLabel(), getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String labelForCompleteButton = "";
                                        if (form.getStage().isBlockEntryForm()) {
                                            labelForCompleteButton = getString(R.string.edit);
                                        } else {
                                            labelForCompleteButton = getString(R.string.incomplete);
                                        }

                                        eventClick.getComplete().setText(labelForCompleteButton);
                                        eventClick.getEvent().setStatus(Event.STATUS_COMPLETED);
                                        form.getEvent().setFromServer(false);
                                        form.getEnrollment().setFromServer(false);
                                        TrackedEntityInstance trackedEntityInstance =TrackerController.getTrackedEntityInstance(form.getEnrollment().getTrackedEntityInstance());
                                        trackedEntityInstance.setFromServer(false);
                                        trackedEntityInstance.save();
                                        ProgramStage currentProgramStage = MetaDataController
                                                .getProgramStage(form.getEvent().getProgramStageId());

                                        // checking if should schedule new event
                                        boolean isShowingSchedulingOfNewEvent = false;
                                        if (currentProgramStage.getAllowGenerateNextVisit()) {
                                            if (currentProgramStage.getRepeatable()) {
                                                DateTime scheduleTime = calculateScheduledDate(currentProgramStage, form.getEnrollment());
                                                isShowingSchedulingOfNewEvent = true;
                                                showDatePicker(currentProgramStage, scheduleTime); // datePicker will close this fragment when date is picked and new event is scheduled
                                            } else {
                                                int sortOrder = currentProgramStage.getSortOrder();
                                                Program currentProgram = currentProgramStage.getProgram();
                                                ProgramStage programStageToSchedule = null;
                                                programStageToSchedule = getNextValidProgramStage(
                                                        sortOrder, currentProgram,
                                                        programStageToSchedule);
                                                if(programStageToSchedule == null) {
                                                    programStageToSchedule =
                                                            getFirstValidProgramStage(
                                                                    currentProgram,
                                                                    programStageToSchedule);
                                                }
                                                if (programStageToSchedule != null) {
                                                    DateTime dateTime = calculateScheduledDate(programStageToSchedule, form.getEnrollment());
                                                    isShowingSchedulingOfNewEvent = true;
                                                    showDatePicker(programStageToSchedule, dateTime); // datePicker will close this fragment when date is picked and new event is scheduled
                                                }
                                            }
                                        }
                                        // Checking if dataEntryForm should be blocked after completed
                                        if (currentProgramStage.isBlockEntryForm()) {
                                            setEditableDataEntryRows(form, false, true);
                                        }

                                        eventClick.getEvent().setCompletedDate(new DateTime().toString());

                                        Dhis2Application.getEventBus().post(new RowValueChangedEvent(null, null));
                                        //Exit the activity if it has just been completed.
                                        if (currentProgramStage.isBlockEntryForm() && !isShowingSchedulingOfNewEvent) {
                                            goBackToPreviousActivity();
                                        }
                                    }
                                });

                    }
                });
            } else {
                eventClick.getComplete().setText(R.string.complete);
                form.getEvent().setStatus(Event.STATUS_ACTIVE);
                form.getEvent().setFromServer(false);

                // Checking if dataEntryForm should be enabled after un-completed
                ProgramStage currentProgramStage = MetaDataController.getProgramStage(form.getEvent().getProgramStageId());

                if (currentProgramStage.isBlockEntryForm()) {
                    setEditableDataEntryRows(form, true, true);
                }

                Dhis2Application.getEventBus().post(new RowValueChangedEvent(null, null));
            }
        } else {
            HashMap<ErrorType, ArrayList<String>>  allErrors = getValidationErrors();
            allErrors.put(ErrorType.PROGRAM_RULE, getProgramRuleFragmentHelper().getProgramRuleValidationErrors());
            allErrors.put(ErrorType.INVALID_FIELD, getRowsErrors(getContext(), form));
            showValidationErrorDialog(allErrors);
        }
    }

    @Nullable
    private ProgramStage getFirstValidProgramStage(Program currentProgram,
            ProgramStage programStageToSchedule) {
            for (ProgramStage programStage : currentProgram.getProgramStages()) {
                if (programStageToSchedule == null) {
                    programStageToSchedule = getValidProgramStage(programStageToSchedule, programStage);
                }else{
                    return programStageToSchedule;
                }
            }
        return programStageToSchedule;
    }

    @Nullable
    private ProgramStage getNextValidProgramStage(int sortOrder, Program currentProgram,
            ProgramStage programStageToSchedule) {
        for (ProgramStage programStage : currentProgram.getProgramStages()) {
            if (programStage.getSortOrder() >= (sortOrder + 1) && programStageToSchedule == null) {
                programStageToSchedule = getValidProgramStage(programStageToSchedule, programStage);
                if(programStageToSchedule!=null){
                    return programStageToSchedule;
                }
            }
        }
        return programStageToSchedule;
    }

    private ProgramStage getValidProgramStage(ProgramStage programStageToSchedule,
            ProgramStage programStage) {
        if(programStage.isRepeatable()) {
            programStageToSchedule = programStage;
        }else if(TrackerController.getEvent(form.getEnrollment().getLocalId(), programStage.getUid()) != null){
            if(programStage.isRepeatable()) {
                programStageToSchedule = programStage;
            }else{
                if (hasTheCorrectNumberOfEvents(programStage)) return programStage;
            }
        }
        return programStageToSchedule;
    }

    private boolean hasTheCorrectNumberOfEvents(ProgramStage programStageToSchedule) {
        List<Event> events = form.getEnrollment().getEvents();
        List<Event> eventForStage = new ArrayList<>();
        for (Event event : events) {
            if (programStageToSchedule.getUid().equals(event.getProgramStageId())) {
                eventForStage.add(event);
            }
        }
        if(eventForStage.size()==0){
            return true;
        }
        return false;
    }

    @Subscribe
    public void onRowValueChanged(final RowValueChangedEvent event) {
        super.onRowValueChanged(event);

        // do not run program rules for EditTextRows - DelayedDispatcher takes care of this
        if (event.getRow() == null || !(event.getRow().isEditTextRow())) {
            evaluateRulesAndIndicators(event.getId());
        }

        //if rowType is coordinate or event date, save the event
       if(event.getRowType() == null
                || DataEntryRowTypes.EVENT_COORDINATES.toString().equals(event.getRowType())
                || DataEntryRowTypes.EVENT_DATE.toString().equals(event.getRowType())) {
            //save event
            saveThread.scheduleSaveEvent();
            List<Event> eventsForEnrollment = new ArrayList<>();

            for (Event eventd : form.getEnrollment().getEvents()) {
                if (eventd.getUid().equals(form.getEvent().getUid())) {
                    eventsForEnrollment.add(form.getEvent());
                } else {
                    eventsForEnrollment.add(eventd);
                }
            }
            form.getEnrollment().setEvents(eventsForEnrollment);
        } else {// save data element
            saveThread.scheduleSaveDataValue(event.getId());
        }

        //added by ifhaam for partograph
        drawGraph();

        //rules evaluation are triggered depending on the data element uid and if it has rules
        //for event date, we have to trigger it manually
        if (DataEntryRowTypes.EVENT_DATE.toString().equals(event.getRowType())) {
            initiateEvaluateProgramRules();
        }
    }

    protected void drawGraph(){
        float timeFloat=0;
        float timeFloat1=0;
        String time="";
        String time1="";
        List<Entry> entries;
        List<Entry> tempEntries;





        if(form.getEvent().getProgramStageId().equals(PARTOGRAPH_STAGE_ID)){
            entries = new ArrayList<>();
            tempEntries = new ArrayList<>();
            /* old
                for(DataValue val :form.getEvent().getDataValues()){

                if(val!=null && !val.getValue().equals("")) {
                    float v = Float.parseFloat(val.getValue());
                    Entry entry = new Entry(i, v);
                    entries.add(entry);
                    i++;
                }


            }*/

            HashMap<String,DataValue> dvMapped = new HashMap<>();

            for(DataValue dv:form.getEvent().getDataValues()){
                if(dv!=null && !dv.getValue().equals("")){
                    dvMapped.put(dv.getDataElement(),dv);
                }
            }

            String[] dataElements = {CERVICAL_READING1,CERVICAL_READING2,CERVICAL_READING3,CERVICAL_READING4,
                    CERVICAL_READING5,CERVICAL_READING6,CERVICAL_READING7,CERVICAL_READING8,CERVICAL_READING9,CERVICAL_READING10};
            String[] timeElements = {CERVICAL_TIME1,CERVICAL_TIME2,CERVICAL_TIME3,CERVICAL_TIME4,CERVICAL_TIME5
                    ,CERVICAL_TIME6,CERVICAL_TIME7,CERVICAL_TIME8,CERVICAL_TIME9,CERVICAL_TIME10};

            if(dvMapped.containsKey(CERVICAL_TIME1)){
//               timeFloat1 = Float.parseFloat(dvMapped.get(CERVICAL_TIME1).getValue().replace(":","."));
                for(int i=0;i<dataElements.length;i++){
                    DataValue dvData = dvMapped.get(dataElements[i]);
                    DataValue dvTime = dvMapped.get(timeElements[i]);
                    if(dvData!=null && dvTime!=null) {
                        if(dvData.getDataElement().equals(CERVICAL_READING1))
                        {

                            if(dvData.getValue().contains("4"))
                            {
                                timeFloat1=0;
                            }
                            else if(dvData.getValue().contains("5"))
                            {
                                timeFloat1=1;
                            }
                            else if(dvData.getValue().contains("6"))
                            {
                                timeFloat1=2;
                            }
                            else if(dvData.getValue().contains("7"))
                            {
                                timeFloat1=3;
                            }
                            else if(dvData.getValue().contains("8"))
                            {
                                timeFloat1=4;
                            }
                            else if(dvData.getValue().contains("9"))
                            {
                                timeFloat1=5;
                            }
                            else if(dvData.getValue().contains("10"))
                            {
                                timeFloat1=6;
                            }
                            else
                            {
                                if(timeFloat1>12)
                                {
                                    timeFloat1=timeFloat1-12;
                                }

                                timeFloat1 = Float.parseFloat(dvMapped.get(CERVICAL_TIME1).getValue().replace(":","."));
                            }
                            float x = timeFloat1;
                            float y = Float.parseFloat(dvData.getValue());
                            entries.add(new Entry(x, y));
                        }
                        else if(dvData.getDataElement().equals(CERVICAL_READING2))
                        {
                            timeFloat1 = Float.parseFloat(dvMapped.get(CERVICAL_TIME1).getValue().replace(":","."));
                            entries.get(0).getX();
                            if(timeFloat1>12)
                            {
                                timeFloat1=timeFloat1-12;
                                float x = Float.parseFloat(dvTime.getValue().replace(":", "."));
                                if(x>12)
                                {
                                    x = Float.parseFloat(dvTime.getValue().replace(":", ".")) - timeFloat1-12+entries.get(0).getX();
                                }
                                else
                                {
                                    x = Float.parseFloat(dvTime.getValue().replace(":", "."))+12 - timeFloat1+entries.get(0).getX();
                                }
                                float y = Float.parseFloat(dvData.getValue());
                                entries.add(new Entry(x, y));
                            }
                            else
                            {
                                //@Sou ToDO fix for time plot
                                if(Float.parseFloat(dvTime.getValue().replace(":", "."))<12)
                                {

                                    float x = Float.parseFloat(dvTime.getValue().replace(":", ".")) -timeFloat1+entries.get(0).getX();
                                    float y = Float.parseFloat(dvData.getValue());
                                    entries.add(new Entry(x, y));
                                }
                                else
                                {
                                    float x = Float.parseFloat(dvTime.getValue().replace(":", ".")) - timeFloat1+entries.get(0).getX();
                                    float y = Float.parseFloat(dvData.getValue());
                                    entries.add(new Entry(x, y));
                                }

                            }
                        }
                        else
                        {
                            timeFloat1 = Float.parseFloat(dvMapped.get(timeElements[i-1]).getValue().replace(":","."));
                            if(timeFloat1>12)
                            {
                                timeFloat1=timeFloat1-12;
                                float x = Float.parseFloat(dvTime.getValue().replace(":", "."));
                                if(x>12)
                                {
                                     x = Float.parseFloat(dvTime.getValue().replace(":", ".")) - timeFloat1-12+entries.get(i-1).getX();
                                }
                                else
                                {
                                    x = Float.parseFloat(dvTime.getValue().replace(":", "."))+12 - timeFloat1+entries.get(i-1).getX();
                                }
                                float y = Float.parseFloat(dvData.getValue());
                                entries.add(new Entry(x, y));
                            }
                            else
                            {
                                //@Sou ToDO fix for time plot
                                if(Float.parseFloat(dvTime.getValue().replace(":", "."))<12)
                                {

                                    float x = Float.parseFloat(dvTime.getValue().replace(":", ".")) -timeFloat1+entries.get(i-1).getX();
                                    float y = Float.parseFloat(dvData.getValue());
                                    entries.add(new Entry(x, y));
                                }
                                else
                                {
                                    float x = Float.parseFloat(dvTime.getValue().replace(":", ".")) - timeFloat1+entries.get(i-1).getX();
                                    float y = Float.parseFloat(dvData.getValue());
                                    entries.add(new Entry(x, y));
                                }

                            }

                        }

                    }
                }
            }




//            for(DataValue dv:form.getEvent().getDataValues()){
//                if(dv!=null && !dv.getValue().equals("")){
//
//                    switch (dv.getDataElement()){
//                        case CERVICAL_TIME1:
//                            time1=dv.getValue();
//                            timeFloat1 = Float.parseFloat(time1.replace(":","."));
//                            break;
//                        case CERVICAL_TIME2:
//                             time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//                        case CERVICAL_TIME3:
//                            time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//                        case CERVICAL_TIME4:
//                             time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//
//                        case CERVICAL_TIME9:
//                            time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//                        case CERVICAL_TIME5:
//                            time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//
//                        case CERVICAL_TIME6:
//                            time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//                        case CERVICAL_TIME7:
//                             time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//                        case CERVICAL_TIME8:
//                            time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//                        case CERVICAL_TIME10:
//                            time=dv.getValue();
//                            timeFloat = Float.parseFloat(time.replace(":","."));
//                            //timeFloat=timeFloat-timeFloat1;
//                            break;
//
//                        case CERVICAL_READING1:
//                            Entry e1 = new Entry(0,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e1);
//                            break;
//
//                        case CERVICAL_READING2:
//
//                            Entry e2 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e2);
//                            break;
//                        case CERVICAL_READING3:
//                            Entry e3 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e3);
//                            break;
//                        case CERVICAL_READING4:
//                            Entry e4 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e4);
//                            break;
//                        case CERVICAL_READING5:
//                            Entry e5 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e5);
//                            break;
//                        case CERVICAL_READING6:
//                            Entry e6 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e6);
//                            break;
//                        case CERVICAL_READING7:
//                            Entry e7 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e7);
//                            break;
//                        case CERVICAL_READING8:
//                            Entry e8 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e8);
//                            break;
//                        case CERVICAL_READING9:
//                            Entry e9 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e9);
//                            break;
//                        case CERVICAL_READING10:
//                            Entry e10 = new Entry(timeFloat,Float.parseFloat(dv.getValue()));
//                            tempEntries.add(e10);
//                            break;
//
//                    }
//                }
//            }

//            for(int i=0;i<tempEntries.size();){
//                boolean added = false;
//                for(Entry entry:tempEntries){
//
//                    //Old one
////                    if(entry.getX()==i){
////                        entries.add(entry);
////                        added =true;
////                        break;
////                    }
//
//                    //TO add object at index
//                    if(tempEntries.indexOf(tempEntries.get(i))==i){
//                        entries.add(tempEntries.get(i));
//                        added =true;
//                        break;
//                    }
//                }
//                if(added) {
//                    i++;
//
//                }else{
//                    break;
//                }
//
//            }

//            for(Entry entry:tempEntries){
//                if(entries.size()==0){
//                    entries.add(entry);
//                }else if(entries.size()==1) {
//                    if(entries.get(0).getX()>entry.getX()){
//                        entries.add(0,entry);
//                    }else{
//                        entries.add(entry);
//                    }
//                }else{
//                    boolean added = false;
//                    for(int i=0;i<entries.size()-1;i++){
//                        if(entry.getX()>entries.get(i).getX() && entry.getX()<entries.get(i+1).getX()){
//                            entries.add(i+1,entry);
//                            added = true;
//                            break;
//                        }
//
//                    }
//                    if(!added){
//                        entries.add(entry);
//                    }
//                }
//            }
//
//            for(Entry entry:entries){
//                entry.setX(entry.getX()-entries.get(0).getX());
//            }

            //setup y axis
            YAxis yAxisLet = chart.getAxisLeft();
            yAxisLet.setAxisMaximum(10.0f);
            yAxisLet.setAxisMinimum(4.0f);

            chart.getAxisRight().setEnabled(false);
            chart.setGridBackgroundColor(getResources().getColor(R.color.alert_zone));
            chart.setDrawGridBackground(true);
            chart.setAlpha(1.0f);
            //setup x axis
            XAxis xAxis = chart.getXAxis();
            xAxis.setAxisMinimum(0.0f);
            xAxis.setAxisMaximum(10.0f);

            //create action line
            List<Entry> actionLineEntries = new ArrayList<>();
            actionLineEntries.add(new Entry(4f,4f));
            actionLineEntries.add(new Entry(10f,10f));
            LineDataSet actionLineDataset = new LineDataSet(actionLineEntries,"Action Area");
            int actionZoneColor = getResources().getColor(R.color.action_zone);
            actionLineDataset.setColor(actionZoneColor);
            actionLineDataset.setDrawValues(false);
            actionLineDataset.setFillColor(actionZoneColor);
            actionLineDataset.setDrawFilled(true);
            actionLineDataset.setFillAlpha(255);



            //create Alert Line
            List<Entry> alertLineEntries = new ArrayList<>();
            alertLineEntries.add(new Entry(0f,4f));
            alertLineEntries.add(new Entry(6f,10f));
            LineDataSet alertLineDataset = new LineDataSet(alertLineEntries,"Normal Zone");
            int safeZoneColor = getResources().getColor(R.color.safe_zone);
            alertLineDataset.setColor(safeZoneColor);
            alertLineDataset.setFillColor(safeZoneColor);
            alertLineDataset.setDrawValues(false);
            alertLineDataset.setDrawFilled(true);
            actionLineDataset.setFillAlpha(255);
            alertLineDataset.setHighlightEnabled(true);
            alertLineDataset.setHighlightEnabled(true);

            alertLineDataset.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet iLineDataSet, LineDataProvider lineDataProvider) {
                    return 10f;
                }
            });


            LineDataSet dataSet = new LineDataSet(entries,"Data");//"Starts at: "+ time1);
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.RED);

            List<Entry> dummyEntry = new ArrayList<>();
            dummyEntry.add(new Entry(-1f,-1f));
            LineDataSet dummyDataSet = new LineDataSet(dummyEntry,"Alert Zone");
            dummyDataSet.setColor(getResources().getColor(R.color.alert_zone));

            LineData lineData = new LineData();
            lineData.addDataSet(alertLineDataset);
            lineData.addDataSet(dummyDataSet);
            lineData.addDataSet(actionLineDataset);
            if(entries.size()>0)lineData.addDataSet(dataSet);

            chart.setData(lineData);

            MarkerView mv = new CustomMarker(this.getContext(),R.layout.marker_view);
            mv.setChartView(chart);
            chart.setMarker(mv);
            chart.highlightValue(6f,1);
            //To disable the Legend
//            chart.getLegend().setEnabled(false);


            chart.setPinchZoom(false);
            chart.setTouchEnabled(false);
            Description desc = new Description();
            desc.setText("Action Area");
            chart.setDescription(desc);
            chart.invalidate();

            xAxisLabel.setText("Duration in hrs");
            yAxisLabel.setText("Cervical dilation (cm)");
            xAxisLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chart.saveToGallery(Calendar.getInstance().getTime().toString(),80);
                }
            });
        }
    }

    //To Convert time in ratio of 100
    public static float convertTo100(float input)
    {
        String input_string = Float.toString(input);
        BigDecimal inputBD = new BigDecimal(input_string);
        String hhStr = input_string.split("\\.")[0];
        BigDecimal output = new BigDecimal(Float.toString(Integer.parseInt(hhStr)));
        output = output.add((inputBD.subtract(output).divide(BigDecimal.valueOf(60), 10, BigDecimal.ROUND_HALF_EVEN)).multiply(BigDecimal.valueOf(100)));

        return Float.parseFloat(output.toString());
    }

    // TO Convert time in ratio of 60
    public static String convertTo60(float input)
    {
        String input_string = Float.toString(input);
        BigDecimal inputBD = new BigDecimal(input_string);
        String hhStr = input_string.split("\\.")[0];
        BigDecimal output = new BigDecimal(Float.toString(Integer.parseInt(hhStr)));
        output = output.add((inputBD.subtract(output).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_EVEN)).multiply(BigDecimal.valueOf(60)));

        return output.toString().replace(".",":");
    }

    @Subscribe
    public void onRunProgramRules(final RunProgramRulesEvent event) {
        evaluateRulesAndIndicators(event.getId());
    }

    public EventSaveThread getSaveThread() {
        return saveThread;
    }

    public void setSaveThread(EventSaveThread saveThread) {
        this.saveThread = saveThread;
    }

    public EventDataEntryFragmentForm getForm() {
        return form;
    }

    public void setForm(EventDataEntryFragmentForm form) {
        this.form = form;
    }

    public Map<String, List<ProgramRule>> getProgramRulesForDataElements() {
        return programRulesForDataElements;
    }

    public void setProgramRulesForDataElements(Map<String, List<ProgramRule>> programRulesForDataElements) {
        this.programRulesForDataElements = programRulesForDataElements;
    }

    public Map<String, List<ProgramIndicator>> getProgramIndicatorsForDataElements() {
        return programIndicatorsForDataElements;
    }

    public void setProgramIndicatorsForDataElements(Map<String, List<ProgramIndicator>> programIndicatorsForDataElements) {
        this.programIndicatorsForDataElements = programIndicatorsForDataElements;
    }

    private void showDatePicker(final ProgramStage programStage, DateTime scheduledDueDate) {

//        final DateTime dueDate = new DateTime(1, 1, 1, 1, 0);
        int standardInterval = 0;

        if (programStage.getStandardInterval() > 0) {
            standardInterval = programStage.getStandardInterval();
        }

//        LocalDate currentDate = new LocalDate();

        final DatePickerDialog enrollmentDatePickerDialog =
                new DatePickerDialog(getActivity(),
                        null, scheduledDueDate.getYear(),
                        scheduledDueDate.getMonthOfYear() - 1, scheduledDueDate.getDayOfMonth() + standardInterval);
        enrollmentDatePickerDialog.setTitle(getActivity().getString(R.string.please_enter) + getContext().getString(R.string.due_date_for) + programStage.getDisplayName());
        enrollmentDatePickerDialog.setCanceledOnTouchOutside(true);

        enrollmentDatePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.ok_option),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatePicker dp = enrollmentDatePickerDialog.getDatePicker();
                        DateTime pickedDueDate = new DateTime(dp.getYear(), dp.getMonth() + 1, dp.getDayOfMonth(), 0, 0);
                        scheduleNewEvent(programStage, pickedDueDate);
                        goBackToPreviousActivity();
                    }
                });
        enrollmentDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.cancel_option),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goBackToPreviousActivity();
                    }
                });

        enrollmentDatePickerDialog.show();
    }

    private DateTime calculateScheduledDate(ProgramStage programStage, Enrollment enrollment) {
        DateTime scheduledDate = new DateTime();

        if (programStage.getPeriodType() == null ||
                programStage.getPeriodType().equals("")) {
            List<Event> eventsForEnrollment = new ArrayList<>();
            eventsForEnrollment.addAll(enrollment.getEvents());
            Collections.sort(eventsForEnrollment, new EventDateComparator());
            if(eventsForEnrollment.size()>0) {
                Event lastKnownEvent = eventsForEnrollment.get(eventsForEnrollment.size() - 1);

                if (lastKnownEvent != null) {
                    return new DateTime(lastKnownEvent.getEventDate());
                }
            }

            if (programStage.getProgram().getDisplayIncidentDate()) {
                return new DateTime(enrollment.getIncidentDate());
            } else if (programStage.getGeneratedByEnrollmentDate()) {
                return new DateTime(enrollment.getEnrollmentDate());
            }
        } else {
            //// TODO: 18.04.16  implement periods

        }

        return scheduledDate;
    }

    private void goBackToPreviousActivity() {
        getActivity().finish();
    }

    public void scheduleNewEvent(ProgramStage programStage, DateTime scheduledDueDate) {
        Event event = new Event(form.getEnrollment().getOrgUnit(), Event.STATUS_FUTURE_VISIT,
                form.getEnrollment().getProgram().getUid(), programStage,
                form.getEnrollment().getTrackedEntityInstance(),
                form.getEnrollment().getEnrollment(), scheduledDueDate.toString());
        event.save();
        List<Event> eventsForEnrollment = form.getEnrollment().getEvents();
        eventsForEnrollment.add(event);
        form.getEnrollment().setEvents(eventsForEnrollment);
        form.getEnrollment().save();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            doBack();
            return true;
        }else
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean doBack() {
        List<String> errors = getRowsErrors(getContext(), form);
        if (errors.size() > 0) {
            showErrorAndGoBack();
            return false;
        } else {
            return super.doBack();
        }
    }

    private void showErrorAndGoBack() {

        String title = getContext().getString(R.string.validation_field_title);
        String message = getContext().getString(R.string.validation_field_exit);
        UiUtils.showConfirmDialog(getActivity(),
                title, message,
                getString(R.string.ok_option),
                getString(org.hisp.dhis.android.sdk.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //discard
                        EventDataEntryFragment.this.removeInvalidFields();
                        EventDataEntryFragment.super.doBack();
                    }
                });
    }

    private void removeInvalidFields() {
        for (DataEntryFragmentSection dataEntryFragmentSection : form.getSections()) {
            for (Row row : dataEntryFragmentSection.getRows()) {
                if (row.getValidationError() != null && row.getValue() != null) {
                    row.getValue().delete();
                }
            }
        }
    }


    class CustomMarker extends MarkerView {
        private TextView tvContent;

        public CustomMarker(Context context, int layoutResource) {
            super(context, layoutResource);
            tvContent = (TextView)findViewById(R.id.tv_content);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight){
            if(e instanceof CandleEntry){
                CandleEntry ce = (CandleEntry) e;
                tvContent.setText(""+ Utils.formatNumber(ce.getHigh(),0,true));

            }else{
                //tvContent.setText(""+ Utils.formatNumber(e.getY(),0,true));
                //if(e.getX()>4){
                    tvContent.setText(" Normal Zone");
                //}
            }
            super.refreshContent(e,highlight);
        }
        @Override
        public MPPointF getOffset(){
            return new MPPointF(-getChartView().getWidth()/2,0);
        }

    }
}
