package mykrake.com.krakesample

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.krake.core.app.LoginAndPrivacyActivity
import com.krake.core.app.OnContentItemSelectedListener
import com.krake.core.model.ContentItem
import java.util.*

/**
 * Created by antoniolig on 13/03/2017.
 */
class MainActivity : LoginAndPrivacyActivity(), OnContentItemSelectedListener, Observer {
    override fun onCreate(savedInstanceState: Bundle?, layout: Int) {
        super.onCreate(savedInstanceState, layout)

        //        getLayoutInflater().inflate(R.layout.activity_main, (ViewGroup) findViewById(R.id.activity_content_container));
        inflateMainView(R.layout.activity_main, true)

        val bounds = LatLngBounds(LatLng(10.0, 15.0), LatLng(20.0, 25.0))

        val s = Gson().toJson(bounds)
        s.toCharArray()
    }

    override fun onStart() {
        super.onStart()
        //((KrakeApp) getApplication()).near.addObserver(this);
    }

    override fun onStop() {
        super.onStop()

        //((KrakeApp) getApplication()).near.deleteObserver(this);
    }

    override fun changeContentVisibility(visible: Boolean) {

    }

    override fun onShowContentItemDetails(senderFragment: Any, contentItem: ContentItem) {

    }

    override fun onContentItemInEvidence(senderFragment: Any, contentItem: ContentItem) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun update(observable: Observable, data: Any) {

    }
}