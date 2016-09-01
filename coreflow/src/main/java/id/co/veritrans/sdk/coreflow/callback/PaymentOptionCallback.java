package id.co.veritrans.sdk.coreflow.callback;

import id.co.veritrans.sdk.coreflow.callback.exception.PaymentOptionError;
import id.co.veritrans.sdk.coreflow.models.snap.Transaction;

/**
 * Created by ziahaqi on 8/31/16.
 */
public interface PaymentOptionCallback {

    public void onSuccess(Transaction transaction);

    public void onFailure(Transaction transaction, String reason);

    public void onError(Throwable error);

}
