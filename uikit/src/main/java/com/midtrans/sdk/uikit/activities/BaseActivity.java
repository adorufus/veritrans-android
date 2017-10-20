package com.midtrans.sdk.uikit.activities;

import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.LayoutRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.midtrans.sdk.corekit.core.Logger;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.MerchantPreferences;
import com.midtrans.sdk.corekit.models.TransactionResponse;
import com.midtrans.sdk.corekit.models.snap.MerchantData;
import com.midtrans.sdk.corekit.models.snap.Transaction;
import com.midtrans.sdk.corekit.utilities.Utils;
import com.midtrans.sdk.uikit.BuildConfig;
import com.midtrans.sdk.uikit.R;
import com.midtrans.sdk.uikit.adapters.TransactionDetailsAdapter;
import com.midtrans.sdk.uikit.fragments.PaymentTransactionStatusFragment;
import com.midtrans.sdk.uikit.widgets.BoldTextView;
import com.midtrans.sdk.uikit.widgets.DefaultTextView;
import com.midtrans.sdk.uikit.widgets.FancyButton;
import java.util.List;

/**
 * @author rakawm
 */
public class BaseActivity extends AppCompatActivity {
    public static final String ENVIRONMENT_DEVELOPMENT = "development";
    private static final String TAG = BaseActivity.class.getSimpleName();
    protected String currentFragmentName;
    protected Fragment currentFragment = null;
    protected boolean saveCurrentFragment = false;
    protected int RESULT_CODE = RESULT_CANCELED;
    protected boolean isDetailShown = false;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        try {
            initMerchantLogo();
            initItemDetails();
        } catch (Exception e) {
            Logger.e(TAG, "appbar:" + e.getMessage());
        }
    }

    public void initializeTheme() {
        initBadgeTestView();

        MidtransSDK mMidtransSDK = MidtransSDK.getInstance();
        if (mMidtransSDK != null) {
            ImageView logo = (ImageView) findViewById(R.id.merchant_logo);
            TextView name = (TextView) findViewById(R.id.merchant_name);
            if (logo != null) {
                if (mMidtransSDK.getMerchantLogo() != null) {
                    if (name != null) {
                        name.setVisibility(View.GONE);
                    }
                    Glide.with(this)
                            .load(mMidtransSDK.getMerchantLogo())
                            .into(logo);
                }
            }

            updateColorTheme(mMidtransSDK);
        }
    }

    private void initBadgeTestView() {
        if (BuildConfig.FLAVOR.equalsIgnoreCase(ENVIRONMENT_DEVELOPMENT)) {
            ImageView badgeView = (ImageView) findViewById(R.id.image_sandbox_badge);
            if (badgeView != null) {
                badgeView.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void initMerchantLogo() {
        ImageView merchantLogo = (ImageView) findViewById(R.id.merchant_logo);
        DefaultTextView merchantNameText = (DefaultTextView) findViewById(R.id.text_page_merchant_name);

        MerchantData merchantData = MidtransSDK.getInstance().getMerchantData();

        if (merchantData != null) {
            MerchantPreferences preferences = merchantData.getPreference();
            if (preferences != null) {
                String merchantName = preferences.getDisplayName();
                String merchantLogoUrl = preferences.getLogoUrl();
                if (!TextUtils.isEmpty(merchantLogoUrl)) {
                    if (merchantLogo != null) {
                        Glide.with(this)
                            .load(merchantLogoUrl)
                            .into(merchantLogo);
                        merchantLogo.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (merchantName != null) {
                        if (merchantNameText != null && !TextUtils.isEmpty(merchantName)) {
                            merchantNameText.setVisibility(View.VISIBLE);
                            merchantNameText.setText(merchantName);
                            if (merchantLogo != null) {
                                merchantLogo.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }
        }

    }

    private void initItemDetails() {
        View itemDetailContainer = findViewById(R.id.container_item_details);
        if (itemDetailContainer != null) {
            initTotalAmount();
        }
    }

    private void initTotalAmount() {
        final Transaction transaction = MidtransSDK.getInstance().getTransaction();
        if (transaction.getTransactionDetails() != null) {
            BoldTextView textTotalAmount = (BoldTextView) findViewById(R.id.text_amount);
            if (textTotalAmount != null) {
                String totalAmount = getString(R.string.prefix_money, Utils
                    .getFormattedAmount(transaction.getTransactionDetails().getAmount()));
                textTotalAmount.setText(totalAmount);
            }
        }
        initTransactionDetail(MidtransSDK.getInstance().getTransactionRequest().getItemDetails());
        //init dim
        findViewById(R.id.background_dim).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOrHideItemDetails();
            }
        });

        final LinearLayout amountContainer = (LinearLayout) findViewById(R.id.container_item_details);
        amountContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOrHideItemDetails();
            }
        });
        //hide total amount if virtual keyboard appeared
        findViewById(R.id.button_primary).getViewTreeObserver().addOnGlobalLayoutListener(
            new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    boolean isKeyboardShown = isKeyboardShown(findViewById(android.R.id.content));
                    amountContainer.setVisibility(isKeyboardShown ? View.GONE : View.VISIBLE);
                }
            });
    }

    private void initTransactionDetail(List<ItemDetails> details) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_transaction_detail);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            TransactionDetailsAdapter adapter = new TransactionDetailsAdapter(details);
            recyclerView.setAdapter(adapter);
        }
    }

    private boolean isKeyboardShown(View rootView) {
        /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */
        final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;

        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        /* heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top) */
        int heightDiff = rootView.getBottom() - r.bottom;
        /* Threshold size: dp to pixels, multiply with display density */
        return heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density;
    }

    protected void displayOrHideItemDetails() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_transaction_detail);
        View dimView = findViewById(R.id.background_dim);
        if (recyclerView != null && dimView != null) {
            if (isDetailShown) {
                recyclerView.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.text_amount)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_amount_detail, 0);
                dimView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.text_amount)).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                dimView.setVisibility(View.VISIBLE);
            }
            isDetailShown = !isDetailShown;
        }
    }

    private void hideTotalAmount() {
        findViewById(R.id.container_item_details).setVisibility(View.GONE);
        findViewById(R.id.btn_pay_separator).setVisibility(View.GONE);
        findViewById(R.id.btn_pay_container).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (MidtransSDK.getInstance().getUIKitCustomSetting() != null
            && MidtransSDK.getInstance().getUIKitCustomSetting().isEnabledAnimation()) {
            overridePendingTransition(R.anim.slide_in_back, R.anim.slide_out_back);
        }
    }


    public void updateColorTheme(MidtransSDK mMidtransSDK) {
        try {
            if (mMidtransSDK.getColorTheme() != null) {
                int primaryColor = mMidtransSDK.getColorTheme().getPrimaryColor();
                int primaryDarkColor = mMidtransSDK.getColorTheme().getPrimaryDarkColor();
                if (primaryColor != 0) {
                    // Set primary button color
                    FancyButton primaryButton = (FancyButton) findViewById(R.id.button_primary);
                    if (primaryButton != null) {
                        primaryButton.setBackgroundColor(primaryColor);
                    }

                    // Set button confirm color
                    FancyButton confirmPayButton = (FancyButton) findViewById(R.id.btn_confirm_payment);
                    if (confirmPayButton != null) {
                        confirmPayButton.setBackgroundColor(primaryColor);
                    }

                    // Set button pay now color
                    FancyButton payNowButton = (FancyButton) findViewById(R.id.btn_pay_now);
                    if (payNowButton != null) {
                        payNowButton.setBackgroundColor(primaryColor);
                    }

                    // Set tab indicator color if available
                    TabLayout tabLayout = (TabLayout) findViewById(R.id.instruction_tabs);
                    if (tabLayout != null) {
                        tabLayout.setSelectedTabIndicatorColor(primaryColor);
                    }
                }

                if (primaryDarkColor != 0) {
                    // Set amount text color
                    BoldTextView textTotalAmount = (BoldTextView) findViewById(R.id.text_amount);
                    if (textTotalAmount != null) {
                        textTotalAmount.setTextColor(primaryDarkColor);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("themes", "init:" + e.getMessage());
        }
    }

    public void replaceFragment(Fragment fragment, int fragmentContainer, boolean addToBackStack, boolean clearBackStack) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Logger.i("replace fragment");
            boolean fragmentPopped = false;
            String backStateName = fragment.getClass().getName();

            if (clearBackStack) {
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            if (!fragmentPopped) {
                Logger.i("fragment not in back stack, create it");
                FragmentTransaction ft = fragmentManager.beginTransaction();
                if (MidtransSDK.getInstance().getUIKitCustomSetting() != null
                        && MidtransSDK.getInstance().getUIKitCustomSetting().isEnabledAnimation()) {
                    ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.slide_in_back, R.anim.slide_out_back);
                }
                ft.replace(fragmentContainer, fragment, backStateName);
                if (addToBackStack) {
                    ft.addToBackStack(backStateName);
                }
                ft.commit();
                currentFragmentName = backStateName;
                if (saveCurrentFragment) {
                    currentFragment = fragment;
                }
            }
        }
    }

    protected Fragment getCurrentFagment(Class fragmentClass) {
        if (!TextUtils.isEmpty(currentFragmentName) && currentFragmentName.equals(fragmentClass.getName())) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentName);
            return currentFragment;
        }
        return null;
    }

    protected void initPaymentStatus(TransactionResponse transactionResponse, String errorMessage, int paymentMethod, boolean addToBackStack) {
        if (MidtransSDK.getInstance().getUIKitCustomSetting().isShowPaymentStatus()) {
            PaymentTransactionStatusFragment paymentTransactionStatusFragment =
                    PaymentTransactionStatusFragment.newInstance(transactionResponse, paymentMethod);
            replaceFragment(paymentTransactionStatusFragment, R.id.instruction_container, addToBackStack, false);
//            hideTotalAmount();
        } else {
            setResultCode(RESULT_OK);
            setResultAndFinish(transactionResponse, errorMessage);
        }
    }


    protected void setResultAndFinish(TransactionResponse transactionResponse, String errorMessage) {
        Intent data = new Intent();
        data.putExtra(getString(R.string.transaction_response), transactionResponse);
        data.putExtra(getString(R.string.error_transaction), errorMessage);
        setResult(this.RESULT_CODE, data);
        finish();
    }

    public void setResultCode(int resultCode) {
        this.RESULT_CODE = resultCode;
    }

}
