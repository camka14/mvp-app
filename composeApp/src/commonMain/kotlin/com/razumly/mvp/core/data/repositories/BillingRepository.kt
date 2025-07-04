package com.razumly.mvp.core.data.repositories

import com.razumly.mvp.core.data.dataTypes.EventAbs
import com.razumly.mvp.core.data.dataTypes.Tournament
import com.razumly.mvp.core.util.DbConstants
import io.appwrite.services.Functions
import kotlinx.datetime.Instant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface IBillingRepository : IMVPRepository {
    suspend fun createPurchaseIntent(
        event: EventAbs, teamId: String? = null
    ): Result<PurchaseIntent?>

    suspend fun createAccount(): Result<String>
    suspend fun createCustomer(): Result<Unit>
    suspend fun getOnboardingLink(): Result<String>
}

class BillingRepository(
    private val userRepository: IUserRepository, private val functions: Functions
) : IBillingRepository {
    override suspend fun createPurchaseIntent(
        event: EventAbs, teamId: String?
    ): Result<PurchaseIntent?> = runCatching {
        val user = userRepository.currentUser.value.getOrThrow()
        val isTournament = when (event) {
            is Tournament -> true
            else -> false
        }
        if (user.stripeAccountId.isBlank()) {
            createAccount()
            null
        } else {
            Json.decodeFromString(
                PurchaseIntent.serializer(), functions.createExecution(
                    functionId = DbConstants.BILLING_FUNCTION,
                    body = Json.encodeToString(
                        CreatePurchaseIntent(user.id, event.id, teamId, isTournament)
                    ),
                    async = false,
                ).responseBody
            )
        }
    }

    override suspend fun createAccount(): Result<String> = runCatching {
        val user = userRepository.currentUser.value.getOrThrow()
        val response = Json.decodeFromString<CreateAccountResponse>(
            functions.createExecution(
                functionId = DbConstants.BILLING_FUNCTION,
                body = Json.encodeToString(
                    CreateAccount(user.id)
                ),
                async = false,
            ).responseBody
        )
        userRepository.updateUser(user.copy(stripeAccountId = response.accountId))
        response.onboardingUrl
    }

    override suspend fun createCustomer(): Result<Unit> = runCatching {
        val user = userRepository.currentUser.value.getOrThrow()
        val response = Json.decodeFromString<CreateAccountResponse>(
            functions.createExecution(
                functionId = DbConstants.BILLING_FUNCTION,
                body = Json.encodeToString(
                    CreateCustomer(user.id)
                ),
                async = false,
            ).responseBody
        )
        userRepository.updateUser(user.copy(stripeCustomerId = response.accountId))
    }

    override suspend fun getOnboardingLink(): Result<String> = runCatching {
        val user = userRepository.currentUser.value.getOrThrow()
        if (user.stripeAccountId.isBlank()) throw Exception("User has no stripe account")

        val response = Json.decodeFromString<CreateAccountResponse>(
            functions.createExecution(
                functionId = DbConstants.BILLING_FUNCTION,
                body = Json.encodeToString(
                    GetHostOnboardingLink(user.stripeAccountId)
                ),
                async = false,
            ).responseBody
        )
        userRepository.updateUser(user.copy(stripeAccountId = response.accountId))
        response.onboardingUrl
        }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private data class CreatePurchaseIntent(
    val userId: String,
    val eventId: String,
    val teamId: String?,
    val isTournament: Boolean,
    @EncodeDefault val command: String = "create_purchase_intent",
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private data class CreateAccount(
    val userId: String,
    @EncodeDefault val command: String = "connect_host_account",
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private data class CreateCustomer(
    val userId: String,
    @EncodeDefault val command: String = "create_customer",
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private data class GetHostOnboardingLink(
    val accountId: String,
    @EncodeDefault val command: String = "get_host_onboarding_link",
)

@Serializable
private data class CreateAccountResponse(
    val accountId: String,
    val onboardingUrl: String,
    val expiresAt: Long,
) {
    fun getExpirationDate(): String {
        return Instant.fromEpochSeconds(expiresAt).toString()
    }
}

@Serializable
data class PurchaseIntent(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String,
    val publishableKey: String,
)