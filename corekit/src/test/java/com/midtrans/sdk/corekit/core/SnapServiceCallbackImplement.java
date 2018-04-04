package com.midtrans.sdk.corekit.core;

import com.midtrans.sdk.corekit.callback.CheckoutCallback;
import com.midtrans.sdk.corekit.callback.TransactionCallback;
import com.midtrans.sdk.corekit.callback.TransactionOptionsCallback;
import com.midtrans.sdk.corekit.models.TokenRequestModel;
import com.midtrans.sdk.corekit.models.TransactionResponse;
import com.midtrans.sdk.corekit.models.snap.Token;
import com.midtrans.sdk.corekit.models.snap.Transaction;
import com.midtrans.sdk.corekit.models.snap.payment.BankTransferPaymentRequest;
import com.midtrans.sdk.corekit.models.snap.payment.BasePaymentRequest;
import com.midtrans.sdk.corekit.models.snap.payment.CreditCardPaymentRequest;
import com.midtrans.sdk.corekit.models.snap.payment.GCIPaymentRequest;
import com.midtrans.sdk.corekit.models.snap.payment.GoPayPaymentRequest;
import com.midtrans.sdk.corekit.models.snap.payment.IndosatDompetkuPaymentRequest;
import com.midtrans.sdk.corekit.models.snap.payment.KlikBCAPaymentRequest;
import com.midtrans.sdk.corekit.models.snap.payment.NewMandiriClickPayPaymentRequest;
import com.midtrans.sdk.corekit.models.snap.payment.TelkomselEcashPaymentRequest;

/**
 * Created by ziahaqi on 4/2/18.
 */

public class SnapServiceCallbackImplement implements TransactionCallback, CheckoutCallback, TransactionOptionsCallback {
    CallbackCollaborator callbackCollaborator;
    SnapServiceManager serviceManager;

    public SnapServiceCallbackImplement(SnapServiceManager serviceManager, CallbackCollaborator callbackCollaborator) {
        this.callbackCollaborator = callbackCollaborator;
        this.serviceManager = serviceManager;
    }


    @Override
    public void onError(Throwable error) {
        callbackCollaborator.onError();
    }

    @Override
    public void onSuccess(Token token) {

    }

    @Override
    public void onFailure(Token token, String reason) {

    }

    @Override
    public void onSuccess(TransactionResponse response) {
        callbackCollaborator.onTransactionSuccess();
    }

    @Override
    public void onFailure(TransactionResponse response, String reason) {
        callbackCollaborator.onTransactionFailure();
    }

    @Override
    public void onSuccess(Transaction transaction) {
        callbackCollaborator.onGetPaymentOptionSuccess();
    }

    @Override
    public void onFailure(Transaction transaction, String reason) {
        callbackCollaborator.onGetPaymentOptionFailure();
    }

    public void checkout(TokenRequestModel snapTokenRequestModelMock) {
    }


    public void getTransactionOptions(String tokenId) {
        this.serviceManager.getTransactionOptions(tokenId, this);
    }

    public void paymentUsingCreditCard(String snapToken, CreditCardPaymentRequest creditCardRequest) {
        this.serviceManager.paymentUsingCreditCard(snapToken, creditCardRequest, this);
    }

    public void paymentUsingVa(String snapToken, BankTransferPaymentRequest request) {
        this.serviceManager.paymentUsingVa(snapToken, request, this);
    }

    public void paymentBaseMethod(String snapToken, BasePaymentRequest request) {
        this.serviceManager.paymentUsingBaseMethod(snapToken, request, this);
    }

    public void paymentUsingMandiriClickPay(String snapToken, NewMandiriClickPayPaymentRequest request) {
        this.serviceManager.paymentUsingMandiriClickPay(snapToken, request, this);
    }

    public void paymentUsingTCash(String snapToken, TelkomselEcashPaymentRequest request) {
        this.serviceManager.paymentUsingTelkomselCash(snapToken, request, this);
    }

    public void paymentUsingIndosatDomputku(String snapToken, IndosatDompetkuPaymentRequest request) {
        this.serviceManager.paymentUsingIndosatDompetku(snapToken, request, this);
    }

    public void paymentUsingGci(String snapToken, GCIPaymentRequest request) {
        this.serviceManager.paymentUsingGci(snapToken, request, this);
    }

    public void paymentUsingKlikBca(String snapToken, KlikBCAPaymentRequest request) {
        this.serviceManager.paymentUsingKlikBca(snapToken, request, this);
    }

    public void paymentUsingGopay(String snapToken, GoPayPaymentRequest request) {
        this.serviceManager.paymentUsingGoPay(snapToken, request, this);
    }
}