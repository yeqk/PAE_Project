package com.example.pae_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PermissionsListener {

    private static final String TAG = "OffManActivity";

    private static final String HEATMAP_SOURCE_ID = "HEATMAP_SOURCE_ID";
    private static final String HEATMAP_LAYER_ID = "HEATMAP_LAYER_ID";

    private PermissionsManager permissionsManager;

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    // UI elements
    private MapView mapView;
    private MapboxMap map;
    private ProgressBar progressBar;
    private MenuItem downloadButton;
    private MenuItem listButton;
    private MenuItem addWifiButton;

    private boolean isEndNotified;
    private int regionSelected;

    // Offline objects
    private OfflineManager offlineManager;
    private OfflineRegion offlineRegion;

    private LocationComponent locationComponent;


    private HeatMap hm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {

                map = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

// Assign progressBar for later use
                        progressBar = findViewById(R.id.progress_bar);

// Set up the offlineManager
                        offlineManager = OfflineManager.getInstance(MainActivity.this);

                        enableLocationComponent(style);

                        hm = new HeatMap(MainActivity.this);
                        hm.addANT_2GSource(style);
                        hm.addHeatmapLayer(style);
                        hm.addCircleLayer(style);

                    }
                });
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        downloadButton = menu.findItem(R.id.action_download);
        listButton = menu.findItem(R.id.action_list);
        addWifiButton = menu.findItem(R.id.action_wifi);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_download) {
            downloadRegionDialog();

        } else if (id == R.id.action_list) {
            downloadedRegionList();
        } else if (id == R.id.action_wifi) {
            saveWifi();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            locationComponent = map.getLocationComponent();


// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            //Floating action button
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.compass);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Location loc = locationComponent.getLastKnownLocation();
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(loc.getLatitude(), loc.getLongitude()))
                            .build();// Sets the new camera position

                    map.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 7000);
                }
            });

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            map.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void downloadRegionDialog() {
// Set up download interaction. Display a dialog
// when the user clicks download button and require
// a user-provided region name
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        final EditText regionNameEdit = new EditText(MainActivity.this);
        regionNameEdit.setHint(getString(R.string.set_region_name_hint));

// Build the dialog box
        builder.setTitle(getString(R.string.dialog_title))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regionName = regionNameEdit.getText().toString();
// Require a region name to begin the download.
// If the user-provided string is empty, display
// a toast message and do not begin download.
                        if (regionName.length() == 0) {
                            Toast.makeText(MainActivity.this, getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
                        } else {
// Begin download process
                            downloadRegion(regionName);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

// Display the dialog
        builder.show();
    }

    private void downloadRegion(final String regionName) {
// Define offline region parameters, including bounds,
// min/max zoom, and metadata



// Create offline definition using the current
// style and boundaries of visible map area
        String styleUrl = map.getStyle().getUrl();
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        Log.d("Bouuunds:",  bounds.toString());

        //Limit of map size that can be downloaded
        double lat = bounds.getLatNorth()- bounds.getLatSouth();
        double lon = bounds.getLonEast()- bounds .getLonWest();
        double limitLat = 1.0;
        //The map is too large
        if (Math.abs(lat) > limitLat || Math.abs(lon) > 2) {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_map_too_large), Toast.LENGTH_SHORT).show();
        } else {

            // Start the progressBar
            startProgress();

            double minZoom = map.getCameraPosition().zoom;
            double maxZoom = map.getMaxZoomLevel();
            float pixelRatio = this.getResources().getDisplayMetrics().density;
            OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                    styleUrl, bounds, minZoom, maxZoom, pixelRatio);

// Build a JSONObject using the user-defined offline region title,
// convert it into string, and use it to create a metadata variable.
// The metadata variable will later be passed to createOfflineRegion()
            byte[] metadata;
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JSON_FIELD_REGION_NAME, regionName);
                String json = jsonObject.toString();
                metadata = json.getBytes(JSON_CHARSET);
            } catch (Exception exception) {
                Log.d("Enode", "Failed to encode metadata: " + exception.getMessage());
                metadata = null;
            }

// Create the offline region and launch the download
            offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
                @Override
                public void onCreate(OfflineRegion offlineRegion) {
                    Log.d("C", "Offline region created: " + regionName);
                    MainActivity.this.offlineRegion = offlineRegion;
                    launchDownload();
                }

                @Override
                public void onError(String error) {
                    Log.e("Error", error);
                }
            });
        }

    }

    private void launchDownload() {
// Set up an observer to handle download progress and
// notify the user when the region is finished downloading
        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
// Compute a percentage
                double percentage = status.getRequiredResourceCount() >= 0
                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                        0.0;

                if (status.isComplete()) {
// Download complete
                    endProgress(getString(R.string.end_progress_success));
                    return;
                } else if (status.isRequiredResourceCountPrecise()) {
// Switch to determinate state
                    setPercentage((int) Math.round(percentage));
                }

// Log what is being currently downloaded

                Log.d("Progress", String.valueOf(status.getCompletedResourceCount()) + "/" + String.valueOf(status.getRequiredResourceCount()) +
                        "resources; " + String.valueOf(status.getCompletedResourceSize()) + " bytes downloaded.");
            }

            @Override
            public void onError(OfflineRegionError error) {
                Log.e("Error at dwl", error.getReason());
                Log.e("Error at dwl2", error.getMessage());

            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                Log.d("Limit", "Mapbox tile count limit exceeded: " + limit);
                endProgress(getString(R.string.limit_exceeded));
                Toast.makeText(getApplicationContext(), getString(R.string.limit_exceeded), Toast.LENGTH_SHORT).show();

            }
        });

