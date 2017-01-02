package com.emolance.enterprise.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.mitac.cell.device.bcr.McBcrConnection;
import com.mitac.cell.device.bcr.McBcrMessage;
import com.mitac.cell.device.bcr.MiBcrListener;
import com.mitac.cell.device.bcr.utility.BARCODE;

import butterknife.ButterKnife;

/**
 * Created by yusun on 6/22/15.
 */
public class QRScanActivity extends FragmentActivity {

    private boolean hasScanned = false;
    private McBcrConnection mBcr;	// McBcrConnection help BCR control

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        this.setContentView(R.layout.activity_scan);
        ButterKnife.inject(this);

        mBcr = new McBcrConnection(this);
        mBcr.setListener(new MiBcrListener() {
            @Override
            public void onScanned(final String s, BARCODE.TYPE type, int i) {
                if (hasScanned) return;
                hasScanned = true;
                //mBcr.scan(false);

                Log.i("Scanner", s + " " + type + " " + i);
                Log.i("Scanner", "Shutting down the BarCode scanner.");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(QRScanActivity.this, UserReportCreatorActivity.class);
                        intent.putExtra("qr", s);
                        startActivity(intent);
                    }
                }, 1000);
            }

            @Override
            public void onStatusChanged(int i) {
                Log.i("TEST", "Changed " + i + " ");
                if (!hasScanned && (McBcrMessage.Status_Ready == i || McBcrMessage.Status_ServiceConnected == i)) {
                    mBcr.scan(true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBcr.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBcr.stopListening();
    }

    @Override
    protected void onDestroy() {
        mBcr.close();
        super.onDestroy();
    }

}
