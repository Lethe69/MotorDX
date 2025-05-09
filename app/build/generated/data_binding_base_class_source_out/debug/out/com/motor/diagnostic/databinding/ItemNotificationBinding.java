// Generated by view binder compiler. Do not edit!
package com.motor.diagnostic.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.motor.diagnostic.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemNotificationBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final ImageView btnDeleteNotification;

  @NonNull
  public final ImageView ivNotificationType;

  @NonNull
  public final TextView tvNotificationMessage;

  @NonNull
  public final TextView tvNotificationTime;

  @NonNull
  public final TextView tvNotificationTitle;

  @NonNull
  public final View unreadIndicator;

  private ItemNotificationBinding(@NonNull CardView rootView,
      @NonNull ImageView btnDeleteNotification, @NonNull ImageView ivNotificationType,
      @NonNull TextView tvNotificationMessage, @NonNull TextView tvNotificationTime,
      @NonNull TextView tvNotificationTitle, @NonNull View unreadIndicator) {
    this.rootView = rootView;
    this.btnDeleteNotification = btnDeleteNotification;
    this.ivNotificationType = ivNotificationType;
    this.tvNotificationMessage = tvNotificationMessage;
    this.tvNotificationTime = tvNotificationTime;
    this.tvNotificationTitle = tvNotificationTitle;
    this.unreadIndicator = unreadIndicator;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemNotificationBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemNotificationBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_notification, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemNotificationBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnDeleteNotification;
      ImageView btnDeleteNotification = ViewBindings.findChildViewById(rootView, id);
      if (btnDeleteNotification == null) {
        break missingId;
      }

      id = R.id.ivNotificationType;
      ImageView ivNotificationType = ViewBindings.findChildViewById(rootView, id);
      if (ivNotificationType == null) {
        break missingId;
      }

      id = R.id.tvNotificationMessage;
      TextView tvNotificationMessage = ViewBindings.findChildViewById(rootView, id);
      if (tvNotificationMessage == null) {
        break missingId;
      }

      id = R.id.tvNotificationTime;
      TextView tvNotificationTime = ViewBindings.findChildViewById(rootView, id);
      if (tvNotificationTime == null) {
        break missingId;
      }

      id = R.id.tvNotificationTitle;
      TextView tvNotificationTitle = ViewBindings.findChildViewById(rootView, id);
      if (tvNotificationTitle == null) {
        break missingId;
      }

      id = R.id.unreadIndicator;
      View unreadIndicator = ViewBindings.findChildViewById(rootView, id);
      if (unreadIndicator == null) {
        break missingId;
      }

      return new ItemNotificationBinding((CardView) rootView, btnDeleteNotification,
          ivNotificationType, tvNotificationMessage, tvNotificationTime, tvNotificationTitle,
          unreadIndicator);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
