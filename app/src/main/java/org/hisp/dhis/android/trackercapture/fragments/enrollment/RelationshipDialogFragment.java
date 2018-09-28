package org.hisp.dhis.android.trackercapture.fragments.enrollment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.trackercapture.R;

public class RelationshipDialogFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view =  inflater.inflate(R.layout.relationship_popup_dataentry,container);
        final EnrollmentDataEntryFragment fragment = new EnrollmentDataEntryFragment();
        fragment.setArguments(getArguments());
        getChildFragmentManager().beginTransaction().replace(R.id.relationship_nested_dataentry,fragment).commit
                ();
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.dialogFinish();
                dismiss();
            }
        });
        return view;
    }



}
