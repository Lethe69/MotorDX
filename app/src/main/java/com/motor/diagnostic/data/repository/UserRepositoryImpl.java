package com.motor.diagnostic.data.repository;

import androidx.annotation.NonNull;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.motor.diagnostic.data.model.UserEntity;
import com.motor.diagnostic.domain.model.User;
import com.motor.diagnostic.domain.repository.UserRepository;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of UserRepository that uses Firebase
 */
public class UserRepositoryImpl implements UserRepository {
    
    private static final String USERS_PATH = "users";
    private static final String PROFILE_IMAGES_PATH = "profile_images";
    
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;
    
    public UserRepositoryImpl() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.storageReference = FirebaseStorage.getInstance().getReference();
    }
    
    @Override
    public CompletableFuture<User> registerUser(String email, String password, User user) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        user.setId(firebaseUser.getUid());
                        user.setEmail(email);
                        
                        // Update Firebase Auth profile
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user.getFullName())
                                .build();
                        
                        firebaseUser.updateProfile(profileUpdates)
                                .addOnSuccessListener(aVoid -> saveUserToDatabase(user, future))
                                .addOnFailureListener(future::completeExceptionally);
                    } else {
                        future.completeExceptionally(new Exception("Failed to create user"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        
        return future;
    }
    
    private void saveUserToDatabase(User user, CompletableFuture<User> future) {
        String userId = user.getId();
        UserEntity userEntity = UserEntity.fromDomain(user);
        
        databaseReference.child(USERS_PATH).child(userId).setValue(userEntity.toMap())
                .addOnSuccessListener(aVoid -> future.complete(user))
                .addOnFailureListener(future::completeExceptionally);
    }
    
    @Override
    public CompletableFuture<User> loginUser(String email, String password) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        // For ESP32 hardcoded credentials
        if (email.equals("user@motor.com") && password.equals("motor0303")) {
            // Special handling for ESP32 device
            User espUser = new User();
            espUser.setId("esp32");
            espUser.setFullName("ESP32 Device");
            espUser.setEmail(email);
            future.complete(espUser);
            return future;
        }
        
        try {
            // We need to completely reset Firebase auth before attempting to sign in
            // First sign out to clear any existing session
            firebaseAuth.signOut();
            
            // Sleep for a moment to ensure the sign out is processed
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.w("UserRepository", "Sleep interrupted", e);
            }
            
            // If we are in the main thread, use a background thread for the sign in
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(() -> {
                    try {
                        // Try to sign in with fresh credentials
                        firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser firebaseUser = authResult.getUser();
                                if (firebaseUser != null) {
                                    getUserFromDatabase(firebaseUser.getUid(), future);
                                } else {
                                    future.completeExceptionally(new Exception("Failed to get user after login"));
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (e.getMessage() != null && 
                                    (e.getMessage().contains("invalid credentials") || 
                                     e.getMessage().contains("malformed") || 
                                     e.getMessage().contains("expired"))) {
                                    
                                    // Force complete Firebase reset and retry
                                    try {
                                        // Force refresh token
                                        FirebaseAuth.getInstance().signOut();
                                        
                                        // Wait a bit longer
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException ie) {
                                            Log.w("UserRepository", "Sleep interrupted during retry", ie);
                                        }
                                        
                                        // Final login attempt
                                        Handler mainHandler = new Handler(Looper.getMainLooper());
                                        mainHandler.post(() -> {
                                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                                .addOnSuccessListener(retryResult -> {
                                                    FirebaseUser retryUser = retryResult.getUser();
                                                    if (retryUser != null) {
                                                        getUserFromDatabase(retryUser.getUid(), future);
                                                    } else {
                                                        future.completeExceptionally(new Exception("Failed to get user after final retry"));
                                                    }
                                                })
                                                .addOnFailureListener(retryError -> {
                                                    // If this fails, we've done all we can do
                                                    future.completeExceptionally(new Exception("All login attempts failed: " + retryError.getMessage()));
                                                });
                                        });
                                    } catch (Exception refreshError) {
                                        future.completeExceptionally(new Exception("Error during final retry: " + refreshError.getMessage()));
                                    }
                                } else {
                                    future.completeExceptionally(e);
                                }
                            });
                    } catch (Exception e) {
                        future.completeExceptionally(new Exception("Background login attempt failed: " + e.getMessage()));
                    }
                }).start();
            } else {
                // Already in a background thread, proceed directly
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            getUserFromDatabase(firebaseUser.getUid(), future);
                        } else {
                            future.completeExceptionally(new Exception("Failed to get user after login"));
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (e.getMessage() != null && 
                            (e.getMessage().contains("invalid credentials") || 
                             e.getMessage().contains("malformed") || 
                             e.getMessage().contains("expired"))) {
                            
                            // Force complete Firebase reset and retry
                            try {
                                // Force refresh token
                                FirebaseAuth.getInstance().signOut();
                                
                                // Wait a bit longer
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ie) {
                                    Log.w("UserRepository", "Sleep interrupted during retry", ie);
                                }
                                
                                // Final login attempt
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(retryResult -> {
                                        FirebaseUser retryUser = retryResult.getUser();
                                        if (retryUser != null) {
                                            getUserFromDatabase(retryUser.getUid(), future);
                                        } else {
                                            future.completeExceptionally(new Exception("Failed to get user after final retry"));
                                        }
                                    })
                                    .addOnFailureListener(retryError -> {
                                        // If this fails, we've done all we can do
                                        future.completeExceptionally(new Exception("All login attempts failed: " + retryError.getMessage()));
                                    });
                            } catch (Exception refreshError) {
                                future.completeExceptionally(new Exception("Error during final retry: " + refreshError.getMessage()));
                            }
                        } else {
                            future.completeExceptionally(e);
                        }
                    });
            }
        } catch (Exception e) {
            future.completeExceptionally(new Exception("Login attempt failed: " + e.getMessage()));
        }
        
        return future;
    }
    
    @Override
    public CompletableFuture<User> getCurrentUser() {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            getUserFromDatabase(firebaseUser.getUid(), future);
        } else {
            future.completeExceptionally(new Exception("No user is currently logged in"));
        }
        
        return future;
    }
    
    private void getUserFromDatabase(String userId, CompletableFuture<User> future) {
        databaseReference.child(USERS_PATH).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserEntity userEntity = snapshot.getValue(UserEntity.class);
                    if (userEntity != null) {
                        future.complete(userEntity.toDomain());
                    } else {
                        future.completeExceptionally(new Exception("Failed to parse user data"));
                    }
                } else {
                    // User exists in Auth but not in Database, create a basic record
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        User user = new User();
                        user.setId(firebaseUser.getUid());
                        user.setEmail(firebaseUser.getEmail());
                        user.setFullName(firebaseUser.getDisplayName());
                        
                        saveUserToDatabase(user, future);
                    } else {
                        future.completeExceptionally(new Exception("User not found in database"));
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
    }
    
    @Override
    public CompletableFuture<User> updateUser(User user) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        if (user.getId() == null) {
            future.completeExceptionally(new Exception("User ID cannot be null"));
            return future;
        }
        
        UserEntity userEntity = UserEntity.fromDomain(user);
        
        databaseReference.child(USERS_PATH).child(user.getId()).updateChildren(userEntity.toMap())
                .addOnSuccessListener(aVoid -> {
                    // Update display name in Firebase Auth
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user.getFullName())
                                .build();
                        
                        firebaseUser.updateProfile(profileUpdates)
                                .addOnSuccessListener(v -> future.complete(user))
                                .addOnFailureListener(future::completeExceptionally);
                    } else {
                        future.complete(user);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        
        return future;
    }
    
    @Override
    public CompletableFuture<Boolean> changePassword(String oldPassword, String newPassword) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.getEmail() != null) {
            // Re-authenticate the user first
            firebaseAuth.signInWithEmailAndPassword(firebaseUser.getEmail(), oldPassword)
                    .addOnSuccessListener(authResult -> 
                            firebaseUser.updatePassword(newPassword)
                                    .addOnSuccessListener(aVoid -> future.complete(true))
                                    .addOnFailureListener(e -> future.completeExceptionally(e)))
                    .addOnFailureListener(future::completeExceptionally);
        } else {
            future.completeExceptionally(new Exception("No user is currently logged in"));
        }
        
        return future;
    }
    
    @Override
    public CompletableFuture<Boolean> resetPassword(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> future.complete(true))
                .addOnFailureListener(future::completeExceptionally);
        
        return future;
    }
    
    @Override
    public void signOut() {
        // First sign out from Firebase Auth
        firebaseAuth.signOut();
        
        // Clear any cached data or references
        // This would be where you'd clear any in-memory cache or tokens
        Log.d("UserRepository", "Clearing user data cache");
    }
    
    @Override
    public CompletableFuture<Boolean> logoutUser() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            // Sign out from Firebase Auth
            firebaseAuth.signOut();
            
            // Clear any cached data
            signOut();
            
            // Verify that the user is actually logged out
            if (firebaseAuth.getCurrentUser() == null) {
                // Successfully logged out
                Log.d("UserRepository", "User successfully logged out");
                future.complete(true);
            } else {
                // Failed to log out
                Log.e("UserRepository", "Failed to log out user, still logged in");
                future.completeExceptionally(new Exception("Failed to log out user completely"));
            }
        } catch (Exception e) {
            Log.e("UserRepository", "Error during logout: " + e.getMessage(), e);
            future.completeExceptionally(new Exception("Failed to log out: " + e.getMessage()));
        }
        
        return future;
    }
    
    @Override
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    @Override
    public CompletableFuture<String> updateProfileImage(String imagePath) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            future.completeExceptionally(new Exception("No user is currently logged in"));
            return future;
        }
        
        String userId = firebaseUser.getUid();
        StorageReference imageRef = storageReference.child(PROFILE_IMAGES_PATH + "/" + userId + ".jpg");
        
        // Convert local file path to Uri
        Uri fileUri = Uri.parse("file://" + imagePath);
        
        imageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            
                            // Update user's profile image URL in database
                            databaseReference.child(USERS_PATH).child(userId).child("profileImageUrl")
                                    .setValue(imageUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update Firebase Auth profile
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setPhotoUri(uri)
                                                .build();
                                        
                                        firebaseUser.updateProfile(profileUpdates)
                                                .addOnSuccessListener(v -> future.complete(imageUrl))
                                                .addOnFailureListener(future::completeExceptionally);
                                    })
                                    .addOnFailureListener(future::completeExceptionally);
                        })
                        .addOnFailureListener(future::completeExceptionally))
                .addOnFailureListener(future::completeExceptionally);
        
        return future;
    }
} 