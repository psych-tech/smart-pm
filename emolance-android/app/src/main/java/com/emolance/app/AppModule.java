package com.emolance.app;

import android.accounts.AccountManager;
import android.content.Context;

import com.emolance.app.auth.ApiKeyProvider;
import com.emolance.app.service.EmolanceAPI;
import com.emolance.app.service.ServiceGenerator;
import com.emolance.app.ui.AdminFragment;
import com.emolance.app.ui.MainFragment;
import com.emolance.app.ui.NewMainActivity;
import com.emolance.app.ui.ReportActivity;
import com.emolance.app.ui.ReportFragment;
import com.emolance.app.ui.UserReportCreatorActivity;
import com.emolance.app.util.Constants;

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
                MainFragment.class,
                ReportFragment.class,
                AdminFragment.class,
                UserReportCreatorActivity.class,
                ReportActivity.class
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
