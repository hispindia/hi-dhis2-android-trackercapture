package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

/**
 * Created by sourabh on 12/5/2017.
 */


import android.app.TimePickerDialog;
import android.content.Context;
import android.media.TimedText;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimePickerRow extends Row {
    private static final String EMPTY_FIELD = "";
    private final boolean mAllowDatesInFuture;

    public TimePickerRow(String label, boolean mandatory, String warning, BaseValue value,
                         boolean allowDatesInFuture) {
        mAllowDatesInFuture = allowDatesInFuture;
        mLabel = label;
        mMandatory = mandatory;
        mValue = value;
        mWarning = warning;

        checkNeedsForDescriptionButton();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;
        DatePickerRowHolder holder;

        if (convertView != null && convertView.getTag() instanceof DatePickerRowHolder) {
            view = convertView;
            holder = (DatePickerRowHolder) view.getTag();
        } else {
            View root = inflater.inflate(
                    R.layout.listview_row_datepicker, container, false);
//            detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout);

            holder = new DatePickerRowHolder(root, inflater.getContext(), mAllowDatesInFuture);


            root.setTag(holder);
            view = root;
        }

        if (!isEditable()) {
            holder.clearButton.setEnabled(false);
            holder.pickerInvoker.setEnabled(false);
        } else {
            holder.clearButton.setEnabled(true);
            holder.pickerInvoker.setEnabled(true);
        }
//        holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));
        holder.updateViews(mLabel, mValue);

//        if(isDetailedInfoButtonHidden()) {
//            holder.detailedInfoButton.setVisibility(View.INVISIBLE);
//        }
//        else {
//            holder.detailedInfoButton.setVisibility(View.VISIBLE);
//        }

        if (mWarning == null) {
            holder.warningLabel.setVisibility(View.GONE);
        } else {
            holder.warningLabel.setVisibility(View.VISIBLE);
            holder.warningLabel.setText(mWarning);
        }

        if (mError == null) {
            holder.errorLabel.setVisibility(View.GONE);
        } else {
            holder.errorLabel.setVisibility(View.VISIBLE);
            holder.errorLabel.setText(mError);
        }

        if (!mMandatory) {
            holder.mandatoryIndicator.setVisibility(View.GONE);
        } else {
            holder.mandatoryIndicator.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.TIME.ordinal();
    }


    private class DatePickerRowHolder {
        final TextView textLabel;
        final TextView mandatoryIndicator;
        final TextView warningLabel;
        final TextView errorLabel;
        final TextView pickerInvoker;
        final ImageButton clearButton;
        //        final View detailedInfoButton;
        final TimePickerListener dateSetListener;
        final OnEditTextClickListener invokerListener;
        final ClearButtonListener clearButtonListener;
        TimePickerDialog picker;

        public DatePickerRowHolder(View root, Context context, boolean allowDatesInFuture) {
            textLabel = (TextView) root.findViewById(R.id.text_label);
            mandatoryIndicator = (TextView) root.findViewById(R.id.mandatory_indicator);
            warningLabel = (TextView) root.findViewById(R.id.warning_label);
            errorLabel = (TextView) root.findViewById(R.id.error_label);
            pickerInvoker = (TextView) root.findViewById(R.id.date_picker_text_view);
            clearButton = (ImageButton) root.findViewById(R.id.clear_text_view);
//            this.detailedInfoButton = detailedInfoButton;

            dateSetListener = new TimePickerListener(pickerInvoker);

//            picker = new TimePickerDialog(context, dateSetListener,
//                    00, 00, true);
//            default one changes on the next line are made to show the clock as spinner

            picker = new TimePickerDialog(context,android.R.style.Theme_Holo_Light_Dialog, dateSetListener,
                   00, 00, true);
//

//            picker = new TimePickerDialog(context,android.R.style.Theme_Holo_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
//                @Override
//                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//
//                    //mTimetext.setText(hourOfDay+ ":" +minute);
//
//                }
//            },0,0,true);
////            picker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            picker.show();

            invokerListener = new OnEditTextClickListener(context, dateSetListener,
                    allowDatesInFuture, picker);
            clearButtonListener = new ClearButtonListener(pickerInvoker);

            clearButton.setOnClickListener(clearButtonListener);
            pickerInvoker.setOnClickListener(invokerListener);
        }

        public void updateViews(String label, BaseValue baseValue) {
            dateSetListener.setBaseValue(baseValue);
            clearButtonListener.setBaseValue(baseValue);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                if(baseValue.getValue()!=null) {
                    Date date = sdf.parse(baseValue.getValue());
                    Calendar calendarDate = Calendar.getInstance();
                    calendarDate.setTime(date);
                    picker.updateTime(calendarDate.get(Calendar.HOUR_OF_DAY),
                            calendarDate.get(Calendar.MINUTE));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            textLabel.setText(label);
            pickerInvoker.setText(baseValue.getValue());
        }
    }

    private static class OnEditTextClickListener implements OnClickListener {
        private final Context context;
        private final TimePickerListener listener;
        private final boolean allowDatesInFuture;
        TimePickerDialog picker;

        public OnEditTextClickListener(Context context,
                                       TimePickerListener listener, boolean allowDatesInFuture, TimePickerDialog picker) {
            this.context = context;
            this.listener = listener;
            this.allowDatesInFuture = allowDatesInFuture;
            this.picker = picker;
        }

        @Override
        public void onClick(View view) {
            picker.show();
        }
    }

    private static class ClearButtonListener implements OnClickListener {
        private final TextView textView;
        private BaseValue value;

        public ClearButtonListener(TextView textView) {
            this.textView = textView;
        }

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        @Override
        public void onClick(View view) {
            textView.setText(EMPTY_FIELD);
            value.setValue(EMPTY_FIELD);
            Dhis2Application.getEventBus()
                    .post(new RowValueChangedEvent(value, DataEntryRowTypes.TIME.toString()));
        }
    }

    private static class TimePickerListener implements TimePickerDialog.OnTimeSetListener {
        private static final String DATE_FORMAT = "%s:%s";
        private final TextView textView;
        private BaseValue value;

        public TimePickerListener(TextView textView) {
            this.textView = textView;
        }

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int mins) {
            String newValue = String.format(DATE_FORMAT, getFixedString(hours),
                    getFixedString(mins));
            if (!newValue.equals(value.getValue())) {
                System.out.println("TimePiker Saving value:" + newValue);
                textView.setText(newValue);
                value.setValue(newValue);
                Dhis2Application.getEventBus()
                        .post(new RowValueChangedEvent(value, DataEntryRowTypes.TIME.toString()));
            }
        }

        private String getFixedString(int number) {
            if (String.valueOf(number).length() == 1) {
                return "0" + number;
            } else {
                return "" + number;
            }
        }
    }
}
