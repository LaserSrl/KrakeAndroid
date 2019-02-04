package com.krake.beacons;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.krake.core.PrivacyViewModel;
import com.krake.core.component.module.LoginComponentModule;
import com.krake.core.component.module.OrchardComponentModule;
import com.krake.core.data.DataConnectionModel;
import com.krake.core.data.DataModel;
import com.krake.core.gcm.OrchardContentNotifier;
import com.krake.core.model.ContentItem;
import com.krake.core.model.ContentItemWithTermPart;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

public class BeaconAdsNotifier extends Observable implements Observer {

    private final Context context;
    @NonNull
    private final ProximityBeaconRanger ranger;
    @NonNull
    private final String beaconDisplayAlias;
    private Beacon loadingBeacon;
    private KrakeBeacon loadingKrakeBeacon;
    private DataConnectionModel beaconConnection;
    private DataConnectionModel adsConnection;

    private int maxNumberOfBeaconsToNotify;
    private long minimumTimeToNotifyTheSameBeacon;
    private SparseArray<Date> lastTimeNotifiedBeacon = new SparseArray<>();

    /**
     * Alloca un nuovo notifier per le offerte
     *
     * @param context            context dell'App
     * @param beaconClass        classe di dato dei beacon devono implementare {@link KrakeBeacon}
     * @param adsClass           classe delle offerte devono estendere {@link ContentItem}
     * @param beaconDisplayAlias display alias per caricare i beacon. Deve accettare i parametri "UUID", "majorW, "minor"
     */
    public BeaconAdsNotifier(@NonNull Context context,
                             @NonNull ProximityBeaconRanger ranger,
                             @NonNull Class beaconClass,
                             @NonNull Class adsClass,
                             @NonNull String beaconDisplayAlias) {
        this.context = context;
        this.ranger = ranger;
        ranger.addObserver(this);
        ranger.getBeaconManager().addObserver(this);
        this.beaconDisplayAlias = beaconDisplayAlias;

        OrchardComponentModule beaconConnectionModule = new OrchardComponentModule()
                .startConnectionAfterActivityCreated(false)
                .dataClass(beaconClass)
                .displayPath(beaconDisplayAlias);

        PrivacyViewModel privacyViewModel = new PrivacyViewModel();

        beaconConnection = new DataConnectionModel(beaconConnectionModule,
                new LoginComponentModule(),
                privacyViewModel);

        beaconConnection.getModel().observeForever(new android.arch.lifecycle.Observer<DataModel>() {
            @Override
            public void onChanged(@Nullable DataModel dataModel) {
                if (dataModel != null)
                    onLoadedKrakeBeacons(dataModel);
            }
        });

        OrchardComponentModule adsConnectionModule = new OrchardComponentModule()
                .startConnectionAfterActivityCreated(false)
                .dataClass(adsClass)
                .displayPath("");

        adsConnection = new DataConnectionModel(adsConnectionModule,
                new LoginComponentModule(),
                privacyViewModel);

        adsConnection.getModel().observeForever(new android.arch.lifecycle.Observer<DataModel>() {
            @Override
            public void onChanged(@Nullable DataModel dataModel) {
                if (dataModel != null) {
                    loadedAds(dataModel.getListData());
                }
            }
        });

        minimumTimeToNotifyTheSameBeacon = context.getResources().getInteger(R.integer.minimumTimeToNotifiySameBeacon) * 1000;
        maxNumberOfBeaconsToNotify = context.getResources().getInteger(R.integer.maxNumberOfBeaconsToNotify);
    }

    private void clearData() {
        lastTimeNotifiedBeacon.clear();
    }

