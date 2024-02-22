package com.spit.fam;

import android.view.View;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class CaptureActivityPortrait extends CaptureActivity {
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.custom_zxing_capture);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CaptureActivityPortrait.this.onBackPressed();
            }
        });
        return (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);
    }

}
