package com.example.pae_project;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

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
    private static final String HEATMAP_LAYER_ID = "ANT_2Gs-heat";
    private static final String HEATMAP_LAYER_SOURCE = "ANT_2Gs";
    private static final String CIRCLE_LAYER_ID = "ANT_2Gs-circle";
    private static final String ANT_WIFI_SOURCE_ID = "ANT_WIFI";
    private static final String HEATMAP_LAYER_WIFI_ID = "ANT_WIFI-heat";
    private static final String HEATMAP_LAYER_WIFI_SOURCE = "ANT_WIFI";
    private static final String CIRCLE_LAYER_WIFI_ID = "ANT_WIFI-circle";

    Context ctx;

    private int index = 0;

    private Expression[] listOfHeatmapColors;
    private Expression[] listOfHeatmapRadiusStops;
    private Float[] listOfHeatmapIntensityStops;

    private ValueAnimator attractionsColorAnimator;

    public HeatMap(Context ctx) {
        this.ctx = ctx;
    }

    public void addANT_2GSource(@NonNull Style loadedMapStyle) {
        GeoJsonSource hotelSource = new GeoJsonSource(ANT_2G_SOURCE_ID, loadJsonFromAsset("data.geojson"));
        loadedMapStyle.addSource(hotelSource);
    }
    public void addANT_Wifiource(@NonNull Style loadedMapStyle) {
        GeoJsonSource hotelSource = new GeoJsonSource(ANT_WIFI_SOURCE_ID, loadJsonFromAsset("wifi.geojson"));
        loadedMapStyle.addSource(hotelSource);
    }

    public void addHeatmapLayerWIFI(@NonNull Style loadedMapStyle) {
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_WIFI_ID, ANT_WIFI_SOURCE_ID);
        layer.setMaxZoom(9);
        layer.setSourceLayer(HEATMAP_LAYER_WIFI_SOURCE);
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


// Increase the heatmap color weight weight by zoom level
// heatmap-intensity is a multiplier on top of heatmap-weight
                heatmapIntensity(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 1),
                                stop(9, 1)
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
    }

    public void addCircleLayerWIFI(@NonNull Style loadedMapStyle) {
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_WIFI_ID, ANT_WIFI_SOURCE_ID);
        circleLayer.setProperties(

// Size circle radius by ANT_2G magnitude and zoom level
                PropertyFactory.circleRadius(
                        4.0f
                ),



// Color circle by ANT_2G magnitude
                circleColor(
                        rgba(250, 0, 0, 0.5)
                ),

// Transition from heatmap to circle layer by zoom level
                circleOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 0),
                                stop(8, 1)
                        )
                ),
                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
        );

        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_WIFI_ID);
    }

    public void addHeatmapLayer(@NonNull Style loadedMapStyle) {
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, ANT_2G_SOURCE_ID);
        layer.setMaxZoom(9);
        layer.setSourceLayer(HEATMAP_LAYER_SOURCE);
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


// Increase the heatmap color weight weight by zoom level
// heatmap-intensity is a multiplier on top of heatmap-weight
                heatmapIntensity(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 1),
                                stop(9, 1)
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
    }

    public void addCircleLayer(@NonNull Style loadedMapStyle) {
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, ANT_2G_SOURCE_ID);
        circleLayer.setProperties(

// Size circle radius by ANT_2G magnitude and zoom level
                PropertyFactory.circleRadius(
                        4.0f
                ),



// Color circle by ANT_2G magnitude
                circleColor(
                        rgba(103, 169, 207, 0.5)
                ),

// Transition from heatmap to circle layer by zoom level
                circleOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 0),
                                stop(8, 1)
                        )
                ),
                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
        );

        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
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
}

