package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.IllegalArgumentException
import kotlin.math.log
import kotlin.text.StringBuilder

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {

    val userInfo : String
    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString (" ")
            .capitalize()
    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")
    private var phone: String? = null
        set(value) {
            field = value?.replace("""[^+\d]""".toRegex(), "")
        }
    private var _login: String? = null
    internal var login: String
        set(value) {
            _login = value.toLowerCase()
        }
        get() = _login!!

    internal var salt: String = ""
    internal lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null


    //for email
    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        password: String
    ): this(firstName, lastName, email = email, meta = mapOf("auth" to "password")){
        println("Secondary mail constructor")
        passwordHash = encrypt(password)
    }



    //for phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ): this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")){
        println("Secondary phone constructor")
        val code = generateAccessCode()
        println(code)

        passwordHash = encrypt(code)
        accessCode = code
        println(passwordHash)
        println(accessCode)
        sendAccessCodeToUser(rawPhone, code)
    }

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        salt: String,
        hash: String,
        phone: String?
    ) : this(firstName, lastName, email, rawPhone = phone, meta = mapOf("src" to "csv")){
        println("Secondary csv constructor")
        passwordHash = hash
        this@User.salt = salt
    }


    init{
        println("First init block, primery constructor was called")

        check(!firstName.isBlank()){"FirstName must be not blank!"}
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank()){"Email or phone must be not blank"}

        phone = rawPhone
        login = email ?: phone!!
        println("phone: $phone")
        println("login: $login")
        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun checkPassword(pass: String) = encrypt(pass) == passwordHash

    fun changePassword(oldPass: String, newPass: String){
        if(checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalArgumentException("The entered password does not match the current password")
    }

    internal fun encrypt(password: String): String {
        salt = getSalt()
        return salt.plus(password).md5()
    }

    private fun getSalt(): String {
        return if (salt.isNullOrBlank()){
            ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        }else salt
    }

    internal fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return StringBuilder().apply {
            repeat(6){
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    internal fun sendAccessCodeToUser(phone: String, code: String) {
        println(".... sending access code: $code on $phone")
    }


    private fun String.md5(): String{
        val md = MessageDigest.getInstance("MD5")
        val digest: ByteArray = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }
    companion object Factory{
        fun makeUser(
            fullName: String,
            email: String? = null,
            salt: String?,
            hash: String?,
            password: String? = null,
            phone: String? = null
        ): User{
            val (firstName, lastName) = fullName.fullNameToPair()
            println("1. $fullName")
            println(phone)
            return when {
                !salt.isNullOrBlank() && !hash.isNullOrBlank() -> User(firstName, lastName, email, salt, hash, phone)
                !phone.isNullOrBlank() -> User(firstName,lastName,phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName, email, password)
                else -> throw IllegalArgumentException("Email or phone must be not null")
            }
        }

        private fun String.fullNameToPair() : Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when(size){
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException("Fullname must contain only first name " +
                                "and last name, current split result ${this@fullNameToPair}")
                    }
                }
        }
    }


}