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

package com.frostchein.atlant.activities.receive;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.frostchein.atlant.Config;
import com.frostchein.atlant.activities.base.BasePresenter;
import com.frostchein.atlant.utils.BitmapUtils;
import com.frostchein.atlant.utils.CredentialHolder;
import com.frostchein.atlant.utils.IntentUtils.EXTRA_STRING;
import javax.inject.Inject;

public class ReceivePresenterImpl implements ReceivePresenter, BasePresenter {

  private ReceiveView view;
  private String line;

  @Inject
  ReceivePresenterImpl(ReceiveView view) {
    this.view = view;
  }

  @Override
  public void onCopyAddress() {
    if (view != null) {
      ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData clip = ClipData.newPlainText(EXTRA_STRING.ADDRESS, CredentialHolder.getAddress());
      clipboard.setPrimaryClip(clip);
      view.onKeyCopied();
    }
  }

  @Override
  public void onCreated() {
    view.setAddress(CredentialHolder.getAddress());
    line = CredentialHolder.getAddress();
    new GenerationQR().execute();
  }

  private class GenerationQR extends AsyncTask<Void, Void, Void> {

    private Bitmap bitmap;

    @Override
    protected Void doInBackground(Void... params) {
      try {
        bitmap = BitmapUtils.QR.generateBitmap(line, Config.SIZE_PX_QR, Config.SIZE_PX_QR, BitmapUtils.QR.MARGIN_NONE);
      } catch (Exception ignored) {

      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      if (bitmap != null && view != null) {
        view.setQR(bitmap);
      }
    }
  }


}
