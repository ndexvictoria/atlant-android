/*
 * Copyright 2017, 2018 Tensigma Ltd.
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.frostchein.atlant.activities.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.LayoutParams;
import android.support.design.widget.AppBarLayout.LayoutParams.ScrollFlags;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.frostchein.atlant.MyApplication;
import com.frostchein.atlant.R;
import com.frostchein.atlant.activities.backup.BackupActivity;
import com.frostchein.atlant.activities.camera.CameraActivity;
import com.frostchein.atlant.activities.export_wallet.ExportActivity;
import com.frostchein.atlant.activities.home.HomeActivity;
import com.frostchein.atlant.activities.import_wallet.ImportActivity;
import com.frostchein.atlant.activities.login.LoginActivity;
import com.frostchein.atlant.activities.login_selected.LoginSelectedActivity;
import com.frostchein.atlant.activities.login_selected_app.LoginSelectedAppActivity;
import com.frostchein.atlant.activities.main.MainActivity;
import com.frostchein.atlant.activities.receive.ReceiveActivity;
import com.frostchein.atlant.activities.send.SendActivity;
import com.frostchein.atlant.activities.settings.SettingsActivity;
import com.frostchein.atlant.dagger2.component.AppComponent;
import com.frostchein.atlant.drawer_menu.DrawerContent;
import com.frostchein.atlant.drawer_menu.DrawerHelper;
import com.frostchein.atlant.events.login.CredentialsCleared;
import com.frostchein.atlant.utils.CredentialHolder;
import com.frostchein.atlant.utils.DialogUtils;
import com.frostchein.atlant.utils.FontsUtils;
import com.frostchein.atlant.utils.IntentUtils;
import com.frostchein.atlant.views.BaseCustomView;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public abstract class BaseActivity extends AppCompatActivity implements BaseView,
    Drawer.OnDrawerItemClickListener {

  public static final long DISCONNECT_TIMEOUT = 60 * 1000; // 1 min = 1 * 60 * 1000 ms
  public static final int REQUEST_CODE_LOGIN = 1;
  public static final int REQUEST_CODE_LOGIN_SElECT = 2;
  public static final int REQUEST_CODE_LOGIN_SElECT_APP = 3;
  public static final int REQUEST_CODE_HOME = 4;
  public static final int REQUEST_CODE_CAMERA = 5;
  public static final int REQUEST_CODE_RECEIVE = 6;
  public static final int REQUEST_CODE_SEND = 7;
  public static final int REQUEST_CODE_SETTING = 8;
  public static final int REQUEST_CODE_IMPORT_WALLET = 9;
  public static final int REQUEST_CODE_EXPORT = 10;
  public static final int REQUEST_CODE_BACKUP = 11;

  private static final int PERMISSION_REQUEST_CAMERA = 1;
  private static int typeResult;

  protected DrawerHelper drawerHelper;
  @BindView(R.id.appbar)
  protected AppBarLayout appBarLayout;
  @BindView(R.id.coordinator)
  protected CoordinatorLayout coordinatorLayout;
  @BindView(R.id.toolbar)
  protected Toolbar toolbar;
  @BindView(R.id.toolbar_title_text)
  protected TextView toolbarTitle;
  @BindView(R.id.refresh)
  protected SwipeRefreshLayout swipeRefreshLayout;
  @BindView(R.id.custom_toolbar)
  protected FrameLayout baseCustomViewToolbar;

  public static boolean timerLogOut;
  private static Intent intent;
  private static boolean restartApp = false;

  private static Handler disconnectHandler = new Handler();
  private static Runnable runnable = new Runnable() {
    @Override
    public void run() {
      restartApp = true;
      CredentialHolder.clearAndRestart();
    }
  };

  @Override
  public void setContentView(@LayoutRes int layoutResID) {
    RelativeLayout rootLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.activity_base, null);

    FrameLayout activityContainer = rootLayout.findViewById(R.id.base_activity_content);
    getLayoutInflater().inflate(layoutResID, activityContainer, true);
    super.setContentView(rootLayout);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    drawerHelper = new DrawerHelper(this);

    setupComponent(MyApplication.get(this).getAppComponent());
    ButterKnife.bind(this);

    toolbar.setTitle("");
    FontsUtils.toOctarineBold(getContext(), toolbarTitle);
    setSupportActionBar(toolbar);
    if (useToolbar()) {
      toolbar.setVisibility(View.VISIBLE);
    } else {
      toolbar.setVisibility(View.GONE);
    }

    if (useSwipeRefresh()) {
      swipeRefreshLayout.setEnabled(true);
      swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.accent));
      swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
        @Override
        public void onRefresh() {
          if (useSwipeRefresh()) {
            onRefreshSwipe();
          }
        }
      });
    } else {
      swipeRefreshLayout.setEnabled(false);
    }

    if (useCustomToolbar()) {
      baseCustomViewToolbar.addView(getCustomToolbar());
      baseCustomViewToolbar.setVisibility(View.VISIBLE);
    } else {
      baseCustomViewToolbar.setVisibility(View.GONE);
    }
    setDrawerMenu();
    initUI();
  }

  @Override
  public void onUserInteraction() {
    eventsOnScreen();
  }

  private void eventsOnScreen() {
    timerLogOut = timerLogOut();
    if (timerLogOut) {
      resetDisconnectTimer();
    } else {
      stopDisconnectTimer();
    }
  }

  public static void resetDisconnectTimer() {
    disconnectHandler.removeCallbacks(runnable);
    disconnectHandler.postDelayed(runnable, DISCONNECT_TIMEOUT);
  }

  public static void stopDisconnectTimer() {
    disconnectHandler.removeCallbacks(runnable);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (restartApp) {
      restartApp();
    }
    EventBus.getDefault().register(this);
    eventsOnScreen();
  }

  @Override
  public void onPause() {
    if (!isScreenOn() && timerLogOut()) {
      CredentialHolder.clearAndRestart();
    }
    EventBus.getDefault().unregister(this);
    super.onPause();
  }

  protected boolean isScreenOn() {
    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    boolean isScreenOn;
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH) {
      isScreenOn = powerManager.isInteractive();
    } else {
      isScreenOn = powerManager.isScreenOn();
    }
    return isScreenOn;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.menu_toolbar, menu);

    MenuItem menuItemQRCode = menu.findItem(R.id.menu_items_qrcode);
    menuItemQRCode.setVisible(useToolbarActionQRCode());
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    switch (menuItem.getItemId()) {
      case R.id.menu_items_qrcode: {
        onToolbarQR();
      }
      break;
      case android.R.id.home: {
        onToolbarActionHomeSelected();
      }
      break;
    }
    return super.onOptionsItemSelected(menuItem);
  }

  public void setDrawerMenu() {
    if (useDrawer()) {
      drawerHelper.setToolbar(this, toolbar);
      drawerHelper.setEnableState();
      drawerHelper.setListener(this);
      updateDrawerMenu();
    } else {
      if (getSupportActionBar() != null && useToolbarActionHome()) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }
      drawerHelper.setDisableState();
    }
  }

  public void disableScrollToolbar() {
    LayoutParams p = (LayoutParams) toolbar.getLayoutParams();
    p.setScrollFlags(0);
    toolbar.setLayoutParams(p);
  }

  public void enableScrollToolbar() {
    LayoutParams p = (LayoutParams) toolbar.getLayoutParams();
    p.setScrollFlags(LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | LayoutParams.SCROLL_FLAG_SCROLL);
    toolbar.setLayoutParams(p);
  }

  public void enableScrollToolbar(@ScrollFlags int flags) {
    LayoutParams p = (LayoutParams) toolbar.getLayoutParams();
    p.setScrollFlags(flags);
    toolbar.setLayoutParams(p);
  }

  private void updateDrawerMenu() {
    drawerHelper.setDrawerContent(DrawerContent.getDrawerContent());
  }

  protected void setToolbarTitle(int resourceString) {
    toolbarTitle.setText(resourceString);
  }

  @Override
  public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
    int currentDrawerItemResource = drawerHelper.getDrawerItemResourceInPosition(position);
    switch (currentDrawerItemResource) {
      case DrawerContent.ITEM_SEND: {
        goToSendActivity(false, "");
        break;
      }
      case DrawerContent.ITEM_RECEIVE: {
        goToReceiveActivity(false);
        break;
      }

      case DrawerContent.ITEM_SETTINGS: {
        goToSettingsActivity(false);
        break;
      }
    }

    if (drawerHelper.isDrawerOpen()) {
      drawerHelper.closeDrawer();
    }
    return true;
  }

  protected void goToSettingsActivity(boolean withFinish) {
    createIntentWithoutFlags(SettingsActivity.class);
    startActivity(REQUEST_CODE_SETTING, withFinish);
  }

  protected void goToSendActivity(boolean withFinish, String address) {
    createIntentWithoutFlags(SendActivity.class);
    intent.putExtra(IntentUtils.EXTRA_STRING.ADDRESS, address);
    startActivity(REQUEST_CODE_SEND, withFinish);
  }

  protected void goToReceiveActivity(boolean withFinish) {
    createIntentWithoutFlags(ReceiveActivity.class);
    startActivity(REQUEST_CODE_RECEIVE, withFinish);
  }

  protected void goToLoginActivity(boolean withFinish, int typeResult) {
    BaseActivity.typeResult = typeResult;
    createIntentWithFlags(LoginActivity.class);
    intent.putExtra(IntentUtils.EXTRA_STRING.TYPE_RESULT, typeResult);
    startActivity(REQUEST_CODE_LOGIN, withFinish);
  }

  protected void goToLoginActivity(boolean withFinish, int typeResult, String privateKey) {
    BaseActivity.typeResult = typeResult;
    createIntentWithFlags(LoginActivity.class);
    intent.putExtra(IntentUtils.EXTRA_STRING.TYPE_RESULT, typeResult);
    intent.putExtra(IntentUtils.EXTRA_STRING.PRIVATE_KEY, privateKey);
    startActivity(REQUEST_CODE_LOGIN, withFinish);
  }

  protected void goToLoginSelectedActivity(boolean withFinish) {
    createIntentWithFlags(LoginSelectedActivity.class);
    startActivity(REQUEST_CODE_LOGIN_SElECT, withFinish);
  }

  protected void goToLoginSelectedAppActivity(boolean withFinish) {
    createIntentWithFlags(LoginSelectedAppActivity.class);
    startActivity(REQUEST_CODE_LOGIN_SElECT_APP, withFinish);
  }

  protected void goToImportWalletActivity(boolean withFinish) {
    createIntentWithoutFlags(ImportActivity.class);
    startActivity(REQUEST_CODE_IMPORT_WALLET, withFinish);
  }

  protected void goToHomeActivity(boolean withFinish) {
    createIntentWithFlags(HomeActivity.class);
    startActivity(REQUEST_CODE_HOME, withFinish);
  }

  protected void goToCameraActivity(boolean withFinish, int typeResult) {
    BaseActivity.typeResult = typeResult;
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    } else {
      createIntentWithoutFlags(CameraActivity.class);
      intent.putExtra(IntentUtils.EXTRA_STRING.TYPE_RESULT, typeResult);
      startActivity(REQUEST_CODE_CAMERA, withFinish);
    }
  }

  protected void goToExportActivity(boolean withFinish) {
    createIntentWithoutFlags(ExportActivity.class);
    startActivity(REQUEST_CODE_EXPORT, withFinish);
  }

  protected void goToBackupActivity(boolean withFinish) {
    createIntentWithoutFlags(BackupActivity.class);
    startActivity(REQUEST_CODE_BACKUP, withFinish);
  }

  private void createIntentWithoutFlags(Class<?> activityClass) {
    intent = new Intent(this, activityClass);
  }

  private void createIntentWithFlags(Class<?> activityClass, int... flags) {
    intent = new Intent(this, activityClass);
    for (Integer flag : flags) {
      intent.addFlags(flag);
    }
  }

  private void startActivity(int requestCode, boolean withFinish) {
    startActivityForResult(intent, requestCode);
    intent = null;
    if (withFinish && !(this instanceof MainActivity)) {
      finish();
    }
  }

  @Override
  public Context getContext() {
    return this;
  }

  @Override
  public void onNoInternetConnection() {
    showMessage(getString(R.string.system_no_internet_connection));
  }

  @Override
  public void onRefreshStart() {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        if (!swipeRefreshLayout.isRefreshing()) {
          swipeRefreshLayout.setRefreshing(true);
        }
      }
    }, 10);
  }

  @Override
  public void onRefreshComplete() {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        if (swipeRefreshLayout.isRefreshing()) {
          swipeRefreshLayout.setRefreshing(false);
        }
      }
    }, 10);
  }

  @Override
  public void showProgressDialog(String dialogMessage) {
    DialogUtils.openDialogProgress(this, getResources().getString(R.string.app_name), dialogMessage);
  }

  @Override
  public void hideProgressDialog() {
    DialogUtils.hideDialog();
  }

  protected void onToolbarActionHomeSelected() {
    finish();
  }

  protected void onToolbarQR() {
  }

  protected void onRefreshSwipe() {
  }

  protected BaseCustomView getCustomToolbar() {
    return null;
  }

  protected abstract void initUI();

  protected abstract void setupComponent(AppComponent appComponent);

  protected abstract boolean useToolbar();

  protected abstract boolean useDrawer();

  protected abstract boolean useToolbarActionQRCode();

  protected abstract boolean useCustomToolbar();

  protected abstract boolean useSwipeRefresh();

  protected abstract boolean useToolbarActionHome();

  protected abstract boolean timerLogOut();

  //restart application
  @Subscribe
  public void onCredentialsCleared(CredentialsCleared event) {
    restartApp();
  }

  private void restartApp() {
    DialogUtils.hideDialog();
    Intent i = getBaseContext().getPackageManager()
        .getLaunchIntentForPackage(getBaseContext().getPackageName());
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(i);

    restartApp = false;
  }

  protected void showMessage(String message) {
    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
  }

  protected void onScreenError(String text) {
    DialogUtils.openDialogError(getContext(), getString(R.string.system_error), text, R.drawable.ic_dialog_error, null);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode == PERMISSION_REQUEST_CAMERA) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        createIntentWithoutFlags(CameraActivity.class);
        intent.putExtra(IntentUtils.EXTRA_STRING.TYPE_RESULT, typeResult);
        startActivity(REQUEST_CODE_CAMERA, false);
      } else {
        showMessage(getString(R.string.camera_permission));
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }
}
