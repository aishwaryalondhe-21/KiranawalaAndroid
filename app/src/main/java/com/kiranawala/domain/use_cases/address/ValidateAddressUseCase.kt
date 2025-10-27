package com.kiranawala.domain.use_cases.address

import com.kiranawala.domain.models.AddressType
import javax.inject.Inject

data class AddressValidationRequest(
    val formattedAddress: String,
    val addressLine1: String,
    val city: String,
    val state: String,
    val pincode: String,
    val receiverName: String,
    val receiverPhone: String,
    val addressType: AddressType
)

data class AddressValidationErrors(
    val formattedAddressError: String? = null,
    val addressLine1Error: String? = null,
    val cityError: String? = null,
    val stateError: String? = null,
    val pincodeError: String? = null,
    val receiverNameError: String? = null,
    val receiverPhoneError: String? = null
) {
    val isValid: Boolean
        get() = formattedAddressError == null &&
            addressLine1Error == null &&
            cityError == null &&
            stateError == null &&
            pincodeError == null &&
            receiverNameError == null &&
            receiverPhoneError == null
}

class ValidateAddressUseCase @Inject constructor() {

    operator fun invoke(request: AddressValidationRequest): AddressValidationErrors {
        val receiverNamePattern = Regex("^[A-Za-z ]{2,50}")
        val receiverPhonePattern = Regex("^[0-9]{10}")
        val pincodePattern = Regex("^[0-9]{5,6}")

        val formattedAddressError = if (request.formattedAddress.isBlank()) {
            "Select a location from the map"
        } else null

        val addressLine1Error = if (request.addressLine1.trim().length < 3) {
            "Enter a detailed house or building name"
        } else null

        val cityError = if (request.city.trim().length < 2) {
            "Enter a valid city"
        } else null

        val stateError = if (request.state.trim().length < 2) {
            "Enter a valid state"
        } else null

        val pincodeError = if (!pincodePattern.matches(request.pincode.trim())) {
            "Enter a valid pincode"
        } else null

        val receiverNameError = when {
            request.receiverName.isBlank() -> "Receiver name is required"
            !receiverNamePattern.matches(request.receiverName.trim()) -> "Use 2-50 alphabetic characters"
            else -> null
        }

        val receiverPhoneError = when {
            !receiverPhonePattern.matches(request.receiverPhone.trim()) -> "Enter 10 digit phone number"
            else -> null
        }

        return AddressValidationErrors(
            formattedAddressError = formattedAddressError,
            addressLine1Error = addressLine1Error,
            cityError = cityError,
            stateError = stateError,
            pincodeError = pincodeError,
            receiverNameError = receiverNameError,
            receiverPhoneError = receiverPhoneError
        )
    }
}
