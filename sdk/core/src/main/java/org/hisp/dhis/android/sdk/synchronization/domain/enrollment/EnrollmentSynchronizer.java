package org.hisp.dhis.android.sdk.synchronization.domain.enrollment;


import android.util.Log;


import com.google.gson.Gson;
import com.pddstudio.urlshortener.URLShortener;
import com.squareup.okhttp.HttpUrl;

import org.hisp.dhis.android.sdk.controllers.DhisController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.Httphandler;
import org.hisp.dhis.android.sdk.network.APIException;
import org.hisp.dhis.android.sdk.network.DhisApi;
import org.hisp.dhis.android.sdk.network.Session;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.persistence.models.ImportSummary;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.synchronization.domain.common.Synchronizer;
import org.hisp.dhis.android.sdk.synchronization.domain.event.EventSynchronizer;
import org.hisp.dhis.android.sdk.synchronization.domain.event.IEventRepository;
import org.hisp.dhis.android.sdk.synchronization.domain.faileditem.IFailedItemRepository;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnrollmentSynchronizer extends Synchronizer {
    IEnrollmentRepository mEnrollmentRepository;
    IEventRepository mEventRepository;
    IFailedItemRepository mFailedItemRepository;
    public static final String PARTOGRAM = "bNhQuFV59bs";
    public static final String PARTOGRAM_STAGE = "kl1qQxjtlyx";
    LinkedHashMap<Float,Float> dvMapped_value = new LinkedHashMap<>();
    List<String> keyValue = new ArrayList<String>();
    List<String> value = new ArrayList<String>();
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
    public static  String orgnumber;
//    public static final String PARTOGRAPH_DATASTORE = "http://apps.hispindia.org/icmr/api/dataStore/partograph";
    public static final String PARTOGRAPH_DATASTORE = "/dataStore/partograph";
//    public static final String API_ME = "http://apps.hispindia.org/icmr/api/me.json";
    public static final String API_ME = "/me.json";
//    public static  String API_ORG = "http://apps.hispindia.org/icmr/api/organisationUnits/";
    public static  String API_ORG = "/organisationUnits/";
    public static  String PATIENT_NAME;

    public EnrollmentSynchronizer(IEnrollmentRepository enrollmentRepository,
            IEventRepository eventRepository,
            IFailedItemRepository failedItemRepository) {
        super(failedItemRepository);

        mEnrollmentRepository = enrollmentRepository;
        mEventRepository = eventRepository;
        mFailedItemRepository = failedItemRepository;
    }

    public void sync(Enrollment enrollment) {
        boolean existsOnServerPreviously = enrollment.getCreated() != null;

        if (existsOnServerPreviously) {
            syncEvents(enrollment.getLocalId());
            if (syncEnrollment(enrollment))
                changeEnrollmentToSynced(enrollment);
        } else {
            if (syncEnrollment(enrollment))
            {
                syncEvents(enrollment.getLocalId());

                if ((enrollment.getStatus().equals(Enrollment.CANCELLED) ||
                        enrollment.getStatus().equals(Enrollment.COMPLETED))) {
                    //Send again because new enrollment is create as Active on server then
                    // Its necessary to change status from Active to Cancelled or Completed
                    if (syncEnrollment(enrollment));
                        changeEnrollmentToSynced(enrollment);
                }
                else{
                    changeEnrollmentToSynced(enrollment);
                }
            }
        }
    }

    public void sync(List<Enrollment> enrollments) {
        Collections.sort(enrollments, new Enrollment.EnrollmentComparator());

        for (Enrollment enrollment : enrollments) {
            sync(enrollment);
        }
    }

    private boolean syncEnrollment(Enrollment enrollment) {
        boolean isSyncSuccess = true;

        try {
            ImportSummary importSummary = mEnrollmentRepository.sync(enrollment);

            if (importSummary.isSuccessOrOK()) {
                for(int p=0;p<enrollment.getAttributes().size();p++)
                {
                    if(enrollment.getAttributes().get(p).getTrackedEntityAttributeId().equals("P_NAME")){
                        PATIENT_NAME=enrollment.getAttributes().get(p).getValue();
                        if(PATIENT_NAME == null) PATIENT_NAME = "Unnamed";
                    }

                }

                isSyncSuccess = true;

            } else if (importSummary.isError()) {
                super.handleImportSummaryError(importSummary, FailedItem.ENROLLMENT, 200,
                        enrollment.getLocalId());
                isSyncSuccess = false;
            }
        } catch (APIException api) {
            super.handleSerializableItemException(api, FailedItem.ENROLLMENT,
                    enrollment.getLocalId());
            isSyncSuccess = false;
        }

        return isSyncSuccess;
    }

    private void changeEnrollmentToSynced(Enrollment enrollment) {
        enrollment.setFromServer(true);
        mEnrollmentRepository.save(enrollment);
        super.clearFailedItem(FailedItem.ENROLLMENT, enrollment.getLocalId());
    }


    private void syncEvents(long enrollmentId) {
        Httphandler sh = new Httphandler();

        EventSynchronizer eventSynchronizer = new EventSynchronizer(mEventRepository,
                mFailedItemRepository);

        List<Event> events = mEventRepository.getEventsByEnrollment(enrollmentId);
        List<Event> eventsToBeRemoved = mEventRepository.getEventsByEnrollmentToBeRemoved(
                enrollmentId);

        if (eventsToBeRemoved != null && eventsToBeRemoved.size() > 0) {
            eventSynchronizer.syncRemovedEvents(eventsToBeRemoved);
        }


        HashMap<String,DataValue> dvMapped = new HashMap<>();
        String[] dataElements = {CERVICAL_READING1,CERVICAL_READING2,CERVICAL_READING3,CERVICAL_READING4,
                CERVICAL_READING5,CERVICAL_READING6,CERVICAL_READING7,CERVICAL_READING8,CERVICAL_READING9,CERVICAL_READING10};
        String[] timeElements = {CERVICAL_TIME1,CERVICAL_TIME2,CERVICAL_TIME3,CERVICAL_TIME4,CERVICAL_TIME5
                ,CERVICAL_TIME6,CERVICAL_TIME7,CERVICAL_TIME8,CERVICAL_TIME9,CERVICAL_TIME10};

        if (events != null && events.size() > 0) {

            for(int i=0;i<events.size();i++)
            {
                if(events.get(i).getProgramStageId().equals(PARTOGRAM_STAGE))
                {
                        for(DataValue dv:events.get(i).getDataValues()) {
                            if (dv != null && !dv.getValue().equals("")) {
                                dvMapped.put(dv.getDataElement(), dv);
                            }
                        }


                                for(int k=0;k<dataElements.length;k++){
                                    DataValue dvData = dvMapped.get(dataElements[k]);
                                    DataValue dvTime = dvMapped.get(timeElements[k]);
                                    if(dvData!=null && dvTime!=null) {
                                        float x = Float.parseFloat(dvTime.getValue().replace(":", "."));
                                        float y = Float.parseFloat(dvData.getValue());
                                        dvMapped_value.put(x,y);
                                    }
                                }
                    Set mapSet = (Set) dvMapped_value.entrySet();
                    Iterator mapIterator = mapSet.iterator();

                    while (mapIterator.hasNext()) {
                        Map.Entry mapEntry = (Map.Entry) mapIterator.next();
                        // getKey Method of HashMap access a key of map
                        keyValue.add(mapEntry.getKey().toString());
                        value.add(mapEntry.getValue().toString());
                    }

                    for (int j=0;j<keyValue.size()-1;j++)
                    {
                        if(Float.parseFloat(keyValue.get(j+1))-Float.parseFloat(keyValue.get(0))>=4 )
                        {
                            if(Float.parseFloat(value.get(j+1))<=Float.parseFloat(keyValue.get(j+1))-Float.parseFloat(keyValue.get(0)))
                            {
                                //API_ORG=API_ORG+events.get(i).getOrganisationUnitId();
                                OrganisationUnit organisationUnit = MetaDataController.getOrganisationUnit(events.get(i).getOrganisationUnitId());


//                                String partoteis=  sh.makeServiceCall(API_ORG);
//                                if (partoteis != null) {
//                                    try {
//
//                                        JSONObject jsonObj = new JSONObject(partoteis);
////                                        JSONStr meorg = jsonObj.getJSONArray("phoneNumber");
//                                         orgnumber= jsonObj.getString("phoneNumber");
//                                        Log.e("orgnumber",orgnumber);
//                                    }
//                                    catch (final JSONException e) {
//                                  Log.e("Error",e.toString());
//                                    }
//                                }
//                                else {
//                                }


                                orgnumber = organisationUnit.getPhoneNumber();
                                DhisApi dhisApi = DhisController.getInstance().getDhisApi();

                                String teiID = events.get(i).getTrackedEntityInstance();
                                TrackedEntityInstance trackedEntityInstance = dhisApi.getTrackedEntityInstance(teiID, null);
                                if(trackedEntityInstance!=null){
                                    List<TrackedEntityAttributeValue> attributes = trackedEntityInstance.getAttributes();
                                    for(TrackedEntityAttributeValue val :attributes){
                                        if(val.getTrackedEntityAttributeId().equals("briL4htZesc")){
                                            PATIENT_NAME = val.getValue();
                                        }
                                    }
                                }

                                if(PATIENT_NAME == null) PATIENT_NAME = "Un named";
                                Map<String, String> partograph = null;

                                try{
                                    partograph= dhisApi.getDataStoreValues("partograph",
                                           teiID );

                                }catch (APIException ex){
                                    Log.d(getClass().getSimpleName(),ex.getMessage());
                                }
                                boolean smsSent = false;
                                if(partograph !=null && partograph.size()>0){
                                    String value = partograph.get(teiID);
                                    if(value.equals("true"))smsSent = true;
                                }

                                if(!smsSent){
                                    JSONObject bodyJson = new JSONObject();
                                    String body = null;
                                    try {
                                        bodyJson.put(teiID,true);
                                        body = bodyJson.toString();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    //get url or use the default one
                                    Session session = DhisController.getInstance().getSession();
                                    HttpUrl httpUrl = null;
                                    if (session != null) {
                                        httpUrl = session.getServerUrl();
                                    }

                                    String urlDomain = "http://apps.hispindia.org/icmr" ;
                                    Log.i(" Domain",urlDomain);
                                    if(httpUrl!=null)urlDomain = httpUrl.toString();

                                    String url = urlDomain + "/dhis-web-reporting/generateHtmlReport.action?uid=VxG5TioFCcc&ou=so2OuX5N8MH&tei="+events.get(i).getTrackedEntityInstance();

                                    //String shortUrl = URLShortener.shortUrl("http://apps.hispindia.org/icmr/dhis-web-reporting/generateHtmlReport.action?uid=VxG5TioFCcc&ou=so2OuX5N8MH&tei="+events.get(i).getTrackedEntityInstance());

                                    String shortUrl = URLShortener.shortUrl(url);
//                                    URI uri = null;
//                                    try {
//                                        uri = new URI("http","bulksms.mysmsmantra.com",8080,"/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno="+orgnumber+"&message="+"Patient: "+PATIENT_NAME+"  "+shortUrl,null);
//                                        Log.i(" Built URI",uri.toASCIIString());
//                                        Log.i(" Built URI String",uri.toString());
//                                    } catch (URISyntaxException e) {
//                                        e.printStackTrace();
//                                    }
                                    String message = "Patient: "+PATIENT_NAME+"  "+shortUrl;
                                    String encodedmessage = null;
                                    try{
                                        String encodedString = URLEncoder.encode(message, "UTF-8");
                                        encodedmessage = encodedString;
                                        System.out.format("'%s'\n", encodedString);
                                    }catch(Exception ex){
                                        System.out.println("ex");
                                    }

                                    //if(uri!=null){
//                                        sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno="+orgnumber+"&message="+"Patient: "+PATIENT_NAME+"  "+shortUrl);
//                                        Log.e("longurl","http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno="+orgnumber+"&message="+"Patient: "+PATIENT_NAME+"  "+shortUrl);
                                    if(encodedmessage==null){
                                        sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno="+orgnumber+"&message="+shortUrl);
                                        Log.e("longUrl","http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno="+orgnumber+"&message="+shortUrl);

                                    }else{
                                        sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno="+orgnumber+"&message="+encodedmessage);
                                    }

                                        //partograph.remove(teiID);

                                        //sh.makeServiceCall(uri.toASCIIString());

                                        if(partograph!=null){
                                            partograph.put(teiID,"true");
                                            //String body = "{'"+teiID+"':true}";
                                            dhisApi.putDataStoreValues("partograph",
                                                    teiID ,partograph);
                                        }else{
                                            partograph = new HashMap<>();
                                            partograph.put(teiID,"true");
                                            // = "{'"+teiID+"':true}";
                                            dhisApi.postDataStoreValues("partograph",
                                                    teiID ,partograph);
                                        }
                                    //}

                                }
                            }
                        }
                    }
                }

                }
            }
            eventSynchronizer.sync(events);
        }


    }