    @Override
    public void update(Observable observable, Object data) {

        if (observable instanceof ProximityBeaconRanger) {
            if (shouldNotifiyBeacon((Beacon) data) && (loadingBeacon != data || loadingKrakeBeacon == null || loadingBeacon.getMinor() != ((Beacon) data).getMinor())) {

                loadingBeacon = (Beacon) data;
                beaconConnection.getOrchardModule().putExtraParameter(context.getString(R.string.orchard_beacon_uuid_key), loadingBeacon.getUuid().toString());
                beaconConnection.getOrchardModule().putExtraParameter(context.getString(R.string.orchard_beacon_major_key), Integer.valueOf(loadingBeacon.getMajor()).toString());
                beaconConnection.getOrchardModule().putExtraParameter(context.getString(R.string.orchard_beacon_minor_key), Integer.valueOf(loadingBeacon.getMinor()).toString());
                beaconConnection.getOrchardModule().displayPath(beaconDisplayAlias);
                beaconConnection.restartDataLoading();
            }
        } else if (observable instanceof BeaconManager) {
            BeaconEvent event = (BeaconEvent) data;

            switch (event.getWhat()) {
                case BeaconEvent.EVENT_ENTER_REGION:

                    break;

                case BeaconEvent.EVENT_EXIT_REGION: {
                    if (((BeaconManager) observable).isRegionMain(event.getRegion())) {
                        loadingBeacon = null;
                        clearData();
                    }
                }
                break;

                default:
                    break;
            }
        }
    }

    private boolean shouldNotifiyBeacon(Beacon beacon) {

        Date lastTimeNotified = lastTimeNotifiedBeacon.get(beacon.getMinor());
        Date earlyDate = new Date(new Date().getTime() - minimumTimeToNotifyTheSameBeacon);
        boolean before = lastTimeNotified == null || lastTimeNotified.before(earlyDate);
        return lastTimeNotifiedBeacon.size() < maxNumberOfBeaconsToNotify && before;
    }

    public void onLoadedKrakeBeacons(DataModel dataModel) {
        if (!dataModel.getListData().isEmpty() && dataModel.getListData().get(0) instanceof KrakeBeacon) {
            KrakeBeacon kBeacon = (KrakeBeacon) dataModel.getListData().get(0);
            Log.e("BeaconADS", "Beacon from krake");
            if (loadingBeacon != null &&
                    kBeacon.getMinorValue().intValue() == loadingBeacon.getMinor()) {
                loadingKrakeBeacon = kBeacon;

                setChanged();
                notifyObservers(loadingKrakeBeacon.getTitlePartTitle());
                String contentUrl = kBeacon.getUrl().getContentUrl();
                try {
                    contentUrl = URLDecoder.decode(contentUrl, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
                int index = contentUrl.indexOf('?');

                if (index > 0) {
                    String path = contentUrl.substring(0, index);
                    adsConnection.getOrchardModule().displayPath(path);
                    StringTokenizer queryText = new StringTokenizer(contentUrl.substring(index + 1), "&");
                    while (queryText.hasMoreTokens()) {
                        String token = queryText.nextToken();

                        int equalIndex = token.indexOf("=");

                        if (equalIndex > 0) {
                            adsConnection.getOrchardModule().putExtraParameter(token.substring(0, equalIndex), token.substring(equalIndex + 1));
                        }
                    }

                } else
                    adsConnection.getOrchardModule().displayPath(contentUrl);


                adsConnection.restartDataLoading();
            }
        }
    }

    private void loadedAds(List ads) {

        setChanged();
        notifyObservers(ads.size());

        if (ads.size() > 0 && loadingBeacon != null && shouldNotifiyBeacon(loadingBeacon)) {
            lastTimeNotifiedBeacon.append(loadingBeacon.getMinor(), new Date());

            String message;
            if (loadingKrakeBeacon instanceof ContentItemWithTermPart) {
                message = String.format(context.getString(R.string.new_offerts_with_format), ((ContentItemWithTermPart) loadingKrakeBeacon).getTermPart().getName());
            } else {

                message = context.getString(R.string.new_offerts);
            }

//            Bundle extras = new Bundle();
//
//            for (String key : adsConnection.getOrchardExtraParameters().keySet()) {
//                extras.putString(key, adsConnection.getOrchardExtraParameter(key));
//            }

            OrchardContentNotifier.showNotification(context,
                    message,
                    ads,
                    adsConnection.getOrchardModule().getDisplayPath(),
                    adsConnection.getOrchardModule().getExtraParameters(),
                    null,
                    BeaconNotificationChannel.BEACON_NOTIFICATION_CHANNEL);
        }
    }
}
