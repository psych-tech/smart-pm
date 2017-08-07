package com.emolance.enterprise;

import android.accounts.AccountManager;
import android.content.Context;

import com.emolance.enterprise.auth.ApiKeyProvider;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.service.ServiceGenerator;
import com.emolance.enterprise.ui.AdminFragment;
import com.emolance.enterprise.ui.MainFragment;
import com.emolance.enterprise.ui.NewMainActivity;
import com.emolance.enterprise.ui.QRScanActivity;
import com.emolance.enterprise.ui.ReportActivity;
import com.emolance.enterprise.ui.ReportFragment;
import com.emolance.enterprise.ui.UserReportCreatorActivity;
import com.emolance.enterprise.ui.UserReportCreatorFragment;
import com.emolance.enterprise.ui.UserReportsFragment;
import com.emolance.enterprise.util.Constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yusun on 5/26/15.
 */
@Module(
        injects = {
                EmolanceApplication.class,
                NewMainActivity.class,
                QRScanActivity.class,
                MainFragment.class,
                ReportFragment.class,
                UserReportsFragment.class,
                AdminFragment.class,
                UserReportCreatorActivity.class,
                ReportActivity.class,
                UserReportCreatorFragment.class
        }
)
public class AppModule {

    @Provides
    public EmolanceAPI provideEmolanceAPI(ApiKeyProvider apiKeyProvider) {
        return ServiceGenerator.createService(
                EmolanceAPI.class, Constants.ENDPOINT, apiKeyProvider);
    }

    @Provides
    AccountManager provideAccountManager(final Context context) {
        return AccountManager.get(context);
    }

    @Provides
    ApiKeyProvider provideApiKeyProvider(AccountManager accountManager) {
        return new ApiKeyProvider(accountManager);
    }

    @Provides @Singleton
    Context provideApplicationContext() {
        return EmolanceApplication.getInstance();
    }

}
