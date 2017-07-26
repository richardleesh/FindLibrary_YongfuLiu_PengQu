package com.example.liu_qu.findlibrary_yongfuliu_pengqu;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Permission;
import java.util.Vector;
import java.util.jar.Manifest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private PopupWindow popUpWindow;

    private GoogleMap mMap;
    public static final LatLng toronto_southwest_corner = new LatLng(43.580306, -79.639703);
    public static final LatLng toronto_northeast_corner = new LatLng(43.855875, -79.115062);
    public static final LatLngBounds toronto_latLngBounds = new LatLngBounds(toronto_southwest_corner, toronto_northeast_corner);


    //Qu_11111111111111111111111111111111111111111111111111111111111111
    //Class DownLoadData
    public class DownLoadData{
        public String id;
        public String libraryName;
        public String link;
        public String address;
        public String phoneNumber;
        public String coordinates;

        public DownLoadData(){super();};



        public DownLoadData(String id, String libraryName, String link,
                            String address, String phoneNumber, String coordinates){
            this.id=id;
            this.libraryName=libraryName;
            this.link=link;
            this.address=address;
            this.phoneNumber=phoneNumber;
            this.coordinates=coordinates;

        }
    }

    Vector<DownLoadData> downLoadDatas = new Vector<DownLoadData>();
    //Qu_11111111111111111111111111111111111111111111111111111111111111





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        //set place seach
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("selected", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("error", "An error occurred: " + status);
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        //Qu_2222222222222222222222222222222222222222222222222222222222222222222222
        //instantiate a URL object used to point to the kml resource
        URL url = null;
        try{
            url = new URL("http://www.torontopubliclibrary.ca/data/library-data.kml");
        }catch (Exception e){
            Log.d("AN EXCEPTION HAPPENED", e.getMessage());
        }



        //use our custom AsyncTask to download (and process) the xml resource available at the indicated URL
        //call a framework method that will eventually call doInBackground found below
        DownloadAndParseKMLTask task=new DownloadAndParseKMLTask(this);
        task.execute(url);
        //Qu_2222222222222222222222222222222222222222222222222222222222222222222222


    }


    //Qu_3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333
    private class DownloadAndParseKMLTask extends AsyncTask<URL, Integer, Long> {
        MapsActivity mapsActivity=null;
        private DownloadAndParseKMLTask(MapsActivity ma)
        {
            mapsActivity=ma;
        }
        //FYI - doInBackground is a "slot" method where we have to fill in its behaviour to compile
        /*the framework requires us to be able to accept more than one "task",
        so this method has to be able to accept multiple arguments (variadic)
         in this way it's more reusable in different application versions
         */
        @Override
        protected Long doInBackground(URL... urls) {
            long count = urls.length;//this tells us how many urls we have to process. in this app it will always be one URL passed
            long totalSize = 0; // count how many files we have actually downloaded

            //loop to download files.
            for (int i = 0; i < count; i++) {
                //delegate the actual file download to a method in MainActivity (so we can access all the functionality of an Android Context)
                downloadFile(urls[i]);
                totalSize++;
            }

            return totalSize;//tell how many urls will downloaded
        }

        //FYI the methods below are "hook" methods because they already have default implementations that we can override

        //onPostExecute will be called by the framework when our AsyncTask finished
        protected void onPostExecute(Long result){
            //once finished all we do here is print out the headlines
            //you will extend this idea to refresh any views (if necessary)
            Log.d("ASYNCTASK COMPLETE", "Downloaded " + result + "files");
            //Log.d("ASYNCTASK COMPLETE", "Printing " + headlines.size() + "headlines");

            //mapsActivity.updateHeadlines();

        }
    }

    public void downloadFile(URL url){

        //we need a try-catch block right away - almost all this stuff can throw exceptions
        try{
            //create a new http url connection
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //create an InputStreamReader that we can use to read data from the http url connection
            InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
            //do something with the data
            /*
            //display the downloaded xml for debugging purposes
            //wrap the inputStreamReader in a BufferedReader
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //create a string variable to store each line as we read it from bufferedReader
            String inputline = null;
            //here's a cool one liner to read a line and make sure it's not null (in case of EOF)
            while((inputline = bufferedReader.readLine()) != null){
                //display for debugging purposes
                Log.d("HEADLINES DOWNLOADED", inputline);
            }
            */

            //now that we have the downloaded file, use the XMLPullParser API to parse the xml and extract the information we need
            //get an instance of XmlPullParser from XmlPullParserFactory (recall: factory pattern)
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            //configure the factory to create the specific xml parser we will use
            factory.setNamespaceAware(true);
            //get the XmlPullParser instance from the factory
            XmlPullParser xmlPullParser = factory.newPullParser();
            //tell the xmlpullparser where to get its data from
            //note: we can pass a variety of IO streaming type classes here
            xmlPullParser.setInput(inputStreamReader);
            //recall: XmlPullParser communicates with your code using "Events"
            int event = xmlPullParser.getEventType();

            //pattern: use a flag to remember that we are inside a title element
            boolean insidePlacemark=false;
            boolean insideName = false;
            boolean insideDescription=false;
            boolean insideAddress=false;
            boolean insidePhoneNumber=false;
            boolean insideCoordinates=false;

            DownLoadData downLoadData=new DownLoadData();

            /*
            <Placemark id="LIB02">
            <name>Agincourt</name>
            <description>
            Address: 155 Bonis Ave., Toronto, ON, M1T 3W6<br/>Link:
            http://www.torontopubliclibrary.ca/detail.jsp?R=LIB02
            </description>
            <address>155 Bonis Ave., Toronto, ON, M1T 3W6</address>
            <phoneNumber>416-396-8943</phoneNumber>
            <Point>
            <coordinates>-79.29342962962961,43.78516666666665</coordinates>
            </Point>
            </Placemark>
             */
            while(event != XmlPullParser.END_DOCUMENT){
                //process events inside this loop
                if(event == XmlPullParser.START_DOCUMENT)
                { //this will be executed at the start of the document
                    Log.d("PARSING XML", "We reached the start of the document");
                }
                else if(event == XmlPullParser.START_TAG)
                { //this will be executed at the start of EACH tag (element)
                    String tagName = xmlPullParser.getName();
                    Log.d("PARSING XML", "We reached the start of tag: " + tagName);
                    //TODO: you can use the name here to determine whether you've reached a news "item" and/or "link" in the xml
                    if(tagName.equalsIgnoreCase("placemark")){
                        downLoadData.id = xmlPullParser.getAttributeValue(0);
                    }
                    else if(tagName.equalsIgnoreCase("name")){
                        //store this title in the headlines

                        Log.d("PARSING XML", "found a title tag: " + tagName);
                        insideName = true;
                    }
                    else if(tagName.equalsIgnoreCase("description"))
                    {
                        insideDescription=true;
                    }
                    else if(tagName.equalsIgnoreCase("address")){

                        insideAddress=true;
                    }
                    else if (tagName.equalsIgnoreCase("phoneNumber")){
                        insidePhoneNumber=true;
                    }
                    else if (tagName.equalsIgnoreCase("coordinates")){
                        insideCoordinates=true;
                    }
                }else if(event == XmlPullParser.END_TAG)
                { //executed at the end of each tag (element)
                    Log.d("PARSING XML", "We reached the end of tag: " + xmlPullParser.getName());
                    String tagName=xmlPullParser.getName();
                    if(tagName.equalsIgnoreCase("placemark"))
                    {
                        downLoadDatas.add(downLoadData);
                        downLoadData=new DownLoadData();
                    }
                    insideName = false;
                    insideDescription=false;
                    insideAddress=false;
                    insidePhoneNumber=false;
                    insidePlacemark=false;
                    insideCoordinates=false;
                }else if(event == XmlPullParser.TEXT){
                    String text = xmlPullParser.getText();
                    Log.d("PARSING XML", "found text: " + text);
                    if(insideName){
                        downLoadData.libraryName=text;
                        //headlines.add(text);
                    }
                    else if(insideDescription)
                    {
                        downLoadData.link=text;
                        //headlineLinks.add(text);
                    }
                    else if (insideAddress){
                        downLoadData.address=text;
                    }
                    else if (insidePhoneNumber){
                        downLoadData.phoneNumber=text;
                    }
                    else if (insideCoordinates){
                        downLoadData.coordinates=text;
                    }
                }
                //don't forget to get the next event in the xml file
                event = xmlPullParser.next();
            }
            Log.d("PARSING XML", "reached the end of the file");


        } catch (Exception e){
            Log.d("DOWNLOAD ERROR", e.getMessage());
        }
    }
    //Qu_3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setLatLngBoundsForCameraTarget(toronto_latLngBounds);


        //set marker clicker listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

               displayLibrary(marker);
                return false;
            }
        });

        //set map
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        // Add a library marker in Toronto and move the camera
        for(DownLoadData dd : downLoadDatas) {
            String  coordinate = dd.coordinates;
            String[] string_latlng = coordinate.split(",");
            double longitude = Double.parseDouble(string_latlng[0]);
            double latitude = Double.parseDouble(string_latlng[1]);
            LatLng library = new LatLng(latitude,longitude);
            mMap.addMarker(new MarkerOptions()
                    .title(dd.libraryName)
                    .snippet(dd.address)
                    .position(library));
        }

        LatLng toronto = new LatLng(43.653908, -79.384293);
        //set map rang in 5000 meters
        Circle circle = mMap.addCircle(new CircleOptions().center(toronto).radius(5000).strokeColor(Color.RED));
        circle.setVisible(false);
        //convert this circle range to zoom level number
        //Note: !!!getZommLevel() function is borrowed from stackoverflow
        //Note: !!!see details for this code source
        int zoomlevel = getZoomLevel(circle);


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, zoomlevel));

       

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(toronto_latLngBounds, 0));
            }
        });

    }


    //convert kilometers to zoom level
    //!!! note: external code from: https://stackoverflow.com/questions/6002563/android-how-do-i-set-the-zoom-level-of-map-view-to-1-km-radius-around-my-curren
    public int getZoomLevel(Circle circle) {
        int zoomLevel = 13;
        if (circle != null){
            double radius = circle.getRadius();
            double scale = radius / 500;
             zoomLevel=(int) (16 - Math.log(scale) / Math.log(2));
        }
        return  zoomLevel;
    }


    //Display library info
    private void displayLibrary(Marker marker) {
        initiatePopupWindow(marker) ;
    }







    //set up pop up window to display libarary when a map marker is clicked
    private PopupWindow pw;

    private void initiatePopupWindow(Marker marker) {
        try {
            //We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
            //Inflate the view from a predefined XML layout
            View layout = inflater.inflate(R.layout.popup_window_layout, (ViewGroup) findViewById(R.id.popup_window));
            // create a 300px width and 470px height PopupWindow
            pw = new PopupWindow(layout, 600, 700, true);
            // display the popup in the center
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);


            Button cancelButton = (Button) layout.findViewById(R.id.cancel);
            cancelButton.setOnClickListener(cancel_button_click_listener);

            TextView libaryNameView = (TextView) findViewById(R.id.name);
            String libaryName = marker.getTitle();
            for(DownLoadData dd : downLoadDatas) {
                if(libaryName.equalsIgnoreCase(dd.libraryName)){
                    libaryNameView.setText(dd.address);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener cancel_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
        }
    };
    //end of set up pop up window to display libarary when a map marker is clicked




}
