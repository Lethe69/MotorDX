// Generated by view binder compiler. Do not edit!
package com.motor.diagnostic.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.motor.diagnostic.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySplashBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView ivLogo;

  @NonNull
  public final ProgressBar progressBar;

  @NonNull
  public final TextView tvAppName;

  @NonNull
  public final TextView tvDebugInfo;

  @NonNull
  public final TextView tvVersion;

  private ActivitySplashBinding(@NonNull ConstraintLayout rootView, @NonNull ImageView ivLogo,
      @NonNull ProgressBar progressBar, @NonNull TextView tvAppName, @NonNull TextView tvDebugInfo,
      @NonNull TextView tvVersion) {
    this.rootView = rootView;
    this.ivLogo = ivLogo;
    this.progressBar = progressBar;
    this.tvAppName = tvAppName;
    this.tvDebugInfo = tvDebugInfo;
    this.tvVersion = tvVersion;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySplashBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySplashBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_splash, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySplashBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.ivLogo;
      ImageView ivLogo = ViewBindings.findChildViewById(rootView, id);
      if (ivLogo == null) {
        break missingId;
      }

      id = R.id.progressBar;
      ProgressBar progressBar = ViewBindings.findChildViewById(rootView, id);
      if (progressBar == null) {
        break missingId;
      }

      id = R.id.tvAppName;
      TextView tvAppName = ViewBindings.findChildViewById(rootView, id);
      if (tvAppName == null) {
        break missingId;
      }

      id = R.id.tvDebugInfo;
      TextView tvDebugInfo = ViewBindings.findChildViewById(rootView, id);
      if (tvDebugInfo == null) {
        break missingId;
      }

      id = R.id.tvVersion;
      TextView tvVersion = ViewBindings.findChildViewById(rootView, id);
      if (tvVersion == null) {
        break missingId;
      }

      return new ActivitySplashBinding((ConstraintLayout) rootView, ivLogo, progressBar, tvAppName,
          tvDebugInfo, tvVersion);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
