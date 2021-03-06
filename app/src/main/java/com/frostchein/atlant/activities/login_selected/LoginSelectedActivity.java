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

package com.frostchein.atlant.activities.login_selected;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.frostchein.atlant.R;
import com.frostchein.atlant.activities.base.BaseActivity;
import com.frostchein.atlant.activities.login.LoginActivity;
import com.frostchein.atlant.dagger2.component.AppComponent;
import com.frostchein.atlant.dagger2.component.DaggerLoginSelectedActivityComponent;
import com.frostchein.atlant.dagger2.component.LoginSelectedActivityComponent;
import com.frostchein.atlant.dagger2.modules.LoginSelectedActivityModule;
import com.frostchein.atlant.utils.FontsUtils;
import javax.inject.Inject;

public class LoginSelectedActivity extends BaseActivity implements LoginSelectedView {

  @Inject
  LoginSelectedPresenter presenter;

  @BindView(R.id.name)
  TextView textName;
  @BindView(R.id.login_selected_bt_create)
  Button btCreate;
  @BindView(R.id.login_selected_bt_import)
  Button btImport;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login_selected);
    FontsUtils.toOctarineBold(getContext(), textName);
  }

  @OnClick(R.id.login_selected_bt_create)
  public void onClickCreate() {
    goToLoginActivity(false, LoginActivity.TYPE_AUTHORISATION_FROM_SELECTED);
  }

  @OnClick(R.id.login_selected_bt_import)
  public void onClickImport() {
    goToImportWalletActivity(false);
  }

  @Override
  public void initUI() {

  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    //ActivityCompat.finishAffinity(this);
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    LoginSelectedActivityComponent component = DaggerLoginSelectedActivityComponent.builder()
        .appComponent(appComponent)
        .loginSelectedActivityModule(new LoginSelectedActivityModule(this))
        .build();
    component.inject(this);
  }

  @Override
  public boolean useToolbar() {
    return false;
  }

  @Override
  public boolean useDrawer() {
    return false;
  }

  @Override
  public boolean useToolbarActionHome() {
    return false;
  }

  @Override
  public boolean useToolbarActionQRCode() {
    return false;
  }

  @Override
  public boolean useCustomToolbar() {
    return false;
  }

  @Override
  public boolean useSwipeRefresh() {
    return false;
  }

  @Override
  public boolean timerLogOut() {
    return false;
  }

}
