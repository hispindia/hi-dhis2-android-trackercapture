package org.hisp.dhis.android.trackercapture.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xelvias on 11/22/17.
 */

public class ReportSingleEntityModel {
    String houseHoldSIN;
    List<ReportRowModel> rows;

    public ReportSingleEntityModel(String houseHoldSIN) {
        this.houseHoldSIN = houseHoldSIN;
        rows =  new ArrayList<>();
    }


    public String getHouseHoldSIN() {
        return houseHoldSIN;

    }

    public void setHouseHoldSIN(String houseHoldSIN) {
        this.houseHoldSIN = houseHoldSIN;
    }

    public List<ReportRowModel> getRows() {
        return rows;
    }

    public void addRow(ReportRowModel model){
        rows.add(model);
    }
}
