package com.spit.fam.SystemFragment;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.orhanobut.hawk.Hawk;

public class AssetsDetailFragment extends BaseFragment {

    public static AssetsDetailFragment newInstance(){
        return new AssetsDetailFragment();
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.assets_details_fragment, null);

        return view;
    }

    public void onResume() {
        super.onResume();

        (view.findViewById(R.id.asset_header)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("asset_header", "asset_header ");

                if(((TextView) view.findViewById(R.id.asset_information_close)).getText().toString().equals("-")){
                    ((TextView) view.findViewById(R.id.asset_information_close)).setText("+");
                    (view.findViewById(R.id.asset_panel)).setVisibility(View.GONE);
                }else if(((TextView) view.findViewById(R.id.asset_information_close)).getText().toString().equals("+")){
                    ((TextView) view.findViewById(R.id.asset_information_close)).setText("-");
                    (view.findViewById(R.id.asset_panel)).setVisibility(View.VISIBLE);
                }
            }
        });

        (view.findViewById(R.id.financial_header)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((TextView) view.findViewById(R.id.financial_information_close)).getText().toString().equals("-")){
                    ((TextView) view.findViewById(R.id.financial_information_close)).setText("+");
                    (view.findViewById(R.id.financial_panel)).setVisibility(View.GONE);
                } else if(((TextView) view.findViewById(R.id.financial_information_close)).getText().toString().equals("+")){
                    ((TextView) view.findViewById(R.id.financial_information_close)).setText("-");
                    (view.findViewById(R.id.financial_panel)).setVisibility(View.VISIBLE);
                }
            }
        });

        (view.findViewById(R.id.tag_information)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((TextView) view.findViewById(R.id.tag_information_close)).getText().toString().equals("-")){
                    ((TextView) view.findViewById(R.id.tag_information_close)).setText("+");
                    (view.findViewById(R.id.tag_panel)).setVisibility(View.GONE);
                } else if(((TextView) view.findViewById(R.id.tag_information_close)).getText().toString().equals("+")){
                    ((TextView) view.findViewById(R.id.tag_information_close)).setText("-");
                    (view.findViewById(R.id.tag_panel)).setVisibility(View.VISIBLE);
                }
            }
        });

        try {
            ((TextView)view.findViewById(R.id.assetno)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getAssetno()));
        } catch (Exception e) {
        }

        try {
            ((TextView)view.findViewById(R.id.name)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getName()));
        } catch (Exception e) {
        }


        if(false) {
            String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + AssetsDetailWithTabFragment.asset.getAssetno();

            int value = Hawk.get(status_key, -1);

            if (value != -1) {
                if (value == 0) {
                    ((TextView) view.findViewById(R.id.status)).setText(getString(R.string.missing));
                } else if (value == 1) {
                    ((TextView) view.findViewById(R.id.status)).setText(getString(R.string.in_library));
                }
            } else if (AssetsDetailWithTabFragment.IN_STOCK) {
                ((TextView) view.findViewById(R.id.status)).setText(getString(R.string.in_library));
            } else {
                ((TextView) view.findViewById(R.id.status)).setText(getString(R.string.missing));
            }


            if(AssetsDetailWithTabFragment.asset.isFound()) {
                ((TextView) view.findViewById(R.id.status)).setText(getString(R.string.in_library));
            }
        } else {
            try {
                ((TextView) view.findViewById(R.id.status)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getStatus().getName()));


                if(AssetsDetailWithTabFragment.asset.getStatus().id == 2)
                    ((TextView) view.findViewById(R.id.status)).setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                else if(AssetsDetailWithTabFragment.asset.getStatus().id == 3 || AssetsDetailWithTabFragment.asset.getStatus().id == 4)
                    ((TextView) view.findViewById(R.id.status)).setTextColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));
                else if(AssetsDetailWithTabFragment.asset.getStatus().id == 5 || AssetsDetailWithTabFragment.asset.getStatus().id == 6 || AssetsDetailWithTabFragment.asset.getStatus().id == 7 || AssetsDetailWithTabFragment.asset.getStatus().id == 8 || AssetsDetailWithTabFragment.asset.getStatus().id == 9999)
                    ((TextView) view.findViewById(R.id.status)).setTextColor(getActivity().getResources().getColor(android.R.color.holo_red_dark));

            } catch (Exception e) {
            }
        }

        try {
            ((TextView) view.findViewById(R.id.brand)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getBrand()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.model)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getModel()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.serialno)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getSerialNo()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.unit)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getUnit()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.category)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getCategoryString()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.location)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getLocationString()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.last_stock_date)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getLastStockDate()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.create_date)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getCreateDate()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.created_by)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getCreated_by().getName()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.purchase_date)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getPurchaseDate()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.invoice_date)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getInvoiceDate()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.purchase_date)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getPurchaseDate()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.invoice_no)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getInvoiceNo()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.funding_source)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getFundingSource()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.group)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getUsergroup()));
        } catch (Exception e) {
        }
        try {
            ((TextView) view.findViewById(R.id.holder)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getPossessor()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.source)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getExhibitsource()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.witness)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getExhibitwitness()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.prosecution_no)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getLastassetno()));
        } catch (Exception e) {
        }

        //assetsDetailsFragmentBinding.fundingSource.setText(AssetsDetailWithTabFragment.asset.getF);
        try {
            ((TextView) view.findViewById(R.id.supplier)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getSupplier()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.maintenance_date)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getMaintenanceDate()));
        } catch (Exception e) {
        }

        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(8);
        nf.setMaximumIntegerDigits(20);

        try {
            ((TextView) view.findViewById(R.id.cost)).setText(getRelevantText( nf.format(Double.parseDouble(AssetsDetailWithTabFragment.asset.getCost())) ) );
            ((TextView) view.findViewById(R.id.practical_value)).setText(getRelevantText(nf.format(Double.parseDouble(AssetsDetailWithTabFragment.asset.getPracticalValue()))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ((TextView) view.findViewById(R.id.estimated_lifetime_month)).setText(getRelevantText("" + AssetsDetailWithTabFragment.asset.getEstimatedLifeTime()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.type_tag)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getTag_type().getName()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.barcode)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getBarcode()));
        } catch (Exception e) {
        }

        try {
            ((TextView) view.findViewById(R.id.epc)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getEPC()));
        } catch (Exception e) {
        }

        try {
            if (AssetsDetailWithTabFragment.asset.getNewEPC() != null && AssetsDetailWithTabFragment.asset.getNewEPC().length() > 0) {
                ((TextView) view.findViewById(R.id.new_epc)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getNewEPC()));
                (view.findViewById(R.id.new_epc_wrapper)).setVisibility(View.VISIBLE);
            } else {
                (view.findViewById(R.id.new_epc_wrapper)).setVisibility(View.GONE);
            }
        } catch (Exception e) {
        }


        int length =  AssetsDetailWithTabFragment.asset == null? 0 :AssetsDetailWithTabFragment.asset.getCertType().split(",").length;

        ((ViewGroup)view.findViewById(R.id.cert_panel)).removeAllViews();

        for(int i = 0; i < length; i ++) {
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.layout_cert, null);

            try {
                if (AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[i] != null && AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[i].length() > 0) {
                    (linearLayout.findViewById(R.id.cert_panel_info)).setVisibility(View.VISIBLE);
                } else {
                    (linearLayout.findViewById(R.id.cert_panel_info)).setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                ((TextView) linearLayout.findViewById(R.id.cert_type)).setText(getRelevantText("" + AssetsDetailWithTabFragment.asset.getCertType().split(",")[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                ((TextView) linearLayout.findViewById(R.id.cert_url)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getCertUrl().split(",")[i] ));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Spannable WordtoSpan = new SpannableString(AssetsDetailWithTabFragment.asset.getCertUrl().split(",")[i]);
                WordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 0, AssetsDetailWithTabFragment.asset.getCertUrl().split(",")[i].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                WordtoSpan.setSpan(new UnderlineSpan(), 0, AssetsDetailWithTabFragment.asset.getCertUrl().split(",")[i].length(), 0);
                ((TextView) linearLayout.findViewById(R.id.cert_url)).setText(WordtoSpan);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                int finalPos = i;
                ((ViewGroup) ((TextView) linearLayout.findViewById(R.id.cert_url)).getParent()).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.SKIP_DOWNLOAD_ONCE = true;
                        String url = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_CERT_PATH, "") + AssetsDetailWithTabFragment.asset.getCertUrl().split(",")[finalPos];
                        Log.i("onclick", "onclick " + url);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Log.i("epc", "epc " + AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[i]);

                for(int x = 0;  x < AssetsDetailWithTabFragment.asset.getCerstatus().split(",").length; x++) {
                    Log.i("data", "data " + AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[x]);
                }
                if (AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[i] != null) {
                    if (AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[i].equals("24")) {
                        ((TextView) linearLayout.findViewById(R.id.cert_status)).setText(getString(R.string.cert_valid));
                    } else if (AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[i].equals("23")) {
                        ((TextView) linearLayout.findViewById(R.id.cert_status)).setText(getString(R.string.cert_valid));
                    } else if (AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[i].equals("21")) {
                        ((TextView) linearLayout.findViewById(R.id.cert_status)).setText(getString(R.string.expired));
                    } else if (AssetsDetailWithTabFragment.asset.getCerstatus().split(",")[i].equals("22")) {
                        ((TextView) linearLayout.findViewById(R.id.cert_status)).setText(getString(R.string.going_to_outdated));
                    } else {
                        ((TextView) linearLayout.findViewById(R.id.cert_status)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getCerstatus()));
                    }
                }
                ((TextView) linearLayout.findViewById(R.id.valid_date)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getStartdate().split(",")[i]) + " - " + getRelevantText(AssetsDetailWithTabFragment.asset.getEnddate().split(",")[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((LinearLayout)view.findViewById(R.id.cert_panel)).addView(linearLayout);
        }
        /*
        if(AssetsDetailWithTabFragment.asset.getCerstatus() != null && AssetsDetailWithTabFragment.asset.getCerstatus().length() > 0) {
            (view.findViewById(R.id.cert_panel_info)).setVisibility(View.VISIBLE);
        } else {
            (view.findViewById(R.id.cert_panel_info)).setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.cert_type)).setText(getRelevantText("" + AssetsDetailWithTabFragment.asset.getCertType()));
        ((TextView) view.findViewById(R.id.cert_url)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getCertUrl()));

        Spannable WordtoSpan = new SpannableString(AssetsDetailWithTabFragment.asset.getCertUrl());
        WordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), 0, AssetsDetailWithTabFragment.asset.getCertUrl().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        WordtoSpan.setSpan(new UnderlineSpan(), 0, AssetsDetailWithTabFragment.asset.getCertUrl().length(), 0);

        ((TextView) view.findViewById(R.id.cert_url)).setText(WordtoSpan);


        ((ViewGroup)((TextView) view.findViewById(R.id.cert_url)).getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.SKIP_DOWNLOAD_ONCE = true;
                String url = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_CERT_PATH, "") + AssetsDetailWithTabFragment.asset.getCertUrl() ;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        if(AssetsDetailWithTabFragment.asset.getCerstatus() != null) {
            if(AssetsDetailWithTabFragment.asset.getCerstatus().equals("24")) {
                ((TextView) view.findViewById(R.id.cert_status)).setText(getString(R.string.cert_valid));
            }
            if(AssetsDetailWithTabFragment.asset.getCerstatus().equals("23")) {
                ((TextView) view.findViewById(R.id.cert_status)).setText(getString(R.string.cert_valid));
            }else if(AssetsDetailWithTabFragment.asset.getCerstatus().equals("21")) {
                ((TextView) view.findViewById(R.id.cert_status)).setText(getString(R.string.expired));
            } else if(AssetsDetailWithTabFragment.asset.getCerstatus().equals("22")) {
                ((TextView) view.findViewById(R.id.cert_status)).setText(getString(R.string.going_to_outdated));
            } else {
                ((TextView) view.findViewById(R.id.cert_status)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getCerstatus()));
            }
        }
        ((TextView) view.findViewById(R.id.valid_date)).setText(getRelevantText(AssetsDetailWithTabFragment.asset.getStartdate()) + " - " + getRelevantText(AssetsDetailWithTabFragment.asset.getEnddate()));
*/
        (view.findViewById(R.id.cert_information)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("cert_information", "cert_information");


                if(((TextView) view.findViewById(R.id.cert_information_close)).getText().toString().equals("-")){
                    ((TextView) view.findViewById(R.id.cert_information_close)).setText("+");
                    (view.findViewById(R.id.cert_panel)).setVisibility(View.GONE);
                }else if(((TextView) view.findViewById(R.id.cert_information_close)).getText().toString().equals("+")){
                    ((TextView) view.findViewById(R.id.cert_information_close)).setText("-");
                    (view.findViewById(R.id.cert_panel)).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public String getRelevantText(String text) {
        if(text == null || text.length() == 0 || text.equals("null")) {
            return "-";
        }

        return text;
    }
}