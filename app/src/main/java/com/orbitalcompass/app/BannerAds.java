package com.orbitalcompass.app;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/**
 * Tira inferior siempre reservada (50 dp). El anuncio se superpone al placeholder.
 * En debug no espera a UMP: los IDs de prueba deben verse sin cuenta AdMob.
 */
final class BannerAds {
    private static final String TAG = "BannerAds";
    private final Activity activity;
    private final FrameLayout container;
    private final TextView placeholder;
    private final TextView privacyLink;
    private final ConsentInformation consent;
    private AdView adView;
    private boolean starting;

    BannerAds(Activity activity, FrameLayout container, TextView placeholder, TextView privacyLink) {
        this.activity = activity;
        this.container = container;
        this.placeholder = placeholder;
        this.privacyLink = privacyLink;
        this.consent = UserMessagingPlatform.getConsentInformation(activity);
        privacyLink.setOnClickListener(v ->
                UserMessagingPlatform.showPrivacyOptionsForm(activity, error -> { }));
        if (BuildConfig.DEBUG) startAds();
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consent.requestConsentInfoUpdate(activity, params,
                this::onConsentUpdated,
                error -> {
                    Log.w(TAG, "UMP update failed: " + error.getMessage());
                    onConsentUpdated();
                });
    }

    private void onConsentUpdated() {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, error -> {
            refreshPrivacyLink();
            if (consent.canRequestAds()) startAds();
        });
    }

    private void startAds() {
        if (starting || adView != null) return;
        starting = true;
        MobileAds.initialize(activity, status -> activity.runOnUiThread(this::loadBanner));
    }

    private void loadBanner() {
        if (adView != null || activity.isFinishing()) return;
        AdView view = new AdView(activity);
        view.setAdUnitId(BuildConfig.ADMOB_BANNER_UNIT_ID);
        view.setAdSize(AdSize.BANNER);
        view.setAdListener(new AdListener() {
            @Override public void onAdLoaded() {
                placeholder.setVisibility(View.GONE);
                view.setVisibility(View.VISIBLE);
                refreshPrivacyLink();
            }

            @Override public void onAdFailedToLoad(LoadAdError error) {
                Log.w(TAG, "Ad failed: " + error.getCode() + " " + error.getMessage());
                view.setVisibility(View.GONE);
                placeholder.setVisibility(View.VISIBLE);
            }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        view.setVisibility(View.INVISIBLE);
        container.addView(view, params);
        adView = view;
        view.loadAd(new AdRequest.Builder().build());
    }

    private void refreshPrivacyLink() {
        boolean required = consent.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
        privacyLink.setVisibility(required ? View.VISIBLE : View.GONE);
    }

    void pause() {
        if (adView != null) adView.pause();
    }

    void resume() {
        if (adView != null) adView.resume();
    }

    void destroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }
}
