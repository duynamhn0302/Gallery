package com.example.gallery.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.Gravity;
import android.view.View;

import com.example.gallery.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.common.PlaceConstants;
import com.mapbox.mapboxsdk.plugins.places.common.utils.ColorUtils;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.ui.CurrentPlaceSelectionBottomSheet;
import com.mapbox.mapboxsdk.plugins.places.picker.ui.PlacePickerActivity;
import com.mapbox.mapboxsdk.plugins.places.picker.viewmodel.PlacePickerViewModel;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import timber.log.Timber;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * Use the places plugin to take advantage of Mapbox's location search ("geocoding") capabilities. The plugin
 * automatically makes geocoding requests, has built-in saved locations, includes location picker functionality,
 * and adds beautiful UI into your Android project.
 */
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnCameraMoveStartedListener,
        MapboxMap.OnCameraIdleListener, Observer<CarmenFeature>, PermissionsListener  {
    private PermissionsManager permissionsManager;
    CurrentPlaceSelectionBottomSheet bottomSheet;
    CarmenFeature carmenFeature;
    private PlacePickerViewModel viewModel;
    private PlacePickerOptions options;
    private ImageView markerImage;
    private MapboxMap mapboxMap;
    private String accessToken;
    private MapView mapView;
    private FloatingActionButton userLocationButton;
    private boolean includeReverseGeocode;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_places_plugin);

        if (savedInstanceState == null) {
            accessToken = getString(R.string.access_token);
            options = PlacePickerOptions.builder()
                    .language("vi")
                    .statingCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(40.7544, -73.9862)).zoom(16).build())
                    .build();
            includeReverseGeocode = options.includeReverseGeocode();
        }

        // Initialize the view model.
        viewModel = ViewModelProviders.of(this).get(PlacePickerViewModel.class);
        viewModel.getResults().observe(this, this);
        bindViews();
        addBackButtonListener();
        addPlaceSelectedButton();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void bindViews() {
        mapView = findViewById(R.id.map_view);
        bottomSheet = findViewById(R.id.mapbox_plugins_picker_bottom_sheet);
        markerImage = findViewById(R.id.mapbox_plugins_image_view_marker);
        userLocationButton = findViewById(R.id.user_location_button);
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .language(getString(R.string.language_map))
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MapsActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    private void addBackButtonListener() {
        ImageView backButton = findViewById(R.id.mapbox_place_picker_toolbar_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void addPlaceSelectedButton() {
        FloatingActionButton placeSelectedButton = findViewById(R.id.place_picker);
        placeSelectedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (carmenFeature == null && includeReverseGeocode) {
                    Snackbar.make(bottomSheet,
                            getString(R.string.mapbox_plugins_place_picker_not_valid_selection),
                            LENGTH_LONG).show();
                    return;
                }
                placeSelected();
            }
        });
    }



    void placeSelected() {
        Intent returningIntent = new Intent();
        if (includeReverseGeocode) {
            String json = carmenFeature.toJson();
            returningIntent.putExtra(PlaceConstants.RETURNING_CARMEN_FEATURE, json);
        }
        returningIntent.putExtra(PlaceConstants.MAP_CAMERA_POSITION, mapboxMap.getCameraPosition());
        setResult(AppCompatActivity.RESULT_OK, returningIntent);
        finish();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @SuppressLint("WrongConstant")
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);
                localizationPlugin.setMapLanguage("vi");
                adjustCameraBasedOnOptions();
                if (includeReverseGeocode) {
                    // Initialize with the marker's current coordinates.
                    makeReverseGeocodingSearch();
                }
                bindListeners();

                enableLocationComponent(style);
            }
        });
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

    // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);


// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);


            addUserLocationButton();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    private void addUserLocationButton() {
        userLocationButton.show();
        userLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
                    Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()))
                                    .zoom(17.5)
                                    .build()
                    ),1400);
                } else {
                   }
            }
        });
    }
    private void adjustCameraBasedOnOptions() {
        if (options != null) {
            if (options.startingBounds() != null) {
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(options.startingBounds(), 0));
            } else if (options.statingCameraPosition() != null) {
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(options.statingCameraPosition()));
            }
        }
    }
    private void bindListeners() {
        this.mapboxMap.addOnCameraMoveStartedListener(this);
        this.mapboxMap.addOnCameraIdleListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(17.5)
                                    .build()), 1400);

                }
            }
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
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

    @Override
    public void onCameraMoveStarted(int reason) {
        Timber.v("Map camera has begun moving.");
        if (markerImage.getTranslationY() == 0) {
            markerImage.animate().translationY(-75)
                    .setInterpolator(new OvershootInterpolator()).setDuration(250).start();
            if (includeReverseGeocode) {
                if (bottomSheet.isShowing()) {
                    bottomSheet.dismissPlaceDetails();
                }
            }
        }
    }

    @Override
    public void onCameraIdle() {
        Timber.v("Map camera is now idling.");
        markerImage.animate().translationY(0)
                .setInterpolator(new OvershootInterpolator()).setDuration(250).start();
        if (includeReverseGeocode) {
            bottomSheet.setPlaceDetails(null);
            // Initialize with the markers current location information.
            makeReverseGeocodingSearch();
        }
    }

    private void makeReverseGeocodingSearch() {
        LatLng latLng = mapboxMap.getCameraPosition().target;
        if (latLng != null) {
            viewModel.reverseGeocode(
                    Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()),
                    accessToken, options
            );
        }
    }
    @Override
    public void onChanged(@Nullable CarmenFeature carmenFeature) {
        if (carmenFeature == null) {
            carmenFeature = CarmenFeature.builder().placeName(
                    String.format(Locale.US, "[%f, %f]",
                            mapboxMap.getCameraPosition().target.getLatitude(),
                            mapboxMap.getCameraPosition().target.getLongitude())
            ).text("No address found").properties(new JsonObject()).build();
        }
        this.carmenFeature = carmenFeature;
        bottomSheet.setPlaceDetails(carmenFeature);
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
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
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
}