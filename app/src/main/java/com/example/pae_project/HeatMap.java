package com.example.pae_project;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapWeight;

public class HeatMap {


    private static final String ANT_2G_SOURCE_ID = "ANT_2Gs";
    private static final String ANT_3G_SOURCE_ID = "ANT_3Gs";
    private static final String ANT_4G_SOURCE_ID = "ANT_4Gs";
    private static final String HEATMAP_LAYER_2G_ID = "ANT_2Gs-heat";
    private static final String HEATMAP_LAYER_3G_ID = "ANT_3Gs-heat";
    private static final String HEATMAP_LAYER_4G_ID = "ANT_4Gs-heat";
    private static final String CIRCLE_LAYER_2G_ID = "ANT_2Gs-circle";
    private static final String CIRCLE_LAYER_3G_ID = "ANT_3Gs-circle";
    private static final String CIRCLE_LAYER_4G_ID = "ANT_4Gs-circle";


    Context ctx;

    private int index = 0;

    private Expression[] listOfHeatmapColors;
    private Expression[] listOfHeatmapRadiusStops;
    private Float[] listOfHeatmapIntensityStops;

    private ValueAnimator attractionsColorAnimator;

    private Style style;

    public HeatMap(Context ctx, Style st) {
        this.ctx = ctx;
        style = st;
    }

    public void addANT_2GSource(@NonNull Style loadedMapStyle, Boolean fromAssets) {
            GeoJsonSource antenes2g = new GeoJsonSource(ANT_2G_SOURCE_ID, loadJsonFromAsset("datos_2g_default.geojson"));
            loadedMapStyle.addSource(antenes2g);
            GeoJsonSource antenes3g = new GeoJsonSource(ANT_3G_SOURCE_ID, loadJsonFromAsset("datos_3g_default.geojson"));
            loadedMapStyle.addSource(antenes3g);
            GeoJsonSource antenes4g = new GeoJsonSource(ANT_4G_SOURCE_ID, loadJsonFromAsset("datos_4g_default.geojson"));
            loadedMapStyle.addSource(antenes4g);

    }

    public void update2g(@NonNull Style loadedMapStyle) {
        GeoJsonSource ss2 = loadedMapStyle.getSourceAs(ANT_2G_SOURCE_ID);
        ss2.setGeoJson(loadFromInternalStorage("data_2g_reales.geojson"));
    }
    public void update3g(@NonNull Style loadedMapStyle) {
        GeoJsonSource ss3 = loadedMapStyle.getSourceAs(ANT_3G_SOURCE_ID);
        ss3.setGeoJson(loadFromInternalStorage("data_3g_reales.geojson"));
    }
    public void update4g(@NonNull Style loadedMapStyle) {
        GeoJsonSource ss4 = loadedMapStyle.getSourceAs(ANT_4G_SOURCE_ID);
        ss4.setGeoJson(loadFromInternalStorage("data_4g_reales.geojson"));
    }

    public void addHeatmapLayer(@NonNull Style loadedMapStyle) {
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_2G_ID, ANT_2G_SOURCE_ID);
        layer.setMaxZoom(9);
        layer.setSourceLayer(ANT_2G_SOURCE_ID);
        layer.setProperties(

// Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
// Begin color ramp at 0-stop with a 0-transparency color
// to create a blur-like effect.
                heatmapColor(
                        interpolate(
                                linear(), heatmapDensity(),
                                literal(0), rgba(33, 102, 172, 0),
                                literal(0.2), rgba(103, 169, 207, 0.5),
                                literal(0.4), rgba(209, 229, 240, 0.5),
                                literal(0.6), rgba(253, 219, 199, 0.5),
                                literal(0.8), rgba(239, 138, 98, 0.5) ,
                                literal(1), rgba(178, 24, 43 ,0.5)
                        )
                ),

// Adjust the heatmap radius by zoom level
                heatmapRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 2),
                                stop(9, 15)
                        )
                ),

// Transition from heatmap to circle layer by zoom level
                heatmapOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 0.7),
                                stop(9, 0.7)
                        )
                )
        );

        loadedMapStyle.addLayerAbove(layer, "waterway-label");

        //-----------------------------------3G-------------------------------------
        HeatmapLayer layer3g = new HeatmapLayer(HEATMAP_LAYER_3G_ID, ANT_3G_SOURCE_ID);
        layer3g.setMaxZoom(9);
        layer3g.setSourceLayer(ANT_3G_SOURCE_ID);
        layer3g.setProperties(

// Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
// Begin color ramp at 0-stop with a 0-transparency color
// to create a blur-like effect.
                heatmapColor(
                        interpolate(
                                linear(), heatmapDensity(),
                                literal(0), rgba(33, 102, 172, 0),
                                literal(0.2), rgba(103, 169, 207, 0.5),
                                literal(0.4), rgba(209, 229, 240, 0.5),
                                literal(0.6), rgba(253, 219, 199, 0.5),
                                literal(0.8), rgba(239, 138, 98, 0.5) ,
                                literal(1), rgba(178, 24, 43 ,0.5)
                        )
                ),


// Adjust the heatmap radius by zoom level
                heatmapRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 2),
                                stop(9, 15)
                        )
                ),

