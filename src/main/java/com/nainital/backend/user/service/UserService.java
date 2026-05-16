package com.nainital.backend.user.service;

import com.nainital.backend.user.model.Address;
import com.nainital.backend.user.model.User;
import com.nainital.backend.user.repository.AddressRepository;
import com.nainital.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    // ─────────────────────────────────────────────
    // Profile
    // ─────────────────────────────────────────────

    public Map<String, Object> getProfile(String userId) {
        User user = findUser(userId);
        List<Address> addresses = addressRepository.findByUserId(userId);
        return Map.of("user", user, "addresses", addresses);
    }

    public User updateProfile(String userId, String name, String phone,
                              String profileImage, String gender, String dateOfBirth) {
        User user = findUser(userId);

        if (name != null && !name.isBlank()) {
            user.setName(name);
            // Mark onboarding as complete when name is set for the first time
            if (!user.isOnboardingCompleted()) {
                user.setOnboardingCompleted(true);
            }
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (profileImage != null) {
            user.setProfileImage(profileImage);
        }
        if (gender != null) {
            user.setGender(gender);
        }
        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth);
        }

        return userRepository.save(user);
    }

    // ─────────────────────────────────────────────
    // Addresses
    // ─────────────────────────────────────────────

    public List<Address> getAddresses(String userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address addAddress(String userId, String label, String line1, String line2,
                              String city, String state, String pincode, boolean defaultAddress) {
        if (defaultAddress) {
            clearDefaultAddresses(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .label(label)
                .line1(line1)
                .line2(line2)
                .city(city)
                .state(state)
                .pincode(pincode)
                .defaultAddress(defaultAddress)
                .build();

        return addressRepository.save(address);
    }

    public Address updateAddress(String userId, String addressId, String label,
                                 String line1, String line2, String city,
                                 String state, String pincode, Boolean defaultAddress) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found."));

        if (label != null) address.setLabel(label);
        if (line1 != null) address.setLine1(line1);
        if (line2 != null) address.setLine2(line2);
        if (city != null) address.setCity(city);
        if (state != null) address.setState(state);
        if (pincode != null) address.setPincode(pincode);

        if (defaultAddress != null) {
            if (defaultAddress) {
                clearDefaultAddresses(userId);
            }
            address.setDefaultAddress(defaultAddress);
        }

        return addressRepository.save(address);
    }

    public void deleteAddress(String userId, String addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found."));
        addressRepository.delete(address);
    }

    public Address setDefaultAddress(String userId, String addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found."));

        clearDefaultAddresses(userId);
        address.setDefaultAddress(true);
        return addressRepository.save(address);
    }

    // ─────────────────────────────────────────────
    // FCM Push Tokens
    // ─────────────────────────────────────────────

    public void registerFcmToken(String userId, String token) {
        User user = findUser(userId);
        List<String> tokens = user.getFcmTokens() != null ? new ArrayList<>(user.getFcmTokens()) : new ArrayList<>();
        if (!tokens.contains(token)) {
            tokens.add(token);
            if (tokens.size() > 10) tokens = tokens.subList(tokens.size() - 10, tokens.size());
        }
        user.setFcmTokens(tokens);
        userRepository.save(user);
    }

    public void removeFcmToken(String userId, String token) {
        User user = findUser(userId);
        List<String> tokens = user.getFcmTokens();
        if (tokens != null && tokens.remove(token)) {
            user.setFcmTokens(tokens);
            userRepository.save(user);
        }
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    private void clearDefaultAddresses(String userId) {
        List<Address> defaults = addressRepository.findByUserIdAndDefaultAddress(userId, true);
        defaults.forEach(addr -> addr.setDefaultAddress(false));
        addressRepository.saveAll(defaults);
    }
}