// Change the region state
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }

    private void saveWifi() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        final Location loc = locationComponent.getLastKnownLocation();
        String jsonToAdd = "{ \"type\": \"Feature\", \"properties\": { \"id\": \"name\", \"mag\": 1 }, \"geometry\": { \"type\": \"Point\", \"coordinates\": [" + loc.getLongitude() +"," + loc.getLatitude() + "] } }";

        //{ "type": "Feature", "properties": { "id": "ak16994521", "mag": 1 }, "geometry": { "type": "Point", "coordinates": [ -151.5129, 63.1016, 0.0 ] } },

        // afegir a geojson
        // TRACTAR ERROR SI JA EXISTEIX
        System.out.println("66666666666666666666666666666666666666666666666666666666666666666666666666666666666666");
        addWifiJson(jsonToAdd);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {

                MarkerOptions options = new MarkerOptions();
                options.title("WiFi: WLAN_2534 \nPWD: 2d5f88e2r");
                options.position(new LatLng(  loc.getLatitude() , loc.getLongitude()));
                mapboxMap.addMarker(options);
            }
        });
    }

    private void addWifiJson(String jsonToAdd){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("wifi.geojson")));

            // do reading, usually loop until end of file reading
            String mLine;
            Boolean next = false;
            String GeoOut="";
            while ((mLine = reader.readLine()) != null) {

                if(next)
                    GeoOut += "\n"+jsonToAdd+"\n";

                GeoOut +="\n"+mLine+"\n";

                next=false;
                if(mLine.equals( "\"features\": ["))
                    next=true;
            }
            System.out.println(GeoOut);
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

    private void downloadedRegionList() {
// Build a region list when the user clicks the list button

// Reset the region selected int to 0
        regionSelected = 0;

// Query the DB asynchronously
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
// Check result. If no regions have been
// downloaded yet, notify user and return
                if (offlineRegions == null || offlineRegions.length == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show();
                    return;
                }

// Add all of the region names to a list
                ArrayList<String> offlineRegionsNames = new ArrayList<>();
                for (OfflineRegion offlineRegion : offlineRegions) {
                    offlineRegionsNames.add(getRegionName(offlineRegion));
                }
                final CharSequence[] items = offlineRegionsNames.toArray(new CharSequence[offlineRegionsNames.size()]);

// Build a dialog containing the list of regions
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.navigate_title))
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
// Track which region the user selects
                                regionSelected = which;
                            }
                        })
                        .setPositiveButton(getString(R.string.navigate_positive_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Toast.makeText(MainActivity.this, items[regionSelected], Toast.LENGTH_LONG).show();

// Get the region bounds and zoom
                                LatLngBounds bounds = ((OfflineTilePyramidRegionDefinition)
                                        offlineRegions[regionSelected].getDefinition()).getBounds();
                                double regionZoom = ((OfflineTilePyramidRegionDefinition)
                                        offlineRegions[regionSelected].getDefinition()).getMinZoom();

// Create new camera position
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(bounds.getCenter())
                                        .zoom(regionZoom)
                                        .build();

// Move camera to new position
                                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            }
                        })
                        .setNeutralButton(getString(R.string.navigate_neutral_button_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
// Make progressBar indeterminate and
// set it to visible to signal that
// the deletion process has begun
                                progressBar.setIndeterminate(true);
                                progressBar.setVisibility(View.VISIBLE);

// Begin the deletion process
                                offlineRegions[regionSelected].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                                    @Override
                                    public void onDelete() {
// Once the region is deleted, remove the
// progressBar and display a toast
                                        progressBar.setVisibility(View.INVISIBLE);
                                        progressBar.setIndeterminate(false);
                                        Toast.makeText(getApplicationContext(), getString(R.string.toast_region_deleted),
                                                Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        progressBar.setIndeterminate(false);
                                        Log.e("error", error);
                                    }
                                });
                            }
                        })
                        .setNegativeButton(getString(R.string.navigate_negative_button_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
// When the user cancels, don't do anything.
// The dialog will automatically close
                            }
                        }).create();
                dialog.show();

            }

            @Override
            public void onError(String error) {
                Log.e("error", error);
            }
        });
    }

    private String getRegionName(OfflineRegion offlineRegion) {
// Get the region name from the offline region metadata
        String regionName;

        try {
            byte[] metadata = offlineRegion.getMetadata();
            String json = new String(metadata, JSON_CHARSET);
            JSONObject jsonObject = new JSONObject(json);
            regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
        } catch (Exception exception) {
            Log.e("error", "Failed to decode metadata: " + exception.getMessage());
            regionName = String.format(getString(R.string.region_name), offlineRegion.getID());
        }
        return regionName;
    }

    // Progress bar methods
    private void startProgress() {
// Disable buttons

        downloadButton.setEnabled(false);
        listButton.setEnabled(false);

// Start and show the progress bar
        isEndNotified = false;
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
// Don't notify more than once
        if (isEndNotified) {
            return;
        }

// Enable buttons
        downloadButton.setEnabled(true);
        listButton.setEnabled(true);

// Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

// Show a toast
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
