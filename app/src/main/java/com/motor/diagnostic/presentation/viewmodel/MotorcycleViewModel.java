package com.motor.diagnostic.presentation.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.motor.diagnostic.domain.model.Motorcycle;
import com.motor.diagnostic.domain.repository.MotorcycleRepository;
import com.motor.diagnostic.domain.usecase.ConnectMotorcycleUseCase;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for motorcycle management
 */
public class MotorcycleViewModel extends ViewModel {

    private final MotorcycleRepository motorcycleRepository;
    private final ConnectMotorcycleUseCase connectMotorcycleUseCase;

    private final MutableLiveData<List<Motorcycle>> userMotorcycles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Motorcycle> selectedMotorcycle = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addMotorcycleResult = new MutableLiveData<>();

    public MotorcycleViewModel(
            MotorcycleRepository motorcycleRepository,
            ConnectMotorcycleUseCase connectMotorcycleUseCase) {
        this.motorcycleRepository = motorcycleRepository;
        this.connectMotorcycleUseCase = connectMotorcycleUseCase;
    }

    /**
     * Load user's motorcycles
     */
    public void loadUserMotorcycles() {
        loading.setValue(true);
        errorMessage.setValue(null);

        motorcycleRepository.getUserMotorcycles()
                .thenAccept(motorcycles -> {
                    userMotorcycles.postValue(motorcycles);
                    loading.postValue(false);

                    if (!motorcycles.isEmpty() && selectedMotorcycle.getValue() == null) {
                        selectMotorcycle(motorcycles.get(0));
                    }
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }

    /**
     * Add a new motorcycle
     * @param motorcycle The motorcycle to add
     */
    public void addMotorcycle(Motorcycle motorcycle) {
        addMotorcycle(motorcycle, null);
    }
    
    /**
     * Add a new motorcycle with an image
     * @param motorcycle The motorcycle to add
     * @param imageUri The URI of the motorcycle image
     */
    public void addMotorcycle(Motorcycle motorcycle, Uri imageUri) {
        loading.setValue(true);
        errorMessage.setValue(null);
        addMotorcycleResult.setValue(false);

        if (imageUri != null) {
            // Upload image first, then add motorcycle with image URL
            motorcycleRepository.uploadMotorcycleImage(imageUri)
                    .thenAccept(imageUrl -> {
                        motorcycle.setImageUrl(imageUrl);
                        addMotorcycleToDatabase(motorcycle);
                    })
                    .exceptionally(e -> {
                        errorMessage.postValue("Failed to upload image: " + e.getMessage());
                        loading.postValue(false);
                        return null;
                    });
        } else {
            // Add motorcycle without image
            addMotorcycleToDatabase(motorcycle);
        }
    }
    
    /**
     * Add motorcycle to database
     * @param motorcycle The motorcycle to add
     */
    private void addMotorcycleToDatabase(Motorcycle motorcycle) {
        motorcycleRepository.addMotorcycle(motorcycle)
                .thenAccept(addedMotorcycle -> {
                    if (addedMotorcycle != null) {
                        loadUserMotorcycles();
                        addMotorcycleResult.postValue(true);
                    } else {
                        errorMessage.postValue("Failed to add motorcycle");
                        loading.postValue(false);
                    }
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }

    /**
     * Delete a motorcycle
     * @param motorcycleId The ID of the motorcycle to delete
     */
    public void deleteMotorcycle(String motorcycleId) {
        loading.setValue(true);
        errorMessage.setValue(null);

        motorcycleRepository.deleteMotorcycle(motorcycleId)
                .thenAccept(success -> {
                    if (success) {
                        loadUserMotorcycles();
                    } else {
                        errorMessage.postValue("Failed to delete motorcycle");
                        loading.postValue(false);
                    }
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    loading.postValue(false);
                    return null;
                });
    }

    /**
     * Select a motorcycle
     * @param motorcycle The motorcycle to select
     */
    public void selectMotorcycle(Motorcycle motorcycle) {
        selectedMotorcycle.setValue(motorcycle);
    }

    /**
     * Get user motorcycles
     * @return LiveData holding user motorcycles
     */
    public LiveData<List<Motorcycle>> getUserMotorcycles() {
        return userMotorcycles;
    }

    /**
     * Get selected motorcycle
     * @return LiveData holding the selected motorcycle
     */
    public LiveData<Motorcycle> getSelectedMotorcycle() {
        return selectedMotorcycle;
    }

    /**
     * Get loading state
     * @return LiveData holding loading state
     */
    public LiveData<Boolean> getLoading() {
        return loading;
    }

    /**
     * Get error message
     * @return LiveData holding error message
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Get add motorcycle result
     * @return LiveData holding add motorcycle result
     */
    public LiveData<Boolean> getAddMotorcycleResult() {
        return addMotorcycleResult;
    }
} 