// Transition from heatmap to circle layer by zoom level
                heatmapOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 0.7),
                                stop(9, 0.7)
                        )
                )
        );

        loadedMapStyle.addLayerAbove(layer3g, HEATMAP_LAYER_2G_ID);

        //------------------------------------4G-------------------------

        HeatmapLayer layer4g = new HeatmapLayer(HEATMAP_LAYER_4G_ID, ANT_4G_SOURCE_ID);
        layer4g.setMaxZoom(9);
        layer4g.setSourceLayer(ANT_4G_SOURCE_ID);
        layer4g.setProperties(

// Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
// Begin color ramp at 0-stop with a 0-transparency color
// to create a blur-like effect.
                heatmapColor(
                        interpolate(
                                linear(), heatmapDensity(),
                                literal(0), rgba(33, 102, 172, 0),
                                literal(0.2), rgba(103, 169, 207, 0.5),
                                literal(0.4), rgba(209, 229, 240, 0.5),
                                literal(0.6), rgba(253, 219, 199, 0.5),
                                literal(0.8), rgba(239, 138, 98, 0.5) ,
                                literal(1), rgba(178, 24, 43 ,0.5)
                        )
                ),

// Adjust the heatmap radius by zoom level
                heatmapRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 2),
                                stop(9, 15)
                        )
                ),

// Transition from heatmap to circle layer by zoom level
                heatmapOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 0.7),
                                stop(9, 0.7)
                        )
                )
        );

        loadedMapStyle.addLayerAbove(layer4g, HEATMAP_LAYER_3G_ID);
    }

    public void addCircleLayer(@NonNull Style loadedMapStyle) {
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_2G_ID, ANT_2G_SOURCE_ID);
        circleLayer.setProperties(

// Size circle radius by ANT_2G magnitude and zoom level
                PropertyFactory.circleRadius(
                        4.0f
                ),

// Color circle by ANT_2G magnitude
                circleColor(
                        rgba(0, 0, 255, 0.7)
                ),


                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
        );

        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_2G_ID);

        //------------------------------------3G---------------------------------
        CircleLayer circleLayer3g = new CircleLayer(CIRCLE_LAYER_3G_ID, ANT_3G_SOURCE_ID);
        circleLayer3g.setProperties(

// Size circle radius by ANT_2G magnitude and zoom level
                PropertyFactory.circleRadius(
                        4.0f
                ),

// Color circle by ANT_2G magnitude
                circleColor(
                        rgba(0, 255, 0, 0.7)
                ),


                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
        );

        loadedMapStyle.addLayerBelow(circleLayer3g, HEATMAP_LAYER_3G_ID);

        //-----------------------------------------4G-----------------------
        CircleLayer circleLayer4g = new CircleLayer(CIRCLE_LAYER_4G_ID, ANT_4G_SOURCE_ID);
        circleLayer4g.setProperties(

// Size circle radius by ANT_2G magnitude and zoom level
                PropertyFactory.circleRadius(
                        4.0f
                ),

// Color circle by ANT_2G magnitude
                circleColor(
                        rgba(255, 0, 0, 0.7)
                ),

                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
        );

        loadedMapStyle.addLayerBelow(circleLayer4g, HEATMAP_LAYER_4G_ID);
    }

    private void setLayerVisible(String layerId,@NonNull Style loadedMapStyle) {
        Layer layer = loadedMapStyle.getLayer(layerId);
        if (layer == null) {
            return;
        }
        if (VISIBLE.equals(layer.getVisibility().getValue())) {
// Layer is visible
            layer.setProperties(
                    PropertyFactory.visibility(Property.NONE)
            );
        } else {
// Layer isn't visible
            layer.setProperties(
                    PropertyFactory.visibility(VISIBLE)
            );
        }
    }

    public  void set2GVisible(){
        setLayerVisible(CIRCLE_LAYER_2G_ID,style);
        setLayerVisible(HEATMAP_LAYER_2G_ID,style);

    }

    public void set3GVisible() {
        setLayerVisible(CIRCLE_LAYER_3G_ID,style);
        setLayerVisible(HEATMAP_LAYER_3G_ID,style);
    }

    public void set4GVisible() {
        setLayerVisible(CIRCLE_LAYER_4G_ID,style);
        setLayerVisible(HEATMAP_LAYER_4G_ID,style);
    }

    private String loadJsonFromAsset(String filename) {
// Using this method to load in GeoJSON files from the assets folder.
        try {
            InputStream is = ctx.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private String loadFromInternalStorage(String filename) {
// Using this method to load in GeoJSON files from the assets folder.
        try {
            FileInputStream is = ctx.openFileInput(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }


}

